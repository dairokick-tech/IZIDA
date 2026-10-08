package com.izida.wallet.feature.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.izida.wallet.data.remote.IzidaApi
import com.izida.wallet.domain.qr.IzidaQrPayload

@Composable
fun MyQrScreen(api: IzidaApi, onBack: () -> Unit) {
    var account by remember { mutableStateOf<com.izida.wallet.data.remote.AccountResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { api.getMyAccount() }
            .onSuccess { account = it }
            .onFailure { error = it.message ?: "No se pudo cargar tu QR." }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Mi QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        account?.let {
            val payload = IzidaQrPayload(accountId = it.id, displayName = it.fullName, currency = it.currency)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Código de pago IZIDA", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    IzidaQrImage(payload.encode())
                    Spacer(Modifier.height(16.dp))
                    Text(it.fullName)
                    Text("Cuenta: " + it.id, style = MaterialTheme.typography.bodySmall)
                    Text("Moneda: " + it.currency, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Text("Este QR identifica tu cuenta IZIDA. El pago se procesa en el backend.")
                }
            }
        }
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun IzidaQrImage(value: String) {
    val bitmap = remember(value) {
        runCatching {
            val matrix: BitMatrix = MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, 720, 720)
            val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        }.getOrNull()
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Código QR de IZIDA",
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
    }
}

@Composable
fun ScanQrScreen(onBack: () -> Unit, onPay: (IzidaQrPayload) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Escanear QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Apunta la cámara al código QR de IZIDA.")
        Spacer(Modifier.height(16.dp))

        if (!hasCameraPermission) {
            Button(
                onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Permitir cámara") }
        } else {
            val cameraProviderFuture = remember(context) {
                androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
            }
            var handled by remember { mutableStateOf(false) }

            AndroidView(
                modifier = Modifier.fillMaxWidth().height(420.dp),
                factory = { ctx ->
                    val previewView = androidx.camera.view.PreviewView(ctx)
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                        val analysis = androidx.camera.core.ImageAnalysis.Builder()
                            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analysis.setAnalyzer(androidx.core.content.ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { codes ->
                                    if (handled) return@addOnSuccessListener
                                    val value = codes.firstOrNull()?.rawValue ?: return@addOnSuccessListener
                                    val decoded = IzidaQrPayload.decode(value) ?: return@addOnSuccessListener
                                    handled = true
                                    onPay(decoded)
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        }

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (_: Exception) {
                            analysis.clearAnalyzer()
                        }
                    }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )
        }
    }
}
