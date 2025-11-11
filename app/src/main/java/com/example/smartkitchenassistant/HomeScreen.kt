package com.example.smartkitchenassistant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}){
    val auth = Firebase.auth
    val user = auth.currentUser

    val context = LocalContext.current
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestEmail().build()
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column {
            Text("HOME SCREEN", fontSize = 30.sp)

            if (user != null){
                Text(user.email.toString())
            }else{
                Text("No hay usuario")
            }
            Button(onClick = {
                //auth.signOut()
                auth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    onClickLogout
                }
                //onClickLogout()
            },
                colors = ButtonDefaults.buttonColors()) {
                Text("Cerrar Sesión")
            }
        }
    }
}