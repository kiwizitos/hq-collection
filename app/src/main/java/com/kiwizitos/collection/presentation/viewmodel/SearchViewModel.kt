package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.CoverItem
import com.kiwizitos.collection.data.model.PaginationInfo
import com.kiwizitos.collection.data.model.SerieResult
import com.kiwizitos.collection.data.remote.GuiaFailureMessages
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversParams
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextPageParams
import com.kiwizitos.collection.domain.usecase.LoadNextPageUseCase
import com.kiwizitos.collection.domain.usecase.SearchComicsParams
import com.kiwizitos.collection.domain.usecase.SearchComicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
 * @param paginationError Falha da próxima página; preserva os resultados e exige retry manual.
 */
data class PaginatedSearchResult(
    val series: List<SerieResult>,
    val paginationInfo: PaginationInfo,
    val paginationError: String? = null
)

/**
 * Resultado paginado de capas de uma série, pronto para a UI.
 *
 * @param seriesTitle       Título da série para exibição.
 * @param covers            Lista acumulada de capas (todas as páginas carregadas até agora).
 * @param paginationInfo    Estado atual de paginação.
 * @param isStandalone      True se a série tem apenas uma edição única.
 * @param singleEditionUrl  URL relativa da edição única; não-nulo apenas quando [isStandalone] = true.
 * @param paginationError   Falha da próxima página; preserva as capas e exige retry manual.
 */
