package com.muriithi.dekutcallforhelp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.progressindicator.LinearProgressIndicator

class WelcomeActivity : AppCompatActivity() {
    private lateinit var progressIndicator: LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressIndicator = findViewById(R.id.progress_indicator)

        findViewById<View>(R.id.button_create_account).setOnClickListener {
            showProgressAndNavigate(CreateAccountActivity::class.java)
        }

        findViewById<View>(R.id.button_sign_in).setOnClickListener {
            showProgressAndNavigate(SignInActivity::class.java)
        }
    }

    private fun showProgressAndNavigate(destination: Class<*>) {
        progressIndicator.visibility = View.VISIBLE
        Log.d("WelcomeActivity", "Switched to ${destination.simpleName}")
        startActivity(Intent(this, destination))
        progressIndicator.visibility = View.GONE
    }
}