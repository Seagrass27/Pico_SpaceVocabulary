Ambient audio can perceive the relative orientation between the sound source and the listener, but does not include reverb effects or consider the distance between the sound source and the listener.
`AmbientAudioComponent` can be understood as a listener-centric "audio skybox" that dynamically adjusts audio output by perceiving the relative orientation of sound sources.
## Characteristics

* **Constant volume**: Regardless of how the listener moves, the volume does not change with the listener's position.
* **Directionality limited**: Listeners can hear sound only within the sound source's directional area; outside this range, the sound is completely muted.

## Use cases
In virtual environments, ambient audio is suitable for creating a sense of direction and atmosphere, rather than creating a sense of spatial distance. Typical use cases are as follows:

* Configure non-narrative sounds, such as background music or ambient sounds, as ambient audio to prevent changes in the listener's viewpoint from affecting the listening experience.
* Sounds that convey a strong sense of direction but do not require distance attenuation, such as wind, flowing water, or distant machinery.

By using environmental audio, you can enhance the sense of immersion in the scene and provide a more natural and coherent auditory experience without increasing the complexity of spatialization calculations.
## General development procedure
For the general procedure of using spatial audio, refer to the "Development procedure" section in "[Spatial audio overview](/en_spatial-audio-overview)".
## Use ambient audios
The following code demonstrates how to use ambient audios, including creating entities, loading audio files, and using the player controller to play, pause, and stop the audio.
```Kotlin
// Create an entity instance
val entity = Entity()

// Load an audio file
val audioResource =
    AudioResource.load(
        "your_custom_name",
        "asset://your_ambient_audio_file.wav",
        LoadType.FROM_ASSETS
    )

// Add the ambient audio component to the entity
val ambientAudioComponent = AmbientAudioComponent(volume = 1.0f)
entity.components.set(ambientAudioComponent)

// Get the audio player controller (choose any method)
// Method 1:
val audioPlayerController = entity.prepareAudio(audioResource)
// Method 2:
val audioPlayerController = entity.playAudio(audioResource)

// Control audio playback, including starting, pausing, and stopping playback
audioPlayerController.play()
audioPlayerController.pause()
audioPlayerController.stop()
```

## Select an audio perception calculation mode
`AmbientAudioComponent` supports two different audio perception calculation modes through `AmbientOrientationMode`, allowing it to adapt to different spatial audio scenarios, such as MR window anchoring or VR background audio.
| **Mode** | **Description** |
| --- | --- |
| `POSITION_AND_ORIENTATION` | When calculating sound, this mode takes into account both the entity's absolute spatial position and its relative orientation to the listener. This mode provides a clear dynamic parallax effect, meaning the perceived direction of the sound source changes relative to the user as the user moves through physical space. <br> Recommended for MR scenarios. For example, when the application window or sound-emitting entity is anchored in the real world and does not move with the user's head, this mode ensures that the sound always comes from the physical direction of the window. |
| `ORIENTATION_ONLY` | This mode considers only the source's base direction and does not include perceptual parallax caused by the user's positional movement. It provides stable 3DoF-style audio behavior, where the relative sound direction does not change as the user walks around. <br> Recommended for VR scenarios. It is mainly used for pure background music, movie viewing in VR mode, 180/360 panoramic videos, and other scenarios where the listener's absolute position in space does not need to be considered. |
You can specify the calculation mode directly when constructing `AmbientAudioComponent`:
```Kotlin
// Use the default mode (ORIENTATION_ONLY), suitable for pure background audio or VR movie viewing
val ambientAudio = AmbientAudioComponent(volume = 1.0f)

// Initialize with the position-involved mode, suitable for MR anchored scenarios
val mrAmbientAudio = AmbientAudioComponent(
 volume = 1.0f,
 ambientOrientationMode = AmbientOrientationMode.POSITION_AND_ORIENTATION
)
```

In addition, you can dynamically read or write the current calculation mode at runtime through the `ambientOrientationMode` property:
```Kotlin
// Switch the calculation mode to the position-involved mode
ambientAudio.ambientOrientationMode = AmbientOrientationMode.POSITION_AND_ORIENTATION

// Read the current calculation mode
val currentMode = ambientAudio.ambientOrientationMode
```

## API reference
The `AmbientAudioComponent` class provides relevant properties and functions. For more information, refer to the API Reference. Choose a doc link per your geographical location.

* Chinese mainland: [https://developer-cn.picoxr.com/spatial-api/index.html](https://developer-cn.picoxr.com/spatial-api/index.html)
* Outside Chinese mainland: [https://developer.picoxr.com/spatial-api/index.html](https://developer.picoxr.com/spatial-api/index.html)


