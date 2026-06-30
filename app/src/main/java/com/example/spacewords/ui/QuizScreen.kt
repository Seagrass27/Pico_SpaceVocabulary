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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.model.AllWords
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.ensureVocabularyLoaded
import com.example.spacewords.model.findWord
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import kotlin.random.Random

@Composable
fun QuizScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ensureVocabularyLoaded(context)
    SpaceWordsStore.ensureLoaded(context)
    val version by SpaceWordsStore.version
    val quizWords = remember(version) {
        SpaceWordsStore.rememberedWords().mapNotNull { findWord(it.wordId) }
    }
    val masteredCount = remember(version) {
        SpaceWordsStore.masteredWords().count { findWord(it.wordId) != null }
    }
    var lastResult by remember { mutableStateOf<String?>(null) }
    val currentWord = quizWords.firstOrNull()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(30.dp)
    ) {
        if (currentWord == null) {
            EmptyQuizPanel(masteredCount = masteredCount)
        } else {
            val questionType = remember(currentWord.id) {
                if (Random(currentWord.id.hashCode()).nextBoolean()) QuestionType.WordToMeaning else QuestionType.MeaningToWord
            }
            val options = remember(currentWord.id, questionType) {
                buildOptions(currentWord, questionType)
            }
            QuizPanel(
                word = currentWord,
                questionType = questionType,
                options = options,
                remaining = quizWords.size,
                masteredCount = masteredCount,
                lastResult = lastResult,
                onAnswer = { option ->
                    if (option.isCorrect) {
                        SpaceWordsStore.markMastered(currentWord.id)
                        lastResult = "Correct. Added to mastered words."
                    } else {
                        SpaceWordsStore.putInRevision(currentWord.id)
                        lastResult = "Incorrect. Sent back to revision."
                    }
                }
            )
        }
        version.hashCode()
    }
}

@Composable
private fun QuizPanel(
    word: VocabWord,
    questionType: QuestionType,
    options: List<AnswerOption>,
    remaining: Int,
    masteredCount: Int,
    lastResult: String?,
    onAnswer: (AnswerOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        QuizHeader(remaining = remaining, masteredCount = masteredCount)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.30f))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (questionType) {
                    QuestionType.WordToMeaning -> "Choose the Chinese meaning"
                    QuestionType.MeaningToWord -> "Choose the English word"
                },
                color = Color(0xFF657386),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = when (questionType) {
                    QuestionType.WordToMeaning -> word.word
                    QuestionType.MeaningToWord -> word.meaningZh
                },
                color = Color(0xFF17202C),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (questionType == QuestionType.WordToMeaning && word.phonetic.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = word.phonetic, color = Color(0xFF6C7B8E), fontSize = 20.sp)
            }
            Spacer(Modifier.height(34.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                options.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { option ->
                            AnswerButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(88.dp),
                                option = option,
                                onClick = { onAnswer(option) }
                            )
                        }
                    }
                }
            }
            lastResult?.let {
                Spacer(Modifier.height(22.dp))
                Text(
                    text = it,
                    color = Color(0xFF506174),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuizHeader(remaining: Int, masteredCount: Int) {
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
                text = "Quiz",
                color = Color(0xFF1F2733),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Answer correctly to move words into mastery.",
                color = Color(0xFF6D7684),
                fontSize = 15.sp
            )
        }
        Text(
            text = "$remaining ready  ·  $masteredCount mastered",
            color = Color(0xFF516175),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AnswerButton(modifier: Modifier, option: AnswerOption, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.38f))
            .clickable(onClick = onClick)
            .spatialHoverEffect()
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.label,
            color = Color(0xFF263344),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyQuizPanel(masteredCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No quiz words",
            color = Color(0xFF1F2733),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "$masteredCount words are mastered. Mark more words as remembered to test them here.",
            color = Color(0xFF6F7B89),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun buildOptions(word: VocabWord, questionType: QuestionType): List<AnswerOption> {
    val wrongOptions = AllWords
        .filter { it.id != word.id }
        .sortedWith(
            compareByDescending<VocabWord> { it.topicId == word.topicId }
                .thenBy { it.word }
        )
        .map { candidate ->
            when (questionType) {
                QuestionType.WordToMeaning -> AnswerOption(candidate.meaningZh, isCorrect = false)
                QuestionType.MeaningToWord -> AnswerOption(candidate.word, isCorrect = false)
            }
        }
        .distinctBy { it.label }
        .take(12)
        .shuffled(Random(word.id.hashCode() + questionType.ordinal))
        .take(3)

    val correct = when (questionType) {
        QuestionType.WordToMeaning -> AnswerOption(word.meaningZh, isCorrect = true)
        QuestionType.MeaningToWord -> AnswerOption(word.word, isCorrect = true)
    }
    return (wrongOptions + correct)
        .shuffled(Random(word.id.hashCode() * 31 + questionType.ordinal))
}

private enum class QuestionType {
    WordToMeaning,
    MeaningToWord
}

private data class AnswerOption(
    val label: String,
    val isCorrect: Boolean
)
