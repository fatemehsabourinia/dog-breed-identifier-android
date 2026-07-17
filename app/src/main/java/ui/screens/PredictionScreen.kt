package com.zaeri.sabourinia.dogbreedidentifier.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

private const val MODEL_NAME = "mobilenet_dogbreed.tflite"
private const val LABELS_NAME = "labels.txt"
private const val INPUT_SIZE = 224

@Composable
fun DogBreedDemoScreen() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var breed by remember { mutableStateOf<String?>(null) }
    var confidence by remember { mutableStateOf<Float?>(null) }
    var preprocessingMs by remember { mutableStateOf<Double?>(null) }
    var inferenceMs by remember { mutableStateOf<Double?>(null) }
    var totalMs by remember { mutableStateOf<Double?>(null) }
    var useNnapi by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun clearResult() {
        breed = null
        confidence = null
        preprocessingMs = null
        inferenceMs = null
        totalMs = null
        errorMessage = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        clearResult()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
            clearResult()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraImageUri = createImageUri(context)
            cameraLauncher.launch(cameraImageUri)
        } else {
            errorMessage = "Camera permission is required to capture an image."
        }
    }

    val bitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dog Breed Inference Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "A minimal on-device TensorFlow Lite portfolio interface. Images remain on this device.",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) { Text("Select image") }
            OutlinedButton(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.weight(1f)
            ) { Text("Use camera") }
        }

        bitmap?.let { selectedBitmap ->
            Image(
                bitmap = selectedBitmap.asImageBitmap(),
                contentDescription = "Selected dog",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("NNAPI acceleration", fontWeight = FontWeight.Medium)
                    Text("Applied to the next inference", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = useNnapi, onCheckedChange = { useNnapi = it })
            }

            Button(
                onClick = {
                    try {
                        val options = Interpreter.Options().apply {
                            setNumThreads(4)
                            setUseNNAPI(useNnapi)
                        }
                        Interpreter(loadModelFile(context, MODEL_NAME), options).use { model ->
                            val labels = loadLabels(context, LABELS_NAME)
                            val totalStartTime = System.nanoTime()
                            val preprocessingStartTime = System.nanoTime()
                            val inputBuffer = preprocessBitmap(selectedBitmap, INPUT_SIZE)
                            val preprocessingEndTime = System.nanoTime()
                            val output = Array(1) { FloatArray(labels.size) }
                            val inferenceStartTime = System.nanoTime()
                            model.run(inputBuffer, output)
                            val inferenceEndTime = System.nanoTime()
                            val totalEndTime = System.nanoTime()

                            preprocessingMs = (preprocessingEndTime - preprocessingStartTime) / 1_000_000.0
                            inferenceMs = (inferenceEndTime - inferenceStartTime) / 1_000_000.0
                            totalMs = (totalEndTime - totalStartTime) / 1_000_000.0
                            BenchmarkLogger.logPredictionPerformance(
                                preprocessingTimeMs = preprocessingMs!!,
                                inferenceTimeMs = inferenceMs!!,
                                totalPredictionTimeMs = totalMs!!
                            )
                            BenchmarkLogger.runAssetBenchmarkOnce(context, INPUT_SIZE)

                            val maxIndex = output[0].indices.maxByOrNull { output[0][it] }
                                ?: error("Model returned no predictions")
                            breed = labels[maxIndex]
                            confidence = output[0][maxIndex] * 100
                            errorMessage = null
                        }
                    } catch (exception: Exception) {
                        errorMessage = exception.message ?: "Inference failed"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Run inference") }
        }

        if (breed != null && confidence != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Prediction", style = MaterialTheme.typography.titleMedium)
                    Text(breed!!, style = MaterialTheme.typography.headlineSmall)
                    Text("Confidence: %.2f%%".format(confidence))
                    LinearProgressIndicator(
                        progress = { (confidence!! / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider()
                    Text("Preprocessing: %.2f ms".format(preprocessingMs))
                    Text("Inference: %.2f ms".format(inferenceMs))
                    Text("End to end: %.2f ms".format(totalMs))
                    Text(
                        if (useNnapi) "Acceleration: NNAPI requested" else "Acceleration: CPU (4 threads)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (selectedImageUri != null) {
            OutlinedButton(
                onClick = {
                    selectedImageUri = null
                    clearResult()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Portfolio demo — predictions are informational only.",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun createImageUri(context: Context): Uri {
    val imageFile = File(
        context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
        "photo_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
}

fun loadModelFile(context: Context, modelName: String): ByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

fun loadLabels(context: Context, fileName: String): List<String> {
    return context.assets.open(fileName).bufferedReader().readLines()
}

fun preprocessBitmap(bitmap: Bitmap, imageSize: Int): ByteBuffer {
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
    val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until imageSize) {
        for (x in 0 until imageSize) {
            val pixel = scaledBitmap.getPixel(x, y)
            byteBuffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixel and 0xFF) / 255.0f))
        }
    }
    return byteBuffer
}
