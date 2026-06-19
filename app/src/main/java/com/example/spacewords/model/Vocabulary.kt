package com.example.spacewords.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray

data class Topic(
    val id: String,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val words: List<VocabWord>
)

data class VocabWord(
    val id: String,
    val topicId: String,
    val word: String,
    val phonetic: String,
    val meaningZh: String,
    val example: String,
    val exampleZh: String,
    val audioPath: String,
    val imagePath: String? = null
)

private const val VocabJsonAssetPath = "vocab_bundle/vocab.json"
private const val VocabAssetPrefix = "vocab_bundle/"

private val TopicMetas = listOf(
    TopicMeta("food", "FOOD", "Meals, taste, and restaurant words", Color(0xFFE9B85B)),
    TopicMeta("travel", "TRAVEL", "Airport, hotel, and trip basics", Color(0xFF6AB6E8)),
    TopicMeta("office", "OFFICE", "Workplace routines and tools", Color(0xFF8FA6EA)),
    TopicMeta("interview", "INTERVIEW", "Hiring conversations and self-introduction", Color(0xFFB698F0)),
    TopicMeta("workout", "WORKOUT", "Fitness actions and training plans", Color(0xFF73C7A4)),
    TopicMeta("education", "EDUCATION", "Study, class, and learning goals", Color(0xFFD5B35D))
)

private val FallbackTopics = TopicMetas.map { meta ->
    Topic(
        id = meta.id,
        title = meta.title,
        subtitle = meta.subtitle,
        tint = meta.tint,
        words = listOf(
            VocabWord(
                id = "${meta.id}_sample",
                topicId = meta.id,
                word = meta.title.lowercase(),
                phonetic = "",
                meaningZh = "词表加载中",
                example = "Vocabulary content will appear after the bundle is loaded.",
                exampleZh = "词表资源加载后会显示完整内容。",
                audioPath = "",
                imagePath = null
            )
        )
    )
}

private var loaded = false
private var topicsCache: List<Topic> = FallbackTopics
private var wordsById: Map<String, VocabWord> = FallbackTopics.flatMap { it.words }.associateBy { it.id }

val Topics: List<Topic>
    get() = topicsCache

val AllWords: List<VocabWord>
    get() = topicsCache.flatMap { it.words }

fun ensureVocabularyLoaded(context: Context) {
    if (loaded) return
    runCatching {
        val json = context.assets.open(VocabJsonAssetPath).bufferedReader().use { it.readText() }
        val words = parseWords(JSONArray(json))
        topicsCache = TopicMetas.map { meta ->
            Topic(
                id = meta.id,
                title = meta.title,
                subtitle = meta.subtitle,
                tint = meta.tint,
                words = words.filter { it.topicId == meta.id }
            )
        }
        wordsById = words.associateBy { it.id }
        loaded = true
    }.onFailure {
        loaded = true
    }
}

fun findTopic(id: String?): Topic = Topics.firstOrNull { it.id == id } ?: Topics.first()

fun findWord(id: String): VocabWord? = wordsById[id]

private fun parseWords(array: JSONArray): List<VocabWord> =
    buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val audio = item.optString("audio")
            add(
                VocabWord(
                    id = item.getString("id"),
                    topicId = item.getString("topic"),
                    word = item.getString("word"),
                    phonetic = item.optString("phonetic"),
                    meaningZh = item.optString("meaningZh"),
                    example = item.optString("example"),
                    exampleZh = item.optString("exampleZh"),
                    audioPath = if (audio.isBlank()) "" else VocabAssetPrefix + audio,
                    imagePath = item.optString("image").takeIf { it.isNotBlank() }
                )
            )
        }
    }

private data class TopicMeta(
    val id: String,
    val title: String,
    val subtitle: String,
    val tint: Color
)
