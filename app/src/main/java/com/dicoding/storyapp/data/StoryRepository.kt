package com.dicoding.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.local.Story
import com.dicoding.storyapp.data.local.StoryDatabase
import com.dicoding.storyapp.data.paging.StoryRemoteMediator
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.remote.response.DetailStoryResponse
import com.dicoding.storyapp.data.remote.response.MessageResponse
import com.dicoding.storyapp.data.remote.response.StoryResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(
                getUserToken(),
                storyDatabase,
                apiService,
            ),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getStoriesWithLocation(): LiveData<Result<StoryResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val response = apiService.getStoriesWithLocation(getUserToken())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.error) {
                    emit(Result.Success(body))
                } else {
                    emit(Result.Error("Login Failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, MessageResponse::class.java)
                    emit(Result.Error(errorResponse.message))
                } else {
                    emit(Result.Error("Register Failed"))
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getDetailStory(id: String): LiveData<Result<DetailStoryResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)

            try {
                val response = apiService.getDetailStory(getUserToken(), id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && !body.error) {
                        emit(Result.Success(body))
                    } else {
                        emit(Result.Error("Login Failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrBlank()) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, MessageResponse::class.java)
                        emit(Result.Error(errorResponse.message))
                    } else {
                        emit(Result.Error("Register Failed"))
                    }
                }
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun addStory(
        description: String, photo: File, lat: Float?, lon: Float?
    ): LiveData<Result<MessageResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val requestDescription = description.toRequestBody("text/plain".toMediaType())
            val requestPhoto = photo.asRequestBody("image/jpeg".toMediaType())

            val multipartPhoto = MultipartBody.Part.createFormData(
                "photo",
                photo.name,
                requestPhoto
            )
            val response =
                apiService.addStory(
                    getUserToken(),
                    multipartPhoto,
                    requestDescription,
                    lat,
                    lon
                )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.error) {
                    emit(Result.Success(body))
                } else {
                    emit(Result.Error("Login Failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, MessageResponse::class.java)
                    emit(Result.Error(errorResponse.message))
                } else {
                    emit(Result.Error("Register Failed"))
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    private fun getUserToken(): String {
        val token = runBlocking {
            userPreference.getToken().first()
        }
        return "Bearer $token"
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(userPreference, apiService, storyDatabase)
            }.also { instance = it }
    }
}