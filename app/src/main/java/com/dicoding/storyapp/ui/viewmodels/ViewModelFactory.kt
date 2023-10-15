package com.dicoding.storyapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.StoryRepository
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.di.Injection

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(MainViewModel::class.java) -> MainViewModel(userRepository, storyRepository)
            isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(userRepository)
            isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(userRepository)
            isAssignableFrom(DetailViewModel::class.java) -> DetailViewModel(storyRepository)
            isAssignableFrom(AddViewModel::class.java) -> AddViewModel(storyRepository)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: ViewModelFactory(
                Injection.provideUserRepository(context),
                Injection.provideStoryRepository(context)
            )
        }.also { instance = it }
    }
}