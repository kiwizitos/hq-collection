package com.kiwizitos.collection.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representa um item salvo na galeria do usuário.
 *
 * Mapeado para a tabela `user_items` no Supabase.
 * Os nomes das propriedades seguem snake_case via [@SerialName] para
 * corresponder às colunas do banco sem precisar configurar o cliente.
 *
 * [ownership] e [readStatus] são independentes entre si:
 * - Posse: TENHO / QUERO (mutuamente exclusivos)
 * - Leitura: LIDO / LENDO (mutuamente exclusivos)
 * Ao menos um deles deve ser não-nulo para o item estar na galeria.
 */
@Serializable
data class UserItem(
    @SerialName("id")            val id: String           = "",
    @SerialName("user_id")       val userId: String       = "",
    @SerialName("guia_url")      val guiaUrl: String,
    @SerialName("guia_title")    val guiaTitle: String,
    @SerialName("series_url")    val seriesUrl: String?   = null,
    @SerialName("series_title")  val seriesTitle: String? = null,
    @SerialName("ownership")     val ownership: Ownership?   = null,
    @SerialName("read_status")   val readStatus: ReadStatus? = null
) {
    /** Converte para [ItemStatus] para uso na UI e no cache. */
    fun toItemStatus(): ItemStatus = ItemStatus(ownership, readStatus)
}
