package com.kiwizitos.collection.presentation.view.components

import android.R.drawable.ic_menu_gallery
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.network.HttpException
import coil.request.ImageRequest
import com.kiwizitos.collection.data.remote.GuiaUrls

@Composable
fun rememberCoverPainter(url: String?): Painter {
    val fallback = painterResource(ic_menu_gallery)
    return rememberAsyncImagePainter(
        model = rememberCoverRequest(url),
        placeholder = fallback,
        error = fallback,
        fallback = fallback
    )
}

@Composable
fun rememberCoverRequest(url: String?): ImageRequest {
    val context = LocalContext.current
    return remember(context, url) {
        ImageRequest.Builder(context)
            .data(url?.let { GuiaUrls.imageUrl(it) ?: it })
            .crossfade(true)
            .listener(onError = { _, result ->
                val status = (result.throwable as? HttpException)?.response?.code
                Log.w("CoverImage",
                    "Falha ao carregar capa; HTTP=$status; ${result.throwable.javaClass.simpleName}",
                    result.throwable)
            })
            .build()
    }
}
