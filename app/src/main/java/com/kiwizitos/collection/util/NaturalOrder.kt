package com.kiwizitos.collection.util

/**
 * Comparador de ordenação natural para strings com números embutidos.
 *
 * Divide cada string em segmentos alternados de texto e dígitos, e compara
 * os segmentos numéricos como [Int] em vez de lexicograficamente, produzindo
 * a ordenação esperada por humanos:
 *
 * ```
 * Lexicográfica : 1, 10, 11, 12, 2, 20, 21, 3...
 * Natural        : 1, 2, 3, 10, 11, 12, 20, 21...
 * ```
 *
 * A comparação é case-insensitive nos segmentos de texto.
 */
val NaturalOrderComparator: Comparator<String> = Comparator { a, b ->
    val segA = splitNatural(a)
    val segB = splitNatural(b)
    val len = minOf(segA.size, segB.size)
    for (i in 0 until len) {
        val sa = segA[i]
        val sb = segB[i]
        val cmp = if (sa.isDigits() && sb.isDigits()) {
            // Compara como número, evitando overflow com toLong
            sa.toLong().compareTo(sb.toLong())
        } else {
            sa.compareTo(sb, ignoreCase = true)
        }
        if (cmp != 0) return@Comparator cmp
    }
    segA.size.compareTo(segB.size)
}

/** Divide a string em chunks alternados de não-dígitos e dígitos. */
private fun splitNatural(s: String): List<String> {
    val result = mutableListOf<String>()
    val buf = StringBuilder()
    var inDigits = false
    for (ch in s) {
        val isDigit = ch.isDigit()
        if (buf.isNotEmpty() && isDigit != inDigits) {
            result += buf.toString()
            buf.clear()
        }
        inDigits = isDigit
        buf.append(ch)
    }
    if (buf.isNotEmpty()) result += buf.toString()
    return result
}

private fun String.isDigits() = isNotEmpty() && all { it.isDigit() }

