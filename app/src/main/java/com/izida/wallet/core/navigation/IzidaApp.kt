package com.izida.wallet.core.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.izida.wallet.core.security.AuthSession
import com.izida.wallet.data.account.DemoAccountRepository
import com.izida.wallet.feature.account.AccountScreen
import com.izida.wallet.feature.auth.*
import com.izida.wallet.feature.home.HomeScreen
import com.izida.wallet.feature.movement.MovementsScreen

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
}

@Composable
fun IzidaApp() {
    val navController = rememberNavController()
    val session = remember { AuthSession() }
    val accountRepository = remember { DemoAccountRepository() }

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
                onSuccess = {
                    session.start()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onCompleted = { navController.navigate(Routes.VERIFY_IDENTITY) }
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
                onCompleted = {
                    session.start()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onAccount = { navController.navigate(Routes.ACCOUNT) },
                onSecurity = { navController.navigate(Routes.SECURITY) }
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                repository = accountRepository,
                onMovements = { navController.navigate(Routes.MOVEMENTS) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MOVEMENTS) {
            MovementsScreen(
                repository = accountRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SECURITY) {
            SecurityCenterScreen(
                onBack = { navController.popBackStack() },
                onLock = {
                    session.end()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
