package com.izida.wallet.feature.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.ledger.InMemoryLedger
import com.izida.wallet.domain.ledger.TransactionService
import com.izida.wallet.domain.transfer.Recipient
import com.izida.wallet.domain.transfer.RecipientDirectory
import java.math.BigDecimal
import java.util.UUID

private enum class SendStep { RECIPIENT, AMOUNT, CONFIRM, RESULT }

@Composable
fun SendMoneyScreen(
    directory: RecipientDirectory,
    ledger: com.izida.wallet.domain.ledger.Ledger,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(SendStep.RECIPIENT) }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf<Recipient?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var transactionId by remember { mutableStateOf<String?>(null) }

    val service = remember(ledger) { TransactionService(ledger) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Spacer(Modifier.height(8.dp))
        Text("Enviar dinero", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        when (step) {
            SendStep.RECIPIENT -> {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter(Char::isDigit).take(9) },
                    label = { Text("Número celular") },
                    placeholder = { Text("9 dígitos") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        recipient = directory.findByPhone(phone)
                        message = if (recipient == null) "No se encontró el destinatario." else null
                        if (recipient != null) step = SendStep.AMOUNT
                    },
                    enabled = phone.length == 9,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Buscar destinatario") }
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            SendStep.AMOUNT -> {
                Text("Destinatario: " + recipient!!.displayName)
                Text("Teléfono: " + recipient!!.phoneNumber)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Monto en PEN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if ((amount.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                            step = SendStep.CONFIRM
                            message = null
                        } else message = "Ingresa un monto válido."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
            }

            SendStep.CONFIRM -> {
                Text("Confirmar envío", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("Destinatario: " + recipient!!.displayName)
                Text("Monto: PEN " + amount)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        val result = service.transfer(
                            sourceAccountId = "ACC-DEMO-000001",
                            destinationAccountId = recipient!!.accountId,
                            amount = amount.toBigDecimal(),
                            currency = "PEN",
                            idempotencyKey = UUID.randomUUID().toString()
                        )
                        result.onSuccess {
                            transactionId = it.id
                            step = SendStep.RESULT
                        }.onFailure {
                            message = it.message
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Confirmar operación") }
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            SendStep.RESULT -> {
                Text("Operación procesada", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("ID: " + transactionId)
                Text("Monto: PEN " + amount)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}
