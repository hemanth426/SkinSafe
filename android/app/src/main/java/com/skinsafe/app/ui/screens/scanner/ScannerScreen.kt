package com.skinsafe.app.ui.screens.scanner

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.skinsafe.app.ui.components.ErrorBanner
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.SecondaryOutlinedButton
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.ScannerUiState
import com.skinsafe.app.ui.viewmodels.ScannerViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScannerScreen(
    scannerViewModel: ScannerViewModel,
    onNavigateBack: () -> Unit,
    onOcrCompleted: (extractedText: String, ingredients: List<String>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by scannerViewModel.uiState.collectAsState()
    val capturedUri by scannerViewModel.capturedImageUri.collectAsState()
    val capturedFile by scannerViewModel.capturedImageFile.collectAsState()
    val isFlashOn by scannerViewModel.isFlashEnabled.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val file = copyUriToFile(context, uri)
            scannerViewModel.setCapturedImage(uri, file)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uiState) {
        if (uiState is ScannerUiState.OcrSuccess) {
            val success = uiState as ScannerUiState.OcrSuccess
            onOcrCompleted(success.response.extractedText, success.response.cleanedIngredients)
            scannerViewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (capturedUri != null) {
                // Image Preview Mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceWhite),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scannerViewModel.clearCapturedImage() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                        Text(
                            text = "Confirm Photo",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Image
                    Image(
                        painter = rememberAsyncImagePainter(model = capturedUri),
                        contentDescription = "Captured Cosmetic Deck",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // Actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState is ScannerUiState.Error) {
                            ErrorBanner(errorMessage = (uiState as ScannerUiState.Error).message)
                        }

                        PrimaryButton(
                            text = "Extract Ingredients with OCR",
                            isLoading = uiState is ScannerUiState.ProcessingOcr,
                            icon = Icons.Default.DocumentScanner,
                            onClick = {
                                if (capturedFile != null) {
                                    scannerViewModel.processOcr(capturedFile!!)
                                }
                            }
                        )

                        SecondaryOutlinedButton(
                            text = "Retake Photo",
                            icon = Icons.Default.Refresh,
                            onClick = { scannerViewModel.clearCapturedImage() }
                        )
                    }
                }
            } else if (hasCameraPermission) {
                // Live CameraX Viewfinder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                                cameraControl = camera.cameraControl
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Guide & Top Bar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                scannerViewModel.toggleFlash()
                                cameraControl?.enableTorch(!isFlashOn)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Flash",
                                tint = if (isFlashOn) Color.Yellow else Color.White
                            )
                        }
                    }

                    // Center Targeting Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .height(280.dp)
                            .border(BorderStroke(2.dp, TealLight), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Position ingredient list within this frame",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Bottom Controls (Gallery, Shutter)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Button
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick from gallery", tint = Color.White)
                        }

                        // Shutter Button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    takePhoto(context, imageCapture) { uri, file ->
                                        scannerViewModel.setCapturedImage(uri, file)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(TealPrimary)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.size(50.dp))
                    }
                }
            } else {
                // Permission Denied View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceWhite)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Camera Permission Required", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Please grant camera access to photograph ingredient lists on product packaging.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                    PrimaryButton(
                        text = "Grant Permission",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecondaryOutlinedButton(
                        text = "Upload from Gallery Instead",
                        onClick = { galleryLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri, File) -> Unit
) {
    if (imageCapture == null) return

    val photoFile = File(
        context.cacheDir,
        "SCAN_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri, photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}

private fun copyUriToFile(context: Context, uri: Uri): File {
    val file = File(context.cacheDir, "UPLOAD_${System.currentTimeMillis()}.jpg")
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}
