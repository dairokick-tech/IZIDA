package com.izida.wallet.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.domain.ledger.Ledger

@Composable
fun HomeScreen(
    onAccount: () -> Unit = {},
    onSend: () -> Unit = {},
    onReceive: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSecurity: () -> Unit = {},
    ledger: Ledger? = null
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("IZIDA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Saldo disponible", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text("PEN 0.00", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(6.dp))
                Text("Cuenta preparada. El saldo real será controlado por el backend/ledger.")
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onSend, modifier = Modifier.weight(1f)) { Text("Enviar") }
            OutlinedButton(onClick = onReceive, modifier = Modifier.weight(1f)) { Text("Recibir") }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onScanQr, modifier = Modifier.fillMaxWidth()) {
            Text("Escanear QR")
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAccount, modifier = Modifier.fillMaxWidth()) {
            Text("Mi cuenta y movimientos")
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSecurity, modifier = Modifier.fillMaxWidth()) {
            Text("Centro de seguridad")
        }
    }
}
