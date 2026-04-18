package com.kiwizitos.siege.tokens

import androidx.annotation.DrawableRes
import com.kiwizitos.siege.R

/**
 * Catálogo centralizado de ícones do Design System Siege.
 *
 * Todos os ícones são drawables próprios localizados em `res/drawable/`.
 * Nomenclatura: `ic_{nome}` para a variante outline/padrão e `ic_{nome}_solid` para a preenchida.
 *
 * Uso:
 * ```kotlin
 * SiegeIcon(icon = SiegeIcons.ic_home)
 * SiegeIcon(icon = SiegeIcons.ic_user_solid, tint = SiegeColors.AccentPink)
 * ```
 */
object SiegeIcons {

    // ── Bottom Bar ──
    @DrawableRes
    val ic_home: Int = R.drawable.ic_home
    @DrawableRes
    val ic_home_solid: Int = R.drawable.ic_home_solid

    @DrawableRes
    val ic_explore: Int = R.drawable.ic_explore
    @DrawableRes
    val ic_explore_solid: Int = R.drawable.ic_explore_solid

    @DrawableRes
    val ic_feed: Int = R.drawable.ic_feed
    @DrawableRes
    val ic_feed_solid: Int = R.drawable.ic_feed_solid

    @DrawableRes
    val ic_user: Int = R.drawable.ic_user
    @DrawableRes
    val ic_user_solid: Int = R.drawable.ic_user_solid

    // ── Likes ──
    @DrawableRes
    val ic_like: Int = R.drawable.ic_like
    @DrawableRes
    val ic_like_solid: Int = R.drawable.ic_like_solid

    // ── Glasses ──
    @DrawableRes
    val ic_glasses: Int = R.drawable.ic_glasses
    @DrawableRes
    val ic_glasses_solid: Int = R.drawable.ic_glasses_solid

    // ── Flag ──
    @DrawableRes
    val ic_flag: Int = R.drawable.ic_flag
    @DrawableRes
    val ic_flag_solid: Int = R.drawable.ic_flag_solid

    // ── Bookmarks ──
    @DrawableRes
    val ic_bookmarks: Int = R.drawable.ic_bookmarks
    @DrawableRes
    val ic_bookmarks_solid: Int = R.drawable.ic_bookmarks_solid

    // ── Share ──
    @DrawableRes
    val ic_share: Int = R.drawable.ic_share
    @DrawableRes
    val ic_share_solid: Int = R.drawable.ic_share_solid

    // ── Filter ──
    @DrawableRes
    val ic_filter: Int = R.drawable.ic_filter
    @DrawableRes
    val ic_filter_solid: Int = R.drawable.ic_filter_solid

    // ── Folder ──
    @DrawableRes
    val ic_folder: Int = R.drawable.ic_folder
    @DrawableRes
    val ic_folder_solid: Int = R.drawable.ic_folder_solid

    // ── File ──
    @DrawableRes
    val ic_file: Int = R.drawable.ic_file
    @DrawableRes
    val ic_file_solid: Int = R.drawable.ic_file_solid

    // ── Book ──
    @DrawableRes
    val ic_book: Int = R.drawable.ic_book
    @DrawableRes
    val ic_book_solid: Int = R.drawable.ic_book_solid

    // ── Cancel ──
    @DrawableRes
    val ic_cancel: Int = R.drawable.ic_cancel

    // ── Arrow ──
    @DrawableRes
    val ic_arrow_solid: Int = R.drawable.ic_arrow_solid
    @DrawableRes
    val ic_arrow_alt_solid: Int = R.drawable.ic_arrow_alt_solid
}
