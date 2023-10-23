package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapp.data.UserRepository

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession() = repository.getSession().asLiveData()

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)
}