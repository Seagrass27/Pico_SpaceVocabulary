package com.example.spacewords.ui

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.pico.spatial.core.ecs.Component
import com.pico.spatial.core.ecs.EntityQueryCondition
import com.pico.spatial.core.ecs.SceneUpdateContext
import com.pico.spatial.core.ecs.System as SpatialSystem
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
    val revision = remember { SpaceWordsStore.revisionWords() }
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
    var selectedId by remember { mutableStateOf<String?>(null) }
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

    DisposableEffect(Unit) {
        RevisionOrbitMotion.reset()
        SpatialSystem.register(RevisionOrbitSystem::class.java)
        onDispose {
            SpatialSystem.unregister(RevisionOrbitSystem::class.java)
            RevisionOrbitMotion.reset()
        }
    }

    Box(Modifier.fillMaxSize()) {
        key(Unit) {
            SpatialView(
                modifier = Modifier.fillMaxSize(),
                initial = { content, attachments ->
                    orbitItems.forEach { item ->
                        val word = item.orbitWord.word
                        attachments.entity(id = tagId(word.id))?.let { attachment ->
                            attachment.components.set(
                                RevisionOrbitComponent(
                                    wordId = word.id,
                                    baseAngle = item.angle,
                                    distance = item.distance,
                                    y = 1.42f + stableYOffset(word.id)
                                )
                            )
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
                                onClick = {
                                    RevisionOrbitMotion.pause()
                                    selectedId = item.orbitWord.word.id
                                }
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
                            val angle = animatedOrbitAngle(item, RevisionOrbitMotion.currentOffsetDeg())
                            card.components[TransformComponent::class.java]?.setPosition(
                                orbitPosition(angle = angle, distance = (item.distance - 0.12f).coerceAtLeast(0.58f), y = 1.5f)
                            )
                            card.components[TransformComponent::class.java]?.setEulerAngles(faceCenterEuler(angle))
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
                                    RevisionOrbitMotion.hideWord(selectedWord.id)
                                    selectedId = null
                                    SpaceWordsStore.markRemembered(selectedWord.id)
                                    RevisionOrbitMotion.resume()
                                },
                                onKeep = {
                                    selectedId = null
                                    RevisionOrbitMotion.resume()
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
    val shape = RoundedCornerShape(24.dp)
    val accentColor = revisionUrgencyColor(priority)
    val borderColor = accentColor.copy(alpha = 0.36f + priority * 0.42f)
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 112.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding((7 - priority * 5).dp)
                .shadow(
                    elevation = (4 + priority * 18).dp,
                    shape = shape,
                    ambientColor = accentColor.copy(alpha = 0.20f + priority * 0.42f),
                    spotColor = accentColor.copy(alpha = 0.18f + priority * 0.38f)
                )
                .clip(shape)
                .background(accentColor.copy(alpha = 0.10f + priority * 0.18f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .shadow(
                    elevation = (2 + priority * 8).dp,
                    shape = shape,
                    ambientColor = Color.White.copy(alpha = 0.18f + priority * 0.32f),
                    spotColor = accentColor.copy(alpha = 0.16f + priority * 0.26f)
                )
                .clip(shape)
                .background(Color(0xFFEAF4FF).copy(alpha = 0.38f + priority * 0.34f))
                .border(width = (1 + priority).dp, color = borderColor, shape = shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word.word,
                color = Color(0xFF263344).copy(alpha = 0.76f + priority * 0.24f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun revisionUrgencyColor(priority: Float): Color =
    when {
        priority > 0.66f -> Color(0xFFFF8A7A)
        priority > 0.33f -> Color(0xFFFFD88A)
        else -> Color(0xFF8FD7FF)
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

private fun animatedOrbitAngle(item: OrbitItem, orbitOffsetDeg: Float): Float =
    item.angle + orbitOffsetDeg * PI.toFloat() / 180f

private fun normalizeDegrees(degrees: Float): Float =
    ((degrees % 360f) + 360f) % 360f

private const val OrbitAngularSpeedDegPerSecond = 6f

class RevisionOrbitComponent(
    var wordId: String = "",
    var baseAngle: Float = 0f,
    var distance: Float = 0f,
    var y: Float = 1.42f
) : Component()

class RevisionOrbitSystem : SpatialSystem() {
    override fun update(context: SceneUpdateContext) {
        val orbitOffsetDeg = RevisionOrbitMotion.currentOffsetDeg()
        val condition = EntityQueryCondition.hasComponent(RevisionOrbitComponent::class.java)
        context.scene.queryEntity(condition).forEach { entity ->
            val orbit = entity.components[RevisionOrbitComponent::class.java] ?: return@forEach
            val shouldShow = !RevisionOrbitMotion.isHidden(orbit.wordId)
            if (entity.enabled != shouldShow) {
                entity.enabled = shouldShow
            }
            if (!shouldShow) return@forEach
            val angle = orbit.baseAngle + orbitOffsetDeg * PI.toFloat() / 180f
            entity.components[TransformComponent::class.java]?.apply {
                setPosition(orbitPosition(angle = angle, distance = orbit.distance, y = orbit.y))
                setEulerAngles(faceCenterEuler(angle))
            }
        }
    }
}

private object RevisionOrbitMotion {
    private var baseOffsetDeg = 0f
    private var runningSinceMs = SystemClock.elapsedRealtime()
    private var paused = false
    private val hiddenWordIds = mutableSetOf<String>()

    fun reset() {
        baseOffsetDeg = 0f
        runningSinceMs = SystemClock.elapsedRealtime()
        paused = false
        hiddenWordIds.clear()
    }

    fun currentOffsetDeg(): Float {
        if (paused) return baseOffsetDeg
        val elapsedSeconds = (SystemClock.elapsedRealtime() - runningSinceMs) / 1000f
        return normalizeDegrees(baseOffsetDeg - OrbitAngularSpeedDegPerSecond * elapsedSeconds)
    }

    fun pause() {
        if (!paused) {
            baseOffsetDeg = currentOffsetDeg()
            paused = true
        }
    }

    fun resume() {
        if (paused) {
            runningSinceMs = SystemClock.elapsedRealtime()
            paused = false
        }
    }

    fun hideWord(wordId: String) {
        hiddenWordIds.add(wordId)
    }

    fun isHidden(wordId: String): Boolean = hiddenWordIds.contains(wordId)
}

private fun stableYOffset(wordId: String): Float {
    val normalized = (wordId.hashCode() and 0xFFFF) / 65535f
    return (normalized - 0.5f) * 0.08f
}

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
