package com.example.smartkitchenassistant

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.delay

@Composable
fun loginScreen(onClickRegister : ()-> Unit = {}, onSuccessfulLogin : ()-> Unit = {}){

    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity

    //ESTADOS
    var inputEmail by remember {
        mutableStateOf("")
    }
    var inputPassword by remember {
        mutableStateOf("")
    }
    var loginError by remember {
        mutableStateOf("")
    }
    var emailError by remember {
        mutableStateOf("")
    }
    var passwordError by remember {
        mutableStateOf("")
    }

    //Recuperación de la contraseña
    var showResetDialog by remember {
        mutableStateOf(false)
    }
    var resetEmail by remember {
        mutableStateOf("")
    }
    var resetMessage by remember {
        mutableStateOf("")
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Login Image",
                modifier = Modifier.size(200.dp)
            )
            Text(text = "Bienvenido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Inicia sesion en tu cuenta")

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = {
                    Text(text = "Correo Electronico")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                supportingText = {
                    if (emailError.isNotEmpty()){
                        Text(
                            text = emailError,
                            color = Color.Red
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = {
                    Text(text = "Contraseña")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                supportingText = {
                    if (passwordError.isNotEmpty()){
                        Text(
                            text = passwordError,
                            color = Color.Red
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Password
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (loginError.isNotEmpty()){
                Text(
                    loginError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 8.dp)
                )
            }

            Button(onClick = {
                val isValidEmail = validateEmail(inputEmail).first
                val isValidPassword = validatePassword(inputPassword).first
                emailError = validateEmail(inputEmail).second
                passwordError = validatePassword(inputPassword).second

                if (isValidEmail && isValidPassword) {
                    auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null && user.isEmailVerified) {
                                    onSuccessfulLogin()
                                } else {
                                    loginError = "Debes verificar tu correo antes de iniciar sesión."
                                    auth.signOut()
                                }
                            } else {
                                loginError = when (task.exception) {
                                    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrecta"
                                    is FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo"
                                    else -> "Error al iniciar sesión. Intenta de nuevo"
                                }
                            }
                        }
                }
            }) {
                Text(text = "Iniciar Sesión")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onClickRegister) {
                Text("¿No tienes una cuenta? Regístrate")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { showResetDialog = true}) {
                Text(text = "Olvidé mi contraseña")
            }
        }

        if (resetMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = resetMessage,
                        color = Color(0xFF1565C0),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hemos enviado un correo de recuperación. Revisa tu bandeja y vuelve a intentar iniciar sesión.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (resetEmail.isNotEmpty()) {
                        auth.sendPasswordResetEmail(resetEmail)
                            .addOnCompleteListener { task ->
                                resetMessage = if (task.isSuccessful) {
                                    "Se ha enviado un correo para restablecer tu contraseña."
                                } else {
                                    "No se pudo enviar el correo. Verifica la dirección."
                                }
                                showResetDialog = false
                            }
                    } else {
                        resetMessage = "Por favor, ingresa tu correo electrónico."
                        showResetDialog = false
                    }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true
                    )
                }
            }
        )
    }
    if (resetMessage.isNotEmpty()) {
        LaunchedEffect(resetMessage) {
            delay(6000)
            resetMessage = ""
        }
    }
}