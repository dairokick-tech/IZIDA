package com.izida.wallet.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.account.AccountRepository
import java.util.Locale

@Composable
fun HomeScreen(
    onAccount: () -> Unit = {},
    onSend: () -> Unit = {},
    onReceive: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSecurity: () -> Unit = {},
    repository: AccountRepository? = null,
    accountId: String = "00000000-0000-0000-0000-000000000001"
) {
    var balance by remember { mutableStateOf("0.00") }
    var status by remember { mutableStateOf("Conectando…") }

    LaunchedEffect(repository, accountId) {
        if (repository == null) {
            status = "Modo local"
        } else {
            runCatching { repository.getAccount(accountId) }
                .onSuccess {
                    balance = it.availableBalance.setScale(2).toPlainString()
                    status = "Cuenta sincronizada"
                }
                .onFailure { status = "Sin conexión al backend" }
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("IZIDA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Saldo disponible", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text("PEN $balance", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(6.dp))
                Text(status)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onSend, modifier = Modifier.weight(1f)) { Text("Enviar") }
            OutlinedButton(onClick = onReceive, modifier = Modifier.weight(1f)) { Text("Recibir") }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onScanQr, modifier = Modifier.fillMaxWidth()) { Text("Escanear QR") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAccount, modifier = Modifier.fillMaxWidth()) { Text("Mi cuenta y movimientos") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSecurity, modifier = Modifier.fillMaxWidth()) { Text("Centro de seguridad") }
    }
}
