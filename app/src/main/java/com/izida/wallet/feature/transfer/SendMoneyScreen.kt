package com.izida.wallet.feature.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.remote.IzidaApi
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.launch

private enum class SendStep { RECIPIENT, AMOUNT, CONFIRM, RESULT }

@Composable
fun SendMoneyScreen(
    api: IzidaApi,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(SendStep.RECIPIENT) }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var transactionId by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                    label = { Text("Número celular IZIDA") },
                    placeholder = { Text("9 dígitos") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { message = null; step = SendStep.AMOUNT },
                    enabled = phone.length == 9,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
                Text("El destinatario se validará al procesar la transferencia.", style = MaterialTheme.typography.bodySmall)
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            SendStep.AMOUNT -> {
                Text("Destinatario: +51 $phone")
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
                        val value = amount.toBigDecimalOrNull()
                        if (value != null && value > BigDecimal.ZERO && value.scale() <= 2) {
                            message = null
                            step = SendStep.CONFIRM
                        } else message = "Ingresa un monto válido de hasta 2 decimales."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
            }

            SendStep.CONFIRM -> {
                Text("Confirmar envío", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("Celular: +51 $phone")
                Text("Monto: PEN $amount")
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        loading = true
                        message = null
                        val key = UUID.randomUUID().toString()
                        scope.launch {
                            runCatching { api.transfer(phone, amount, key) }
                                .onSuccess {
                                    transactionId = it.transactionId
                                    step = SendStep.RESULT
                                }
                                .onFailure { message = it.message ?: "No se pudo procesar la transferencia." }
                            loading = false
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Procesando…" else "Confirmar operación") }
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            SendStep.RESULT -> {
                Text("Operación procesada", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("ID: $transactionId")
                Text("Monto: PEN $amount")
                Spacer(Modifier.height(18.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}
