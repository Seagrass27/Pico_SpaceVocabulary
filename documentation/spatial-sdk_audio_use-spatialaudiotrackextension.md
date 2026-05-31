In spatial app development, developers often encounter pain points such as high integration costs and complex audio-video synchronization logic when integrating Spatial Audio into third-party players (such as ExoPlayer or Media Player). Developers typically need to make deep modifications to the underlying playback engine or manually maintain complex 3D pose synchronization logic.
To address these pain points, PICO Spatial SDK provides `SpatialAudioTrackExtension`. `SpatialAudioTrackExtension` extends the native Android [AudioTrack](https://developer.android.com/reference/android/media/AudioTrack) interface. For third-party players based on Android native [AudioTrack](https://developer.android.com/reference/android/media/AudioTrack), you can associate audio with 3D entities or application containers without changing the original playback logic.
![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/4d79b1c9af52451eac28feed8f18c9e1~tplv-goo7wpa0wc-image.image)
## Development process
### Step 1: Configure SpatialAudioTrackExtension
First, create a `SpatialAudioTrackExtension` instance, then call its `spatialAudioTrackExtensionConfig()` method to complete the configuration. The parameters of `spatialAudioTrackExtensionConfig()` are as follows:
| Parameters | Description |
| --- | --- |
| **mode** | Spatial audio mode. <br>  <br>    * `SpatialAudioMode.OBJECT`: Full 3D position audio, with both position and orientation information. Each audio source has its own 3D position and can be spatially rendered. Use case: Audio sources with a clear position in 3D space, such as character dialogue in a video panel or sound effects for specific scenes. <br>    * `SpatialAudioMode.AMBIENT`: Environmental audio that considers the listener's orientation. The audio is mixed based on the listener's head orientation, but does not include position information. Use case: Background environmental sounds (such as wind or rain), where directional perception is needed when the user turns their head, but precise positioning is not required. <br>    * `SpatialAudioMode.CHANNEL`: Traditional channel-based audio (stereo, 5.1 channels, and so on). No spatial processing is performed; audio is played from the speakers in its original form. Use case: Regular 2D audio content that does not require spatial effects, or as a fallback when spatialization is unavailable. |
| **isAmbisonic** | Whether to enable Ambisonic. This parameter is only valid when **mode** is set to `SpatialAudioMode.AMBIENT`. <br>  <br> * true: Enables Ambisonic. <br> * false (default): Disables Ambisonic. |
| **builder** | Pass in an `AudioTrack.Builder` instance. The SDK will complete the Spatial Audio-related configuration based on this builder. |
```Kotlin
val builder = AudioTrack.Builder()
val extension = SpatialAudioTrackExtension()
// Configure Spatial Audio parameters
extension.spatialAudioTrackExtensionConfig(
    mode = SpatialAudioMode.OBJECT,
    isAmbisonic = false,
    builder = builder
)
```

### Step 2: Associate AudioTrack with an entity or container via SpatialAudioTrackExtension
At the following two times, you can use `SpatialAudioTrackExtension` to associate `AudioTrack` with an entity or container:

* Use `AudioTrack.Builder` to complete the association during the creation phase of `AudioTrack`. For more details, see [Step 1: Associate AudioTrack with an entity or container during the AudioTrack creation phase](/sdk/use-spatial-audio-track-extension).
* Associate an existing `AudioTrack` instance. For more details, see [Method 2: Associate an existing AudioTrack with an entity or container](/sdk/use-spatial-audio-track-extension).

#### Step 1: Associate AudioTrack with an entity or container during the AudioTrack creation phase
You can use `AudioTrack.Builder` to complete the association during the `AudioTrack` creation phase. You can call the following methods to associate `AudioTrack` with an entity or a spatial container:

* `attachToEntityWithBuilder()`: Associates `AudioTrack` with an entity. Use when audio needs to follow the position of a specific 3D object in the scene (for example, a video panel entity).
* `attachToContainerWithBuilder()`: Associate `AudioTrack` with the spatial container. Use when audio only needs to follow the position of the window or container (for example, fixed background music).

```Kotlin
// Associate AudioTrack with entity
extension.attachToEntityWithBuilder(targetEntity, builder)
```

#### Method 2: Associate an existing AudioTrack with an entity or container
To associate the `AudioTrack` with an entity or container after the third-party player has created an `AudioTrack` instance, use the following methods:

* `attachToEntityWithAudioTrack()`: Associate `AudioTrack` with an entity. Use when audio needs to follow the position of a specific 3D object in the scene (for example, a video panel entity).
* `attachToContainerWithAudioTrack()`: Associate `AudioTrack` with the spatial container. Use when audio only needs to follow the position of a window or container (for example, fixed background music).

```Kotlin
// Associate AudioTrack with entity
extension.attachToEntityWithAudioTrack(targetEntity, audioTrack)
```

## Sample code
This section provides four complete code examples based on the combination of "association timing" and "association object". You can directly copy and use these based on your scenario.
| **Scenario** | **Association timing** | **Associated object** | **Applicable scenario** |
| --- | --- | --- | --- |
| [Scenario 1: Associate with an entity during AudioTrack creation](/sdk/use-spatial-audio-track-extension) | Creation phase (Builder) | Entity | Custom player, and the audio needs to follow a specific 3D object in the scene |
| [Scenario 2: Associate an existing AudioTrack with an entity](/sdk/use-spatial-audio-track-extension) | Existing instance | Entity | A third-party player (such as ExoPlayer) has already created an `AudioTrack` and needs to follow the entity |
| [Scenario 3: Associate with a container during AudioTrack creation](/sdk/use-spatial-audio-track-extension) | Creation phase (Builder) | spatial container | Scenarios such as window-level background music that only need to follow the container position |
| [Scenario 4: Associate an existing AudioTrack with a container](/sdk/use-spatial-audio-track-extension) | Existing instance | spatial container | A third-party player has already created an `AudioTrack` and needs to follow the container |
### Scenario 1: Associate with an entity during AudioTrack creation
Use case: For custom players, and when audio needs to follow the position of a specific entity in the scene.
```Kotlin
val extension = SpatialAudioTrackExtension()
extension.spatialAudioTrackExtensionConfig(
    mode = SpatialAudioMode.OBJECT,
    isAmbisonic = false,
    builder = audioTrackBuilder
)
extension.attachToEntityWithBuilder(targetEntity, audioTrackBuilder)
val audioTrack = audioTrackBuilder.build()
```

### Scenario 2: Associate an existing AudioTrack with an entity
Use case: For situations where a third-party player (such as ExoPlayer or Media Player) has already created an `AudioTrack` instance, and it needs to be associated with an entity in the scene.
```Kotlin
val extension = SpatialAudioTrackExtension()
extension.spatialAudioTrackExtensionConfig(
    mode = SpatialAudioMode.OBJECT,
    isAmbisonic = false,
    builder = audioTrackBuilder
)
val audioTrack = audioTrackBuilder.build() // Created by a third-party framework
extension.attachToEntityWithAudioTrack(targetEntity, audioTrack)
```

### Scenario 3: Associate with a container during AudioTrack creation
Use case: For scenarios where audio only needs to follow the position of the window or spatial container, such as fixed background music.
```Kotlin
val extension = SpatialAudioTrackExtension()
extension.spatialAudioTrackExtensionConfig(
    mode = SpatialAudioMode.AMBIENT,
    isAmbisonic = false,
    builder = audioTrackBuilder
)
extension.attachToContainerWithBuilder(context, audioTrackBuilder)
val audioTrack = audioTrackBuilder.build()
```

### Scenario 4: Associate an existing AudioTrack with a container
Use case: For situations where a third-party player has already created an `AudioTrack` instance, and the audio only needs to follow the position of the spatial container.
```Kotlin
val extension = SpatialAudioTrackExtension()
extension.spatialAudioTrackExtensionConfig(
    mode = SpatialAudioMode.AMBIENT,
    isAmbisonic = false,
    builder = audioTrackBuilder
)
val audioTrack = audioTrackBuilder.build()
extension.attachToContainerWithAudioTrack(context, audioTrack)
```

## Developer notes

* **Spatial audio effect switch**: You can call `enableSpatialAudio(enable, audioTrack)` to dynamically enable or disable spatial audio at runtime.

## API reference
The `SpatialAudioTrackExtension` class provides relevant properties and functions. For detailed information, see API reference. Select the appropriate documentation link based on your geographic location:

* Chinese mainland: [https://developer-cn.picoxr.com/spatial-api/index.html](https://developer-cn.picoxr.com/spatial-api/index.html)
* Outside Chinese mainland: [https://developer.picoxr.com/spatial-api/index.html](https://developer.picoxr.com/spatial-api/index.html)
