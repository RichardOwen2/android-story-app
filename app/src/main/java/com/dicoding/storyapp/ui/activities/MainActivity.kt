package com.dicoding.storyapp.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.ui.adapters.LoadingStateAdapter
import com.dicoding.storyapp.ui.adapters.StoryAdapter
import com.dicoding.storyapp.ui.viewmodels.MainViewModel
import com.dicoding.storyapp.ui.viewmodels.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (user.token == "") {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        setupView()
        setupAction()
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
        supportActionBar?.hide()

        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStory.addItemDecoration(itemDecoration)

        val adapter = StoryAdapter()
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        viewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun setupAction() {
        binding.fabLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }

        binding.fabLocation.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        binding.rvStory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && (binding.fabLogout.isShown || binding.fabAdd.isShown)) {
                    binding.fabLogout.hide()
                    binding.fabAdd.hide()
                } else if (dy < 0 && !(binding.fabLogout.isShown || binding.fabAdd.isShown)) {
                    binding.fabLogout.show()
                    binding.fabAdd.show()
                }
            }
        })
    }
}