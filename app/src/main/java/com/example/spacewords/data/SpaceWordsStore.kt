package com.example.spacewords.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

enum class WordStatus {
    New,
    Revision,
    Remembered,
    Mastered
}

data class WordProgress(
    val wordId: String,
    val status: WordStatus,
    val addedAt: Long,
    val rememberedAt: Long? = null
)

object SpaceWordsStore {
    private const val PrefName = "space_words_progress_v2"
    private const val RevisionKey = "revision"
    private const val RememberedKey = "remembered"
    private const val MasteredKey = "mastered"
    private const val LearningPrefix = "learning_index_"

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
        prefs.getStringSet(MasteredKey, emptySet()).orEmpty().forEach { encoded ->
            decode(encoded, WordStatus.Mastered)?.let { progress[it.wordId] = it }
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

    fun masteredWords(): List<WordProgress> =
        progress.values
            .filter { it.status == WordStatus.Mastered }
            .sortedByDescending { it.rememberedAt ?: it.addedAt }

    fun learningIndexFor(topicId: String, difficulty: Int = 0): Int {
        if (!loaded) return 0
        return appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .getInt(learningKey(topicId, difficulty), 0)
    }

    fun saveLearningIndex(topicId: String, index: Int, difficulty: Int = 0) {
        if (!loaded) return
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .putInt(learningKey(topicId, difficulty), index.coerceAtLeast(0))
            .apply()
    }

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

    fun markMastered(wordId: String) {
        val existing = progress[wordId]
        val now = System.currentTimeMillis()
        progress[wordId] = WordProgress(
            wordId = wordId,
            status = WordStatus.Mastered,
            addedAt = existing?.addedAt ?: now,
            rememberedAt = now
        )
        persist()
    }

    fun resetToNew(wordId: String) {
        progress.remove(wordId)
        persist()
    }

    fun resetToNew(wordIds: Collection<String>) {
        var changed = false
        wordIds.forEach { wordId ->
            changed = progress.remove(wordId) != null || changed
        }
        if (changed) persist()
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
        val mastered = progress.values
            .filter { it.status == WordStatus.Mastered }
            .map { encode(it) }
            .toSet()
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(RevisionKey, revision)
            .putStringSet(RememberedKey, remembered)
            .putStringSet(MasteredKey, mastered)
            .apply()
        version.value += 1
    }

    private fun learningKey(topicId: String, difficulty: Int): String =
        if (difficulty > 0) "$LearningPrefix${topicId}_$difficulty" else LearningPrefix + topicId

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
