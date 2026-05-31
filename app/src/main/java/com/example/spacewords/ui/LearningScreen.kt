package com.example.spacewords.ui

import android.os.Bundle
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.example.spacewords.model.Topic
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.findTopic
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.example.spacewords.TopicBundleKey

@Composable
fun LearningScreen(bundle: Bundle?, modifier: Modifier = Modifier) {
    SpaceWordsStore.ensureLoaded(LocalContext.current)
    val topic = remember(bundle) { findTopic(bundle?.getString(TopicBundleKey)) }
    var index by remember(topic.id) { mutableIntStateOf(0) }
    val spatialNavigator = LocalSpatialNavigator.current
    val currentWord = topic.words.getOrNull(index)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(30.dp)
    ) {
        if (currentWord == null) {
            CompletionPanel(topic = topic, onClose = { spatialNavigator.closeWindowContainer() })
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
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
                            index += 1
                        },
                        label = "Add to Revision"
                    )
                    ActionButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        onClick = {
                            SpaceWordsStore.markRemembered(currentWord.id)
                            index += 1
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.32f))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(topic.tint.copy(alpha = 0.78f), Color.White.copy(alpha = 0.42f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Image placeholder", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = word.word,
            color = Color(0xFF17202C),
            fontSize = 54.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(text = word.phonetic, color = Color(0xFF617086), fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        ActionButton(onClick = {}, modifier = Modifier.size(width = 172.dp, height = 48.dp), label = "Pronunciation")
        Spacer(Modifier.height(22.dp))
        Text(text = word.meaningZh, color = Color(0xFF2D3948), fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(18.dp))
        Text(
            text = word.example,
            color = Color(0xFF4D5C6F),
            fontSize = 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompletionPanel(topic: Topic, onClose: () -> Unit) {
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
            text = "Your choices were saved locally.",
            color = Color(0xFF6F7B89),
            fontSize = 18.sp
        )
        Spacer(Modifier.height(28.dp))
        ActionButton(onClick = onClose, modifier = Modifier.size(width = 180.dp, height = 62.dp), label = "Close")
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit, modifier: Modifier = Modifier, label: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.36f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color(0xFF263344), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
