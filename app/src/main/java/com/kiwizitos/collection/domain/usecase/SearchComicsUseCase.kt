package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.SeriesSearchResult
import com.kiwizitos.collection.data.repository.ComicDataSource
import javax.inject.Inject

/**
 * Parâmetros para [SearchComicsUseCase].
 *
 * @param query Termo de busca digitado pelo usuário.
 */
data class SearchComicsParams(val query: String)

/**
 * Caso de uso que realiza a busca inicial de séries de quadrinhos.
 *
 * Valida o termo de busca antes de delegar ao [ComicDataSource].
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class SearchComicsUseCase @Inject constructor(
    private val dataSource: ComicDataSource
) {
    /**
     * Executa a busca de séries.
     *
     * @param params Parâmetros com o termo de busca.
     * @return [Result] com a lista de séries e informações de paginação.
     */
    suspend operator fun invoke(params: SearchComicsParams): Result<SeriesSearchResult> {
        if (params.query.isBlank()) {
            return Result.failure(IllegalArgumentException("O termo de busca não pode estar vazio"))
        }
        if (params.query.trim().length < 2) {
            return Result.failure(IllegalArgumentException("O termo de busca deve ter ao menos 2 caracteres"))
        }
        return dataSource.searchSeries(params.query.trim())
    }
}

