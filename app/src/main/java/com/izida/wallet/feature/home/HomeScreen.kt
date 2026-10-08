package com.izida.wallet.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("IZIDA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Saldo disponible", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text("S/ 0.00", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(6.dp))
                Text("Saldo de demostración; el saldo real dependerá del ledger del backend.")
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Enviar") }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Recibir") }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Escanear QR") }
        Spacer(Modifier.height(28.dp))
        Text("Movimientos recientes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Text("Todavía no hay movimientos.")
    }
}
