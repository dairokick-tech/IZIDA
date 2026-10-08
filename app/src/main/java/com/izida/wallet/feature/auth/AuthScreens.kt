package com.izida.wallet.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable fun WelcomeScreen(onLogin:()->Unit,onRegister:()->Unit){
 Column(Modifier.fillMaxSize().padding(28.dp),Arrangement.Center,Alignment.CenterHorizontally){Text("IZIDA",style=MaterialTheme.typography.displaySmall);Text("Tu dinero. Tu control.",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(40.dp));Button(onClick=onLogin,Modifier.fillMaxWidth()){Text("Iniciar sesión")};Spacer(Modifier.height(12.dp));OutlinedButton(onClick=onRegister,Modifier.fillMaxWidth()){Text("Crear cuenta")}}
}
@Composable fun LoginScreen(onBack:()->Unit,onLogin:suspend (String,String)->Result<String>){
 var phone by remember{mutableStateOf("")};var pin by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
 Column(Modifier.fillMaxSize().padding(24.dp)){TextButton(onClick=onBack){Text("← Atrás")};Spacer(Modifier.height(24.dp));Text("Iniciar sesión",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(24.dp))
 OutlinedTextField(phone,{phone=it.filter(Char::isDigit).take(9)},label={Text("Celular")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),modifier=Modifier.fillMaxWidth())
 Spacer(Modifier.height(12.dp));OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={Text("PIN de acceso")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),modifier=Modifier.fillMaxWidth())
 error?.let{Spacer(Modifier.height(10.dp));Text(it,color=MaterialTheme.colorScheme.error)}
 Spacer(Modifier.height(20.dp));Button(onClick={loading=true;error=null;scope.launch{val r=onLogin(phone,pin);loading=false;r.onFailure{error=it.message?:"No se pudo iniciar sesión"}}},enabled=!loading&&phone.length==9&&pin.length==6,modifier=Modifier.fillMaxWidth()){Text(if(loading)"Validando…" else "Iniciar sesión")}
 Text("La sesión se valida contra el backend de IZIDA.",style=MaterialTheme.typography.bodySmall)
 }
}
@Composable fun RegisterScreen(onBack:()->Unit,onCompleted:(String,String)->Unit){var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(24.dp)){TextButton(onClick=onBack){Text("← Atrás")};Spacer(Modifier.height(24.dp));Text("Crear cuenta IZIDA",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(24.dp));OutlinedTextField(name,{name=it},label={Text("Nombre completo")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));OutlinedTextField(phone,{phone=it.filter(Char::isDigit).take(9)},label={Text("Celular")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(20.dp));Button(onClick={onCompleted(name,phone)},enabled=name.trim().length>=3&&phone.length==9,modifier=Modifier.fillMaxWidth()){Text("Continuar")}}}
@Composable fun IdentityVerificationScreen(onBack:()->Unit,onVerified:()->Unit){var dni by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(24.dp)){TextButton(onClick=onBack){Text("← Atrás")};Spacer(Modifier.height(24.dp));Text("Verificación de identidad",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(8.dp));Text("Este paso prepara la verificación oficial.");Spacer(Modifier.height(24.dp));OutlinedTextField(dni,{dni=it.filter(Char::isDigit).take(8)},label={Text("DNI")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(20.dp));Button(onClick=onVerified,enabled=dni.length==8,modifier=Modifier.fillMaxWidth()){Text("Continuar")}}}
@Composable fun CreatePinScreen(onBack:()->Unit,onCompleted:suspend (String)->Result<String>){var pin by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope();val valid=pin.length==6&&pin==confirm&&pin.toSet().size>1;Column(Modifier.fillMaxSize().padding(24.dp)){TextButton(onClick=onBack){Text("← Atrás")};Spacer(Modifier.height(24.dp));Text("Crear PIN de seguridad",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(24.dp));OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={Text("PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(6)},label={Text("Repetir PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(20.dp));error?.let{Spacer(Modifier.height(10.dp));Text(it,color=MaterialTheme.colorScheme.error)};Spacer(Modifier.height(20.dp));Button(onClick={loading=true;error=null;scope.launch{val r=onCompleted(pin);loading=false;r.onFailure{error=it.message?:"No se pudo crear la cuenta"}}},enabled=valid&&!loading,modifier=Modifier.fillMaxWidth()){Text(if(loading)"Creando cuenta…" else "Crear PIN")}}
}
@Composable fun SecurityCenterScreen(onBack:()->Unit,onLock:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp)){TextButton(onClick=onBack){Text("← Atrás")};Spacer(Modifier.height(24.dp));Text("Centro de seguridad",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(16.dp));Text("Sesión autenticada contra servidor.");Spacer(Modifier.height(24.dp));OutlinedButton(onClick=onLock,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}}}
