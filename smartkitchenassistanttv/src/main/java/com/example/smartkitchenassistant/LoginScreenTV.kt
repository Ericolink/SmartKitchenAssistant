package com.example.smartkitchenassistant

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth

@Composable
fun LoginScreenTV(
    onLoginSuccess: (String) -> Unit
) {
    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(60.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Smart Kitchen Assistant (TV)",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(Modifier.height(40.dp))

        // Email
        Text("Correo electrónico", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.DarkGray,
                focusedContainerColor = Color.Black
            )
        )

        Spacer(Modifier.height(20.dp))

        // Password
        Text("Contraseña", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        TextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.DarkGray,
                focusedContainerColor = Color.Black
            )
        )

        Spacer(Modifier.height(25.dp))

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(25.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                onLoginSuccess(uid)
                            }
                        } else {
                            error = when (task.exception) {
                                is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
                                is FirebaseAuthInvalidUserException -> "Esta cuenta no existe"
                                else -> "Error al iniciar sesión"
                            }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.titleLarge)
        }
    }
}
