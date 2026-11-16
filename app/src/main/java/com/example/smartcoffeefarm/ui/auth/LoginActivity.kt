package com.example.smartcoffeefarm.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcoffeefarm.databinding.ActivityLoginBinding
import com.example.smartcoffeefarm.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Smooth Slide-Up + Fade-In Entrance Animation on Card
        val slideUp = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0.25f,
            Animation.RELATIVE_TO_SELF, 0f
        ).apply {
            duration = 750
            fillAfter = true
        }
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 750
            fillAfter = true
        }
        val animSet = AnimationSet(true).apply {
            addAnimation(slideUp)
            addAnimation(fadeIn)
        }
        binding.cardLogin.startAnimation(animSet)

        // Logo & Welcome header scale-in animation
        val scaleIn = ScaleAnimation(
            0.6f, 1.0f,
            0.6f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 800
            fillAfter = true
        }
        binding.layoutWelcomeHeader.startAnimation(scaleIn)

        // Slow zoom (Ken Burns effect) animation on background
        val scaleAnim = ScaleAnimation(
            1.0f, 1.08f,
            1.0f, 1.08f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 6000
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        binding.ivLoginBg.startAnimation(scaleAnim)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.tilUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.tilUsername.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            viewModel.login(username, password)
        }

        binding.btnGuestLogin.setOnClickListener {
            Toast.makeText(this, "Masuk sebagai Tamu", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USER_ID", 0L)
                putExtra("USERNAME", "Tamu")
            }
            startActivity(intent)
            finish()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.authResult.observe(this) { result ->
            if (result != null) {
                result.fold(
                    onSuccess = { user ->
                        Toast.makeText(this, "Login Berhasil: ${user.username}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("USER_ID", user.id)
                            putExtra("USERNAME", user.username)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, error.message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                    }
                )
                viewModel.resetResult()
            }
        }
    }
}
