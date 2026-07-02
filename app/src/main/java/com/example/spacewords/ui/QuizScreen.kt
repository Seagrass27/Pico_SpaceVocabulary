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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
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
                onCorrect = { SpaceWordsStore.markMastered(currentWord.id) },
                onIncorrect = { SpaceWordsStore.putInRevision(currentWord.id) }
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
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit
) {
    var selectedOption by remember(word.id) { mutableStateOf<AnswerOption?>(null) }
    var showIncorrectDialog by remember(word.id) { mutableStateOf(false) }
    val answered = selectedOption != null

    LaunchedEffect(word.id, selectedOption) {
        val selected = selectedOption
        if (selected?.isCorrect == true) {
            delay(520)
            onCorrect()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        QuizHeader(remaining = remaining, masteredCount = masteredCount)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.30f))
                .padding(30.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                                    state = answerFeedbackState(option = option, selectedOption = selectedOption),
                                    enabled = !answered,
                                    onClick = {
                                        selectedOption = option
                                        if (!option.isCorrect) {
                                            showIncorrectDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (showIncorrectDialog) {
                    Spacer(Modifier.height(18.dp))
                    IncorrectAnswerNotice(
                        word = word,
                        onContinue = {
                            showIncorrectDialog = false
                            onIncorrect()
                        }
                    )
                }
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
private fun AnswerButton(
    modifier: Modifier,
    option: AnswerOption,
    state: AnswerFeedbackState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (state) {
        AnswerFeedbackState.Neutral -> Color.White.copy(alpha = 0.38f)
        AnswerFeedbackState.NeutralAnswered -> Color.White.copy(alpha = 0.46f)
        AnswerFeedbackState.Correct -> Color(0xFF8ED6A5).copy(alpha = 0.62f)
        AnswerFeedbackState.Incorrect -> Color(0xFFEFA29A).copy(alpha = 0.68f)
    }
    val textColor = when (state) {
        AnswerFeedbackState.Neutral,
        AnswerFeedbackState.NeutralAnswered -> Color(0xFF263344)
        AnswerFeedbackState.Correct -> Color(0xFF1E5A36)
        AnswerFeedbackState.Incorrect -> Color(0xFF7B2E29)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick).spatialHoverEffect() else Modifier)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.label,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IncorrectAnswerNotice(word: VocabWord, onContinue: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFE1DD).copy(alpha = 0.58f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Needs Review",
                color = Color(0xFF7B2E29),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "\"${word.word}\" will be added back to your revision orbit.",
                color = Color(0xFF7D5A56),
                fontSize = 15.sp
            )
        }
        Box(
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.48f))
                .clickable(onClick = onContinue)
                .spatialHoverEffect()
                .padding(horizontal = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue",
                color = Color(0xFF263344),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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

private enum class AnswerFeedbackState {
    Neutral,
    NeutralAnswered,
    Correct,
    Incorrect
}

private fun answerFeedbackState(option: AnswerOption, selectedOption: AnswerOption?): AnswerFeedbackState =
    when {
        selectedOption == null -> AnswerFeedbackState.Neutral
        option.isCorrect -> AnswerFeedbackState.Correct
        option == selectedOption -> AnswerFeedbackState.Incorrect
        else -> AnswerFeedbackState.NeutralAnswered
    }
