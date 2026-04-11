package com.kiwizitos.collection.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Série/título salvo pelo usuário para acesso rápido.
 * Mapeado para `user_series` no Supabase.
 *
 * Não possui status de posse/leitura — é apenas uma referência à série
 * para exibição rápida (ex: tela inicial, biblioteca).
 * Salvo a partir da CoversScreen.
 */
@Serializable
data class UserSeries(
    @SerialName("id")           val id: String          = "",
    @SerialName("user_id")      val userId: String      = "",
    @SerialName("series_url")   val seriesUrl: String,
    @SerialName("series_title") val seriesTitle: String,
    @SerialName("cover_url")    val coverUrl: String?   = null,
    @SerialName("publisher")    val publisher: String?  = null,
    @SerialName("issue_count")  val issueCount: String? = null
)

