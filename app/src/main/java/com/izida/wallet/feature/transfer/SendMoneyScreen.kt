package com.izida.wallet.feature.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.remote.IzidaApi
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

private enum class SendStep { RECIPIENT, AMOUNT, CONFIRM, RESULT }

@Composable
fun SendMoneyScreen(api: IzidaApi, onBack: () -> Unit) {
    var step by remember { mutableStateOf(SendStep.RECIPIENT) }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf<com.izida.wallet.data.remote.RecipientResponse?>(null) }
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
                    label = { Text("Número celular") },
                    placeholder = { Text("9 dígitos") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        loading = true; message = null
                        scope.launch {
                            runCatching { api.findRecipient(phone) }
                                .onSuccess { found -> recipient = found; step = SendStep.AMOUNT }
                                .onFailure { message = it.message ?: "No se encontró el destinatario." }
                            loading = false
                        }
                    },
                    enabled = phone.length == 9 && !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Buscando…" else "Buscar destinatario") }
                message?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
            }

            SendStep.AMOUNT -> {
                Text("Destinatario: " + recipient!!.displayName)
                Text("Teléfono: " + recipient!!.phone)
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
                            step = SendStep.CONFIRM; message = null
                        } else message = "Ingresa un monto válido."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continuar") }
                message?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
            }

            SendStep.CONFIRM -> {
                Text("Confirmar envío", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("Destinatario: " + recipient!!.displayName)
                Text("Monto: PEN " + amount)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        loading = true; message = null
                        scope.launch {
                            runCatching { api.transfer(recipient!!.phone, amount, UUID.randomUUID().toString()) }
                                .onSuccess { result -> transactionId = result.transactionId; step = SendStep.RESULT }
                                .onFailure { message = it.message ?: "No se pudo procesar la transferencia." }
                            loading = false
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Procesando…" else "Confirmar operación") }
                message?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
            }

            SendStep.RESULT -> {
                Text("Operación procesada", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("ID: " + transactionId)
                Text("Monto: PEN " + amount)
                Text("El saldo y el historial se actualizarán desde PostgreSQL.")
                Spacer(Modifier.height(18.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            }
        }
    }
}
