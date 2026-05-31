package com.example.spacewords

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spacewords.ui.HomeScreen
import com.example.spacewords.ui.LearningScreen
import com.example.spacewords.ui.RevisionStage
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.pico.spatial.ui.foundation.dsl.Stage
import com.pico.spatial.ui.foundation.dsl.WindowContainer

const val LearningWindowId = "SpaceWordsLearning"
const val LearningWindowTag = "learning"
const val RevisionStageId = "SpaceWordsRevisionStage"
const val TopicBundleKey = "topic"

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                HomeScreen(Modifier.windowConstraints(width = 1120.dp, height = 720.dp))
            }
        }

        WindowContainer(id = LearningWindowId) {
            PicoTheme {
                LearningScreen(
                    bundle = this.bundle,
                    modifier = Modifier.windowConstraints(width = 820.dp, height = 720.dp)
                )
            }
        }

        Stage(id = RevisionStageId) {
            PicoTheme {
                RevisionStage()
            }
        }
    }
