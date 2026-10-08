package com.izida.wallet.core.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.izida.wallet.core.security.AuthSession
import com.izida.wallet.BuildConfig
import com.izida.wallet.data.account.RemoteAccountRepository
import com.izida.wallet.data.remote.IzidaApi
import com.izida.wallet.feature.transfer.SendMoneyScreen
import com.izida.wallet.feature.account.AccountScreen
import com.izida.wallet.feature.auth.*
import com.izida.wallet.feature.home.HomeScreen
import com.izida.wallet.feature.movement.MovementsScreen
import com.izida.wallet.feature.qr.MyQrScreen
import com.izida.wallet.feature.qr.ScanQrScreen
import com.izida.wallet.feature.qr.QrPaymentScreen
import com.izida.wallet.domain.qr.IzidaQrPayload

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY_IDENTITY = "verify_identity"
    const val CREATE_PIN = "create_pin"
    const val HOME = "home"
    const val ACCOUNT = "account"
    const val MOVEMENTS = "movements"
    const val SECURITY = "security"
    const val SEND = "send"
    const val MY_QR = "my_qr"
    const val SCAN_QR = "scan_qr"
    const val QR_PAYMENT = "qr_payment"
}

@Composable
fun IzidaApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val session = remember { AuthSession(context) }
    val api = remember { IzidaApi(BuildConfig.IZIDA_API_BASE_URL) }
    val remoteRepository = remember { RemoteAccountRepository(api) }
    var pendingName by remember { mutableStateOf("") }
    var pendingPhone by remember { mutableStateOf("") }
    var qrPayload by remember { mutableStateOf<IzidaQrPayload?>(null) }
    var sessionChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedToken = session.getToken()
        if (savedToken != null) {
            api.setToken(savedToken)
            val valid = runCatching { api.getMyAccount(); true }.getOrDefault(false)
            if (valid) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            } else {
                api.setToken(null)
                session.end()
            }
        }
        sessionChecked = true
    }

    if (!sessionChecked) return

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLogin = { phone, pin ->
                    runCatching {
                        val result = api.login(phone, pin)
                        api.setToken(result.token)
                        session.start(result.token)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                        result.token
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onCompleted = { name, phone ->
                    pendingName = name
                    pendingPhone = phone
                    navController.navigate(Routes.CREATE_PIN)
                }
            )
        }
        composable(Routes.VERIFY_IDENTITY) {
            IdentityVerificationScreen(
                onBack = { navController.popBackStack() },
                onVerified = { navController.navigate(Routes.CREATE_PIN) }
            )
        }
        composable(Routes.CREATE_PIN) {
            CreatePinScreen(
                onBack = { navController.popBackStack() },
                onCompleted = { pin ->
                    runCatching {
                        val result = api.register(pendingName, pendingPhone, pin)
                        api.setToken(result.token)
                        session.start(result.token)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                        result.token
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onAccount = { navController.navigate(Routes.ACCOUNT) },
                onSend = { navController.navigate(Routes.SEND) },
                onReceive = { navController.navigate(Routes.MY_QR) },
                onScanQr = { navController.navigate(Routes.SCAN_QR) },
                onSecurity = { navController.navigate(Routes.SECURITY) },
                repository = remoteRepository
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                repository = remoteRepository,
                userId = "AUTHENTICATED",
                onMovements = { navController.navigate(Routes.MOVEMENTS) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MOVEMENTS) {
            MovementsScreen(
                repository = remoteRepository,
                accountId = "AUTHENTICATED",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SEND) {
            SendMoneyScreen(
                api = api,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MY_QR) {
            MyQrScreen(api = api, onBack = { navController.popBackStack() })
        }
        composable(Routes.SCAN_QR) {
            ScanQrScreen(
                onBack = { navController.popBackStack() },
                onPay = { payload -> qrPayload = payload; navController.navigate(Routes.QR_PAYMENT) }
            )
        }
        composable(Routes.QR_PAYMENT) {
            val payload = qrPayload
            if (payload == null) navController.popBackStack()
            else QrPaymentScreen(api = api, payload = payload, onBack = { navController.popBackStack() })
        }
        composable(Routes.SECURITY) {
            SecurityCenterScreen(
                onBack = { navController.popBackStack() },
                onLock = {
                    api.setToken(null)
                    session.end()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
