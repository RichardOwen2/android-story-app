package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapp.data.UserRepository

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession() = repository.getSession().asLiveData()

    fun login(email: String, password: String) = repository.login(email, password)
}