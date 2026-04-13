package com.kiwizitos.collection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.kiwizitos.collection.navigation.AppNavHost
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.siege.theme.SiegeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val galleryViewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restaura galeria se já há sessão ativa (cold start com sessão salva)
        authViewModel.currentUserId()?.let { galleryViewModel.loadGallery(it) }

        setContent {
            CollectionApp(
                authViewModel = authViewModel,
                galleryViewModel = galleryViewModel
            )
        }
    }
}

@Composable
fun CollectionApp(
    authViewModel: AuthViewModel,
    galleryViewModel: GalleryViewModel
) {
    val navController = rememberNavController()
    SiegeTheme(darkTheme = true) {
        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            galleryViewModel = galleryViewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionAppPreview() {
    // Preview sem ViewModels reais — apenas estrutura
}

