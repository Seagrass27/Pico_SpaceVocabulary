package com.example.spacewords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.model.Difficulty
import com.example.spacewords.model.Topic
import com.example.spacewords.model.Topics
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.ensureVocabularyLoaded
import com.example.spacewords.model.findWord
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

@Composable
fun MasteredScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ensureVocabularyLoaded(context)
    SpaceWordsStore.ensureLoaded(context)
    val version by SpaceWordsStore.version
    val masteredWords = SpaceWordsStore.masteredWords().mapNotNull { findWord(it.wordId) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var resetTopic by remember { mutableStateOf<Topic?>(null) }
    val selectedTopic = selectedTopicId?.let { id -> Topics.firstOrNull { it.id == id } }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(30.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (selectedTopic == null) {
                MasteredHeader(total = masteredWords.size)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Topics.forEach { topic ->
                        val words = masteredWords.filter { it.topicId == topic.id }
                        MasteredTopicRow(
                            topic = topic,
                            words = words,
                            onOpen = { selectedTopicId = topic.id },
                            onReset = { resetTopic = topic }
                        )
                    }
                }
            } else {
                val words = masteredWords.filter { it.topicId == selectedTopic.id }
                MasteredTopicDetail(
                    topic = selectedTopic,
                    words = words,
                    onBack = { selectedTopicId = null }
                )
            }
        }
        resetTopic?.let { topic ->
            val words = masteredWords.filter { it.topicId == topic.id }
            ResetConfirmDialog(
                topic = topic,
                wordCount = words.size,
                onCancel = { resetTopic = null },
                onConfirm = {
                    SpaceWordsStore.resetToNew(words.map { it.id })
                    resetTopic = null
                    if (selectedTopicId == topic.id) {
                        selectedTopicId = null
                    }
                }
            )
        }
    }
    version.hashCode()
}

@Composable
private fun MasteredHeader(total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.34f))
            .padding(horizontal = 26.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Mastered Words",
                color = Color(0xFF1F2733),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Words answered correctly in quiz are collected here.",
                color = Color(0xFF6D7684),
                fontSize = 15.sp
            )
        }
        Text(
            text = "$total mastered",
            color = Color(0xFF516175),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MasteredTopicRow(
    topic: Topic,
    words: List<VocabWord>,
    onOpen: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.27f))
            .clickable(onClick = onOpen)
            .spatialHoverEffect()
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(topic.tint.copy(alpha = 0.78f))
        )
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = topic.title,
                    color = Color(0xFF243044),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${words.size} words",
                    color = Color(0xFF667487),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = if (words.isEmpty()) {
                    "No mastered words in this topic yet."
                } else {
                    difficultySummary(words)
                },
                color = Color(0xFF5B6878),
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(18.dp))
        ResetTopicButton(
            enabled = words.isNotEmpty(),
            onClick = onReset
        )
    }
}

@Composable
private fun MasteredTopicDetail(topic: Topic, words: List<VocabWord>, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.34f))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBack)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${topic.title} Mastered",
                    color = Color(0xFF1F2733),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = difficultySummary(words),
                    color = Color(0xFF6D7684),
                    fontSize = 15.sp
                )
            }
            Text(
                text = "${words.size} words",
                color = Color(0xFF516175),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                DifficultyWordSection(
                    difficulty = difficulty,
                    words = words.filter { it.difficulty == difficulty.level }
                )
            }
        }
    }
}

@Composable
private fun DifficultyWordSection(difficulty: Difficulty, words: List<VocabWord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.27f))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = difficulty.label,
                color = Color(0xFF243044),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${words.size} words",
                color = Color(0xFF667487),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (words.isEmpty()) {
            Text(
                text = "No mastered words at this difficulty.",
                color = Color(0xFF7A8797),
                fontSize = 15.sp
            )
        } else {
            words.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { word ->
                        WordPill(
                            modifier = Modifier.weight(1f),
                            word = word
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WordPill(modifier: Modifier, word: VocabWord) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.36f))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word.word,
            color = Color(0xFF303C4D),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.36f))
            .clickable(onClick = onClick)
            .spatialHoverEffect(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Back",
            color = Color(0xFF263344),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResetConfirmDialog(
    topic: Topic,
    wordCount: Int,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820).copy(alpha = 0.20f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(560.dp)
                .clip(RoundedCornerShape(30.dp))
                .backgroundMaterial(true, Material.Regular)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset ${topic.title}?",
                color = Color(0xFF1F2733),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Move $wordCount mastered words in this topic back to unlearned status.",
                color = Color(0xFF657386),
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DialogButton(
                    label = "Cancel",
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                DialogButton(
                    label = "Reset",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    destructive = true
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (destructive) Color(0xFFDFA6A0).copy(alpha = 0.34f)
                else Color.White.copy(alpha = 0.38f)
            )
            .clickable(onClick = onClick)
            .spatialHoverEffect(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (destructive) Color(0xFF7E3A34) else Color(0xFF263344),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResetTopicButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) Color.White.copy(alpha = 0.40f)
                else Color.White.copy(alpha = 0.18f)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick).spatialHoverEffect() else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Reset Topic",
            color = if (enabled) Color(0xFF263344) else Color(0xFF8A94A2),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun difficultySummary(words: List<VocabWord>): String =
    Difficulty.entries.joinToString("  ·  ") { difficulty ->
        "${difficulty.label} ${words.count { it.difficulty == difficulty.level }}"
    }
