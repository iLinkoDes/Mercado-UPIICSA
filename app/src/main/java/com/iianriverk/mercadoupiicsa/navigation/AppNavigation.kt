package com.iianriverk.mercadoupiicsa.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iianriverk.mercadoupiicsa.views.auth.LoginScreen
import com.iianriverk.mercadoupiicsa.views.auth.RegistroPaso1Screen
import com.iianriverk.mercadoupiicsa.views.auth.RegistroPaso2Screen
import com.iianriverk.mercadoupiicsa.views.feed.CatalogoVendedorScreen
import com.iianriverk.mercadoupiicsa.views.feed.FeedScreen
import com.iianriverk.mercadoupiicsa.views.vendedor.CrearEditarProductoScreen
import com.iianriverk.mercadoupiicsa.viewModels.AuthViewModel
import com.iianriverk.mercadoupiicsa.views.chat.ChatScreen
import com.iianriverk.mercadoupiicsa.views.encargos.HacerEncargoScreen
import com.iianriverk.mercadoupiicsa.views.encargos.MisEncargosScreen
import com.iianriverk.mercadoupiicsa.views.perfil.EditarPerfilScreen
import com.iianriverk.mercadoupiicsa.views.perfil.PerfilScreen

sealed class Screen(val route: String) {
    object Login          : Screen("login")
    object RegistroPaso1  : Screen("registerPaso1")
    object RegistroPaso2  : Screen("registerPaso2")
    object Feed           : Screen("feed")
    object Catalogo       : Screen("catalogo/{idVendedor}") {
        fun createRoute(idVendedor: String) = "catalogo/$idVendedor"
    }
    object ProductoNuevo  : Screen("producto/nuevo")
    object ProductoEditar : Screen("producto/editar/{idProducto}") {
        fun createRoute(idProducto: String) = "producto/editar/$idProducto"
    }

    object HacerEncargo : Screen("encargo/{idProducto}/{idVendedor}") {
        fun createRoute(idProducto: String, idVendedor: String) = "encargo/$idProducto/$idVendedor"
    }
    object MisEncargos : Screen("mis_encargos")

    object Perfil       : Screen("perfil")
    object EditarPerfil : Screen("perfil/editar")

    object Chat : Screen("chat/{idEncargo}/{idReceptor}/{nombreProducto}") {
        fun createRoute(
            idEncargo:      String,
            idReceptor:     String,
            nombreProducto: String
        ) = "chat/$idEncargo/$idReceptor/${nombreProducto.encodeURL()}"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.RegistroPaso1.route) {
            RegistroPaso1Screen(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.RegistroPaso2.route) {
            RegistroPaso2Screen(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.Feed.route) {
            FeedScreen(navController = navController)
        }

        composable(Screen.Catalogo.route) { backStack ->
            val idVendedor = backStack.arguments?.getString("idVendedor") ?: ""
            CatalogoVendedorScreen(
                idVendedor    = idVendedor,
                navController = navController
            )
        }

        composable(Screen.ProductoNuevo.route) {
            CrearEditarProductoScreen(
                idProducto    = null,
                navController = navController
            )
        }

        composable(Screen.ProductoEditar.route) { backStack ->
            val idProducto = backStack.arguments?.getString("idProducto")
            CrearEditarProductoScreen(
                idProducto    = idProducto,
                navController = navController
            )
        }

        composable(Screen.HacerEncargo.route) { backStack ->
            val idProducto = backStack.arguments?.getString("idProducto") ?: ""
            val idVendedor = backStack.arguments?.getString("idVendedor") ?: ""
            HacerEncargoScreen(
                idProducto    = idProducto,
                idVendedor    = idVendedor,
                navController = navController
            )
        }

        composable(Screen.MisEncargos.route) {
            MisEncargosScreen(navController = navController)
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(navController = navController)
        }

        composable(Screen.EditarPerfil.route) {
            EditarPerfilScreen(navController = navController)
        }

        composable(Screen.Chat.route) { backStack ->
            val idEncargo      = backStack.arguments?.getString("idEncargo")      ?: ""
            val idReceptor     = backStack.arguments?.getString("idReceptor")     ?: ""
            val nombreProducto = backStack.arguments?.getString("nombreProducto")
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
            ChatScreen(
                idEncargo      = idEncargo,
                idReceptor     = idReceptor,
                nombreProducto = nombreProducto,
                navController  = navController
            )
        }
    }
}

fun String.encodeURL(): String =
    java.net.URLEncoder.encode(this, "UTF-8")