package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.PaginationInfo
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversParams
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextPageUseCase
import com.kiwizitos.collection.domain.usecase.SearchComicsParams
import com.kiwizitos.collection.domain.usecase.SearchComicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

// ── UiState ───────────────────────────────────────────────────────────────────

/**
 * Estado da UI para operações assíncronas.
 *
 * - [Idle]        → Estado inicial; nenhuma busca realizada ainda.
 * - [Loading]     → Carregando a primeira página.
 * - [Success]     → Dados disponíveis.
 * - [LoadingMore] → Carregando página adicional — mantém dados atuais visíveis.
 * - [Error]       → Falha — exibe mensagem amigável e opção de retry.
 * - [Empty]       → Busca concluída sem resultados.
 */
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class LoadingMore<T>(val currentData: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()

    fun isLoading(): Boolean = this is Loading || this is LoadingMore
}

// ── Modelos de apresentação ───────────────────────────────────────────────────

/**
 * Resultado paginado de uma busca de séries, pronto para a UI.
 *
 * @param series         Lista acumulada de séries (todas as páginas carregadas até agora).
 * @param paginationInfo Estado atual de paginação.
 */
data class PaginatedSearchResult(
    val series: List<SerieResult>,
    val paginationInfo: PaginationInfo
)

/**
 * Resultado paginado de capas de uma série, pronto para a UI.
 *
 * @param seriesTitle    Título da série para exibição.
 * @param covers         Lista acumulada de capas (todas as páginas carregadas até agora).
 * @param paginationInfo Estado atual de paginação.
 */
