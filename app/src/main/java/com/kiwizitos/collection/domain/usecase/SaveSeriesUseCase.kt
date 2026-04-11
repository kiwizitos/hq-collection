package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.data.repository.GalleryRepository

class SaveSeriesUseCase(private val repo: GalleryRepository) {
    suspend operator fun invoke(series: UserSeries): Result<UserSeries> =
        repo.saveSeries(series)
}
