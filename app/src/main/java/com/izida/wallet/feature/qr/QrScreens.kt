package com.izida.wallet.feature.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.remote.IzidaApi
import com.izida.wallet.domain.qr.IzidaQrPayload
import kotlinx.coroutines.launch

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
                    Spacer(Modifier.height(12.dp))
                    Text(it.fullName)
                    Text("Cuenta: " + it.id, style = MaterialTheme.typography.bodySmall)
                    Text("Moneda: " + it.currency, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Text(payload.encode(), style = MaterialTheme.typography.bodySmall)
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
fun ScanQrScreen(onBack: () -> Unit, onPay: (IzidaQrPayload) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
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
            val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
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
                            if (mediaImage != null) {
                                val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { codes ->
                                        val value = codes.firstOrNull()?.rawValue
                                        if (value != null) {
                                            val decoded = IzidaQrPayload.decode(value)
                                            if (decoded != null) onPay(decoded)
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else imageProxy.close()
                        }
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                        } catch (_: Exception) {}
                    }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )
        }
    }
}
