package com.example.smartview.auth

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class to manage local profile preferences using SharedPreferences.
 * Stores profile photo locally without affecting the Google account.
 */
class ProfilePreferences(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "profile_prefs"
        private const val KEY_LOCAL_PHOTO_PATH = "local_photo_path"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Saves the selected photo to internal storage and stores the path in SharedPreferences.
     * Returns the local file path if successful, null otherwise.
     */
    fun saveProfilePhoto(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            // Delete previous photo if exists
            getSavedPhotoPath()?.let { oldPath ->
                File(oldPath).delete()
            }
            
            // Copy the image to internal storage
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            
            // Save the path in SharedPreferences
            prefs.edit().putString(KEY_LOCAL_PHOTO_PATH, file.absolutePath).apply()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Returns the saved local photo path, or null if not set.
     */
    fun getSavedPhotoPath(): String? {
        return prefs.getString(KEY_LOCAL_PHOTO_PATH, null)
    }
    
    /**
     * Clears the saved local photo.
     */
    fun clearProfilePhoto() {
        getSavedPhotoPath()?.let { path ->
            File(path).delete()
        }
        prefs.edit().remove(KEY_LOCAL_PHOTO_PATH).apply()
    }
}
