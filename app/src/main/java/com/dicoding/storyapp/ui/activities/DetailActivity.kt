package com.dicoding.storyapp.ui.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.databinding.ActivityDetailBinding
import com.dicoding.storyapp.ui.viewmodels.DetailViewModel
import com.dicoding.storyapp.ui.viewmodels.ViewModelFactory
import com.dicoding.storyapp.utils.formatDate
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val storyId = intent.getStringExtra(STORY_ID) as String

        viewModel.getDetailStory(storyId).observe(this) { story ->
            when (story) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    with(story.data.story) {
                        binding.apply {
                            Glide.with(this@DetailActivity)
                                .load(photoUrl)
                                .into(storyPhoto)

                            createdAt.text = this@with.createdAt.formatDate()
                            storyName.text = name
                            storyDescription.text = description
                        }
                    }
                }

                is Result.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, story.error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val STORY_ID = "STORY_ID"
    }
}