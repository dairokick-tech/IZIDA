package com.izida.wallet.feature.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.ledger.InMemoryLedger
import com.izida.wallet.domain.ledger.TransactionService
import com.izida.wallet.domain.qr.IzidaQrPayload
import com.izida.wallet.domain.receipt.PaymentReceipt
import com.izida.wallet.feature.receipt.ReceiptScreen
import java.math.BigDecimal
import java.util.UUID

private enum class PaymentStep { SCAN, AMOUNT, CONFIRM, RESULT }

@Composable
fun QrPaymentScreen(
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(PaymentStep.SCAN) }
    var raw by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<IzidaQrPayload?>(null) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var transactionId by remember { mutableStateOf<String?>(null) }
    var receipt by remember { mutableStateOf<PaymentReceipt?>(null) }

    val ledger = remember { InMemoryLedger() }
    val transactionService = remember { TransactionService(ledger) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Pagar con QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        when (step) {
            PaymentStep.SCAN -> {
                Text("Escanea el QR de un comercio o usuario IZIDA.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = raw,
                    onValueChange = { raw = it; error = null },
                    label = { Text("Payload QR") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        payload = IzidaQrPayload.decode(raw)
                        error = if (payload == null) "QR IZIDA no válido." else null
                        if (payload != null) step = PaymentStep.AMOUNT
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Validar QR") }
            }

            PaymentStep.AMOUNT -> {
                Text("Destinatario", style = MaterialTheme.typography.labelLarge)
                Text(payload!!.displayName, style = MaterialTheme.typography.titleLarge)
                Text("Cuenta: " + payload!!.accountId)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { c -> c.isDigit() || c == '.' }.take(12)
                        error = null
                    },
                    label = { Text("Monto en PEN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val value = amount.toBigDecimalOrNull()
                        if (value != null && value > BigDecimal.ZERO) {
                            step = PaymentStep.CONFIRM
                        } else {
                            error = "Ingresa un monto válido."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
            }

            PaymentStep.CONFIRM -> {
                Text("Confirmar pago", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text("Destinatario: " + payload!!.displayName)
                Text("Monto: PEN " + amount)
                Spacer(Modifier.height(8.dp))
                Text("La autorización financiera debe confirmarse antes de ejecutar el débito.")
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        val result = transactionService.transfer(
                            sourceAccountId = "ACC-DEMO-000001",
                            destinationAccountId = payload!!.accountId,
                            amount = amount.toBigDecimal(),
                            currency = payload!!.currency,
                            idempotencyKey = "QR-" + UUID.randomUUID(),
                            reference = "QR_PAYMENT"
                        )
                        result.onSuccess {
                            transactionId = it.id
                            receipt = PaymentReceipt(
                                transactionId = it.id,
                                operationType = "QR_PAYMENT",
                                recipientName = payload!!.displayName,
                                destinationAccountId = payload!!.accountId,
                                amount = amount.toBigDecimal(),
                                currency = payload!!.currency,
                                status = it.status.name,
                                createdAt = it.createdAt,
                                reference = it.reference
                            )
                            step = PaymentStep.RESULT
                        }.onFailure {
                            error = it.message ?: "No se pudo procesar el pago."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Autorizar pago") }
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            PaymentStep.RESULT -> {
                receipt?.let { currentReceipt ->
                    ReceiptScreen(receipt = currentReceipt, onBack = onBack)
                }
            }
        }

        error?.takeIf { step != PaymentStep.CONFIRM }?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
