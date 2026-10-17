package com.bharatsuraksha.saathi.ui.navigation

import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bharatsuraksha.saathi.ui.common.JyotiBottomBar
import com.bharatsuraksha.saathi.ui.common.jyotiTabs
import com.bharatsuraksha.saathi.ui.debug.DebugScreen
import com.bharatsuraksha.saathi.ui.easy.EasyModeScreen
import com.bharatsuraksha.saathi.ui.family.FamilyScreen
import com.bharatsuraksha.saathi.ui.history.HistoryScreen
import com.bharatsuraksha.saathi.ui.home.HomeScreen
import com.bharatsuraksha.saathi.ui.image.ImageCheckScreen
import com.bharatsuraksha.saathi.ui.learn.LearnScreen
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
import com.bharatsuraksha.saathi.ui.theme.motionAllowed

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val LEARN = "learn"
    const val FAMILY = "family"
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
    const val DEBUG = "debug"
}

@Composable
fun AppNavHost(
    startDestination: String = Routes.HOME,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route
    val tabs = jyotiTabs(Routes.LEARN, Routes.FAMILY, Routes.HOME, Routes.HISTORY)
    val showBar = tabs.any { it.route == current }
    val motion = motionAllowed()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBar) {
                JyotiBottomBar(tabs = tabs, currentRoute = current, motion = motion) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {

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

            composable(Routes.LEARN) { LearnScreen() }
            composable(Routes.FAMILY) { FamilyScreen() }

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
                    onOpenTips = { navController.navigate(Routes.TIPS) },
                    onOpenDebug = { navController.navigate(Routes.DEBUG) }
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
            composable(Routes.DEBUG) { DebugScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
