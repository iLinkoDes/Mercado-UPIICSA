package com.iianriverk.mercadoupiicsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iianriverk.mercadoupiicsa.navigation.AppNavigation
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.ui.theme.MercadoUPIICSATheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iianriverk.mercadoupiicsa.viewModels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MercadoUPIICSATheme(dynamicColor = false) {
                val authViewModel: AuthViewModel = viewModel()
                // Si hay sesion, avanza automaticamente al Feed
                val start = if (authViewModel.isLoggedIn()) Screen.Feed.route
                else Screen.Login.route
                AppNavigation(startDestination = start)
            }
        }
    }
}