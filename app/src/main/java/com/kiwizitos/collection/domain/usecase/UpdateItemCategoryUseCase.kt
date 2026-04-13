package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.ItemStatus
import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

class UpdateItemStatusUseCase @Inject constructor(private val repo: GalleryRepository) {
    suspend operator fun invoke(guiaUrl: String, status: ItemStatus): Result<Unit> =
        repo.updateStatus(guiaUrl, status)
}
