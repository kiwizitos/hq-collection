package com.kiwizitos.collection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.kiwizitos.collection.navigation.AppNavHost
import com.kiwizitos.siege.theme.SiegeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollectionApp()
        }
    }
}

@Composable
fun CollectionApp() {
    val navController = rememberNavController()

    SiegeTheme(darkTheme = true) {
        AppNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionAppPreview() {
    CollectionApp()
}

