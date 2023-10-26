package com.dicoding.storyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
class Story (
    @PrimaryKey val id: String,
    val name: String,
    val photoUrl: String,
    val description: String,
    val lat: String? = null,
    val lon: String? = null,
)