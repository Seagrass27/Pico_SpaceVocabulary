This document records the changes in each version of the PICO Spatial SDK.
## 0.12.x - 2026-05-14
The changes in version 0.12.x are as follows. To upgrade the SDK in your project from version 0.11.x to 0.12.x, refer to [Upgrade SDK](/upgrade-sdk).
**Additions**

* **SpatialUI：**
   * Added controller vibration feedback API to support triggering controller vibration during UI interactions. Relevant interfaces include `ControllerHapticConfiguration`, `HandController`, `HandControllerHapticType`, and `SpatialHandControllerHaptic`. For details, see "[Controller haptic feedback](/controller-vibration-feedback)".
   * `Toolbar` adds `supportingContent` for implementing segmented toolbars.
* **SpatialPack：**
   * 2D Drag and Drop now supports dragging 3D content. When the drag target is `SpatialView`, its associated 3D model can be dragged along with it.
   * Added the `AnimationBindTarget.bindBlendShapeWeights()` method for binding a target to drive BlendShape animation. For details, see "[Blend shape animation](/blendshape-animation)".
   * Added `MeshModel` API to support dynamic creation of `MeshResource` using custom vertex, index, UV, normal, and other data.
   * Added animation update events. Global events are dispatched before and after the underlying animation system settles, allowing developers to process animation data at the corresponding nodes.
   * Added `SpatialAudioTrackExtension` to facilitate integration of PICO Spatial Audio capabilities based on native `AudioTrack`. For details, see "[Using SpatialAudioTrackExtension](/use-spatial-audio-track-extension)".
   * `AudioResource` adds the `getConfig()` interface, and `AudioGroupResource` adds the `getPlayMode()` interface for querying configuration parameters.
   * Added the reserved value `UNKNOWN` to multiple enumeration types (such as `AnchorUpdate.Event` and `TrackingState`) for future version compatibility.
* **SpatialML：**
   * `Pipeline.ModelInferenceType` adds the options `LITE_RT_CPU`, `LITE_RT_GPU`, and `LITE_RT_NPU` to provide more inference hardware paths.
   * `Pipeline` adds the `getDepthMap(...)` method for obtaining depth map information.

**Modifications**

* **SpatialUI：**
   * **TabBar：**
      * The `TabBar(...)` constructor adds the `followViewpoints` and `focusable` parameters.
      * The `TabBarScope.item(...)` overload form of `text` / `icon` has been changed to a `content` content block form.
   * **Toolbar and Subwindow:**
      * The constructor signatures of `Toolbar(...)` and `Subwindow(...)` have been adjusted. Please update your invocation code according to the new signatures.
   * **Augment：**
      * The `Augment(...)` constructor adds the `focusable` parameter to control whether the window widget can receive focus.
   * **SegmentControl：**
      * The `SegmentItem(...)` adds the `onClick` callback parameter for handling click events.
   * **Snackbar：**
      * The overloaded method signature of `SnackbarHostState.show(...)` has been adjusted.
      * `Snackbar` adds background color and background glass effect control parameters, and supports programmatic cancellation.
   * **TextToolbar：**
      * The layout logic of `TextToolbar` has been adjusted to be horizontally centered based on the text selection area.
* **SpatialPack：**
   * Custom Component must now provide a `public` parameterless constructor. Previously, this constraint was only a Lint warning. Now, it is strictly enforced during the register phase at runtime. If the default constructor is missing, an exception will be thrown.
   * `AmbientAudioComponent` constructor adds the `AmbientOrientationMode` parameter, which specifies the calculation mode for audio orientation. Available options are `ORIENTATION_ONLY` or `POSITION_AND_ORIENTATION`. If not explicitly provided, `ORIENTATION_ONLY` is used by default.
   * Adds `UNKNOWN` or `AUTO` enum values to certain core enumeration types (such as `ShadowFaceCullingMode`) to enhance compatibility.

**Remove**

* **SpatialUI：**
   * Removed the `com.pico.spatial.ui.design.windows.TabBarPlacement` class; related capabilities have been integrated.
   * Removed the legacy `Augment(...)` constructor that does not include the `focusable` parameter.
* **SpatialPack：**
   * Removed the `AmbientAudioComponent(float volume)` constructor that only accepts the `volume` parameter.

**Fix**

* **SpatialPack：**
   * Fixed the potential `format not supported` error when loading models from the SAF file picker using `ContentResolver` + `Uri`.
* **SpatialUI：**
   * Fixed the issue where `PageControl` is not visible when in the `disable` state.
   * Fixed the issue where `Menu` and `SpatialPopup` that extend beyond the main window area do not automatically disappear when clicking outside the area.
   * Adjusted the timing for playing short sound effects during click interactions from `PressUp` to `PressDown`.
   * Corrected the issue where `backgroundMaterial` carries `back = 1` by default.

## 0.11.x - 2026-04-23
The changes in version 0.11.x are as follows. To upgrade the old SDK in your project to version 0.11.x, refer to the "Upgrade SDK" documentation for v0.11.x.
**Add**

