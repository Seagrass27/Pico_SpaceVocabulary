package com.example.spacewords.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.findWord
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RevisionStage() {
    SpaceWordsStore.ensureLoaded(LocalContext.current)
    val version by SpaceWordsStore.version
    val revision = SpaceWordsStore.revisionWords()
    val words = revision.mapNotNull { findWord(it.wordId) }
    var selectedId by remember(version) { mutableStateOf(words.firstOrNull()?.id) }
    val selectedWord = selectedId?.let { findWord(it) }
    val spatialNavigator = LocalSpatialNavigator.current
    val coroutineScope = rememberCoroutineScope()

    key(version) {
        SpatialView(
            initial = { content, attachments ->
                words.forEachIndexed { index, word ->
                    val attachment = attachments.entity(id = tagId(word.id)) ?: return@forEachIndexed
                    val distance = 0.9f + index * 0.08f
                    val angle = (-130f + (260f / words.size.coerceAtLeast(1)) * index) * PI.toFloat() / 180f
                    attachment.components[TransformComponent::class.java]?.setPosition(
                        Vector3(
                            sin(angle) * distance,
                            1.35f,
                            -cos(angle) * distance
                        )
                    )
                    content.addEntity(attachment)
                }
                attachments.entity(id = "selected-card")?.let { card ->
                    card.components[TransformComponent::class.java]?.setPosition(Vector3(0f, 1.55f, -1.25f))
                    content.addEntity(card)
                }
            },
            attachments = {
                words.forEachIndexed { index, word ->
                    AttachmentPanel(id = tagId(word.id)) {
                        OrbitWordTag(
                            word = word,
                            isOlder = index < 3,
                            onClick = { selectedId = word.id }
                        )
                    }
                }
                AttachmentPanel(id = "selected-card") {
                    if (selectedWord == null) {
                        EmptyRevisionCard(onClose = { coroutineScope.launch { spatialNavigator.closeStage() } })
                    } else {
                        RevisionWordCard(
                            word = selectedWord,
                            onRemembered = {
                                SpaceWordsStore.markRemembered(selectedWord.id)
                                selectedId = SpaceWordsStore.revisionWords().firstOrNull()?.wordId
                            },
                            onKeep = {
                                SpaceWordsStore.putInRevision(selectedWord.id)
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun OrbitWordTag(word: VocabWord, isOlder: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 190.dp, height = 76.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isOlder) Color(0xFFE6F0FF).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.46f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word.word,
                color = Color(0xFF263344),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RevisionWordCard(word: VocabWord, onRemembered: () -> Unit, onKeep: () -> Unit) {
    Column(
        modifier = Modifier
            .size(width = 560.dp, height = 430.dp)
            .clip(RoundedCornerShape(30.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = word.word,
            color = Color(0xFF17202C),
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(text = word.phonetic, color = Color(0xFF657386), fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        Text(text = word.meaningZh, color = Color(0xFF2C3745), fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = word.example,
            color = Color(0xFF506174),
            fontSize = 19.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StageActionButton(onClick = onKeep, modifier = Modifier.size(width = 210.dp, height = 62.dp), label = "Keep Reviewing")
            StageActionButton(onClick = onRemembered, modifier = Modifier.size(width = 180.dp, height = 62.dp), label = "Remembered")
        }
    }
}

@Composable
private fun EmptyRevisionCard(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .size(width = 520.dp, height = 260.dp)
            .clip(RoundedCornerShape(30.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No words in revision", color = Color(0xFF263344), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        StageActionButton(onClick = onClose, modifier = Modifier.size(width = 160.dp, height = 58.dp), label = "Close")
    }
}

private fun tagId(wordId: String) = "word-$wordId"

@Composable
private fun StageActionButton(onClick: () -> Unit, modifier: Modifier, label: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.36f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color(0xFF263344), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
