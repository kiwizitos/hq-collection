package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

class GetGalleryUseCase @Inject constructor(private val repo: GalleryRepository) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        repo.loadGallery(userId)
}
