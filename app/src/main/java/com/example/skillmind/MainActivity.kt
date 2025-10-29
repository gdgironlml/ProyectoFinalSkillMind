package com.example.skillmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.skillmind.presentations.screens.JuegoScreen
import com.example.skillmind.presentations.screens.ResultadosScreen
import com.example.skillmind.presentations.viewmodel.PreguntasViewModel
import com.example.skillmind.presentations.screens.CamaraScreen
import com.example.skillmind.presentations.screens.EsperaScreen
import com.example.skillmind.presentations.screens.GenerarScreen
import com.example.skillmind.presentations.screens.LoginPage
import com.example.skillmind.presentations.screens.PDFScreen
import com.example.skillmind.presentations.screens.RegisterScreen
import com.example.skillmind.presentations.screens.SalasScreen
import com.example.skillmind.presentations.screens.JugarSoloScreen
import com.example.skillmind.presentations.screens.UnirseScreen
import com.example.skillmind.theme.SkillMindTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private val viewModelFactory by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillMindTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MyApp(viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    val loginRoute = stringResource(id = R.string.login_screen_route)
    val registerRoute = stringResource(id = R.string.register_screen_route)
    val salasRoute = stringResource(id = R.string.salas_screen_route)

    val viewModel: PreguntasViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)

    val auth = Firebase.auth
    val startDestination = if (auth.currentUser != null) salasRoute else loginRoute

    NavHost(navController = navController, startDestination = startDestination) {
        composable("sincodigo") {
            JugarSoloScreen(navController = navController)
        }
        composable("generate") {
            GenerarScreen(navController = navController, vm = viewModel)
        }
        composable("pdf") {
            PDFScreen(navController = navController, vm = viewModel)
        }
        composable("camera") {
            CamaraScreen(navController = navController, vm = viewModel)
        }

        composable(route = loginRoute) {
            LoginPage(navController = navController)
        }
        composable(route = registerRoute) {
            RegisterScreen(navController = navController)
        }
        composable(route = salasRoute) {
            SalasScreen(navController = navController, vm = viewModel)
        }
        composable(
            route = "espera_screen/{codigoSala}/{esHost}/{nombreUsuario}",
            arguments = listOf(
                navArgument("codigoSala") { type = NavType.StringType },
                navArgument("esHost") { type = NavType.BoolType },
                navArgument("nombreUsuario") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoSala") ?: ""
            val isHost = backStackEntry.arguments?.getBoolean("esHost") ?: false
            val nombreUsuario = backStackEntry.arguments?.getString("nombreUsuario") ?: ""

            EsperaScreen(
                navController = navController,
                codigoSala = codigo,
                esHost = isHost,
                nombreUsuario = nombreUsuario
            )
        }
        composable(
            route = "unirse_screen/{nombreUsuario}",
            arguments = listOf(
                navArgument("nombreUsuario") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombreUsuario = backStackEntry.arguments?.getString("nombreUsuario") ?: ""
            UnirseScreen(navController = navController, nombreUsuario = nombreUsuario)
        }

        composable(
            route = "juego_screen/{codigoSala}",
            arguments = listOf(
                navArgument("codigoSala") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val codigoSala = backStackEntry.arguments?.getString("codigoSala") ?: ""
            JuegoScreen(navController = navController, codigoSala = codigoSala)
        }

        composable(
            route = "resultados_screen/{codigoSala}",
            arguments = listOf(
                navArgument("codigoSala") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val codigoSala = backStackEntry.arguments?.getString("codigoSala") ?: ""
            ResultadosScreen(navController = navController, codigoSala = codigoSala)
        }
    }
}