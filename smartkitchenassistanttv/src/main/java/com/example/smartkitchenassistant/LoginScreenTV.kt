package com.example.smartkitchenassistant

// Importaciones necesarias para la UI y Firebase
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import android.app.Activity
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
    onLoginSuccess: (String) -> Unit // Callback cuando el login sale bien
) {
    // Instancia de Firebase Auth
    val auth = Firebase.auth

    // Necesario para que Firebase pueda ejecutar listeners dentro de Compose
    val activity = LocalView.current.context as Activity

    // Variables del formulario controladas con remember
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Paleta de colores personalizada
    val bgColor = Color(0xFFF2EAD3)
    val inputBg = Color(0xFFF9F5F0)
    val textColor = Color(0xFF344F1F)
    val accent = Color(0xFFF4991A)

    // Box que cubre toda la pantalla y aplica el color de fondo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor) // Fondo completo
    ) {
        // Contenedor principal del contenido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp) // Espacio lateral para respiración visual
                .align(androidx.compose.ui.Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {

            // Título de la pantalla
            Text(
                text = "Smart Display (TV)",
                style = MaterialTheme.typography.displayMedium,
                color = textColor
            )

            Spacer(Modifier.height(40.dp))

            //--------------------------------------------------------------------
            // EMAIL
            //--------------------------------------------------------------------

            Text("Correo electrónico", style = MaterialTheme.typography.titleMedium, color = textColor)
            Spacer(Modifier.height(10.dp))

            // Campo de texto para email
            TextField(
                value = email,
                onValueChange = { email = it },         // Actualiza la variable email
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,   // Fondo cuando no está seleccionado
                    focusedContainerColor = inputBg,     // Fondo cuando sí está seleccionado
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = textColor,             // Color de la línea del cursor
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(Modifier.height(20.dp))

            //--------------------------------------------------------------------
            // PASSWORD
            //--------------------------------------------------------------------

            Text("Contraseña", style = MaterialTheme.typography.titleMedium, color = textColor)
            Spacer(Modifier.height(10.dp))

            // Campo de texto para contraseña
            TextField(
                value = pass,
                onValueChange = { pass = it },           // Actualiza la variable pass
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), // Oculta caracteres
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = textColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(Modifier.height(25.dp))

            // Si hay un error, se muestra aquí
            if (error.isNotEmpty()) {
                Text(error, color = Color.Red)
            }

            Spacer(Modifier.height(25.dp))

            //--------------------------------------------------------------------
            // BOTÓN DE INICIAR SESIÓN
            //--------------------------------------------------------------------

            Button(
                onClick = {
                    // Intento de login con Firebase Auth
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                // Si se logró el login, obtener el UID del usuario
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    onLoginSuccess(uid) // Continuar con la app
                                }
                            } else {
                                // Manejo de errores comunes
                                error = when (task.exception) {
                                    is FirebaseAuthInvalidCredentialsException ->
                                        "Correo o contraseña incorrectos"

                                    is FirebaseAuthInvalidUserException ->
                                        "Esta cuenta no existe"

                                    else -> "Error al iniciar sesión"
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.colors(
                    containerColor = accent, // Color naranja del botón
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar Sesión", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
