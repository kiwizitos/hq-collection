package com.kiwizitos.collection.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Módulo de rede — provê instâncias singleton de [OkHttpClient], [Retrofit]
 * e [GuiaQuadrinhosService] sem necessidade de injeção de dependência.
 *
 * Inicialização lazy: as instâncias só são criadas na primeira utilização.
 */
object NetworkModule {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://www.guiadosquadrinhos.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    /** Instância singleton do serviço Retrofit. */
    val service: GuiaQuadrinhosService by lazy {
        retrofit.create(GuiaQuadrinhosService::class.java)
    }
}

