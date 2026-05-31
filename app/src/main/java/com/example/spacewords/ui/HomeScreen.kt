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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.LearningWindowId
import com.example.spacewords.LearningWindowTag
import com.example.spacewords.RevisionStageId
import com.example.spacewords.TopicBundleKey
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.model.Topic
import com.example.spacewords.model.Topics
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SpaceWordsStore.ensureLoaded(context)
    val version by SpaceWordsStore.version
    val revisionCount = SpaceWordsStore.revisionWords().size
    val rememberedCount = SpaceWordsStore.rememberedWords().size
    val spatialNavigator = LocalSpatialNavigator.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .backgroundMaterial(true, Material.Regular)
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Header(rememberedCount = rememberedCount, revisionCount = revisionCount)
            TopicGrid(
                topics = Topics,
                onTopicSelected = { topic ->
                    spatialNavigator.openWindowContainer(
                        id = LearningWindowId,
                        tag = "$LearningWindowTag-${topic.id}",
                        bundle = Bundle().apply { putString(TopicBundleKey, topic.id) }
                    )
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                WideActionPanel(
                    modifier = Modifier.weight(1f),
                    title = "Revision",
                    subtitle = "Open your wordbook in a Mixed Stage orbit.",
                    value = "$revisionCount words",
                    enabled = revisionCount > 0,
                    onClick = {
                        coroutineScope.launch {
                            spatialNavigator.minimizeWindowContainer()
                            spatialNavigator.openStage(RevisionStageId, style = StageStyle.Mixed)
                        }
                    }
                )
                WideActionPanel(
                    modifier = Modifier.weight(1f),
                    title = "Quiz",
                    subtitle = "A mastery test for remembered words is reserved here.",
                    value = "$rememberedCount ready",
                    enabled = false,
                    onClick = {}
                )
            }
        }
    }
    version.hashCode()
}

@Composable
private fun Header(rememberedCount: Int, revisionCount: Int) {
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
                text = "SpaceWords",
                color = Color(0xFF1F2733),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A calm spatial vocabulary app for focused adult English learning.",
                color = Color(0xFF6D7684),
                fontSize = 15.sp
            )
        }
        Text(
            text = "$rememberedCount remembered  ·  $revisionCount in revision",
            color = Color(0xFF516175),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TopicGrid(topics: List<Topic>, onTopicSelected: (Topic) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        topics.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { topic ->
                    TopicTile(
                        modifier = Modifier.weight(1f),
                        topic = topic,
                        onClick = { onTopicSelected(topic) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicTile(modifier: Modifier, topic: Topic, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(124.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            topic.tint.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.30f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(39.dp))
                    .background(topic.tint.copy(alpha = 0.38f))
            )
            Column(Modifier.align(Alignment.Center)) {
                Text(
                    text = topic.title,
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${topic.words.size} words",
                    color = Color.White.copy(alpha = 0.74f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun WideActionPanel(
    modifier: Modifier,
    title: String,
    subtitle: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF2B3441),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(text = subtitle, color = Color(0xFF748090), fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = value,
                color = if (enabled) Color(0xFF40546E) else Color(0xFF8D95A0),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
