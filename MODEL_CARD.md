# Dog Breed Classification Model Card

## Overview

The Dog Breed Identifier public demo includes a TensorFlow Lite image-classification model for on-device dog-breed prediction. The model was trained and exported by the project authors and is bundled with the Android application as `app/src/main/assets/mobilenet_dogbreed.tflite`.

The model produces an estimated visual classification. It is intended as an engineering demonstration and informational tool, not as a veterinary, medical, ancestry, behavioral, or legal identification system.

## Model interface

| Property | Value |
|---|---|
| Inputs | 1 |
| Input shape | `1 × 224 × 224 × 3` |
| Input type | Float32 |
| Channel order | RGB |
| Input normalization | Each channel divided by `255.0` |
| Outputs | 1 |
| Output shape | `1 × 120` |
| Output type | Float32 |
| Label mapping | Ordered entries in `app/src/main/assets/labels.txt` |

The Android demo resizes the selected bitmap to 224 × 224 pixels, writes RGB values to a native-order direct `ByteBuffer`, runs TensorFlow Lite inference, and selects the label corresponding to the maximum output score.

## Intended use

- Demonstrating TensorFlow Lite deployment in an Android application.
- Exploring on-device image classification with gallery and camera input.
- Inspecting prediction scores and local preprocessing/inference latency.
- Supporting technical demonstrations and portfolio discussion of mobile machine-learning integration.

## Out-of-scope use

- Veterinary or medical diagnosis.
- Breed certification, pedigree, ancestry, or legal classification.
- Safety, insurance, housing, breeding, or behavioral decisions.
- Identifying an individual animal or its owner.
- Any use requiring guaranteed or independently certified accuracy.

## Training-data provenance

Training used the [Kaggle Dog Breed Identification competition dataset](https://www.kaggle.com/c/dog-breed-identification), which is based on the canine subset associated with [Stanford Dogs](https://vision.stanford.edu/aditya86/ImageNetDogs/) and [ImageNet](https://www.image-net.org/).

The training dataset and source images are not redistributed in this repository. Users must obtain data from the original providers and comply with their terms.

## Limitations

- The output covers only the 120 labels bundled with the application.
- Mixed-breed dogs may not correspond to a single available label.
- Predictions can be affected by pose, framing, lighting, background, occlusion, image quality, and visual similarity between breeds.
- Output scores should be interpreted as model estimates rather than proof of breed identity.
- Performance and acceleration behavior can vary by Android device and available NNAPI drivers.
- This repository does not publish a formal fairness, calibration, or production-accuracy certification for the model.

## Privacy

Inference in the public demo runs on the Android device and does not use a remote prediction service. See [PRIVACY.md](PRIVACY.md) for image handling, permissions, benchmark data, and repository scope.

## Licensing and attribution

The model was created by the project authors. This model card does not itself grant reuse or redistribution rights. Refer to the repository's licensing status and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) before using the model or related assets.
