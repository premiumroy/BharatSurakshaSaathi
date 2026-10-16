package com.bharatsuraksha.saathi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bharatsuraksha.saathi.ui.easy.EasyModeScreen
import com.bharatsuraksha.saathi.ui.history.HistoryScreen
import com.bharatsuraksha.saathi.ui.home.HomeScreen
import com.bharatsuraksha.saathi.ui.image.ImageCheckScreen
import com.bharatsuraksha.saathi.ui.link.LinkCheckScreen
import com.bharatsuraksha.saathi.ui.notifications.NotificationAccessScreen
import com.bharatsuraksha.saathi.ui.onboarding.OnboardingScreen
import com.bharatsuraksha.saathi.ui.phone.PhoneCheckScreen
import com.bharatsuraksha.saathi.ui.privacy.PrivacyScreen
import com.bharatsuraksha.saathi.ui.recover.RecoveryScreen
import com.bharatsuraksha.saathi.ui.result.ResultScreen
import com.bharatsuraksha.saathi.ui.settings.SettingsScreen
import com.bharatsuraksha.saathi.ui.tips.TipsScreen
import com.bharatsuraksha.saathi.ui.voice.VoiceScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val RESULT = "result"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val NOTIFICATION_ACCESS = "notification_access"
    const val IMAGE = "image"
    const val LINK = "link"
    const val PHONE = "phone"
    const val VOICE = "voice"
    const val EASY = "easy"
    const val TIPS = "tips"
    const val RECOVER = "recover"
    const val PRIVACY = "privacy"
}

@Composable
fun AppNavHost(
    startDestination: String = Routes.HOME,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenResult = { navController.navigate(Routes.RESULT) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenImage = { navController.navigate(Routes.IMAGE) },
                onOpenLink = { navController.navigate(Routes.LINK) },
                onOpenPhone = { navController.navigate(Routes.PHONE) },
                onOpenTips = { navController.navigate(Routes.TIPS) },
                onOpenRecover = { navController.navigate(Routes.RECOVER) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenEasy = { navController.navigate(Routes.EASY) },
                onOpenVoice = { navController.navigate(Routes.VOICE) }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onBack = { navController.popBackStack() },
                onCheckAnother = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenRecover = { navController.navigate(Routes.RECOVER) }
            )
        }

        composable(Routes.HISTORY) { HistoryScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenNotificationAccess = { navController.navigate(Routes.NOTIFICATION_ACCESS) },
                onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                onOpenTips = { navController.navigate(Routes.TIPS) }
            )
        }

        composable(Routes.NOTIFICATION_ACCESS) {
            NotificationAccessScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.IMAGE) {
            ImageCheckScreen(
                onBack = { navController.popBackStack() },
                onOpenResult = { navController.navigate(Routes.RESULT) }
            )
        }

        composable(Routes.LINK) { LinkCheckScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.PHONE) { PhoneCheckScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.VOICE) { VoiceScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.EASY) {
            EasyModeScreen(
                onBack = { navController.popBackStack() },
                onCheckMessage = { navController.navigate(Routes.HOME) },
                onSpeak = { navController.navigate(Routes.VOICE) }
            )
        }

        composable(Routes.TIPS) { TipsScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.RECOVER) { RecoveryScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
    }
}