data class PaginatedCoversResult(
    val seriesTitle: String,
    val covers: List<CoverItem>,
    val paginationInfo: PaginationInfo
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * ViewModel responsável pelo fluxo de busca e capas de quadrinhos.
 *
 * Expõe dois fluxos de estado:
 * - [searchState] → resultados de busca de séries com paginação.
 * - [coversState] → capas de uma série selecionada com paginação.
 *
 * Aplica **debounce de 500ms** na busca para evitar requisições excessivas.
 * Preserva a **posição de scroll** entre recomposições.
 *
 * @param searchComicsUseCase     Caso de uso de busca inicial de séries.
 * @param loadNextPageUseCase     Caso de uso de próxima página de séries.
 * @param getSeriesCoversUseCase  Caso de uso de capas de uma série.
 * @param loadNextCoversPageUseCase Caso de uso de próxima página de capas.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchComicsUseCase: SearchComicsUseCase,
    private val loadNextPageUseCase: LoadNextPageUseCase,
    private val getSeriesCoversUseCase: GetSeriesCoversUseCase,
    private val loadNextCoversPageUseCase: LoadNextCoversPageUseCase
) : ViewModel() {

    // ── Search State ──────────────────────────────────────────────────────────

    private val _searchState = MutableStateFlow<UiState<PaginatedSearchResult>>(UiState.Idle)
    val searchState: StateFlow<UiState<PaginatedSearchResult>> = _searchState.asStateFlow()

    private var currentSearchQuery: String = ""
    private var searchJob: Job? = null

    /**
     * Guarda se uma carga de próxima página já está em curso.
     * Impede double-tap no botão "Carregar mais" enquanto a resposta não chegou.
     */
    private var isLoadingNextSearchPage: Boolean = false

    // ── Covers State ──────────────────────────────────────────────────────────

    private val _coversState = MutableStateFlow<UiState<PaginatedCoversResult>>(UiState.Idle)
    val coversState: StateFlow<UiState<PaginatedCoversResult>> = _coversState.asStateFlow()

    private var currentSeriesUrl: String = ""

    /**
     * Guarda se uma carga de próxima página de capas já está em curso.
     * Mesmo propósito que [isLoadingNextSearchPage], mas para a tela de capas.
     */
    private var isLoadingNextCoversPage: Boolean = false

    // ── Busca de Séries ───────────────────────────────────────────────────────

    /**
     * Inicia uma nova busca de séries com debounce de 500ms.
     *
     * Cancela buscas anteriores em andamento automaticamente.
     *
     * @param query Termo de busca digitado pelo usuário.
     */
    fun searchSeries(query: String) {
        searchJob?.cancel()
        isLoadingNextSearchPage = false
        searchJob = viewModelScope.launch {
            _searchState.value = UiState.Loading
            currentSearchQuery = query

            val result = searchComicsUseCase(SearchComicsParams(query))

            _searchState.value = result.fold(
                onSuccess = { searchResult ->
                    if (searchResult.series.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(
                            PaginatedSearchResult(
                                series = searchResult.series,
                                paginationInfo = searchResult.paginationInfo
                            )
                        )
                    }
                },
                onFailure = { error ->
                    UiState.Error(friendlyErrorMessage(error), error)
                }
            )
        }
    }

    /**
     * Carrega a próxima página de resultados da busca atual.
     * Ignorado se não há mais páginas, se já está carregando, ou se uma
     * carga já foi disparada e ainda não concluiu ([isLoadingNextSearchPage]).
     */
    fun loadNextSearchPage() {
        if (isLoadingNextSearchPage) return          // ← guard contra loop

        val currentState = _searchState.value
        if (currentState !is UiState.Success) return

        val pagination = currentState.data.paginationInfo
        if (!pagination.hasNextPage) return
        if (pagination.viewState.isNullOrBlank()) return
        if (pagination.eventValidation.isNullOrBlank()) return
        if (pagination.eventTarget.isNullOrBlank()) return

        isLoadingNextSearchPage = true
        viewModelScope.launch {
            _searchState.value = UiState.LoadingMore(currentState.data)

            val result = loadNextPageUseCase(
                LoadNextPageParams(
                    query = currentSearchQuery,
                    viewState = pagination.viewState,
                    eventValidation = pagination.eventValidation,
                    eventTarget = pagination.eventTarget
                )
            )

            isLoadingNextSearchPage = false

            _searchState.value = result.fold(
                onSuccess = { newPage ->
                    UiState.Success(
                        PaginatedSearchResult(
                            series = currentState.data.series + newPage.series,
                            paginationInfo = newPage.paginationInfo
                        )
                    )
                },
                onFailure = { error ->
                    // Restaura Success para o botão "Carregar mais" reaparecer
                    UiState.Success(
                        currentState.data.copy(
                            paginationInfo = currentState.data.paginationInfo
                        )
                    ).also {
                        // Log do erro sem bloquear a UI
                        android.util.Log.e(
                            "SearchViewModel",
                            "loadNextSearchPage error: ${error.message}",
                            error
                        )
                    }
                }
            )
        }
    }

    // ── Capas da Série ────────────────────────────────────────────────────────

    /**
     * Carrega as capas da série informada (primeira página).
     *
     * @param seriesUrl   Caminho relativo da série no Guia dos Quadrinhos.
     * @param seriesTitle Título da série para exibição no cabeçalho.
     */
    fun getSeriesCovers(seriesUrl: String, seriesTitle: String) {
        currentSeriesUrl = seriesUrl
        isLoadingNextCoversPage = false  // reset ao abrir nova série

        viewModelScope.launch {
            _coversState.value = UiState.Loading

            val result = getSeriesCoversUseCase(
                GetSeriesCoversParams(
                    seriesUrl = seriesUrl,
                    seriesTitle = seriesTitle
                )
            )

            _coversState.value = result.fold(
                onSuccess = { coversResult ->
                    if (coversResult.covers.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(
                            PaginatedCoversResult(
                                seriesTitle = coversResult.seriesTitle,
                                covers = coversResult.covers,
                                paginationInfo = coversResult.paginationInfo
                            )
                        )
                    }
                },
                onFailure = { error ->
                    UiState.Error(friendlyErrorMessage(error), error)
                }
            )
        }
    }

    /**
     * Carrega a próxima página de capas da série atual.
     * Ignorado se não há mais páginas, se já está carregando, ou se uma
     * carga já foi disparada e ainda não concluiu ([isLoadingNextCoversPage]).
     */
    fun loadNextCoversPage() {
        if (isLoadingNextCoversPage) return          // ← guard contra loop

        val currentState = _coversState.value
        if (currentState !is UiState.Success) return

        val pagination = currentState.data.paginationInfo
        if (!pagination.hasNextPage) return
        if (pagination.viewState.isNullOrBlank()) return
        if (pagination.eventValidation.isNullOrBlank()) return
        if (pagination.eventTarget.isNullOrBlank()) return

        isLoadingNextCoversPage = true
        viewModelScope.launch {
            _coversState.value = UiState.LoadingMore(currentState.data)

            val result = loadNextCoversPageUseCase(
                LoadNextCoversPageParams(
                    seriesUrl = currentSeriesUrl,
                    seriesTitle = currentState.data.seriesTitle,
                    viewState = pagination.viewState,
                    eventValidation = pagination.eventValidation,
                    eventTarget = pagination.eventTarget
                )
            )

            isLoadingNextCoversPage = false

            _coversState.value = result.fold(
                onSuccess = { newPage ->
                    UiState.Success(
                        PaginatedCoversResult(
                            seriesTitle = currentState.data.seriesTitle,
                            covers = currentState.data.covers + newPage.covers,
                            paginationInfo = newPage.paginationInfo
                        )
                    )
                },
                onFailure = { error ->
                    android.util.Log.e(
                        "SearchViewModel",
                        "loadNextCoversPage error: ${error.message}",
                        error
                    )
                    UiState.Success(currentState.data)
                }
            )
        }
    }

    /** Reseta o estado de capas ao navegar para fora da tela de capas. */
    fun resetCoversState() {
        _coversState.value = UiState.Idle
        currentSeriesUrl = ""
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Converte uma exceção em mensagem amigável para o usuário. */
    private fun friendlyErrorMessage(throwable: Throwable): String = when (throwable) {
        is UnknownHostException -> "Sem conexão com a internet"
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is IOException -> "Erro de conexão. Verifique sua internet."
        is IllegalArgumentException -> throwable.message ?: "Parâmetros inválidos"
        else -> throwable.message ?: "Erro desconhecido. Tente novamente."
    }
}

