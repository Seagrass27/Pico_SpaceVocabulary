# SpaceWords Project Guide

SpaceWords is a PICO Spatial SDK vocabulary-learning app built in the current project root. It uses Kotlin, Jetpack Compose, Spatial UI, a default planar WindowContainer, a secondary learning WindowContainer, and a non-default Stage for the revision wordbook.

## Current Product Shape

- Home: a translucent glass-style panel with six vocabulary themes: Food, Travel, Office, Interview, Workout, and Education.
- Topic learning: opens a separate WindowContainer for the selected theme. Each theme currently has 8 static words with phonetics, Chinese meaning, example sentence, and an image placeholder. Users choose "Add to Revision" or "Remembered".
- Wordbook revision: opens `SpaceWordsRevisionStage` as a Stage. Revision words are presented as orbiting attachment panels, with older words closer to the user and newer words farther out.
- Quiz: reserved on the home screen and currently disabled until the remembered-word quiz flow is designed.

## Implementation Path

This project was scaffolded from `templates/basic-spatial-template/PlanarWindowContainer` because the first screen is a flat glass UI similar to the provided screenshot. A non-default Stage was added for the Mixed Stage wordbook instead of making Stage the default container, so the home screen remains lightweight and familiar while the revision mode can become immersive.

## Key Files

- `app/src/main/java/com/example/spacewords/Main.kt`: registers the default home WindowContainer, the learning WindowContainer, and the revision Stage.
- `app/src/main/java/com/example/spacewords/ui/HomeScreen.kt`: home dashboard, theme grid, revision entry, and quiz placeholder.
- `app/src/main/java/com/example/spacewords/ui/LearningScreen.kt`: per-topic word-card learning flow.
- `app/src/main/java/com/example/spacewords/ui/RevisionStage.kt`: Stage-based wordbook orbit and selected word review card.
- `app/src/main/java/com/example/spacewords/model/Vocabulary.kt`: static MVP vocabulary data, 8 words per theme.
- `app/src/main/java/com/example/spacewords/data/SpaceWordsStore.kt`: local `SharedPreferences` persistence for revision and remembered states.
- `app/src/main/AndroidManifest.xml`: declares the default planar WindowContainer metadata.

## Spatial SDK Capabilities In Use

- `DefaultWindowContainer` for the main app surface.
- `WindowContainer` for topic learning windows.
- `Stage` for the revision wordbook.
- `SpatialView` and `AttachmentPanel` to place 2D Compose panels in 3D Stage space.
- Spatial material backgrounds through `Modifier.backgroundMaterial`.

## Build And Run

Expected commands:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Then launch the app from the PICO emulator/device launcher. If Java is missing, install or configure a JDK first; this machine currently has `/usr/bin/java` but no usable Java runtime.

## Next Development Steps

- Replace button-based learning decisions with spatial drag/swipe gestures.
- Add real pronunciation playback or Android TTS behind the pronunciation placeholder.
- Replace image placeholders with generated or curated local image assets.
- Build the Quiz flow from `SpaceWordsStore.rememberedWords()`.
- Improve the Stage orbit with gaze/hover targeting once device input behavior is verified.

For API details and constraints, future agents should check `SPATIAL_SDK_REFERENCE.md` if present and the local `documentation/` files before changing Spatial container or ECS behavior.
