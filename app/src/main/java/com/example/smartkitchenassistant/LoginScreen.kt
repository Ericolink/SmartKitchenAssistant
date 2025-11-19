package com.example.smartkitchenassistant

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth

@Composable
fun loginScreen(
    onClickRegister: () -> Unit = {},
    onSuccessfulLogin: () -> Unit = {}
) {
    // 🎨 PALETA DE COLORES
    val Primario = Color(0xFFF9F5F0)
    val Secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity

    // Estados
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    // Reset password dialog
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Primario
    ) { pv ->

        Column(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize()
                .background(Primario)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // LOGO
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CARD CENTRAL ELEVADO (Diseño Modern UI)
            Card(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 14.dp,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                colors = CardDefaults.cardColors(containerColor = Secundario),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Bienvenido",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = verde
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Inicia sesión en tu cuenta",
                        fontSize = 15.sp,
                        color = verde.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // EMAIL
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Correo electrónico", color = verde) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = verde)
                        },
                        supportingText = {
                            if (emailError.isNotEmpty())
                                Text(emailError, color = Color.Red)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Email,
                            autoCorrectEnabled = false
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = naranja,
                            unfocusedBorderColor = verde,
                            cursorColor = naranja
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("Contraseña", color = verde) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = verde)
                        },
                        supportingText = {
                            if (passwordError.isNotEmpty())
                                Text(passwordError, color = Color.Red)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            autoCorrectEnabled = false
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = naranja,
                            unfocusedBorderColor = verde,
                            cursorColor = naranja
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (loginError.isNotEmpty()) {
                        Text(
                            text = loginError,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // BOTÓN LOGIN (moderno)
                    Button(
                        onClick = {
                            val validEmail = validateEmail(inputEmail)
                            val validPass = validatePassword(inputPassword)

                            emailError = validEmail.second
                            passwordError = validPass.second

                            if (validEmail.first && validPass.first) {
                                auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                                    .addOnCompleteListener(activity) { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            if (user != null && user.isEmailVerified) {
                                                onSuccessfulLogin()
                                            } else {
                                                loginError = "Debes verificar tu correo."
                                                auth.signOut()
                                            }
                                        } else {
                                            loginError = when (task.exception) {
                                                is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrecta"
                                                is FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo"
                                                else -> "Error al iniciar sesión"
                                            }
                                        }
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = naranja,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REGISTRO
                    TextButton(onClick = onClickRegister) {
                        Text("¿No tienes cuenta? Regístrate", color = verde)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // RESET PASSWORD
                    TextButton(onClick = { showResetDialog = true }) {
                        Text("Olvidé mi contraseña", color = verde)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // 🔐 DIALOG RESETEAR CONTRASEÑA
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (resetEmail.isNotEmpty()) {
                            auth.sendPasswordResetEmail(resetEmail)
                            resetMessage = "Correo de recuperación enviado."
                        } else {
                            resetMessage = "Ingresa un correo válido."
                        }
                        showResetDialog = false
                    }) {
                        Text("Enviar", color = naranja)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar", color = verde)
                    }
                },
                title = { Text("Restablecer contraseña") },
                text = {
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Correo electrónico") }
                    )
                }
            )
        }
    }
}