* **SpatialPack：**
   * Added `AudioGroupResource` for managing audio groups, supporting playback within the group in different modes (`FORWARD`, `BACKWARD`, `RANDOM`).
   * Supports loading model files in `.obj` and `.stl` formats.
   * Added `uv` and `materialIndex` properties to the `CollisionCastResult` of ray detection and convex hull casting results, allowing retrieval of the texture coordinates and material index at the collision point (this information is available only when the collider is created using a static mesh).
   * Added the `toneMappingEnabled` property to `UnlitMaterial` to control whether tone mapping is enabled.
* **TrackingPack：**
   * Added controller button events, allowing monitoring of specific button press and touch states, as well as values for the Trigger button/grip button, via `ControllerAction` and `ControllerActionData`.
* **SpatialML：**
   * Introduces `PipelineArithmeticScope` to perform tensor operations in a structured manner, replacing the string-based `arithmetic(expression, ...)` API.
   * Added `Pipeline.JavaScriptIO`, enabling custom preprocessing and postprocessing operators for machine learning algorithms using JavaScript.
   * Added audio I/O, supporting direct microphone Input and output of generated audio within the pipeline.


**Modify**

* **Build toolchain upgrade:**
   * **Kotlin**: Upgraded to version 2.0, with the K2 compiler enabled by default.
   * **Compose Compiler**: Now integrated into the Kotlin plugin, configured according to the [official migration guide](https://kotlinlang.org/docs/compose-compiler-migration-guide.html).
   * **Android Gradle Plugin (AGP)**: It is recommended to upgrade the project baseline version to 8.8.0. The minimum officially supported version for Kotlin 2.0 is AGP 8.5, but actual build validation shows that lower versions have compatibility issues during the release build phase.
* **SpatialUI：**
   * Color system refactor: migrated from accent/onAccent + Vibrant to semantic ColorScheme / *Colors (such as fillPrimary, labelPrimary), with component colors now uniformly driven by semantic roles. Custom themes need to be ported to the new system.
   * The package path for `Menu`-related `HorizontalPlacement` and `VerticalPlacement` classes has changed: `com.pico.spatial.ui.design.menu` → `com.pico.spatial.ui.design.windows.popup` (please update import statements).
* **SpatialPack：**
   * `ObjectAudioComponent` now includes the `soundRadiusLevel` property, which controls the perceptual radius of the sound source.
   * `AudioResourceConfig` now supports the `randomStart` and `loopEnable` flags, providing more control over audio playback.
   * When loading `MeshResource` fails, only `ResourceLoadingException` is now thrown, simplifying exception handling logic.


**Deprecated**

* **SpatialML：**
   * The string-based `Pipeline.arithmetic(expression: String, ...)` method has been deprecated. Please port to the new `Pipeline.arithmetic(result: Tensor, operations: PipelineArithmeticScope.() -> ...)` method, which offers better type safety and a more expressive DSL.


**Removed**

* **SpatialUI：**
   * Removed all `*Vibrants` data classes (such as `ButtonVibrants`, `SliderVibrants`, `ChipVibrants`) and related `*Defaults` methods; removed all vibrant* parameters and related overloaded methods from components such as `IconKt`, `TextKt`, `ButtonKt`; removed the `ColorStyle` class used for coupling color and visual effects. Additionally, the `Vibrant` enum has been simplified, with `LightenHover` and `Termination` removed. The original color and vibrant capabilities have been consolidated into ColorScheme / *Colors, and related logic needs to be remapped according to semantic roles.
* **SpatialPack：**
   * `VideoMaterial` removed `setHardwareBuffer`; `VideoComponent` removed `getTransformMode` and `setCropRect`. To play spatial videos using `VideoComponent`, switch to the `SurfaceRenderTexture` pipeline. The `setHardwareBuffer` approach is no longer provided; cropping behavior is now built-in, so `setCropRect` no longer needs to be called.


**Fixed**

* **SpatialPack：**
   * Fixed an issue where loading models via `ContentResolver` + `Uri` (for example, from the SAF file picker) would result in a "format not supported" error.
   * Fixed rendering anomalies when multiple Attachments have parent-child relationships.
* **SpatialUI：**
   * Corrected the blending mode of `Checkbox` to ensure proper display.
   * Fixed potential jitter when moving the cursor in text input components such as `TextField`.


---


## 0.10.x - 2026-03-02
The first official version of the PICO Spatial SDK is now live, targeting devices running PICO OS 6 and helping developers build spatial apps.

* **3D scene / entity / resource system:** Covers core 3D capabilities including resource loading, scene orchestration, animation playback, physics simulation, and collision detection, providing foundational support for spatial experiences.
* **Spatial UI framework:** Provides window management, 2D UI layout and component library, as well as basic spatial hand pose support, bridging 2D content to 3D scenes.
* **Spatial audio and video:** Through Spatial Audio rendering and Spatial Video playback, restores a three-dimensional sound field and visuals, building an immersive audiovisual experience for spatial apps.
* **Environmental perception:** Leveraging the system's environment mesh scanning capability, enables apps to perceive and utilize real-world spatial information.
* **SpatialML:** An ML inference runtime framework for mixed reality scenarios, supporting local execution of custom models trained on mainstream frameworks on PICO devices, and enhancing environmental understanding by integrating camera and spatial positioning data.
* **Development toolchain and sample projects:** Provides supporting development tools such as Spatial Editor, Spatial Plugin, and PICO Emulator, as well as template and sample projects, helping developers quickly build and debug spatial apps.
