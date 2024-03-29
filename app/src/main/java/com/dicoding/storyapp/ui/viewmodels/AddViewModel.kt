package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.StoryRepository
import java.io.File

class AddViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun addStory(description: String, photo: File, lat: Float?, lon: Float?)  = storyRepository.addStory(description, photo, lat, lon)
}