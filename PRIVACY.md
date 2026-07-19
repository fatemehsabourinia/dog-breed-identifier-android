# Privacy

Dog Breed Identifier performs dog-breed classification on the user's Android device. The public demo does not require an account and does not include advertising, analytics, tracking, or a remote prediction service.

## Images

Images selected through the system gallery flow are opened through a content URI supplied by Android. Images captured with the camera are written to the app-specific external pictures directory and shared with the camera app through Android's `FileProvider` mechanism.

Selected and captured images are processed locally for classification. The app does not upload them to the project authors or to a prediction server. Camera captures can remain in the app-specific pictures directory until the app's data is cleared or the files are otherwise removed by the device or user.

## Benchmark data

Debug builds can run an optional local benchmark when the developer supplies benchmark images in the documented asset directory. Benchmark results are written as a CSV file in the app's private files directory and can include timing measurements and a sanitized device manufacturer/model identifier in the filename. This benchmark does not run in non-debuggable builds.

Android system backup or device-transfer services may handle app-private data according to the device owner's settings and the platform's backup behavior.

## Permissions

The app requests camera permission to capture a new image. It does not request broad storage access. The public demo does not declare the Android Internet permission.

## Data collection and sharing

The public demo does not collect or share personal data with the project authors. It contains no account system, analytics SDK, advertising SDK, or cloud-service configuration.

## Scope

This document describes the public demo source in this repository. The production application is distributed through Google Play and is also subject to the privacy information linked from its store listing.

## Contact

For privacy questions, use the developer contact information provided on the [Google Play listing](https://play.google.com/store/apps/details?id=com.zaeri.sabourinia.dogbreedidentifier).
