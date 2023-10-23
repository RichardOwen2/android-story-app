package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.StoryRepository

class LocationViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun getStoriesWithLocation() = storyRepository.getStoriesWithLocation()
}