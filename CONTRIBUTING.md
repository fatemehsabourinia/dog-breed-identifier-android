# Contributing

Thank you for considering a contribution to Dog Breed Identifier. This public repository focuses on the simplified Android demonstration, on-device TensorFlow Lite integration, tests, and public technical documentation.

## Before opening a change

1. Search existing issues and pull requests to avoid duplicate work.
2. For a substantial change, open an issue describing the problem, proposed scope, and expected user-visible impact before implementation.
3. Keep changes focused. Separate documentation, build tooling, tests, and application changes when they can be reviewed independently.

## Repository boundaries

Contributions must respect the distinction between this public engineering demo and the complete production application. Do not submit:

- production-only UI source, navigation, settings, animations, audio, share, or exit implementations;
- unapproved product artwork or screenshots of the simplified demo UI;
- private keys, signing files, credentials, local SDK paths, APKs, AABs, or build output;
- training datasets, notebooks, checkpoints, or SavedModel exports;
- changes to the bundled model or label order without an agreed model-versioning and validation plan.

Official production screenshots and branding already present under `docs/images/` are presentation assets and remain subject to the terms described in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Development setup

Use JDK 17 and Android SDK Platform 36. Let Android Studio generate the ignored `local.properties`, or copy `local.properties.example` and replace its placeholder with your local SDK path.

Run the public validation suite before submitting a pull request:

```bash
./gradlew --no-daemon :app:testDebugUnitTest
./gradlew --no-daemon :app:lintDebug
./gradlew --no-daemon :app:assembleDebug
./gradlew --no-daemon :app:assembleDebugAndroidTest
```

Instrumentation tests require an Android device or emulator and can be run separately with:

```bash
./gradlew --no-daemon :app:connectedDebugAndroidTest
```

## Change quality

- Preserve package identity and existing model input/output contracts unless a maintainer has agreed to a versioned migration.
- Add or update tests when behavior changes.
- Keep documentation accurate to the checked-in implementation.
- Use clear commit messages that explain one coherent change.
- Avoid drive-by formatting or unrelated dependency updates.
- Confirm `git status` contains no local configuration, generated artifacts, or sensitive files before committing.

## Pull requests

A useful pull request includes:

- a concise problem statement and summary of the solution;
- the validation commands run and their results;
- screenshots only when an approved public UI change genuinely requires them;
- linked issues or design discussion where applicable;
- explicit notes about model, privacy, permission, or compatibility implications.

By contributing, you agree that your original contribution may be distributed under the repository's [MIT License](LICENSE). Materials governed by separate terms must not be submitted without the necessary rights.

Please follow the [Code of Conduct](CODE_OF_CONDUCT.md) in all project spaces. Security vulnerabilities should be reported through [SECURITY.md](SECURITY.md), not a public issue.
