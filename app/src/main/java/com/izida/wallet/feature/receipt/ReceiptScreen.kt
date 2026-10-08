package com.izida.wallet.feature.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.domain.receipt.PaymentReceipt
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ReceiptScreen(
    receipt: PaymentReceipt,
    onBack: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Comprobante", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("IZIDA", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text("Estado: " + receipt.status)
                Text("Operación: " + receipt.operationType)
                Text("Destinatario: " + receipt.recipientName)
                Text("Cuenta: " + receipt.destinationAccountId)
                Text("Monto: " + receipt.currency + " " + receipt.amount)
                Text("Fecha: " + formatter.format(receipt.createdAt))
                Text("ID: " + receipt.transactionId)
                receipt.reference?.let { Text("Referencia: " + it) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Este comprobante es informativo. La confirmación definitiva debe provenir del backend.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
