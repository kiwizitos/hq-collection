package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.GalleryRepository

class RemoveItemUseCase(private val repo: GalleryRepository) {
    suspend operator fun invoke(guiaUrl: String): Result<Unit> =
        repo.removeItem(guiaUrl)
}

