# Dog Breed Identifier — Android + TensorFlow Lite

<p align="center">
  <img src="docs/images/app-logo.png" alt="Dog Breed Identifier app logo" width="160">
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.zaeri.sabourinia.dogbreedidentifier"><strong>View Dog Breed Identifier on Google Play</strong></a>
</p>

An Android AI application demonstrating on-device dog-breed classification with Kotlin, Jetpack Compose, and TensorFlow Lite. Users can select a gallery image or capture a photo, run local inference, and inspect the predicted breed, confidence, preprocessing time, inference time, and total latency.

The complete production application is available on [Google Play](https://play.google.com/store/apps/details?id=com.zaeri.sabourinia.dogbreedidentifier), published by **Alireza Zaeri & Fatemeh Sabourinia**. This public repository contains a simplified demonstration interface that presents the core Android and on-device AI engineering workflow. The production UI source code, custom visual design, animations, audio, and proprietary presentation assets are intentionally not included. Official application screenshots are provided only to demonstrate the complete published user experience.

## Repository scope

The repository includes the Android inference workflow, model and labels, camera and gallery input, `FileProvider` integration, debug benchmarking, tests, and a deliberately simplified Compose UI. It does not include the complete production interface, production navigation or settings, product-only animation or audio, or production share and exit implementations.

The logo and screenshots under `docs/images/` are approved presentation assets from the published application. Screenshots of the simplified public demo are intentionally not included.

## Production application

These images are approved copies from the official Google Play listing. They show the production presentation; the repository intentionally builds the simpler engineering demo described above.

<p align="center">
  <img src="docs/images/google-play/home-screen.png" alt="Production home screen" width="190">
  <img src="docs/images/google-play/prediction-golden-retriever.png" alt="Production golden retriever prediction" width="190">
  <img src="docs/images/google-play/prediction-german-shepherd.png" alt="Production German shepherd prediction" width="190">
</p>

<p align="center">
  <img src="docs/images/google-play/prediction-pomeranian.png" alt="Production Pomeranian prediction" width="190">
  <img src="docs/images/google-play/prediction-high-confidence.png" alt="Production high-confidence prediction" width="190">
  <img src="docs/images/google-play/prediction-controls.png" alt="Production image input and reset controls" width="190">
</p>

## What the public demo demonstrates

- Gallery selection and camera capture through Android activity-result contracts.
- Secure camera URI sharing with `FileProvider`.
- Memory-mapped TensorFlow Lite model loading from packaged assets.
- Deterministic image resizing and RGB float preprocessing.
- CPU execution with four threads or optional NNAPI-requested execution.
- Label mapping, maximum-score prediction, and confidence reporting.
- Preprocessing, inference, and end-to-end latency measurement.
- Debug-only repeatable benchmark orchestration and CSV logging.
- Local inference with no account, upload, or remote prediction service.

## Architecture

```text
Gallery / Camera
       │
       ▼
Android ContentResolver + Bitmap decoding
       │
       ▼
Resize to 224 × 224 → RGB normalization to Float32 [0, 1]
       │
       ▼
TensorFlow Lite Interpreter
  ├─ CPU: 4 threads
  └─ NNAPI: user-requested delegate path
       │
       ▼
120-class output → maximum score → labels.txt
       │
       ▼
Breed + confidence + preprocessing/inference/total latency
```

The compact demo keeps UI state in Compose and isolates reusable technical behavior in the inference helpers and `BenchmarkLogger`. Inference failures and denied camera permission are surfaced as visible errors rather than silently ignored.

## Inference workflow

The packaged model expects a `224 × 224` RGB image. The app scales the selected bitmap and writes pixels into a native-order direct `ByteBuffer`, channel by channel:

```text
red   = R / 255.0
green = G / 255.0
blue  = B / 255.0
```

The interpreter produces one float score for each of the 120 labels. The app selects the largest output value, reads the breed at the same index from `labels.txt`, and reports that score as a percentage. This public refinement does not change the model, label order, preprocessing, prediction selection, or confidence calculation.

NNAPI support is device-dependent. Enabling the switch requests NNAPI through TensorFlow Lite; Android may choose device-specific execution depending on available drivers and supported model operations.

## Benchmark workflow

`BenchmarkLogger` preserves warm-up runs, repeated timed inference, percentile reporting, memory measurement, and CSV output to private app storage. The benchmark images used during project evaluation were sourced from the Kaggle Dog Breed Identification dataset and are not redistributed in this repository.

To run the asset benchmark with images you are entitled to use:

1. Add `.jpg`, `.jpeg`, or `.png` files to `app/src/main/assets/benchmark_images/`.
2. Build a debug APK.
3. Run one interactive prediction; the debug-only benchmark starts once per app session.
4. Inspect Logcat with the tag `DogBenchmark`. The CSV is written to the app's private files directory.

The empty directory is represented by its own [benchmark image instructions](app/src/main/assets/benchmark_images/README.md).

## Technology stack

- Kotlin 2.0.21
- Jetpack Compose and Material 3
- Android Gradle Plugin 8.11.1
- Gradle 8.13 wrapper
- TensorFlow Lite 2.16.1
- TensorFlow Lite Support 0.4.4
- TensorFlow Lite GPU runtime 2.16.1
- Android `FileProvider`, camera permission, and activity-result APIs
- JUnit 4 and AndroidX instrumentation test scaffolding

## Project structure

```text
app/src/main/
├── AndroidManifest.xml
├── assets/
│   ├── mobilenet_dogbreed.tflite
│   ├── labels.txt
│   └── benchmark_images/       # user-supplied, rights-cleared test images
├── java/
│   ├── .../MainActivity.kt
│   ├── ui/screens/PredictionScreen.kt
│   └── ui/BenchmarkLogger.kt
└── res/                        # launcher, theme, and FileProvider resources

docs/
└── images/                     # approved logo and official listing screenshots

MODEL_CARD.md                   # model contract, provenance, and limitations
PRIVACY.md                      # public demo privacy details
SECURITY.md                     # security reporting guidance
THIRD_PARTY_NOTICES.md          # dependency and dataset acknowledgements
CONTRIBUTING.md                 # contribution guidelines
CODE_OF_CONDUCT.md              # community standards
docs/BUILDING.md                # build and troubleshooting guide
```

## Build

For detailed environment setup and solutions to common SDK, NDK, JDK, Gradle, and device issues, see the [Android build and troubleshooting guide](docs/BUILDING.md).

Requirements:

- Android Studio with JDK 17 or newer supported by the configured Android Gradle Plugin.
- Android SDK Platform 36.
- Android NDK `26.1.10909125`.

Clone the repository, then either let Android Studio create the ignored `local.properties` or copy the safe template and replace its placeholder:

```bash
cp local.properties.example local.properties
```

Build, test, lint, and compile the instrumentation APK with:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

Running instrumentation tests requires a connected Android device or emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

The debug APK is generated locally under `app/build/outputs/apk/debug/` and is ignored by Git. Do not commit SDK paths, signing configuration, keystores, APKs, or App Bundles.

## Dataset attribution

Model training used the [Kaggle Dog Breed Identification competition dataset](https://www.kaggle.com/c/dog-breed-identification), which is based on the canine subset associated with [Stanford Dogs](https://vision.stanford.edu/aditya86/ImageNetDogs/) and [ImageNet](https://www.image-net.org/).

The original training dataset is **not redistributed** in this repository. Obtain data from the original sources and comply with their terms and attribution requirements. The TensorFlow Lite model included in this repository was trained and exported by the project authors.

## Model information

- Asset: `app/src/main/assets/mobilenet_dogbreed.tflite`
- Input: `1 × 224 × 224 × 3` RGB Float32
- Normalization: each channel divided by `255.0`
- Output mapping: 120 entries in `labels.txt`
- Execution: TensorFlow Lite CPU or NNAPI-requested path
- Processing: entirely on device

The result is an estimated visual classification, not veterinary, medical, ancestry, or legal advice. Mixed breeds and image conditions can affect predictions.

See [MODEL_CARD.md](MODEL_CARD.md) for intended use, model inputs and outputs, provenance, and limitations.

## Security and privacy

- Images are processed locally by the demo and are not sent to a prediction server.
- No API key, user account, analytics credential, or cloud configuration is required.
- Local SDK configuration, IDE state, signing files, keystores, build output, APKs, AABs, and logs are ignored.
- Example configuration files contain placeholders only.
- Camera captures use the app-specific external pictures directory exposed through a scoped `FileProvider` URI.

See [PRIVACY.md](PRIVACY.md) for the public demo's image handling, local benchmark data, permissions, and data-collection details.

Security concerns can be reported using the guidance in [SECURITY.md](SECURITY.md).

## Machine-learning repository relationship

This repository focuses on Android deployment and on-device inference. Dataset preparation, model training, evaluation, and TensorFlow Lite export are documented in the companion Machine Learning repository: [Dog Breed Recognition](https://github.com/fatemehsabourinia/dog-breed-recognition).

## Licence

Original public source code and documentation in this repository are available under the [MIT License](LICENSE).

The production user interface, excluded production source, application branding, screenshots, bundled model, pretrained components, datasets, and third-party dependencies may remain subject to separate terms. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Authors

**Alireza Zaeri & Fatemeh Sabourinia**  
[Dog Breed Identifier on Google Play](https://play.google.com/store/apps/details?id=com.zaeri.sabourinia.dogbreedidentifier)
