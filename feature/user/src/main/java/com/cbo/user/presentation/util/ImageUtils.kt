package com.cbo.user.presentation.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    
    /**
     * Copies an image from a content URI to internal storage
     * @param context Android context
     * @param sourceUri The content URI of the selected image
     * @return The file path of the copied image, or null if the operation failed
     */
    fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            
            if (inputStream == null) {
                Log.e("ImageUtils", "Failed to open input stream for URI: $sourceUri")
                return null
            }
            
            // Create a unique filename
            val fileName = "avatar_${UUID.randomUUID()}.jpg"
            val avatarsDir = File(context.filesDir, "avatars")
            
            // Create avatars directory if it doesn't exist
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }
            
            val outputFile = File(avatarsDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            
            // Copy the image
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val savedPath = outputFile.absolutePath
            Log.d("ImageUtils", "Image copied successfully to: $savedPath")
            
            savedPath
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error copying image to internal storage", e)
            null
        }
    }
    
    /**
     * Deletes an old avatar file if it exists
     * @param filePath The path to the file to delete
     */
    fun deleteOldAvatar(filePath: String?) {
        if (filePath.isNullOrEmpty()) return
        
        try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Log.d("ImageUtils", "Old avatar deleted: $filePath")
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error deleting old avatar: $filePath", e)
        }
    }
}
