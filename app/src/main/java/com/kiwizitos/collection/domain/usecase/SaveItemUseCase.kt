package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.model.UserItem
import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

class SaveItemUseCase @Inject constructor(private val repo: GalleryRepository) {
    suspend operator fun invoke(item: UserItem): Result<UserItem> =
        repo.saveItem(item)
}
