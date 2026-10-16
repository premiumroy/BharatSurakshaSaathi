package com.bharatsuraksha.saathi.ui.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.domain.engine.RulesRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.domain.model.Reason
import com.bharatsuraksha.saathi.domain.model.Verdict
import com.bharatsuraksha.saathi.ui.common.BigButton
import com.bharatsuraksha.saathi.ui.common.VerdictPill
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LinkCheckState(
    val input: String = "",
    val checked: Boolean = false,
    val verdict: Verdict? = null,
    val links: List<String> = emptyList(),
    val reasons: List<Reason> = emptyList()
)

@HiltViewModel
class LinkCheckViewModel @Inject constructor(
    private val engine: ScamEngine,
    rulesRepository: RulesRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    val officialDomains: List<String> = rulesRepository.loadRules().linkRules.officialDomains

    private val _state = MutableStateFlow(LinkCheckState())
    val state: StateFlow<LinkCheckState> = _state.asStateFlow()

    fun onInput(value: String) = _state.update { it.copy(input = value, checked = false) }

    fun check() {
        val text = _state.value.input
        if (text.isBlank()) return
        viewModelScope.launch {
            val sensitivity = settings.currentSensitivity()
            val result = withContext(Dispatchers.Default) { engine.analyze(text, sensitivity) }
            val linkReasons = result.reasons.filter { it.id.startsWith("link_") }
            _state.update {
                it.copy(
                    checked = true,
                    verdict = result.verdict,
                    links = result.links,
                    reasons = linkReasons
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCheckScreen(
    onBack: () -> Unit,
    viewModel: LinkCheckViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hindi = isHindiLocale()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.link_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.link_intro), style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                label = { Text(stringResource(R.string.link_input_hint)) }
            )

            BigButton(
                text = stringResource(R.string.link_check_button),
                onClick = viewModel::check
            )

            if (state.checked) {
                state.verdict?.let { VerdictPill(it) }

                Text(stringResource(R.string.link_found_title), fontWeight = FontWeight.Bold)
                if (state.links.isEmpty()) {
                    Text(stringResource(R.string.link_no_links), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            state.links.forEach {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (state.reasons.isEmpty()) {
                    Text(stringResource(R.string.link_looks_safe), style = MaterialTheme.typography.bodyLarge)
                } else {
                    state.reasons.forEach { reason ->
                        Card {
                            Text(
                                "• " + reason.text(hindi),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.link_official_title), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.link_official_note), style = MaterialTheme.typography.bodyMedium)
            Card {
                Text(
                    text = viewModel.officialDomains.joinToString(", "),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
