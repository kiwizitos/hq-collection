package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.repository.ComicDataSource
import javax.inject.Inject

/**
 * Caso de uso que carrega os detalhes de uma edição específica.
 *
 * @param dataSource Fonte de dados de quadrinhos.
 */
class GetComicDetailsUseCase @Inject constructor(
    private val dataSource: ComicDataSource
) {
    suspend operator fun invoke(url: String): Result<ComicDetails> =
        dataSource.getComicDetails(url)
}

