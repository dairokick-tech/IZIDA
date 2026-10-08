package com.izida.wallet.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("IZIDA", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text("Tu dinero. Tu control.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta") }
    }
}

@Composable
fun LoginScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Atrás") }
        Spacer(Modifier.height(24.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Celular") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pin, { pin = it }, label = { Text("PIN") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick = onSuccess, enabled = phone.isNotBlank() && pin.length >= 4, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
        Spacer(Modifier.height(12.dp))
        Text("Prototipo de interfaz: la autenticación real se implementará en el Paso 2.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onCompleted: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("← Atrás") }
        Spacer(Modifier.height(24.dp))
        Text("Crear cuenta IZIDA", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Celular") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick = onCompleted, enabled = name.isNotBlank() && phone.length >= 9, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
        Spacer(Modifier.height(12.dp))
        Text("La verificación de identidad y las credenciales seguras se implementarán en el Paso 2.")
    }
}
