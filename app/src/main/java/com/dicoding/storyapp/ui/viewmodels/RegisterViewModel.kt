package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.UserRepository

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {
    fun register(name: String, email: String, password: String) = repository.register(name, email, password)
}