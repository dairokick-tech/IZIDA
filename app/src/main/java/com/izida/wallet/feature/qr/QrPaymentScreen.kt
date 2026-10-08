package com.izida.wallet.feature.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.remote.IzidaApi
import com.izida.wallet.domain.qr.IzidaQrPayload
import com.izida.wallet.domain.receipt.PaymentReceipt
import com.izida.wallet.feature.receipt.ReceiptScreen
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

private enum class PaymentStep { AMOUNT, CONFIRM, RESULT }

@Composable
fun QrPaymentScreen(api: IzidaApi, payload: IzidaQrPayload, onBack: () -> Unit) {
    var step by remember { mutableStateOf(PaymentStep.AMOUNT) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var receipt by remember { mutableStateOf<PaymentReceipt?>(null) }
    var idempotencyKey by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Pagar con QR", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Text("Destinatario", style = MaterialTheme.typography.labelLarge)
        Text(payload.displayName, style = MaterialTheme.typography.titleLarge)
        Text("Cuenta: " + payload.accountId)
        Spacer(Modifier.height(16.dp))
        if (step == PaymentStep.AMOUNT) {
            OutlinedTextField(value=amount,onValueChange={amount=it.filter{c->c.isDigit()||c=='.'}.take(12);error=null},label={Text("Monto en PEN")},modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick={
                val value=amount.toBigDecimalOrNull()
                if(value!=null && value>BigDecimal.ZERO && value.scale()<=2) step=PaymentStep.CONFIRM; idempotencyKey=UUID.randomUUID().toString() else error="Ingresa un monto válido (máximo 2 decimales)."
            },modifier=Modifier.fillMaxWidth()){Text("Continuar")}
        } else if(step==PaymentStep.CONFIRM) {
            Text("Confirmar pago",style=MaterialTheme.typography.titleLarge)
            Text("Destinatario: "+payload.displayName)
            Text("Monto: PEN "+amount)
            Spacer(Modifier.height(18.dp))
            Button(enabled=!loading,onClick={
                loading=true; error=null
                scope.launch {
                    runCatching { api.payQr(payload.accountId,amount,idempotencyKey ?: UUID.randomUUID().toString(),payload.currency) }
                        .onSuccess {
                            receipt=PaymentReceipt(it.transactionId,"QR_PAYMENT",payload.displayName,payload.accountId,amount.toBigDecimal(),it.currency,it.status,java.time.Instant.now(),"QR_PAYMENT")
                            step=PaymentStep.RESULT
                        }
                        .onFailure { error=it.message ?: "No se pudo procesar el pago QR." }
                    loading=false
                }
            },modifier=Modifier.fillMaxWidth()){Text(if(loading)"Procesando..." else "Autorizar pago")}
            error?.let{Spacer(Modifier.height(12.dp));Text(it,color=MaterialTheme.colorScheme.error)}
        } else {
            receipt?.let { ReceiptScreen(receipt=it,onBack=onBack) }
        }
        if(step==PaymentStep.AMOUNT) error?.let{Spacer(Modifier.height(12.dp));Text(it,color=MaterialTheme.colorScheme.error)}
    }
}