This article describes how to integrate controller haptic feedback into Spatial UI components. Controller haptic feedback is suitable for key interaction points such as clicking, confirming, and hover prompts, serving as supplementary feedback beyond visual and audio cues.
Controller haptic feedback only takes effect when the interaction method is controller interaction.

## Core concepts
Controller haptic feedback involves two core concepts:

* `HandControllerHapticType` is used to define the physical parameters of the vibration
* `HandController` is used to specify the controller affected by the vibration.

### HandControllerHapticType: Vibration types
`HandControllerHapticType` describes the physical parameters of a single controller vibration. `HandControllerHapticType` provides commonly used presets and also supports custom construction.
#### **Preset vibration types**
`HandControllerHapticType` offers the following preset vibration types, which meet most common interaction scenarios:

* `HandControllerHapticType.Press`: Press feedback, suitable for button clicks, confirmation actions, and similar scenarios. Corresponds to `HandControllerHapticType(240, 127, 15)`.
* `HandControllerHapticType.Hover`: Hover feedback, with lighter intensity and shorter duration, suitable for prompts when the cursor enters an interactive area. Corresponds to `HandControllerHapticType(38, 177, 5)`.
* `HandControllerHapticType.Step`: Step feedback, suitable for steppers, sliding lists, continuous selection, and similar scenarios. Corresponds to `HandControllerHapticType(51, 65, 10)`.
* `HandControllerHapticType.None`: No vibration triggered, can be used to explicitly disable haptic feedback for a specific interaction step.

#### **Custom parameters**
If the preset types do not meet your requirements, you can construct `HandControllerHapticType` yourself. The parameter descriptions are as follows:
| Parameter | Type | Value range | Description |
| --- | --- | --- | --- |
| `level` | Int | 0 ~ 255 | Haptic intensity. The higher the value, the stronger the haptic sensation. |
| `frequency` | Int | 40 ~ 500 | Haptic frequency, unit: Hz. |
| `duration` | Int | 0 ~ 1000 | Duration, unit: milliseconds. |
### HandController: Target controller
`HandController` is an enumeration type used to specify the target controller affected by the vibration:

* `HandController.Left`: Left controller.
* `HandController.Right`: Right controller.

## Implementation methods
PICO Spatial SDK provides three integration methods, covering different needs from single components to global, and from declarative to imperative approaches. Select the appropriate method based on your use case:
| Method | Entry point | Use case |
| --- | --- | --- |
| [Method 1: Use Modifier to add haptic feedback to a single component](/sdk/controller-vibration-feedback) | `Modifier.controllerHapticFeedback()` | Add haptic feedback to a single Composable. This is the most common and concise method. |
| [Method 2: Override the default vibration configuration globally via theme](/sdk/en_controller-vibration-feedback) | `LocalControllerHapticConfiguration` | Uniformly adjust the default haptic feedback for a group of Spatial UI components. |
| [Method 3: Obtain an instance and trigger manually](/sdk/en_controller-vibration-feedback) | `LocalHandControllerHaptic` | Actively trigger vibration, for example, to distinguish between left and right controllers or to customize the trigger timing. |
### Method 1: Use Modifier to add haptic feedback to a single component
#### Add default vibration
Using `Modifier.controllerHapticFeedback()` allows you to add default `Press` vibration to any Composable. The following code demonstrates how to enable default vibration on a clickable component.
When a component uses both `clickable` and `controllerHapticFeedback`, it is recommended to pass in the same `interactionSource` so that the click state and vibration trigger state remain consistent.

```Kotlin
@Composable
fun ControllerHapticFeedbackSample() {
    PicoTheme {
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .controllerHapticFeedback(interactionSource = interactionSource)
                    .clickable(interactionSource = interactionSource) {
                        // Click logic
                    }
        )
    }
}
```

#### Custom vibration parameters
If the preset vibration does not meet your requirements, you can directly construct `HandControllerHapticType`, customize the vibration intensity, frequency, and duration, and then pass it in via the `type` parameter.
When customizing parameters, `level`, `frequency`, and `duration` must fall within the valid ranges of `0~255`, `40~500`, and `0~1000` respectively; otherwise, vibration may not work or may behave abnormally.

