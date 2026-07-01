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
private const val SampleImageAssetDir = "vocab_bundle/sample_images"
private const val ThemeImageAssetDir = "vocab_bundle/theme_pics"

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
        val sampleImages = context.assets.list(SampleImageAssetDir).orEmpty().toSet()
        val themeImages = TopicMetas.associate { meta ->
            meta.id to context.assets.list("$ThemeImageAssetDir/${meta.id}").orEmpty().toSet()
        }
        val words = parseWords(JSONArray(json), sampleImages, themeImages)
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

private fun parseWords(
    array: JSONArray,
    sampleImages: Set<String>,
    themeImages: Map<String, Set<String>>
): List<VocabWord> =
    buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val audio = item.optString("audio")
            val word = item.getString("word")
            val topicId = item.getString("topic")
            val image = item.optString("image")
                .takeIf { it.isNotBlank() }
                ?.let(::assetPath)
                ?: themeImagePath(
                    topicId = topicId,
                    wordId = item.getString("id"),
                    word = word,
                    themeImages = themeImages[topicId].orEmpty()
                )
                ?: sampleImagePath(word, sampleImages)
            add(
                VocabWord(
                    id = item.getString("id"),
                    topicId = topicId,
                    word = word,
                    phonetic = item.optString("phonetic"),
                    meaningZh = item.optString("meaningZh"),
                    example = item.optString("example"),
                    exampleZh = item.optString("exampleZh"),
                    audioPath = if (audio.isBlank()) "" else assetPath(audio),
                    imagePath = image
                )
            )
        }
    }

private fun themeImagePath(
    topicId: String,
    wordId: String,
    word: String,
    themeImages: Set<String>
): String? {
    val idFileName = wordId.removePrefix("${topicId}_") + ".png"
    val candidates = listOf(
        word.lowercase() + ".png",
        word.lowercase().replace(" ", "_") + ".png",
        word.lowercase().replace(" ", "") + ".png",
        idFileName
    ).distinct()
    val fileName = candidates.firstOrNull { it in themeImages } ?: return null
    return "$ThemeImageAssetDir/$topicId/$fileName"
}

private fun sampleImagePath(word: String, sampleImages: Set<String>): String? {
    val fileName = word.lowercase().replace(" ", "_") + ".png"
    return if (fileName in sampleImages) "$SampleImageAssetDir/$fileName" else null
}

private fun assetPath(path: String): String =
    if (path.startsWith(VocabAssetPrefix)) path else VocabAssetPrefix + path

private data class TopicMeta(
    val id: String,
    val title: String,
    val subtitle: String,
    val tint: Color
)
