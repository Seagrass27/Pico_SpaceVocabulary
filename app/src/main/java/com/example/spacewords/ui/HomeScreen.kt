package com.example.spacewords.ui

import androidx.compose.foundation.Image
import android.os.Bundle
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewords.R
import com.example.spacewords.HomeWindowId
import com.example.spacewords.LearningWindowId
import com.example.spacewords.LearningWindowTag
import com.example.spacewords.RevisionStageId
import com.example.spacewords.TopicBundleKey
import com.example.spacewords.data.SpaceWordsStore
import com.example.spacewords.model.Topic
import com.example.spacewords.model.Topics
import com.example.spacewords.model.ensureVocabularyLoaded
import com.example.spacewords.model.findWord
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.StageStyle
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ensureVocabularyLoaded(context)
    SpaceWordsStore.ensureLoaded(context)
    val version by SpaceWordsStore.version
    val revisionCount = SpaceWordsStore.revisionWords().count { findWord(it.wordId) != null }
    val rememberedCount = SpaceWordsStore.rememberedWords().count { findWord(it.wordId) != null }
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
                    coroutineScope.launch {
                        spatialNavigator.minimizeWindowContainer(HomeWindowId)
                        spatialNavigator.openWindowContainer(
                            id = LearningWindowId,
                            tag = "$LearningWindowTag-${topic.id}",
                            bundle = Bundle().apply { putString(TopicBundleKey, topic.id) }
                        )
                    }
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
                            spatialNavigator.openStage(RevisionStageId, style = StageStyle.Mixed)
                            spatialNavigator.minimizeWindowContainer(HomeWindowId)
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
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(shape)
            .clickable(onClick = onClick)
            .spatialHoverEffect()
    ) {
        Image(
            painter = painterResource(id = topicImageRes(topic.id)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.34f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                ShadowedText(
                    text = topic.title,
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 23,
                    fontWeight = FontWeight.SemiBold
                )
                ShadowedText(
                    text = "${topic.words.size} words",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ShadowedText(
    text: String,
    color: Color,
    fontSize: Int,
    fontWeight: FontWeight
) {
    Box {
        Text(
            text = text,
            color = Color.Black.copy(alpha = 0.22f),
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = fontSize.sp,
            fontWeight = fontWeight
        )
    }
}

private fun topicImageRes(topicId: String): Int =
    when (topicId) {
        "food" -> R.drawable.topic_food
        "travel" -> R.drawable.topic_travel
        "office" -> R.drawable.topic_office
        "interview" -> R.drawable.topic_interview
        "workout" -> R.drawable.topic_workout
        "education" -> R.drawable.topic_education
        else -> R.drawable.topic_food
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
            .then(if (enabled) Modifier.clickable(onClick = onClick).spatialHoverEffect() else Modifier)
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