data class PaginatedCoversResult(
    val seriesTitle: String,
    val covers: List<CoverItem>,
    val paginationInfo: PaginationInfo,
    val isStandalone: Boolean = false,
    val singleEditionUrl: String? = null,
    val paginationError: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * ViewModel responsável pelo fluxo de busca e capas de quadrinhos.
 *
 * Expõe dois fluxos de estado:
 * - [searchState] → resultados de busca de séries com paginação.
 * - [coversState] → capas de uma série selecionada com paginação.
 *
 * A busca é explícita, sem debounce, e cancela a operação anterior.
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

    // ── Covers State ──────────────────────────────────────────────────────────

    private val _coversState = MutableStateFlow<UiState<PaginatedCoversResult>>(UiState.Idle)
    val coversState: StateFlow<UiState<PaginatedCoversResult>> = _coversState.asStateFlow()

    /** URL da série cujas capas estão atualmente carregadas. Usado pelo NavHost
     *  para evitar re-fetch desnecessário ao voltar de DetailsScreen. */
    var currentSeriesUrl: String = ""
        private set

    /** Posição de scroll da grade de capas — persistida para restaurar ao voltar. */
    var savedCoversGridIndex: Int = 0
        private set
    var savedCoversGridOffset: Int = 0
        private set

    fun saveCoversGridState(index: Int, offset: Int) {
        savedCoversGridIndex = index
        savedCoversGridOffset = offset
    }

    private var coversJob: Job? = null

    // ── Busca de Séries ───────────────────────────────────────────────────────

    /**
     * Inicia imediatamente uma nova busca explícita de séries.
     *
     * Cancela buscas anteriores em andamento automaticamente.
     *
     * @param query Termo de busca digitado pelo usuário.
     */
    fun searchSeries(query: String) {
        searchJob?.cancel()
        currentSearchQuery = query
        _searchState.value = UiState.Loading
        searchJob = viewModelScope.launch {
            val result = searchComicsUseCase(SearchComicsParams(query))
            coroutineContext.ensureActive()

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
                    if (error is CancellationException) throw error
                    UiState.Error(GuiaFailureMessages.message(error), error)
                }
            )
        }
    }

    /**
     * Carrega a próxima página de resultados da busca atual.
     * Ignorado sem próxima página ou durante uma carga. Uma chamada manual
     * limpa o erro anterior; a UI não deve chamar automaticamente após falhas.
     */
    fun loadNextSearchPage() {
        val currentState = _searchState.value
        if (currentState !is UiState.Success) return

        val pagination = currentState.data.paginationInfo
        if (!pagination.hasNextPage) return
        if (pagination.viewState.isNullOrBlank() ||
            pagination.eventValidation.isNullOrBlank() ||
            pagination.eventTarget.isNullOrBlank() ||
            pagination.viewStateGenerator.isNullOrBlank()
        ) {
            _searchState.value = UiState.Success(currentState.data.copy(
                paginationError = "Tokens de paginação ausentes. Faça uma nova busca."
            ))
            return
        }

        _searchState.value = UiState.LoadingMore(currentState.data.copy(paginationError = null))
        searchJob = viewModelScope.launch {
            val result = loadNextPageUseCase(
                LoadNextPageParams(
                    query = currentSearchQuery,
                    viewState = pagination.viewState,
                    eventValidation = pagination.eventValidation,
                    eventTarget = pagination.eventTarget,
                    viewStateGenerator = pagination.viewStateGenerator
                )
            )

            coroutineContext.ensureActive()

            _searchState.value = result.fold(
                onSuccess = { newPage ->
                    val series = (currentState.data.series + newPage.series).distinctBy { it.relativeLink }
                    if (series.size == currentState.data.series.size) {
                        return@fold UiState.Success(currentState.data.copy(
                            paginationError = "A próxima página não trouxe novos resultados. Tente novamente."
                        ))
                    }
                    UiState.Success(
                        currentState.data.copy(
                            series = series,
                            paginationInfo = newPage.paginationInfo,
                            paginationError = null
                        )
                    )
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    UiState.Success(currentState.data.copy(
                        paginationError = GuiaFailureMessages.message(error)
                    ))
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
        coversJob?.cancel()
        currentSeriesUrl = seriesUrl
        _coversState.value = UiState.Loading

        coversJob = viewModelScope.launch {
            val result = getSeriesCoversUseCase(
                GetSeriesCoversParams(
                    seriesUrl = seriesUrl,
                    seriesTitle = seriesTitle
                )
            )
            coroutineContext.ensureActive()

            _coversState.value = result.fold(
                onSuccess = { coversResult ->
                    if (coversResult.covers.isEmpty() && !coversResult.isStandalone) {
                        UiState.Empty
                    } else {
                        UiState.Success(
                            PaginatedCoversResult(
                                seriesTitle = coversResult.seriesTitle,
                                covers = coversResult.covers,
                                paginationInfo = coversResult.paginationInfo,
                                isStandalone = coversResult.isStandalone,
                                singleEditionUrl = coversResult.singleEditionUrl
                            )
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    UiState.Error(GuiaFailureMessages.message(error), error)
                }
            )
        }
    }

    /**
     * Carrega a próxima página de capas da série atual.
     * Ignorado sem próxima página ou durante uma carga. Uma chamada manual
     * limpa o erro anterior; a UI não deve chamar automaticamente após falhas.
     */
    fun loadNextCoversPage() {
        val currentState = _coversState.value
        if (currentState !is UiState.Success) return

        val pagination = currentState.data.paginationInfo
        if (!pagination.hasNextPage) return
        if (pagination.viewState.isNullOrBlank() ||
            pagination.eventValidation.isNullOrBlank() ||
            pagination.eventTarget.isNullOrBlank() ||
            pagination.viewStateGenerator.isNullOrBlank()
        ) {
            _coversState.value = UiState.Success(currentState.data.copy(
                paginationError = "Tokens de paginação ausentes. Reabra a série."
            ))
            return
        }

        _coversState.value = UiState.LoadingMore(currentState.data.copy(paginationError = null))
        coversJob = viewModelScope.launch {
            val result = loadNextCoversPageUseCase(
                LoadNextCoversPageParams(
                    seriesUrl = currentSeriesUrl,
                    seriesTitle = currentState.data.seriesTitle,
                    viewState = pagination.viewState,
                    eventValidation = pagination.eventValidation,
                    eventTarget = pagination.eventTarget,
                    viewStateGenerator = pagination.viewStateGenerator
                )
            )

            coroutineContext.ensureActive()

            _coversState.value = result.fold(
                onSuccess = { newPage ->
                    val covers = (currentState.data.covers + newPage.covers).distinctBy { it.relativeLink }
                    if (covers.size == currentState.data.covers.size) {
                        return@fold UiState.Success(currentState.data.copy(
                            paginationError = "A próxima página não trouxe novas capas. Tente novamente."
                        ))
                    }
                    UiState.Success(
                        currentState.data.copy(
                            covers = covers,
                            paginationInfo = newPage.paginationInfo,
                            paginationError = null
                        )
                    )
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    UiState.Success(currentState.data.copy(
                        paginationError = GuiaFailureMessages.message(error)
                    ))
                }
            )
        }
    }

    /** Reseta o estado de capas ao navegar para fora da tela de capas. */
    fun resetCoversState() {
        coversJob?.cancel()
        coversJob = null
        _coversState.value = UiState.Idle
        currentSeriesUrl = ""
        savedCoversGridIndex = 0
        savedCoversGridOffset = 0
    }

}
