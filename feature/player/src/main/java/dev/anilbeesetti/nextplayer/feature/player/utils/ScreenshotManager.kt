package dev.anilbeesetti.nextplayer.feature.player.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

@OptIn(UnstableApi::class)
suspend fun captureScreenshot(context: Context, player: Player) {
    withContext(Dispatchers.IO) {
        try {
            val uri = player.currentMediaItem?.localConfiguration?.uri ?: return@withContext
            val positionMs = player.currentPosition
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(
                positionMs * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST,
            )
            retriever.release()

            if (bitmap == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Could not capture frame", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val filename = "nextplayer_screenshot_${System.currentTimeMillis()}.jpg"
            val outputStream: OutputStream?
            val imageUri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NextPlayer")
                }
                imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues,
                )
                outputStream = imageUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val nextPlayerDir = java.io.File(picturesDir, "NextPlayer").apply { mkdirs() }
                val file = java.io.File(nextPlayerDir, filename)
                imageUri = Uri.fromFile(file)
                outputStream = file.outputStream()
            }

            outputStream?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }

            withContext(Dispatchers.Main) {
                if (imageUri != null) {
                    Toast.makeText(context, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not save screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Screenshot failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
