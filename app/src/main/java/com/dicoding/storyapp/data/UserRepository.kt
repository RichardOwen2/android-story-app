package com.dicoding.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.remote.response.MessageResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
) {
    fun login(email: String, password: String): LiveData<Result<Boolean>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val response = apiService.login(email, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.error) {
                    val (name, userId, token) = body.loginResult
                    val user = UserModel(name, userId, token)
                    saveSession(user)
                    emit(Result.Success(true))
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

    fun register(name: String, email: String, password: String): LiveData<Result<Boolean>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)

        try {
            val response = apiService.register(name, email, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.error) {
                    emit(Result.Success(true))
                } else {
                    emit(Result.Error("Register Failed"))
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

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }


    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}