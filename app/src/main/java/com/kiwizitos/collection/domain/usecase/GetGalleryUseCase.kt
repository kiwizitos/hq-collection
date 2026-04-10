package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.repository.GalleryRepository

class GetGalleryUseCase(private val repo: GalleryRepository) {
    suspend operator fun invoke(userId: String): Result<List<UserItem>> =
        repo.loadGallery(userId)
}

