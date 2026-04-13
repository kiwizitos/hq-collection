package com.kiwizitos.collection.domain.usecase

import com.kiwizitos.collection.data.repository.AuthRepository
import com.kiwizitos.collection.data.repository.GalleryRepository
import javax.inject.Inject

/** Encerra a sessão e limpa o cache da galeria. */
class SignOutUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val galleryRepo: GalleryRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepo.signOut().onSuccess {
            galleryRepo.clearCache()
        }
    }
}

