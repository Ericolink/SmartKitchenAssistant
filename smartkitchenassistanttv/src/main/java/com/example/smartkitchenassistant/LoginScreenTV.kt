package com.example.smartkitchenassistant

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
    onLoginSuccess: (String) -> Unit
) {
    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val bgColor = Color(0xFFF2EAD3)
    val inputBg = Color(0xFFF9F5F0)
    val textColor = Color(0xFF344F1F)
    val accent = Color(0xFFF4991A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp)
                .align(androidx.compose.ui.Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Smart Display (TV)",
                style = MaterialTheme.typography.displayMedium,
                color = textColor
            )

            Spacer(Modifier.height(40.dp))

            //--------------------------------------------------------------------
            // EMAIL
            //--------------------------------------------------------------------

            Text("Email", style = MaterialTheme.typography.titleMedium, color = textColor)
            Spacer(Modifier.height(10.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(20.dp))

            //--------------------------------------------------------------------
            // PASSWORD
            //--------------------------------------------------------------------

            Text("Password", style = MaterialTheme.typography.titleMedium, color = textColor)
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

            if (error.isNotEmpty()) {
                Text(error, color = Color.Red)
            }

            Spacer(Modifier.height(25.dp))

            //--------------------------------------------------------------------
            // LOGIN BUTTON
            //--------------------------------------------------------------------

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
                                    is FirebaseAuthInvalidCredentialsException ->
                                        "Incorrect email or password"

                                    is FirebaseAuthInvalidUserException ->
                                        "This account does not exist"

                                    else -> "Error signing in"
                                }
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.colors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text("Sign In", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
