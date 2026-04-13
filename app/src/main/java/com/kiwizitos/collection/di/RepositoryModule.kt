package com.kiwizitos.collection.di

import com.kiwizitos.collection.data.repository.AuthRepository
import com.kiwizitos.collection.data.repository.ComicDataSource
import com.kiwizitos.collection.data.repository.GalleryRepository
import com.kiwizitos.collection.data.repository.GuiaQuadrinhosRepository
import com.kiwizitos.collection.data.repository.SupabaseAuthRepository
import com.kiwizitos.collection.data.repository.SupabaseGalleryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGalleryRepository(impl: SupabaseGalleryRepository): GalleryRepository

    @Binds
    @Singleton
    abstract fun bindComicDataSource(impl: GuiaQuadrinhosRepository): ComicDataSource
}