```Kotlin
@Composable
fun CustomControllerHapticFeedbackSample() {
    PicoTheme {
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .controllerHapticFeedback(
                        type = HandControllerHapticType(level = 200, frequency = 500, duration = 30),
                        interactionSource = interactionSource,
                    )
                    .clickable(interactionSource = interactionSource) {
                        // Click logic
                    }
        )
    }
}
```

### Method 2: Override the default vibration configuration globally via theme
In certain scenarios, you may want to uniformly adjust the default haptic feedback for a group of Spatial UI components, rather than adding a Modifier to each component individually. At this point, you can override the default configuration within the `PicoTheme` scope using `LocalControllerHapticConfiguration`.
When multiple components need to use the same set of custom feedback, this approach is preferred to avoid adding the Modifier to each component individually and resulting in redundancy.

The following example overrides the `Press` haptic feedback with custom parameters. All Spatial UI components within the scope will use the new configuration:
```Kotlin
@Composable
fun OverrideButtonHaptic() {
    PicoTheme {
        val customHapticConfig: ControllerHapticConfiguration =
            LocalControllerHapticConfiguration.current.copy(
                press = HandControllerHapticType(level = 200, frequency = 500, duration = 30),
            )

        CompositionLocalProvider(
            LocalControllerHapticConfiguration provides customHapticConfig,
        ) {
            Button(onClick = { }) {
                Text("Custom haptic configuration, which overrides the Press haptic feedback")
            }
        }
    }
}
```

### Method 3: Obtain an instance and trigger manually
Spatial UI passes the haptic object in the Compose context via `CompositionLocalProvider`. In any Composable, you can obtain the current instance through `LocalHandControllerHaptic.current` and actively trigger haptic feedback. This is suitable for scenarios where you want to customize the trigger timing or differentiate between left and right controllers.
#### Actively trigger haptic feedback
The following example demonstrates how to actively trigger the `Press` haptic feedback for the left controller via the instance in `clickable`:
```Kotlin
@Composable
fun GetHapticFeedback() {
    PicoTheme {
        val controllerHaptic = LocalHandControllerHaptic.current

        Box(
            modifier = Modifier.clickable {
                controllerHaptic.feedback(
                    type = HandControllerHapticType.Press,
                    handController = HandController.Left,
                )
            }
        )
    }
}
```

#### Differentiate haptic feedback for left and right controllers
To provide different feedback for the left and right controllers, you can read the interaction source in `pointerInput`, map it to `HandController` via `change.interactionKindExtra.toHandController()`, and then call `feedback()` separately.
```Kotlin
@Composable
private fun LeftRightDistinctHapticDemo() {
    PicoTheme {
        val localHandControllerHaptic = LocalHandControllerHaptic.current

        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { change ->
                                    if (change.pressed && !change.previousPressed) {
                                        when (change.interactionKindExtra.toHandController()) {
                                            HandController.Left -> {
                                                localHandControllerHaptic.feedback(
                                                    HandControllerHapticType.Press,
                                                    HandController.Left,
                                                )
                                            }

                                            HandController.Right -> {
                                                localHandControllerHaptic.feedback(
                                                    HandControllerHapticType.Step,
                                                    HandController.Right,
                                                )
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .clickable { }
        )
    }
}
```

## Caution

* All methods must be used within the `PicoTheme` scope; otherwise, the theme configuration and haptic instance cannot be obtained correctly.
* When a component uses both `clickable` and `controllerHapticFeedback`, the same `interactionSource` should be shared.
* When customizing `HandControllerHapticType`, ensure that the parameters are within the valid range.
* Method 2 (theme override) only affects components within the scope; components outside the scope still use the default configuration.

## API reference
`Modifier.controllerHapticFeedback()`, `LocalControllerHapticConfiguration`, `LocalHandControllerHaptic`, and `HandControllerHapticType`. For details, see API reference. Select the appropriate documentation link based on your geographic location:

* Chinese mainland: [https://developer-cn.picoxr.com/spatial-api/index.html](https://developer-cn.picoxr.com/spatial-api/index.html)
* Outside Chinese mainland: [https://developer.picoxr.com/spatial-api/index.html](https://developer.picoxr.com/spatial-api/index.html)
