package com.example.smartkitchenassistant

import android.util.Patterns
import org.intellij.lang.annotations.Pattern

//returnar un true si es valido y un false si no es valido
//tambien retorne una cadena que diga que pasa si no es valido

fun validateEmail(email: String): Pair<Boolean, String>{
    return when{
        email.isEmpty() -> Pair(false, "El correo es requerido.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Pair(false, "El correo es invalido")
        !email.endsWith("@gmail.com")-> Pair(false, "Ese email no es corporativo.")
        else -> Pair (true, "")
    }
}

fun validatePassword(password:String): Pair<Boolean, String>{
    return  when{
        password.isEmpty() -> Pair(false, "La contraseña es requerida.")
        password.length < 8 -> Pair(false, "La contraseña debe tener al menos 8 caracteres")
        !password.any{it.isDigit()} -> Pair(false, "La contraseña debe tener al menos un número")
        else -> Pair(true, "")
    }
}