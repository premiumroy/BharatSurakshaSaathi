package com.bharatsuraksha.saathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bharatsuraksha.saathi.ui.navigation.AppNavHost
import com.bharatsuraksha.saathi.ui.navigation.Routes
import com.bharatsuraksha.saathi.ui.session.AgentActivityStore
import com.bharatsuraksha.saathi.ui.session.BankHelplineStore
import com.bharatsuraksha.saathi.ui.session.PhoneHealthStore
import com.bharatsuraksha.saathi.ui.session.CalmMode
import com.bharatsuraksha.saathi.ui.session.CodeWordStore
import com.bharatsuraksha.saathi.ui.session.GameStatsStore
import com.bharatsuraksha.saathi.ui.session.IncomingImageStore
import com.bharatsuraksha.saathi.ui.session.SoundPref
import com.bharatsuraksha.saathi.ui.session.IncomingTextStore
import com.bharatsuraksha.saathi.ui.settings.SettingsViewModel
import com.bharatsuraksha.saathi.ui.theme.SaathiTheme
import com.bharatsuraksha.saathi.ui.theme.languageSwapFade
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI-layer preferences (display only; no data leaves the phone).
        CalmMode.load(applicationContext)
        SoundPref.load(applicationContext)
        GameStatsStore.load(applicationContext)
        CodeWordStore.load(applicationContext)
        BankHelplineStore.load(applicationContext)
        AgentActivityStore.load(applicationContext)
        PhoneHealthStore.load(applicationContext)

        // Text or image shared into us from any other app (share-sheet target).
        handleShareIntent(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val easyMode by settingsViewModel.easyMode.collectAsStateWithLifecycle()
            val highContrast by settingsViewModel.highContrast.collectAsStateWithLifecycle()
            val onboardingDone by settingsViewModel.onboardingDone.collectAsStateWithLifecycle()

            SaathiTheme(easyMode = easyMode, highContrast = highContrast) {
                val done = onboardingDone
                if (done != null) {
                    val start = remember {
                        when {
                            !done -> Routes.ONBOARDING
                            IncomingImageStore.uri.value != null -> Routes.IMAGE
                            else -> Routes.HOME
                        }
                    }
                    // A language change fades the new text in over 250ms.
                    val configuration = LocalConfiguration.current
                    val languageTag = remember(configuration) {
                        configuration.locales[0]?.toLanguageTag().orEmpty()
                    }
                    Box(Modifier.fillMaxSize().languageSwapFade(languageTag)) {
                        AppNavHost(startDestination = start)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val type = intent.type.orEmpty()
                if (type.startsWith("image/")) {
                    val uri: Uri? = IntentCompat.getParcelableExtra(
                        intent, Intent.EXTRA_STREAM, Uri::class.java
                    )
                    if (uri != null) IncomingImageStore.post(uri)
                } else {
                    val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!shared.isNullOrBlank()) IncomingTextStore.post(shared)
                }
            }
            Intent.ACTION_PROCESS_TEXT -> {
                val shared = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                if (!shared.isNullOrBlank()) IncomingTextStore.post(shared)
            }
        }
    }
}
