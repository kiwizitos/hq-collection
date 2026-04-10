package com.kiwizitos.collection.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Interface Retrofit para o Guia dos Quadrinhos.
 *
 * Dois tipos de requisição:
 * - **GET** para a primeira página (30 itens por padrão do site).
 * - **POST** com ViewState ASP.NET para navegar entre páginas.
 */
interface GuiaQuadrinhosService {

    /** GET simples — retorna HTML da página. */
    @GET
    suspend fun getPageHtml(@Url url: String): String

    /**
     * POST com ViewState ASP.NET para navegar para a próxima página.
     * O site mantém o tamanho de página padrão (30 itens).
     */
    @FormUrlEncoded
    @POST
    suspend fun postPageNavigation(
        @Url url: String,
        @Field("__VIEWSTATE") viewState: String,
        @Field("__VIEWSTATEGENERATOR") viewStateGenerator: String = "EEB05935",
        @Field("__VIEWSTATEENCRYPTED") viewStateEncrypted: String = "",
        @Field("__EVENTVALIDATION") eventValidation: String,
        @Field("__EVENTTARGET") eventTarget: String,
        @Field("__EVENTARGUMENT") eventArgument: String = ""
    ): String
}

