package com.example.smartcoffeefarm.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcoffeefarm.data.CoffeeDatabase
import com.example.smartcoffeefarm.data.CoffeeRepository
import com.example.smartcoffeefarm.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CoffeeRepository

    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> get() = _authResult

    init {
        val dao = CoffeeDatabase.getDatabase(application).coffeeDao()
        repository = CoffeeRepository(dao)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByUsername(username)
                if (user != null && user.password == password) {
                    _authResult.value = Result.success(user)
                } else {
                    _authResult.value = Result.failure(Exception("Username atau password salah!"))
                }
            } catch (e: Exception) {
                _authResult.value = Result.failure(e)
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByUsername(username)
                if (existing != null) {
                    _authResult.value = Result.failure(Exception("Username sudah terdaftar!"))
                    return@launch
                }
                val newUser = User(username = username, password = password)
                val userId = repository.registerUser(newUser)
                val createdUser = newUser.copy(id = userId)
                _authResult.value = Result.success(createdUser)
            } catch (e: Exception) {
                _authResult.value = Result.failure(e)
            }
        }
    }

    fun resetResult() {
        _authResult.value = null
    }
}
