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
import com.kiwizitos.collection.data.repository.SupabaseAuthRepository
import com.kiwizitos.collection.data.repository.SupabaseGalleryRepository
import com.kiwizitos.collection.navigation.AppNavHost
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModelFactory
import com.kiwizitos.collection.presentation.viewmodel.AuthViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModel
import com.kiwizitos.collection.presentation.viewmodel.GalleryViewModelFactory
import com.kiwizitos.siege.theme.SiegeTheme

class MainActivity : ComponentActivity() {

    // ViewModels criados no nível da Activity para sobreviver a recomposições
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            authRepository    = SupabaseAuthRepository.instance,
            galleryRepository = SupabaseGalleryRepository.instance
        )
    }

    private val galleryViewModel: GalleryViewModel by viewModels {
        GalleryViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restaura galeria se já há sessão ativa (cold start com sessão salva)
        authViewModel.currentUserId()?.let { galleryViewModel.loadGallery(it) }

        setContent {
            CollectionApp(
                authViewModel    = authViewModel,
                galleryViewModel = galleryViewModel
            )
        }
    }
}

@Composable
fun CollectionApp(
    authViewModel:    AuthViewModel,
    galleryViewModel: GalleryViewModel
) {
    val navController = rememberNavController()
    SiegeTheme(darkTheme = true) {
        AppNavHost(
            navController    = navController,
            authViewModel    = authViewModel,
            galleryViewModel = galleryViewModel,
            modifier         = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionAppPreview() {
    // Preview sem ViewModels reais — apenas estrutura
}

