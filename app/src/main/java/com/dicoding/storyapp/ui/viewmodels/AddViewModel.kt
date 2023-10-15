package com.dicoding.storyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.StoryRepository
import java.io.File

class AddViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun addStory(description: String, photo: File, lat: String?, lon: String?)  = storyRepository.addStory(description, photo, lat, lon)
}