package com.avih6.vehiclecheck.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.avih6.vehiclecheck.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScannerDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is androidx.lifecycle.LifecycleOwner) break
            ctx = ctx.baseContext
        }
        ctx as androidx.lifecycle.LifecycleOwner
    }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val textRecognizer = remember {
        try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (t: Throwable) {
            Log.e("CameraScanner", "Failed to initialize TextRecognizer", t)
            null
        }
    }

    if (textRecognizer == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, R.string.ocr_init_failed, Toast.LENGTH_LONG).show()
            onDismiss()
        }
        return
    }

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(6f) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<CameraInfo?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var detectedPlate by remember { mutableStateOf<String?>(null) }
    var isPaused by remember { mutableStateOf(false) }

    var lastMovementTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val delta = Math.abs(x - lastX) + Math.abs(y - lastY) + Math.abs(z - lastZ)
                if (delta > 0.6f) {
                    lastMovementTime = System.currentTimeMillis()
                }
                lastX = x
                lastY = y
                lastZ = z
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            try { textRecognizer.close() } catch (t: Throwable) {}
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        while (detectedPlate == null) {
            kotlinx.coroutines.delay(5000L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastMovementTime > 40000L) {
                Toast.makeText(context, R.string.camera_timeout_msg, Toast.LENGTH_LONG).show()
                onDismiss()
                break
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            val newRatio = (zoomRatio * zoom).coerceIn(minZoomRatio, maxZoomRatio)
                            if (newRatio != zoomRatio) {
                                zoomRatio = newRatio
                                cameraControl?.setZoomRatio(newRatio)
                            }
                        }
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor) { imageProxy ->
                                    if (!isPaused) {
                                        processImageProxy(textRecognizer, imageProxy) { plate ->
                                            detectedPlate = plate
                                            isPaused = true
                                        }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview, imageAnalyzer
                            )
                            cameraControl = camera.cameraControl
                            cameraInfo = camera.cameraInfo

                            camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                                if (state != null) {
                                    minZoomRatio = state.minZoomRatio
                                    maxZoomRatio = state.maxZoomRatio.coerceAtMost(8f)
                                }
                            }
                        } catch (exc: Exception) {
                            Log.e("CameraScanner", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Viewfinder Box in center
            if (detectedPlate == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .height(130.dp)
                        .border(BorderStroke(2.5.dp, Color(0xFFFFD54F)), RoundedCornerShape(16.dp))
                        .background(Color(0x22FFD54F), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "כוון את לוחית הרישוי למסגרת",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Top Actions (Flash and Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Torch toggle
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (isTorchOn) "כבה פנס" else "הדלק פנס",
                        tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White
                    )
                }

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "סגור סורק", tint = Color.White)
                }
            }

            // Zoom Controls
            if (detectedPlate == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                        .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newRatio = (zoomRatio - 0.5f).coerceIn(minZoomRatio, maxZoomRatio)
                            if (newRatio != zoomRatio) {
                                zoomRatio = newRatio
                                cameraControl?.setZoomRatio(newRatio)
                            }
                        }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "הקטן זום", tint = Color.White)
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.1f", zoomRatio)}x",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = {
                            val newRatio = (zoomRatio + 0.5f).coerceIn(minZoomRatio, maxZoomRatio)
                            if (newRatio != zoomRatio) {
                                zoomRatio = newRatio
                                cameraControl?.setZoomRatio(newRatio)
                            }
                        }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "הגדל זום", tint = Color.White)
                        }
                    }
                }
            }

            // Confirmation Popup
            detectedPlate?.let { plate ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.ocr_confirm_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.ocr_confirm_message, plate))
                            Spacer(Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    detectedPlate = null
                                    isPaused = false
                                }) {
                                    Text(stringResource(R.string.btn_cancel))
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { onResult(plate) }) {
                                    Text(stringResource(R.string.btn_check))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    imageProxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        try {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val text = line.text.filter { it.isDigit() }
                            if (text.length in 7..8) {
                                onResult(text)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } catch (e: Exception) {
            imageProxy.close()
        }
    } else {
        imageProxy.close()
    }
}
