# Third-Party Notices

Dog Breed Identifier uses open-source Android and machine-learning libraries. Those components remain subject to their own licences and notices.

## Runtime libraries

- [AndroidX](https://github.com/androidx/androidx) components, including Activity, Core, Lifecycle, Compose, Material 3, and test libraries — Apache License 2.0 unless otherwise identified by the component.
- [TensorFlow Lite](https://github.com/tensorflow/tensorflow) runtime and GPU libraries — Apache License 2.0.
- [TensorFlow Lite Support](https://github.com/tensorflow/tflite-support) — Apache License 2.0.

## Development and test tooling

- [Kotlin](https://github.com/JetBrains/kotlin) — Apache License 2.0.
- [Gradle](https://github.com/gradle/gradle) — Apache License 2.0.
- [JUnit 4](https://github.com/junit-team/junit4) — Eclipse Public License 1.0.
- [Espresso](https://developer.android.com/training/testing/espresso) and AndroidX Test — Apache License 2.0 unless otherwise identified by the component.

The complete dependency versions are declared in `gradle/libs.versions.toml` and `app/build.gradle.kts`. Transitive dependencies may carry additional notices; consult the dependency artifacts and their upstream projects when preparing a distributed binary.

## Model and data provenance

The bundled TensorFlow Lite model was trained and exported by the project authors. Training used the Kaggle Dog Breed Identification competition dataset, which is based on the canine subset associated with Stanford Dogs and ImageNet. The original training images and dataset archives are not redistributed in this repository.

Dataset access and use remain subject to the terms of their original providers:

- [Kaggle Dog Breed Identification](https://www.kaggle.com/c/dog-breed-identification)
- [Stanford Dogs](https://vision.stanford.edu/aditya86/ImageNetDogs/)
- [ImageNet](https://www.image-net.org/)

## Repository licensing

Original public source code and public documentation in this repository are available under the [MIT License](LICENSE).

The MIT License does not automatically relicense the bundled model, pretrained components or weights, datasets, application logo, branding, Google Play screenshots, or third-party libraries and dependencies. Those materials remain subject to applicable separate terms.
