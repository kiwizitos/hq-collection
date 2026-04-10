package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.repository.GalleryRepository

class SaveItemUseCase(private val repo: GalleryRepository) {
    suspend operator fun invoke(item: UserItem): Result<UserItem> =
        repo.saveItem(item)
}

