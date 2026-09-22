package com.dish_it.dish_it

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.data.FirebaseRepository

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter your username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            FirebaseRepository.login(
                username = username,
                password = password,
                onSuccess = {
                    val uid = FirebaseRepository.currentUserId
                    if (uid == null) {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Something went wrong signing you in.", Toast.LENGTH_SHORT).show()
                        return@login
                    }
                    // Load this user's saved recipes / shopping list / meal plan
                    // before handing off to the rest of the app.
                    AppData.loadFromFirestore(uid) {
                        startActivity(Intent(this, FindRecipesActivity::class.java))
                        finish()
                    }
                },
                onError = { message ->
                    btnLogin.isEnabled = true
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        findViewById<Button>(R.id.btn_register).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            // TODO: open a "forgot password" flow (FirebaseAuth.sendPasswordResetEmail)
        }
    }
}
