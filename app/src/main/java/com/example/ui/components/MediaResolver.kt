package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import com.example.R
import java.io.File

@DrawableRes
fun resolveDrawableRes(key: String): Int {
    return when (key) {
        "avatar_arjun" -> R.drawable.img_creator_arjun_1795504855258
        "avatar_meera" -> R.drawable.img_creator_meera_1795504856666
        "post_mountain" -> R.drawable.img_post_mountain_1795504855842
        "post_street" -> R.drawable.img_post_street_1795504857151
        else -> R.drawable.img_post_mountain_1795504855842
    }
}

fun createCameraCaptureUri(context: Context, prefix: String = "capture"): Uri {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

fun persistMediaCopy(context: Context, sourceUri: Uri, prefix: String = "media"): String? {
    return try {
        val dir = File(context.filesDir, "media").apply { mkdirs() }
        val mime = context.contentResolver.getType(sourceUri).orEmpty()
        val ext = when {
            mime.contains("video") -> "mp4"
            mime.contains("png") -> "png"
            else -> "jpg"
        }
        val destFile = File(dir, "${prefix}_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destFile).toString()
    } catch (_: Exception) {
        sourceUri.toString()
    }
}
