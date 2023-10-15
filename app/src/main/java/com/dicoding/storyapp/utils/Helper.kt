package com.dicoding.storyapp.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.truncateText(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.substring(0, maxLength - 3) + "..."
    }
}

fun String.formatDate(): String {
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    val outputDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))

    return try {
        val date = inputDateFormat.parse(this)
        outputDateFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun createCustomTempFile(context: Context): File {
    val filesDir = context.externalCacheDir
    return File.createTempFile(SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()), ".jpg", filesDir)
}
