package com.dicoding.storyapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
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
) {
    private var userToken: String? = null

    fun getStories(): LiveData<Result<StoryResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val response = apiService.getStories(getUserToken())
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
        description: String, photo: File, lat: String?, lon: String?
    ): LiveData<Result<MessageResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val requestDescription = description.toRequestBody("text/plain".toMediaType())
            val requestPhoto = photo.asRequestBody("image/jpeg".toMediaType())
            val requestLat = lat?.toRequestBody("text/plain".toMediaType())
            val requestLon = lon?.toRequestBody("text/plain".toMediaType())

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
                    requestLat,
                    requestLon
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
        if (userToken != null) return userToken as String
        val token = runBlocking {
            userPreference.getToken().first()
        }
        userToken = "Bearer $token"
        return userToken as String
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(userPreference, apiService)
            }.also { instance = it }
    }
}