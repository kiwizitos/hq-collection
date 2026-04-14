package com.kiwizitos.collection.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Volume/edição salvo na galeria do usuário, com status de posse/leitura.
 * Mapeado para `user_editions` no Supabase.
 *
 * [ownership] e [readStatus] são independentes — ao menos um deve ser não-nulo.
 */
@Serializable
data class UserItem(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("guia_url") val guiaUrl: String,
    @SerialName("title") val title: String,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("series_url") val seriesUrl: String? = null,
    @SerialName("series_title") val seriesTitle: String? = null,
    @SerialName("ownership") val ownership: Ownership? = null,
    @SerialName("read_status") val readStatus: ReadStatus? = null
) {
    fun toItemStatus(): ItemStatus = ItemStatus(ownership, readStatus)
}
