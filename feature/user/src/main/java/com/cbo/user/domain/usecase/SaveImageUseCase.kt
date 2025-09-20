package com.cbo.user.domain.usecase

import android.content.Context
import android.net.Uri
import com.cbo.user.presentation.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Saves an image from a content URI to internal storage
     * @param contentUri The content URI from image picker
     * @param oldImagePath The path of the old image to delete (optional)
     * @return The file path of the saved image, or null if failed
     */
    operator fun invoke(contentUri: Uri, oldImagePath: String? = null): String? {
        // Delete old image if it exists
        if (!oldImagePath.isNullOrEmpty()) {
            ImageUtils.deleteOldAvatar(oldImagePath)
        }
        
        // Copy new image to internal storage
        return ImageUtils.copyImageToInternalStorage(context, contentUri)
    }
}
