package com.dicoding.storyapp

import com.dicoding.storyapp.data.local.Story

object DataDummy {
    fun generateDummyStoryResponse(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                id = i.toString(),
                name = "author + $i",
                photoUrl = "https://source.unsplash.com/random/800x600",
                description = "quote $i",
                lat = null,
                lon = null,
            )
            items.add(story)
        }
        return items
    }
}