package com.example.spacewords.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

enum class WordStatus {
    New,
    Revision,
    Remembered
}

data class WordProgress(
    val wordId: String,
    val status: WordStatus,
    val addedAt: Long,
    val rememberedAt: Long? = null
)

object SpaceWordsStore {
    private const val PrefName = "space_words_progress"
    private const val RevisionKey = "revision"
    private const val RememberedKey = "remembered"

    private var loaded = false
    private lateinit var appContext: Context
    private val progress = mutableStateMapOf<String, WordProgress>()
    val version = mutableStateOf(0)

    fun ensureLoaded(context: Context) {
        if (loaded) return
        appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        prefs.getStringSet(RevisionKey, emptySet()).orEmpty().forEach { encoded ->
            decode(encoded, WordStatus.Revision)?.let { progress[it.wordId] = it }
        }
        prefs.getStringSet(RememberedKey, emptySet()).orEmpty().forEach { encoded ->
            decode(encoded, WordStatus.Remembered)?.let { progress[it.wordId] = it }
        }
        loaded = true
        version.value += 1
    }

    fun statusFor(wordId: String): WordStatus = progress[wordId]?.status ?: WordStatus.New

    fun revisionWords(): List<WordProgress> =
        progress.values
            .filter { it.status == WordStatus.Revision }
            .sortedBy { it.addedAt }

    fun rememberedWords(): List<WordProgress> =
        progress.values
            .filter { it.status == WordStatus.Remembered }
            .sortedByDescending { it.rememberedAt ?: it.addedAt }

    fun putInRevision(wordId: String) {
        val existing = progress[wordId]
        progress[wordId] = WordProgress(
            wordId = wordId,
            status = WordStatus.Revision,
            addedAt = existing?.addedAt ?: System.currentTimeMillis(),
            rememberedAt = null
        )
        persist()
    }

    fun markRemembered(wordId: String) {
        val existing = progress[wordId]
        val now = System.currentTimeMillis()
        progress[wordId] = WordProgress(
            wordId = wordId,
            status = WordStatus.Remembered,
            addedAt = existing?.addedAt ?: now,
            rememberedAt = now
        )
        persist()
    }

    private fun persist() {
        if (!loaded) return
        val revision = progress.values
            .filter { it.status == WordStatus.Revision }
            .map { encode(it) }
            .toSet()
        val remembered = progress.values
            .filter { it.status == WordStatus.Remembered }
            .map { encode(it) }
            .toSet()
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(RevisionKey, revision)
            .putStringSet(RememberedKey, remembered)
            .apply()
        version.value += 1
    }

    private fun encode(progress: WordProgress): String =
        listOf(
            progress.wordId,
            progress.addedAt.toString(),
            (progress.rememberedAt ?: 0L).toString()
        ).joinToString("|")

    private fun decode(value: String, status: WordStatus): WordProgress? {
        val parts = value.split("|")
        if (parts.size < 2) return null
        val addedAt = parts[1].toLongOrNull() ?: return null
        val rememberedAt = parts.getOrNull(2)?.toLongOrNull()?.takeIf { it > 0L }
        return WordProgress(parts[0], status, addedAt, rememberedAt)
    }
}
