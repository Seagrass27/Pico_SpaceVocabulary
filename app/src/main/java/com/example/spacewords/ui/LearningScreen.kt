package com.example.spacewords.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.HomeWindowId
import com.example.spacewords.model.Topic
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.ensureVocabularyLoaded
import com.example.spacewords.model.findTopic
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.example.spacewords.TopicBundleKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun LearningScreen(bundle: Bundle?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ensureVocabularyLoaded(context)
    SpaceWordsStore.ensureLoaded(context)
    val topic = remember(bundle) { findTopic(bundle?.getString(TopicBundleKey)) }
    var index by remember(topic.id) {
        mutableIntStateOf(SpaceWordsStore.learningIndexFor(topic.id).coerceIn(0, topic.words.size))
    }
    val spatialNavigator = LocalSpatialNavigator.current
    val currentWord = topic.words.getOrNull(index)
    val latestIndex by rememberUpdatedState(index)

    DisposableEffect(topic.id) {
        onDispose {
            SpaceWordsStore.saveLearningIndex(topic.id, latestIndex)
            CoroutineScope(Dispatchers.Main).launch {
                spatialNavigator.restoreWindowContainer(HomeWindowId)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(30.dp)
    ) {
        if (currentWord == null) {
            CompletionPanel(topic = topic)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LearningHeader(topic = topic, index = index)
                WordCard(word = currentWord, topic = topic, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    ActionButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        onClick = {
                            SpaceWordsStore.putInRevision(currentWord.id)
                            val nextIndex = (index + 1).coerceAtMost(topic.words.size)
                            index = nextIndex
                            SpaceWordsStore.saveLearningIndex(topic.id, nextIndex)
                        },
                        label = "Add to Revision"
                    )
                    ActionButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        onClick = {
                            SpaceWordsStore.markRemembered(currentWord.id)
                            val nextIndex = (index + 1).coerceAtMost(topic.words.size)
                            index = nextIndex
                            SpaceWordsStore.saveLearningIndex(topic.id, nextIndex)
                        },
                        label = "Remembered"
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningHeader(topic: Topic, index: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    color = Color(0xFF26303D),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = topic.subtitle, color = Color(0xFF778392), fontSize = 14.sp)
            }
            Text(
                text = "${index + 1} / ${topic.words.size}",
                color = Color(0xFF526173),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.35f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth((index + 1).toFloat() / topic.words.size)
                    .height(8.dp)
                    .background(topic.tint)
            )
        }
    }
}

@Composable
private fun WordCard(word: VocabWord, topic: Topic, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val playerHolder = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            playerHolder.value?.release()
            playerHolder.value = null
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.32f))
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(34.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefinitionImage(
                word = word,
                topic = topic,
                modifier = Modifier
                    .weight(1.18f)
                    .aspectRatio(1f)
            )
            Box(
                modifier = Modifier
                    .weight(0.92f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = word.word,
                        color = Color(0xFF17202C),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = word.phonetic, color = Color(0xFF617086), fontSize = 21.sp)
                    Spacer(Modifier.height(14.dp))
                    ActionButton(
                        onClick = { playPronunciation(context, word, playerHolder.value) { playerHolder.value = it } },
                        modifier = Modifier.size(width = 176.dp, height = 50.dp),
                        label = "Pronunciation"
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = word.meaningZh,
                        color = Color(0xFF2D3948),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = word.example,
                        color = Color(0xFF4D5C6F),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start
                    )
                    if (word.exampleZh.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = word.exampleZh,
                            color = Color(0xFF667487),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefinitionImage(word: VocabWord, topic: Topic, modifier: Modifier = Modifier) {
    AssetImageOrPlaceholder(
        assetPath = word.imagePath,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(topic.tint.copy(alpha = 0.62f), Color.White.copy(alpha = 0.42f))
                )
            ),
        contentDescription = word.word
    ) {
        Text(
            text = "Definition image placeholder",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun playPronunciation(
    context: Context,
    word: VocabWord,
    currentPlayer: MediaPlayer?,
    updatePlayer: (MediaPlayer?) -> Unit
) {
    if (word.audioPath.isBlank()) return
    runCatching {
        currentPlayer?.release()
        val descriptor = context.assets.openFd(word.audioPath)
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

@Composable
private fun CompletionPanel(topic: Topic) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${topic.title} completed",
            color = Color(0xFF1F2733),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Your choices were saved locally. Use the window close control to return home.",
            color = Color(0xFF6F7B89),
            fontSize = 18.sp
        )
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit, modifier: Modifier = Modifier, label: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.36f))
            .clickable(onClick = onClick)
            .spatialHoverEffect(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color(0xFF263344), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
