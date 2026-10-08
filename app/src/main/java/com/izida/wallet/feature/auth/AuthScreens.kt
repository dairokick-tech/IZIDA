package com.izida.wallet.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("IZIDA", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text("Tu dinero. Tu control.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onLogin, Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRegister, Modifier.fillMaxWidth()) { Text("Crear cuenta") }
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
        OutlinedTextField(phone, { phone = it.filter(Char::isDigit).take(9) }, label={Text("Celular")}, keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone), modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(6) }, label={Text("PIN de acceso")}, visualTransformation=PasswordVisualTransformation(), keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword), modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick=onSuccess, enabled=phone.length==9 && pin.length>=4, Modifier.fillMaxWidth()) { Text("Continuar") }
        Spacer(Modifier.height(12.dp))
        Text("En Paso 2 se prepara el flujo de seguridad. La validación contra servidor se conectará antes de operaciones financieras.", style=MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onCompleted: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick=onBack) { Text("← Atrás") }
        Spacer(Modifier.height(24.dp))
        Text("Crear cuenta IZIDA", style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(name,{name=it},label={Text("Nombre completo")},modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(phone,{phone=it.filter(Char::isDigit).take(9)},label={Text("Celular")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick=onCompleted,enabled=name.trim().length>=3&&phone.length==9,modifier=Modifier.fillMaxWidth()){Text("Continuar")}
    }
}

@Composable
fun IdentityVerificationScreen(onBack:()->Unit,onVerified:()->Unit) {
    var dni by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick=onBack){Text("← Atrás")}
        Spacer(Modifier.height(24.dp))
        Text("Verificación de identidad",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Este paso prepara la verificación de identidad. La consulta oficial se conectará al backend/KYC en una siguiente iteración.")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(dni,{dni=it.filter(Char::isDigit).take(8)},label={Text("DNI")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick=onVerified,enabled=dni.length==8,modifier=Modifier.fillMaxWidth()){Text("Continuar")}
    }
}

@Composable
fun CreatePinScreen(onBack:()->Unit,onCompleted:()->Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val valid=pin.length==6 && pin==confirm && pin.toSet().size>1
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick=onBack){Text("← Atrás")}
        Spacer(Modifier.height(24.dp))
        Text("Crear PIN de seguridad",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Usa un PIN de 6 dígitos. No lo compartas con nadie.")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={Text("PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(6)},label={Text("Repetir PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(onClick=onCompleted,enabled=valid,modifier=Modifier.fillMaxWidth()){Text("Crear PIN")}
    }
}

@Composable
fun SecurityCenterScreen(onBack:()->Unit,onLock:()->Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick=onBack){Text("← Atrás")}
        Spacer(Modifier.height(24.dp))
        Text("Centro de seguridad",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        SecurityRow("Sesión","Activa en este dispositivo")
        SecurityRow("PIN","Configurado")
        SecurityRow("Dispositivo","Sesión local de desarrollo")
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick=onLock,modifier=Modifier.fillMaxWidth()){Text("Bloquear sesión")}
        Spacer(Modifier.height(12.dp))
        Text("La gestión de sesiones, dispositivos y biometría contra servidor se completará antes de producción.",style=MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SecurityRow(title:String,value:String) {
    Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title,style=MaterialTheme.typography.labelLarge)
            Text(value,style=MaterialTheme.typography.bodyLarge)
        }
    }
}
