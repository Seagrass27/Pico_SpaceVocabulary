This article introduces how to create and configure a Spatial project in Android Studio.
## Prerequisites
You have already installed Android Studio and Spatial Plugin. For more information, refer to [Set up the development environment](/set-up-development-environment).
## Step 1: Create a spatial project

1. Open Android Studio, then click the **New Spatial Project** button, or click **File** > **New** > **New Spatial Project...** in the top menu bar.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/42d0f1fb1424405ca3ec47ef07f4e8d1~tplv-goo7wpa0wc-image.image)
   The **New Project** window appears.
2. Select the **Spatial WindowContainer** template (or others), then click the **Next** button.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/6617effac70547888176e13fa5f03225~tplv-goo7wpa0wc-image.image)
3. Set the project name, package name, save location, and minimum PICO OS 6 version, then click the **Finish** button.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/eb4a2155b00648ca8299aecb205dde6e~tplv-goo7wpa0wc-image.image)
   You will enter the following project edit window.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/dab2205ea3a1457994c9f9d86d1b3c11~tplv-goo7wpa0wc-image.image)
   It is not recommended to modify the default configuration in newly created projects, as this may cause abnormal operation.


## Step 2: Check the SDK version number
Open the libs.versions.toml file, and check the SDK version number in the `[versions]` section. The latest 0.12.2 version is recommended.
```TOML
[versions]
// ...
bom = "0.12.2"
```

## Step 3: Configure NDK (Windows only)
If you are using the Windows operating system, in the module-level build.gradle.kts script file in the `/project/module` directory, add the following NDK configuration in the `defaultConfig {}` section within `android {}`:
```Kotlin
android {
    // ...
    defaultConfig {
        // ...
        ndk { abiFilters.add("arm64-v8a") }
    }
}
```

## Step 4: Run a template project
After creating a Spatial project, you can run the project and see how it works on PICO Emulator or a PICO device.
### PICO Emulator
To run the project and view its effect without an available PICO device, use PICO Emulator.

1. Create a new virtual device:
   1. In the right sidebar of Android Studio, click **Device Manager**.
   2. Select **Create PICO Emulator** to create a new PICO Emulator.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/ba06b02b1b0c4fb4954f24aa4a39cc10~tplv-goo7wpa0wc-image.image)
2. Click this PICO Emulator's **Start** button to launch it.
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/643c54970c1047e2a56485b598c191c9~tplv-goo7wpa0wc-image.image)
   You will see the following PICO Emulator window once it launches:
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/63ff168cf5694c248e39d7e8cb0cad32~tplv-goo7wpa0wc-image.image)
3. Click the **Run** button in the top toolbar of Android Studio.
   <div style="text-align: baseline">Android Studio will launch PICO Emulator, build the corresponding module’s <code>.apk</code> file, and install it onto the emulator. PICO Emulator will then automatically run the module and display its scene.   </div>

   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/860e09c8efaa49168cd6803a45ba085a~tplv-goo7wpa0wc-image.image)
   PICO Emulator displays the following scene:
   ![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/e8f72b4aaf2743d1b0759f8173565dc6~tplv-goo7wpa0wc-image.image)

### PICO device
Android Studio will automatically recognize the connected PICO Emulator and PICO devices, and display them in the **Running devices** list.

1. In the **Running devices** list, select the device that needs to run the project. For example, in the following diagram: "PICO" refers to the PICO Emulator, and "Pico xx" refers to a PICO device.
2. Select the modules to run.
3. Click the **Run** button.

![Image](https://p9-arcosite.byteimg.com/tos-cn-i-goo7wpa0wc/de8ac2e6044f468582677bdf22af404d~tplv-goo7wpa0wc-image.image)
Android Studio packages the corresponding module as an .apk file and installs it on the selected device. The device will automatically run this module and display its scenes.



