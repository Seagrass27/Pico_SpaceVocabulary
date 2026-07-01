package com.example.spacewords.ui

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast

fun playAssetAudio(
    context: Context,
    assetPath: String,
    currentPlayer: MediaPlayer?,
    updatePlayer: (MediaPlayer?) -> Unit
) {
    if (assetPath.isBlank()) return
    runCatching {
        currentPlayer?.release()
        val descriptor = context.assets.openFd(assetPath)
        val player = MediaPlayer()
        player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        descriptor.close()
        player.setOnCompletionListener {
            it.release()
            updatePlayer(null)
        }
        player.prepare()
        player.start()
        updatePlayer(player)
    }.onFailure {
        Toast.makeText(context, "Audio unavailable", Toast.LENGTH_SHORT).show()
        updatePlayer(null)
    }
}
