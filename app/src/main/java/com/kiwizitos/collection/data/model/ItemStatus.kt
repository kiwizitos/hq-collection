package com.kiwizitos.collection.data.model

import androidx.compose.ui.graphics.Color
import com.kiwizitos.siege.tokens.SiegeColors
import kotlinx.serialization.Serializable

/**
 * Representa o status de posse de um item na galeria.
 * Espelha o `ownership_enum` no Supabase.
 * Valores são mutuamente exclusivos entre si.
 */
@Serializable
enum class Ownership {
    TENHO,
    QUERO;

    val displayLabel: String
        get() = when (this) {
            TENHO -> "Tenho"
            QUERO -> "Quero"
        }

    val badgeColor: Color
        get() = when (this) {
            TENHO -> SiegeColors.AccentCyan
            QUERO -> SiegeColors.AccentPink
        }
}

/**
 * Representa o status de leitura de um item na galeria.
 * Espelha o `read_status_enum` no Supabase.
 * Valores são mutuamente exclusivos entre si.
 */
@Serializable
enum class ReadStatus {
    LIDO,
    LENDO;

    val displayLabel: String
        get() = when (this) {
            LIDO -> "Lido"
            LENDO -> "Lendo"
        }

    val badgeColor: Color
        get() = when (this) {
            LIDO -> SiegeColors.AccentPink
            LENDO -> SiegeColors.AccentCyan
        }
}

/**
 * Estado combinado de um item na galeria do usuário.
 *
 * Os dois campos são independentes: [ownership] e [readStatus] podem
 * coexistir livremente — ex: TENHO + LIDO, QUERO + LENDO, só TENHO, etc.
 * Ao menos um dos campos deve ser não-nulo para o item existir na galeria.
 */
data class ItemStatus(
    val ownership: Ownership?,
    val readStatus: ReadStatus?
) {
    /** Retorna true se o item tem ao menos um status definido. */
    fun isNotEmpty(): Boolean = ownership != null || readStatus != null

    /**
     * Todos os badges visíveis, na ordem: Posse (se presente), Leitura (se presente).
     */
    fun badges(): List<Pair<String, Color>> = buildList {
        ownership?.let { add(it.displayLabel to it.badgeColor) }
        readStatus?.let { add(it.displayLabel to it.badgeColor) }
    }
}

