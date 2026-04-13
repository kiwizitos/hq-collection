package com.kiwizitos.collection.di

import com.kiwizitos.collection.data.remote.GuiaQuadrinhosService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkDiModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Collection-App/1.0 (Android; quadrinhos-scraper)")
                    .addHeader("Accept", "text/html,application/xhtml+xml")
                    .addHeader("Accept-Language", "pt-BR,pt;q=0.9")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://www.guiadosquadrinhos.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGuiaQuadrinhosService(retrofit: Retrofit): GuiaQuadrinhosService =
        retrofit.create(GuiaQuadrinhosService::class.java)
}

