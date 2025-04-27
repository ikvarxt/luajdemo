package com.example.luajdemo.helper

import android.content.res.AssetManager
import java.io.File

/**
 * recursively copy all file in [assetPath] directory to external file dir
 */
fun AssetManager.copyTo(assetPath: String, targetFile: File) {
    // Create the destination directory if it doesn't exist
    if (!targetFile.exists()) {
        targetFile.mkdirs()
    }

    // List all files in the asset directory
    val files = this.list(assetPath) ?: return

    for (file in files) {
        val assetFilePath = if (assetPath.isEmpty()) file else "$assetPath/$file"
        val outFile = File(targetFile, file)

        try {
            // Check if it's a directory
            val subFiles = this.list(assetFilePath)
            if (subFiles?.isNotEmpty() == true) {
                // Recursively copy subdirectory
                copyTo(assetFilePath, outFile)
            } else {
                // Copy file
                this.open(assetFilePath).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            // If we can't list directory contents, assume it's a file and try to copy it
            this.open(assetFilePath).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
