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
import com.example.spacewords.HomeWindowId
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.data.WordProgress
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.findWord
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.EulerAngles
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
    val orbitWords = revision.mapNotNull { progress ->
        findWord(progress.wordId)?.let { OrbitWord(it, progress) }
    }
    val orbitItems = orbitWords.mapIndexed { index, orbitWord ->
        val angle = orbitAngle(index = index, count = orbitWords.size)
        val distance = orbitDistance(index = index, count = orbitWords.size)
        OrbitItem(
            orbitWord = orbitWord,
            angle = angle,
            distance = distance,
            position = orbitPosition(angle = angle, distance = distance, y = 1.42f)
        )
    }
    var selectedId by remember(version) { mutableStateOf<String?>(null) }
    val selectedItem = selectedId?.let { id -> orbitItems.firstOrNull { it.orbitWord.word.id == id } }
    val selectedWord = selectedItem?.orbitWord?.word
    val spatialNavigator = LocalSpatialNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val closeRevision = {
        coroutineScope.launch {
            spatialNavigator.restoreWindowContainer(HomeWindowId)
            spatialNavigator.closeStage()
        }
        Unit
    }

    Box(Modifier.fillMaxSize()) {
        key(version) {
            SpatialView(
                modifier = Modifier.fillMaxSize(),
                initial = { content, attachments ->
                    orbitItems.forEach { item ->
                        val word = item.orbitWord.word
                        attachments.entity(id = tagId(word.id))?.let { attachment ->
                            attachment.components[TransformComponent::class.java]?.setPosition(item.position)
                            attachment.components[TransformComponent::class.java]?.setEulerAngles(faceCenterEuler(item.angle))
                            content.addEntity(attachment)
                        }
                    }
                    attachments.entity(id = "close-revision")?.let { close ->
                        close.components[TransformComponent::class.java]?.setPosition(Vector3(0.78f, 2.05f, -0.9f))
                        content.addEntity(close)
                    }
                },
                attachments = {
                    orbitItems.forEach { item ->
                        AttachmentPanel(id = tagId(item.orbitWord.word.id)) {
                            OrbitWordTag(
                                word = item.orbitWord.word,
                                distance = item.distance,
                                onClick = { selectedId = item.orbitWord.word.id }
                            )
                        }
                    }
                    AttachmentPanel(id = "close-revision") {
                        StageActionButton(
                            onClick = closeRevision,
                            modifier = Modifier.size(width = 132.dp, height = 54.dp),
                            label = "Close"
                        )
                    }
                }
            )
        }
        key(version, selectedId) {
            SpatialView(
                modifier = Modifier.fillMaxSize(),
                initial = { content, attachments ->
                    attachments.entity(id = "selected-card")?.let { card ->
                        val item = selectedItem
                        if (item == null) {
                            card.components[TransformComponent::class.java]?.setPosition(Vector3(0f, 1.55f, -0.86f))
                            card.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, 0f))
                        } else {
                            card.components[TransformComponent::class.java]?.setPosition(
                                orbitPosition(angle = item.angle, distance = (item.distance - 0.12f).coerceAtLeast(0.58f), y = 1.5f)
                            )
                            card.components[TransformComponent::class.java]?.setEulerAngles(faceCenterEuler(item.angle))
                        }
                        content.addEntity(card)
                    }
                },
                attachments = {
                    AttachmentPanel(id = "selected-card") {
                        if (selectedWord == null) {
                            if (orbitWords.isEmpty()) {
                                EmptyRevisionCard(onClose = closeRevision)
                            } else {
                                HiddenPanel()
                            }
                        } else {
                            RevisionWordCard(
                                word = selectedWord,
                                onClose = closeRevision,
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
}

@Composable
private fun OrbitWordTag(word: VocabWord, distance: Float, onClick: () -> Unit) {
    val priority = ((1.35f - distance) / 0.7f).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 112.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE6F0FF).copy(alpha = 0.42f + priority * 0.34f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word.word,
                color = Color(0xFF263344),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RevisionWordCard(
    word: VocabWord,
    onClose: () -> Unit,
    onRemembered: () -> Unit,
    onKeep: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 560.dp, height = 430.dp)
            .clip(RoundedCornerShape(30.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = word.word,
                color = Color(0xFF17202C),
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            StageActionButton(
                onClick = onClose,
                modifier = Modifier.size(width = 88.dp, height = 48.dp),
                label = "Close"
            )
        }
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

private data class OrbitWord(
    val word: VocabWord,
    val progress: WordProgress
)

private data class OrbitItem(
    val orbitWord: OrbitWord,
    val angle: Float,
    val distance: Float,
    val position: Vector3
)

private fun orbitDistance(index: Int, count: Int): Float {
    if (count <= 1) return 0.72f
    val normalized = index.toFloat() / (count - 1).toFloat()
    return 0.72f + normalized * 0.58f
}

private fun orbitAngle(index: Int, count: Int): Float {
    if (count <= 1) return 0f
    val step = (2f * PI.toFloat()) / count.toFloat()
    return index * step
}

private fun orbitPosition(angle: Float, distance: Float, y: Float): Vector3 =
    Vector3(
        sin(angle) * distance,
        y,
        -cos(angle) * distance
    )

private fun faceCenterEuler(angle: Float): EulerAngles =
    EulerAngles(0f, normalizeDegrees(360f - angle * 180f / PI.toFloat()), 0f)

private fun normalizeDegrees(degrees: Float): Float =
    ((degrees % 360f) + 360f) % 360f

@Composable
private fun HiddenPanel() {
    Box(Modifier.size(1.dp))
}

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
