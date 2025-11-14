package com.example.smartkitchenassistant

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun NavigationApp() {
    val myNavController = rememberNavController()
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    val myStartDestination = if (currentUser != null) "home" else "login"

    NavHost(
        navController = myNavController,
        startDestination = myStartDestination
    ) {
        composable("login") {
            loginScreen(
                onClickRegister = {
                    myNavController.navigate("register")
                },
                onSuccessfulLogin = {
                    myNavController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onClickBack = { myNavController.popBackStack() },
                onSuccessfulRegister = {
                    myNavController.navigate("home") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onClickLogout = {
                    myNavController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
