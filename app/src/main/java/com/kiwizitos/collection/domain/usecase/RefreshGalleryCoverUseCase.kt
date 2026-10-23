package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

class RefreshGalleryCoverUseCase @Inject constructor(private val repo: GalleryRepository) {
    suspend fun edition(guiaUrl: String, coverUrl: String?): Result<Unit> =
        repo.refreshItemCover(guiaUrl, coverUrl)

    suspend fun series(seriesUrl: String, coverUrl: String?): Result<Unit> =
        repo.refreshSeriesCover(seriesUrl, coverUrl)
}
