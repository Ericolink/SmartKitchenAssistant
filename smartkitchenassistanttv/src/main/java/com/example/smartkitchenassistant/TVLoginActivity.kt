package com.example.smartkitchenassistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class TVLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreenTV { uid ->
                val intent = Intent(this, TVMainActivity::class.java)
                intent.putExtra("uid", uid)
                startActivity(intent)
                finish()
            }
        }
    }
}
