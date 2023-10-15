package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.StoryRepository

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getDetailStory(id: String) = storyRepository.getDetailStory(id)
}