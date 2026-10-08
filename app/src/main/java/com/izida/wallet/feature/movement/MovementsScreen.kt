package com.izida.wallet.feature.movement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izida.wallet.data.account.AccountRepository
import com.izida.wallet.domain.movement.Movement
import com.izida.wallet.domain.movement.MovementType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovementsScreen(
    repository: AccountRepository,
    accountId: String = "ACC-DEMO-000001",
    onBack: () -> Unit
) {
    var movements by remember { mutableStateOf<List<Movement>>(emptyList()) }
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
    }

    LaunchedEffect(accountId) { movements = repository.getMovements(accountId) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Spacer(Modifier.height(8.dp))
        Text("Historial", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (movements.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Sin movimientos")
                    Spacer(Modifier.height(6.dp))
                    Text("Las operaciones registradas aparecerán aquí.")
                }
            }
        } else {
            movements.forEach { movement ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(movement.description, style = MaterialTheme.typography.titleMedium)
                        val sign = if (movement.type == MovementType.CREDIT) "+" else "-"
                        Text(sign + movement.currency + " " + String.format(Locale.US, "%.2f", movement.amount))
                        Text(formatter.format(movement.createdAt), style = MaterialTheme.typography.bodySmall)
                        movement.reference?.let { Text("ID: " + it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
