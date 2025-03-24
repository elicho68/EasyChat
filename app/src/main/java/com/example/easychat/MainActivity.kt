package com.example.easychat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.easychat.ui.screens.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Error al obtener token FCM", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "Token FCM: $token")
            }

        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()

            NavHost(
                navController = navController,
                startDestination = if (auth.currentUser != null) "home" else "login"
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onForgotPassword = { /* por implementar */ },
                        onRegister = { navController.navigate("register") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.popBackStack()
                            navController.navigate("login")
                        },
                        onLogin = { navController.popBackStack() }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onProfile = { /* por implementar */ },
                        onChatSelected = { contactId, contactName ->
                            navController.navigate("chat/$contactId/$contactName")
                        },
                        onGroupSelected = { groupId ->
                            navController.navigate("group_chat/$groupId")
                        },
                   //     onCreateGroup = { /* si más adelante quieres abrir un flujo */ }
                    )
                }

                composable(
                    "chat/{contactId}/{contactName}",
                    arguments = listOf(
                        navArgument("contactId") { type = NavType.StringType },
                        navArgument("contactName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                    val contactName = backStackEntry.arguments?.getString("contactName") ?: ""

                    ChatScreen(
                        contactId = contactId,
                        contactName = contactName,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "group_chat/{groupId}",
                    arguments = listOf(
                        navArgument("groupId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

                    GroupChatScreen(
                        groupId = groupId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
