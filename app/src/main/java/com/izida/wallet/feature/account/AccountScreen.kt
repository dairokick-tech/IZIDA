package com.izida.wallet.feature.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.account.AccountRepository
import com.izida.wallet.domain.account.IzidaAccount
import java.util.Locale

@Composable
fun AccountScreen(
    repository: AccountRepository,
    userId: String = "USR-DEMO-000001",
    onMovements: () -> Unit,
    onBack: () -> Unit
) {
    var account by remember { mutableStateOf<IzidaAccount?>(null) }
    LaunchedEffect(userId) { account = repository.getAccount(userId) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Spacer(Modifier.height(8.dp))
        Text("Mi cuenta IZIDA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(18.dp))

        account?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Saldo disponible", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it.currency + " " + String.format(Locale.US, "%.2f", it.availableBalance),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Cuenta: " + it.id)
                    Text("Estado: " + it.status.name)
                }
            }
        } ?: CircularProgressIndicator()

        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onMovements, modifier = Modifier.fillMaxWidth()) {
            Text("Ver movimientos")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "En producción, el saldo será autoritativo del backend/ledger. La app no podrá modificarlo directamente.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
