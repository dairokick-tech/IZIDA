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
    var raw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var payload by remember { mutableStateOf<IzidaQrPayload?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Escanear QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Valida un QR IZIDA. La cámara se incorporará sin cambiar el flujo financiero.")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = raw,onValueChange = { raw = it; error = null },label = { Text("Payload QR") },modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            payload = IzidaQrPayload.decode(raw)
            error = if (payload == null) "QR IZIDA no válido." else null
        },modifier = Modifier.fillMaxWidth()) { Text("Validar QR") }
        payload?.let {
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Destinatario")
                    Text(it.displayName,style=MaterialTheme.typography.titleLarge)
                    Text("Cuenta: "+it.accountId)
                    Text("Moneda: "+it.currency)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={ onPay(it) },modifier=Modifier.fillMaxWidth()){ Text("Continuar al pago") }
                }
            }
        }
        error?.let { Spacer(Modifier.height(12.dp)); Text(it,color=MaterialTheme.colorScheme.error) }
    }
}