package com.example.smartcoffeefarm.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcoffeefarm.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
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

            viewModel.register(username, password)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.authResult.observe(this) { result ->
            if (result != null) {
                result.fold(
                    onSuccess = { user ->
                        Toast.makeText(this, "Registrasi Berhasil: ${user.username}", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, error.message ?: "Registrasi Gagal", Toast.LENGTH_SHORT).show()
                    }
                )
                viewModel.resetResult()
            }
        }
    }
}
