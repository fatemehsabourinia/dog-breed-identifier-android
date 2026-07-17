package com.zaeri.sabourinia.dogbreedidentifier.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileWriter
import org.tensorflow.lite.Interpreter
import java.util.concurrent.atomic.AtomicBoolean

object BenchmarkLogger {
    private const val TAG = "DogBenchmark"
    private const val MODEL_NAME = "mobilenet_dogbreed.tflite"
    private const val LABELS_NAME = "labels.txt"
    private const val BENCHMARK_IMAGE_DIR = "benchmark_images"
    private const val WARM_UP_RUNS = 10
    private const val TIMED_RUNS_PER_IMAGE = 10
    private const val BENCHMARK_REPETITIONS = 3
    private const val DELAY_BETWEEN_REPETITIONS_MS = 120_000L

    private val benchmarkStarted = AtomicBoolean(false)

    fun logPredictionPerformance(
        preprocessingTimeMs: Double,
        inferenceTimeMs: Double,
        totalPredictionTimeMs: Double
    ) {
        val usedMemoryMb = getUsedMemoryMb()

        Log.d(TAG, "Single prediction preprocessing time: %.3f ms".format(preprocessingTimeMs))
        Log.d(TAG, "Single prediction inference time: %.3f ms".format(inferenceTimeMs))
        Log.d(TAG, "Single prediction total time: %.3f ms".format(totalPredictionTimeMs))
        Log.d(TAG, "Used memory after single prediction: %.2f MB".format(usedMemoryMb))
    }

    fun runAssetBenchmarkOnce(context: Context, inputSize: Int = 224) {
        if (!isDebugBuild(context)) {
            return
        }

        if (!benchmarkStarted.compareAndSet(false, true)) {
            Log.d(TAG, "Deployment benchmark already started in this app session.")
            return
        }

        Thread {
            runAssetBenchmark(context.applicationContext, inputSize)
        }.start()
    }

