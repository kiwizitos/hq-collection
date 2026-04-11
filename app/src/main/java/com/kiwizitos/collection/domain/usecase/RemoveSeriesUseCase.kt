package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.GalleryRepository

class RemoveSeriesUseCase(private val repo: GalleryRepository) {
    suspend operator fun invoke(seriesUrl: String): Result<Unit> =
        repo.removeSeries(seriesUrl)
}

