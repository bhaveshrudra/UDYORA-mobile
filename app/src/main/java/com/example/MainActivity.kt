package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AnalysisProcessingScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BusinessScreen
import com.example.ui.screens.CapitalScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.EntryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.LocationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SharedViewModel
import com.example.ui.screens.TransparencyScreen
import com.example.ui.screens.WelcomeBackScreen
import com.example.ui.screens.admin.AdminAuthScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AuditLogScreen
import com.example.ui.screens.admin.ParticipantManagementScreen
import com.example.ui.screens.admin.SchemeManagementScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UdyoraApp()
            }
        }
    }
}

@Composable
fun UdyoraApp() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()
    
    NavHost(
        navController = navController, 
        startDestination = "entry",
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
        }
    ) {
        composable("entry") { EntryScreen(navController, sharedViewModel) }
        composable(
            "welcome_back/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            WelcomeBackScreen(
                navController,
                userName = backStackEntry.arguments?.getString("userName") ?: "Entrepreneur"
            )
        }
        composable("language") { LanguageSelectionScreen(navController) }
        composable("profile") { ProfileScreen(navController, sharedViewModel) }
        composable("transparency") { TransparencyScreen(navController) }
        composable("location") { LocationScreen(navController, sharedViewModel) }
        composable("business") { BusinessScreen(navController, sharedViewModel) }
        composable("capital") { CapitalScreen(navController, sharedViewModel) }
        composable("review") { ReviewScreen(navController, sharedViewModel) }
        composable("auth") { AuthScreen(navController) }
        composable("home") { HomeScreen(navController, sharedViewModel) }
        composable("chatbot") { ChatbotScreen(navController, sharedViewModel) }
        composable("assessment") { LocationScreen(navController, sharedViewModel) }
        
        // Protected Admin Routes
        composable("admin_auth") { AdminAuthScreen(navController) }
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("admin_participants") { ParticipantManagementScreen(navController) }
        composable("admin_schemes") { SchemeManagementScreen(navController) }
        composable("admin_audit") { AuditLogScreen(navController) }
        
        composable("processing") {
            AnalysisProcessingScreen(
                navController,
                sharedViewModel
            )
        }
        
        composable("result") {
            ResultScreen(
                navController,
                sharedViewModel
            )
        }
    }
}