    private fun isDebugBuild(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun runAssetBenchmark(context: Context, inputSize: Int) {
        try {
            val imageFiles = context.assets.list(BENCHMARK_IMAGE_DIR)
                ?.filter { it.endsWith(".jpg", ignoreCase = true) || it.endsWith(".jpeg", ignoreCase = true) || it.endsWith(".png", ignoreCase = true) }
                ?.sorted()
                ?: emptyList()

            if (imageFiles.isEmpty()) {
                Log.d(TAG, "No benchmark images found in assets/$BENCHMARK_IMAGE_DIR")
                return
            }

            val options = Interpreter.Options().apply { setNumThreads(4) }
            val interpreter = Interpreter(loadModelFile(context, MODEL_NAME), options)
            val labels = loadLabels(context, LABELS_NAME)

            Log.d(TAG, "Deployment benchmark started.")
            Log.d(TAG, "Benchmark images: ${imageFiles.size}")
            Log.d(TAG, "Warm-up runs: $WARM_UP_RUNS")
            Log.d(TAG, "Timed runs per image: $TIMED_RUNS_PER_IMAGE")
            Log.d(TAG, "Benchmark repetitions: $BENCHMARK_REPETITIONS")
            Log.d(TAG, "Total timed runs per repetition: ${imageFiles.size * TIMED_RUNS_PER_IMAGE}")
            Log.d(TAG, "Total timed runs overall: ${imageFiles.size * TIMED_RUNS_PER_IMAGE * BENCHMARK_REPETITIONS}")

            repeat(WARM_UP_RUNS) { index ->
                val imageName = imageFiles[index % imageFiles.size]
                val bitmap = loadBenchmarkBitmap(context, imageName)
                val inputBuffer = preprocessBitmap(bitmap, inputSize)
                val output = Array(1) { FloatArray(labels.size) }
                interpreter.run(inputBuffer, output)
            }

            val preprocessingTimes = mutableListOf<Double>()
            val inferenceTimes = mutableListOf<Double>()
            val totalTimes = mutableListOf<Double>()
            val csvRows = mutableListOf<String>()
            var runIndex = 1

            csvRows.add(
                "benchmark_run,run_index,image_name,preprocessing_ms,inference_ms,end_to_end_ms"
            )

            for (benchmarkRun in 1..BENCHMARK_REPETITIONS) {
                if (benchmarkRun > 1) {
                    Log.d(TAG, "Waiting 2 minutes before benchmark repetition $benchmarkRun")
                    Thread.sleep(DELAY_BETWEEN_REPETITIONS_MS)
                }

                Log.d(TAG, "Starting benchmark repetition $benchmarkRun of $BENCHMARK_REPETITIONS")

                for (imageName in imageFiles) {
                    val bitmap = loadBenchmarkBitmap(context, imageName)

                    repeat(TIMED_RUNS_PER_IMAGE) {
                        val totalStartTime = System.nanoTime()

                        val preprocessingStartTime = System.nanoTime()
                        val inputBuffer = preprocessBitmap(bitmap, inputSize)
                        val preprocessingEndTime = System.nanoTime()

                        val output = Array(1) { FloatArray(labels.size) }

                        val inferenceStartTime = System.nanoTime()
                        interpreter.run(inputBuffer, output)
                        val inferenceEndTime = System.nanoTime()

                        val totalEndTime = System.nanoTime()

                        val preprocessingTimeMs =
                            (preprocessingEndTime - preprocessingStartTime) / 1_000_000.0
                        val inferenceTimeMs =
                            (inferenceEndTime - inferenceStartTime) / 1_000_000.0
                        val totalTimeMs =
                            (totalEndTime - totalStartTime) / 1_000_000.0

                        preprocessingTimes.add(preprocessingTimeMs)
                        inferenceTimes.add(inferenceTimeMs)
                        totalTimes.add(totalTimeMs)

                        csvRows.add(
                            "%d,%d,%s,%.3f,%.3f,%.3f".format(
                                benchmarkRun,
                                runIndex,
                                imageName,
                                preprocessingTimeMs,
                                inferenceTimeMs,
                                totalTimeMs
                            )
                        )

                        runIndex += 1
                    }
                }
            }

            val usedMemoryMb = getUsedMemoryMb()
            val csvFile = saveBenchmarkCsv(context, csvRows)

            Log.d(TAG, "Deployment benchmark completed.")
            Log.d(TAG, "Mean preprocessing time: %.3f ms".format(preprocessingTimes.average()))
            Log.d(TAG, "Mean inference time: %.3f ms".format(inferenceTimes.average()))
            Log.d(TAG, "p50 inference time: %.3f ms".format(percentile(inferenceTimes, 50.0)))
            Log.d(TAG, "p90 inference time: %.3f ms".format(percentile(inferenceTimes, 90.0)))
            Log.d(TAG, "Mean end-to-end prediction time: %.3f ms".format(totalTimes.average()))
            Log.d(TAG, "p50 end-to-end prediction time: %.3f ms".format(percentile(totalTimes, 50.0)))
            Log.d(TAG, "p90 end-to-end prediction time: %.3f ms".format(percentile(totalTimes, 90.0)))
            Log.d(TAG, "Used memory after benchmark: %.2f MB".format(usedMemoryMb))
            Log.d(TAG, "Raw benchmark CSV saved to: ${csvFile.absolutePath}")

            interpreter.close()
        } catch (exception: Exception) {
            Log.e(TAG, "Deployment benchmark failed: ${exception.message}", exception)
        }
    }

    private fun saveBenchmarkCsv(context: Context, rows: List<String>): File {
        val deviceName = buildSafeDeviceName()
        val outputFile = File(
            context.filesDir,
            "dog_benchmark_${deviceName}.csv"
        )

        FileWriter(outputFile, false).use { writer ->
            rows.forEach { row ->
                writer.append(row)
                writer.append('\n')
            }
        }

        return outputFile
    }

    private fun buildSafeDeviceName(): String {
        val rawDeviceName = "${Build.MANUFACTURER}_${Build.MODEL}"
        return rawDeviceName
            .replace(Regex("[^A-Za-z0-9_]+"), "_")
            .trim('_')
    }

    private fun loadBenchmarkBitmap(context: Context, imageName: String) =
        context.assets.open("$BENCHMARK_IMAGE_DIR/$imageName").use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }

    private fun percentile(values: List<Double>, percentile: Double): Double {
        if (values.isEmpty()) return 0.0

        val sortedValues = values.sorted()
        val index = ((percentile / 100.0) * (sortedValues.size - 1)).toInt()
        return sortedValues[index]
    }

    private fun getUsedMemoryMb(): Double {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
    }
}