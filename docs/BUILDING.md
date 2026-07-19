# Android Build and Troubleshooting Guide

This guide covers a reproducible local setup for the public Dog Breed Identifier demo. It supplements the short build commands in the [README](../README.md) without changing the project's checked-in Gradle configuration.

## Required toolchain

| Component | Project requirement |
|---|---|
| JDK | 17 recommended for Android Gradle Plugin 8.11.1 |
| Gradle | 8.13 through the checked-in wrapper |
| Android SDK | Platform 36 |
| Android build tools | Installed through the Android SDK Manager |
| Android NDK | `26.1.10909125` |
| Minimum device API | 24 |

Use the wrapper (`./gradlew` or `gradlew.bat`) instead of a separately installed Gradle version. Android Studio can install the required SDK and NDK components from **Tools → SDK Manager**.

## Local SDK configuration

Android Studio normally writes the local Android SDK path to the ignored `local.properties` file. For command-line setup, copy the safe template:

```bash
cp local.properties.example local.properties
```

Replace the placeholder with the SDK path for your machine. Never commit `local.properties`; it is machine-specific.

You can also provide `ANDROID_HOME` in your shell environment. Confirm that the selected Java runtime is appropriate before building:

```bash
java -version
./gradlew --version
```

## Validation commands

Run each check independently so a failure is easy to identify:

```bash
./gradlew --no-daemon :app:testDebugUnitTest
./gradlew --no-daemon :app:lintDebug
./gradlew --no-daemon :app:assembleDebug
./gradlew --no-daemon :app:assembleDebugAndroidTest
```

The generated debug application and instrumentation APKs are local build artifacts under `app/build/outputs/apk/`. They are intentionally ignored and must not be committed.

To execute instrumentation tests rather than only assembling their APK, start an emulator or connect an authorized device and run:

```bash
./gradlew --no-daemon :app:connectedDebugAndroidTest
```

## Common setup failures

### Android SDK location not found

If Gradle cannot locate the SDK, check that either `local.properties` contains a valid `sdk.dir` or `ANDROID_HOME` points to the installed SDK. Do not copy another contributor's absolute path.

### Platform 36 is missing

Install **Android SDK Platform 36** from the SDK Manager. Then confirm the platform exists under the SDK's `platforms/` directory and rerun the failed task.

### Required NDK is missing

The project requests NDK `26.1.10909125`. In Android Studio, enable **Show Package Details** on the SDK Tools tab and install that exact side-by-side NDK version. The NDK selection is part of the approved build configuration and should not be changed as a workaround.

### Java or Gradle compatibility error

Confirm that Gradle is running on JDK 17 with `./gradlew --version`. If Android Studio and the terminal select different JDKs, align the IDE Gradle JDK and the shell's Java environment, then retry.

### Gradle wrapper is not executable

On macOS or Linux, restore the executable bit locally:

```bash
chmod +x gradlew
```

The repository already tracks the wrapper as executable; a filesystem or archive transfer can occasionally drop that permission.

### Dependencies cannot be resolved

Confirm network access to Google Maven, Maven Central, and the Gradle Plugin Portal. Retry after transient service failures. Avoid adding unapproved repositories or changing dependency versions solely to bypass a local network problem.

### No connected device for instrumentation tests

`assembleDebugAndroidTest` compiles the test APK without a device. `connectedDebugAndroidTest` requires an emulator or physical device visible to Android Debug Bridge. Check with:

```bash
adb devices
```

Authorize the device or finish booting the emulator before retrying.

### Stale generated output

If a task fails after SDK or JDK setup changes, first stop Gradle daemons and rebuild generated output:

```bash
./gradlew --stop
./gradlew clean
```

Then rerun the smallest failing validation command. These commands affect only local generated state.

## Safe repository hygiene

Before opening a pull request, verify that the working tree contains no machine-specific or generated files:

```bash
git status --short
```

Do not stage SDK paths, `local.properties`, signing configuration, keystores, credentials, APKs, AABs, Gradle caches, or `app/build/`. The public debug workflow does not require release-signing material.
