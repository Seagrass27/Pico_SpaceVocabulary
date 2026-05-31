This document describes how to upgrade the version of PICO Spatial SDK in your spatial project from 0.11 to 0.12.
## Step 1: Update the spatial project configuration
You can modify the configuration by following these steps:

1. **Update the BOM version of PICO Spatial SDK**
   In `gradle/libs.versions.toml`, update the BOM version of PICO Spatial SDK (corresponding to `"com.pico.spatial:bom"`) to `"0.12.2"`.
   ```TOML
   [versions]
   spatialBom = "0.12.2"
   ```

   If you are not using version catalogs, update the BOM version in the `dependencies{}` code block of the module-level `build.gradle.kts` file.

2. **Update the version of Spatial Tools**
   If you use the `editor-asset` module, update the versions of `'com.pico.spatial.tools'` and `spatialToolsVersion` in the module-level `build.gradle` file of `editor-asset`.
   ```Groovy
   plugins {
       ...
       id 'com.pico.spatial.tools' version '0.12.0'
   }
   
   spatial {
       name = "editor-asset"
       spatialToolsVersion = 0.12
   }
   ```


## Step 2: Port SpatialUI components
If your spatial project uses the following SpatialUI components, refer to the following content to port these SpatialUI components.

* `TabBar`
* `Augment`
* `SegmentItem`

### TabBar
#### **Scope of impact**
All code that uses `TabBar` and `TabBarScope.item`.
#### **Changes**

* The constructor signature of `TabBar` has changed: the `placement` parameter has been removed, and `followViewpoints` and `focusable` have been added.
* `TabBarScope.item` now uses a `content` block instead of the original `icon` and `text` parameters.

#### **Porting solution**
Update the `TabBar` invocation:

<div style="display: flex;">
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.504750593824228);">

<div style="text-align: center"><strong>Old version</strong></div>

```Kotlin
// Old version
TabBar(placement = TabBarPlacement.Bottom, ...)
```




</div>
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.49524940617577196);margin-left: 16px;">

<div style="text-align: center"><strong>New version</strong></div>

```Kotlin
// New version
TabBar(
    followViewpoints = true, // or false
    focusable = true, // or false
    ...
)
```



</div>
</div>

Update the `TabBarScope.item` invocation:


<div style="display: flex;">
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.5000);">

<div style="text-align: center"><strong>Old version</strong></div>

```Kotlin
// Old version
item(
     selected = ...,
     onClick = { ... },
     icon = { Icon(...) },
     text = { Text("...") }
 )
```




</div>
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.5000);margin-left: 16px;">

<div style="text-align: center"><strong>New version</strong></div>

```Kotlin
// New version
item(
    selected = ...,
    onClick = { ... }
) {
    // Freely combine your Icon and Text in the content block
    Icon(...)
    Text("...")
}
```



</div>
</div>

### Augment
#### **Scope of impact**
All code that uses the `Augment` component.
#### Changes
The constructor of `Augment` now requires the `focusable` parameter.
#### **Porting solution**
Explicitly provide the `focusable` parameter when invoking `Augment`.

<div style="display: flex;">
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.504750593824228);">

<div style="text-align: center"><strong>Old version</strong></div>

```Kotlin
// Old version
Augment(augmentState = ...) { ... }
```




</div>
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.49524940617577196);margin-left: 16px;">

<div style="text-align: center"><strong>New version</strong></div>

```Kotlin
// New version
Augment(
    augmentState = ...,
    focusable = true // Or false, depending on your needs
) { ... }
```



</div>
</div>

### SegmentItem
#### **Scope of impact**
Code that uses `SegmentControlsKt.SegmentItem`.
#### Changes
The `onSelect` callback has been replaced by `onClick`.
#### **Porting solution**
Move your logic from `onSelect` to `onClick`.

<div style="display: flex;">
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.504750593824228);">

<div style="text-align: center"><strong>Old version</strong></div>

```Kotlin
// Old version
SegmentItem(selected = ..., onSelect = { ... }) { ... }
```




</div>
<div style="flex-shrink: 0;width: calc((100% - 16px) * 0.49524940617577196);margin-left: 16px;">

<div style="text-align: center"><strong>New version</strong></div>

```Kotlin
// New version
SegmentItem(selected = ..., onClick = { ... }) { ... }
```



</div>
</div>


