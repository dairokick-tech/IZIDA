package com.izida.wallet.feature.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.domain.qr.IzidaQrPayload

@Composable
fun MyQrScreen(onBack: () -> Unit) {
    val payload = remember {
        IzidaQrPayload(
            accountId = "ACC-DEMO-000001",
            displayName = "Usuario IZIDA"
        )
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Mi QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Código de pago IZIDA", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(payload.encode(), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Text("En una siguiente etapa este payload se renderizará como QR visual.")
            }
        }
    }
}

@Composable
fun ScanQrScreen(onBack: () -> Unit) {
    var raw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var payload by remember { mutableStateOf<IzidaQrPayload?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Escanear QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Pega aquí un payload IZIDA para probar el lector mientras se integra la cámara.")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = raw,
            onValueChange = { raw = it; error = null },
            label = { Text("Payload") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                payload = IzidaQrPayload.decode(raw)
                error = if (payload == null) "QR IZIDA no válido." else null
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Validar QR") }

        payload?.let {
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Destinatario")
                    Text(it.displayName, style = MaterialTheme.typography.titleLarge)
                    Text("Cuenta: " + it.accountId)
                    Text("Moneda: " + it.currency)
                    Spacer(Modifier.height(12.dp))
                    Text("El pago se conectará al flujo de confirmación en el siguiente paso.")
                }
            }
        }
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
