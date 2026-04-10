package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kiwizitos.collection.data.remote.NetworkModule
import com.kiwizitos.collection.data.repository.GuiaQuadrinhosRepository
import com.kiwizitos.collection.domain.usecase.GetSeriesCoversUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextCoversPageUseCase
import com.kiwizitos.collection.domain.usecase.LoadNextPageUseCase
import com.kiwizitos.collection.domain.usecase.SearchComicsUseCase

/**
 * Factory para criar [SearchViewModel] sem injeção de dependência.
 *
 * Constrói manualmente toda a árvore de dependências:
 * [NetworkModule] → [GuiaQuadrinhosRepository] → UseCases → [SearchViewModel].
 *
 * Uso em composables:
 * ```kotlin
 * val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory())
 * ```
 */
class SearchViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val repository = GuiaQuadrinhosRepository(service = NetworkModule.service)

            return SearchViewModel(
                searchComicsUseCase       = SearchComicsUseCase(repository),
                loadNextPageUseCase       = LoadNextPageUseCase(repository),
                getSeriesCoversUseCase    = GetSeriesCoversUseCase(repository),
                loadNextCoversPageUseCase = LoadNextCoversPageUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Classe de ViewModel desconhecida: ${modelClass.name}")
    }
}

