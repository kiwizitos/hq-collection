package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.UserSeries
import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

class SaveSeriesUseCase @Inject constructor(private val repo: GalleryRepository) {
    suspend operator fun invoke(series: UserSeries): Result<UserSeries> =
        repo.saveSeries(series)
}
