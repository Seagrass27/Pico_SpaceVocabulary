package com.example.spacewords.ui

import android.os.SystemClock
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.HomeWindowId
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.data.WordProgress
import com.example.spacewords.model.VocabWord
import com.example.spacewords.model.ensureVocabularyLoaded
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
import com.pico.spatial.ui.foundation.gesture.detectSpatialDragGesture
import com.pico.spatial.ui.foundation.gesture.detectSpatialPointerEvent
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RevisionStage() {
    val context = LocalContext.current
    ensureVocabularyLoaded(context)
    SpaceWordsStore.ensureLoaded(context)
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

    Box(
        Modifier
            .fillMaxSize()
    ) {
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
    val context = LocalContext.current
    val priority = ((1.35f - distance) / 0.7f).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(24.dp)
    val accentColor = revisionUrgencyColor(priority)
    val borderColor = accentColor.copy(alpha = 0.36f + priority * 0.42f)
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 112.dp)
            .pointerInput(word.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    RevisionOrbitMotion.startDrag()
                    waitForUpOrCancellation()
                    RevisionOrbitMotion.finishPress()
                }
            }
            .pointerInput(word.id) {
                detectDragGestures(
                    onDragStart = { RevisionOrbitMotion.startDrag() },
                    onDragEnd = { RevisionOrbitMotion.finishDrag() },
                    onDragCancel = { RevisionOrbitMotion.finishDrag() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        RevisionOrbitMotion.dragBy(dragAmount.x)
                    }
                )
            }
            .pointerInput(word.id, context) {
                var pointerPressed = false
                detectSpatialPointerEvent(context = context) { eventList ->
                    val isPressed = eventList.any { it.pressed }
                    if (isPressed && !pointerPressed) {
                        RevisionOrbitMotion.startDrag()
                    } else if (!isPressed && pointerPressed) {
                        RevisionOrbitMotion.requestPressRelease()
                    }
                    pointerPressed = isPressed
                    false
                }
            }
            .pointerInput(word.id, context) {
                detectSpatialDragGesture(
                    context = context,
                    onDragStart = { RevisionOrbitMotion.startDrag() },
                    onDragEnd = { RevisionOrbitMotion.finishDrag() },
                    onDragCancel = { RevisionOrbitMotion.finishDrag() }
                ) { dragValue ->
                    RevisionOrbitMotion.dragBy(dragValue.dragAmount.x)
                }
            }
            .clickable {
                if (RevisionOrbitMotion.shouldOpenCardOnClick()) {
                    onClick()
                }
            }
            .spatialHoverEffect(),
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
    onRemembered: () -> Unit,
    onKeep: () -> Unit
) {
    val context = LocalContext.current
    val playerHolder = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(word.id) {
        onDispose {
            playerHolder.value?.release()
            playerHolder.value = null
        }
    }

    Column(
        modifier = Modifier
            .size(width = 700.dp, height = 470.dp)
            .clip(RoundedCornerShape(30.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetImageOrPlaceholder(
                assetPath = word.imagePath,
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.28f)),
                contentDescription = word.word,
                contentScale = ContentScale.Fit
            ) {
                Text(
                    text = "Definition image placeholder",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6B7888),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
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
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(text = word.phonetic, color = Color(0xFF657386), fontSize = 19.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = word.meaningZh,
                        color = Color(0xFF2C3745),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = word.example,
                            color = Color(0xFF506174),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                        AudioIconButton(
                            onClick = {
                                playAssetAudio(context, word.exampleAudioPath, playerHolder.value) {
                                    playerHolder.value = it
                                }
                            },
                            modifier = Modifier
                        )
                    }
                    if (word.exampleZh.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = word.exampleZh,
                            color = Color(0xFF657386),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
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

private fun shortestDegreeDelta(from: Float, to: Float): Float =
    ((to - from + 540f) % 360f) - 180f

private const val OrbitAngularSpeedDegPerSecond = 6f
private const val DragActivationPx = 5f
private const val PreDragNoiseDeadZonePx = 0.35f
private const val ActiveDragDeadZonePx = 0.18f
private const val DragDegreesPerPixel = 0.055f
private const val DragVelocityDegreesPerSecondPerPixel = 1.25f
private const val MaxUserOrbitVelocityDegPerSecond = 66f
private const val UserOrbitVelocityDecayPerSecond = 0.38f
private const val DragVelocityIdleTimeoutMs = 140L
private const val TapMaxDurationMs = 320L
private const val FlingMinVelocityDegPerSecond = 18f
private const val DragDeltaSmoothing = 0.35f
private const val SpatialPressReleaseDebounceMs = 90L
private const val DragFollowRatePerSecond = 18f

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
    private var lastMotionMs = runningSinceMs
    private var userVelocityDegPerSecond = 0f
    private var userDragging = false
    private var dragActivated = false
    private var pendingDragPx = 0f
    private var smoothedDragDeltaPx = 0f
    private var dragTargetOffsetDeg = 0f
    private var lastDragInputMs = runningSinceMs
    private var holdStartMs = runningSinceMs
    private var pendingPressReleaseMs: Long? = null
    private var tapAllowed = false
    private var paused = false
    private val hiddenWordIds = mutableSetOf<String>()

    fun reset() {
        baseOffsetDeg = 0f
        runningSinceMs = SystemClock.elapsedRealtime()
        lastMotionMs = runningSinceMs
        userVelocityDegPerSecond = 0f
        userDragging = false
        dragActivated = false
        pendingDragPx = 0f
        smoothedDragDeltaPx = 0f
        dragTargetOffsetDeg = 0f
        lastDragInputMs = runningSinceMs
        holdStartMs = runningSinceMs
        pendingPressReleaseMs = null
        tapAllowed = false
        paused = false
        hiddenWordIds.clear()
    }

    fun currentOffsetDeg(): Float {
        updateMotion()
        return baseOffsetDeg
    }

    fun startDrag() {
        if (paused) return
        if (userDragging) return
        baseOffsetDeg = currentOffsetDeg()
        val now = SystemClock.elapsedRealtime()
        userVelocityDegPerSecond = 0f
        userDragging = true
        dragActivated = false
        pendingDragPx = 0f
        smoothedDragDeltaPx = 0f
        dragTargetOffsetDeg = baseOffsetDeg
        holdStartMs = now
        lastDragInputMs = now
        lastMotionMs = now
        pendingPressReleaseMs = null
        tapAllowed = false
    }

    fun dragBy(deltaX: Float) {
        if (paused) return
        if (!userDragging) startDrag()
        val now = SystemClock.elapsedRealtime()
        pendingPressReleaseMs = null
        var effectiveDelta = deltaX
        if (!dragActivated) {
            if (kotlin.math.abs(deltaX) < PreDragNoiseDeadZonePx) {
                if (now - lastDragInputMs > DragVelocityIdleTimeoutMs) {
                    pendingDragPx = 0f
                    userVelocityDegPerSecond = 0f
                }
                return
            }
            pendingDragPx += deltaX
            val pendingAbs = kotlin.math.abs(pendingDragPx)
            if (pendingAbs < DragActivationPx) {
                userVelocityDegPerSecond = 0f
                return
            }
            effectiveDelta = kotlin.math.sign(pendingDragPx) * (pendingAbs - DragActivationPx)
            pendingDragPx = 0f
            dragActivated = true
        }
        if (kotlin.math.abs(effectiveDelta) < ActiveDragDeadZonePx) {
            if (now - lastDragInputMs > DragVelocityIdleTimeoutMs) {
                userVelocityDegPerSecond = 0f
            }
            return
        }
        updateMotion()
        lastDragInputMs = now
        smoothedDragDeltaPx = if (smoothedDragDeltaPx == 0f) {
            effectiveDelta
        } else {
            smoothedDragDeltaPx * DragDeltaSmoothing + effectiveDelta * (1f - DragDeltaSmoothing)
        }
        effectiveDelta = smoothedDragDeltaPx
        val clampedDelta = effectiveDelta.coerceIn(-58f, 58f)
        dragTargetOffsetDeg = normalizeDegrees(dragTargetOffsetDeg + clampedDelta * DragDegreesPerPixel)
        val targetVelocity = (clampedDelta * DragVelocityDegreesPerSecondPerPixel)
            .coerceIn(-MaxUserOrbitVelocityDegPerSecond, MaxUserOrbitVelocityDegPerSecond)
        userVelocityDegPerSecond = userVelocityDegPerSecond * 0.55f + targetVelocity * 0.45f
    }

    fun finishPress() {
        if (!userDragging || dragActivated) return
        finishDrag()
    }

    fun requestPressRelease() {
        if (!userDragging) return
        pendingPressReleaseMs = SystemClock.elapsedRealtime()
    }

    fun finishDrag() {
        if (!userDragging) return
        val now = SystemClock.elapsedRealtime()
        val wasDragActivated = dragActivated
        if (!wasDragActivated || now - lastDragInputMs > DragVelocityIdleTimeoutMs ||
            kotlin.math.abs(userVelocityDegPerSecond) < FlingMinVelocityDegPerSecond
        ) {
            userVelocityDegPerSecond = 0f
        }
        tapAllowed = !wasDragActivated && now - holdStartMs <= TapMaxDurationMs
        userDragging = false
        dragActivated = false
        pendingDragPx = 0f
        smoothedDragDeltaPx = 0f
        dragTargetOffsetDeg = baseOffsetDeg
        pendingPressReleaseMs = null
        lastMotionMs = now
    }

    fun shouldOpenCardOnClick(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val allowed = tapAllowed ||
            (userDragging && !dragActivated && now - holdStartMs <= TapMaxDurationMs)
        tapAllowed = false
        if (allowed && userDragging && !dragActivated) {
            userDragging = false
            pendingDragPx = 0f
            smoothedDragDeltaPx = 0f
            dragTargetOffsetDeg = baseOffsetDeg
            pendingPressReleaseMs = null
            lastMotionMs = now
        }
        return allowed
    }

    fun pause() {
        if (!paused) {
            baseOffsetDeg = currentOffsetDeg()
            userVelocityDegPerSecond = 0f
            userDragging = false
            dragActivated = false
            pendingDragPx = 0f
            smoothedDragDeltaPx = 0f
            dragTargetOffsetDeg = baseOffsetDeg
            pendingPressReleaseMs = null
            tapAllowed = false
            paused = true
        }
    }

    fun resume() {
        if (paused) {
            runningSinceMs = SystemClock.elapsedRealtime()
            lastMotionMs = runningSinceMs
            paused = false
        }
    }

    fun hideWord(wordId: String) {
        hiddenWordIds.add(wordId)
    }

    fun isHidden(wordId: String): Boolean = hiddenWordIds.contains(wordId)

    private fun updateMotion() {
        val now = SystemClock.elapsedRealtime()
        if (paused) {
            lastMotionMs = now
            return
        }
        pendingPressReleaseMs?.let { releaseMs ->
            if (now - releaseMs >= SpatialPressReleaseDebounceMs) {
                if (!dragActivated || now - lastDragInputMs >= SpatialPressReleaseDebounceMs) {
                    finishDrag()
                } else {
                    pendingPressReleaseMs = null
                }
                return
            }
        }
        if (userDragging) {
            if (now - lastDragInputMs > DragVelocityIdleTimeoutMs) {
                userVelocityDegPerSecond = 0f
            }
            val elapsedSeconds = (now - lastMotionMs) / 1000f
            if (elapsedSeconds > 0f && dragActivated) {
                val delta = shortestDegreeDelta(from = baseOffsetDeg, to = dragTargetOffsetDeg)
                val followAmount = (elapsedSeconds * DragFollowRatePerSecond).coerceIn(0f, 1f)
                baseOffsetDeg = normalizeDegrees(baseOffsetDeg + delta * followAmount)
            }
            lastMotionMs = now
            return
        }
        val elapsedSeconds = (now - lastMotionMs) / 1000f
        if (elapsedSeconds <= 0f) return
        baseOffsetDeg = normalizeDegrees(
            baseOffsetDeg - OrbitAngularSpeedDegPerSecond * elapsedSeconds +
                userVelocityDegPerSecond * elapsedSeconds
        )
        userVelocityDegPerSecond *= Math.pow(
            UserOrbitVelocityDecayPerSecond.toDouble(),
            elapsedSeconds.toDouble()
        ).toFloat()
        if (kotlin.math.abs(userVelocityDegPerSecond) < 0.2f) {
            userVelocityDegPerSecond = 0f
        }
        lastMotionMs = now
    }
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
            .clickable(onClick = onClick)
            .spatialHoverEffect(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color(0xFF263344), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
