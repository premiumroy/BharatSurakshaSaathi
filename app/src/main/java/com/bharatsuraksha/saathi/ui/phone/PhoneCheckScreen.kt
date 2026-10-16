package com.bharatsuraksha.saathi.ui.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.domain.engine.PhoneNumberAnalyzer
import com.bharatsuraksha.saathi.domain.model.PhoneCheckResult
import com.bharatsuraksha.saathi.ui.common.BigButton
import com.bharatsuraksha.saathi.ui.common.VerdictPill
import com.bharatsuraksha.saathi.ui.common.isHindiLocale
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhoneCheckState(
    val input: String = "",
    val result: PhoneCheckResult? = null
)

@HiltViewModel
class PhoneCheckViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneCheckState())
    val state: StateFlow<PhoneCheckState> = _state.asStateFlow()

    val blocklist: StateFlow<Set<String>> =
        settings.blocklist.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun onInput(value: String) = _state.update { it.copy(input = value, result = null) }

    fun check() {
        val input = _state.value.input.trim()
        if (input.isEmpty()) return
        val result = PhoneNumberAnalyzer.analyze(input, blocklist.value)
        _state.update { it.copy(result = result) }
    }

    fun toggleBlocklist(number: String) {
        viewModelScope.launch {
            val current = blocklist.value.toMutableSet()
            if (current.contains(number)) current.remove(number) else current.add(number)
            settings.setBlocklist(current)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCheckScreen(
    onBack: () -> Unit,
    viewModel: PhoneCheckViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val blocklist by viewModel.blocklist.collectAsStateWithLifecycle()
    val hindi = isHindiLocale()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.phone_title), fontWeight = FontWeight.Bold) },
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
            Text(stringResource(R.string.phone_intro), style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInput,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.phone_input_hint)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                )
            )

            BigButton(
                text = stringResource(R.string.phone_check_button),
                onClick = viewModel::check
            )

            state.result?.let { result ->
                VerdictPill(result.verdict)
                Text(
                    text = result.normalized.ifBlank { state.input },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (result.signals.isEmpty()) {
                    Text(stringResource(R.string.phone_no_signals), style = MaterialTheme.typography.bodyLarge)
                } else {
                    result.signals.forEach { signal ->
                        Card {
                            Text(
                                "• " + signal.text(hindi),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                val isBlocked = blocklist.contains(result.normalized)
                TextButton(onClick = { viewModel.toggleBlocklist(result.normalized) }) {
                    Text(
                        stringResource(
                            if (isBlocked) R.string.phone_remove_blocklist else R.string.phone_add_blocklist
                        )
                    )
                }
            }

            Text(stringResource(R.string.phone_blocklist_title), fontWeight = FontWeight.Bold)
            if (blocklist.isEmpty()) {
                Text(stringResource(R.string.phone_blocklist_empty), style = MaterialTheme.typography.bodyLarge)
            } else {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        blocklist.forEach { number ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(number, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                TextButton(onClick = { viewModel.toggleBlocklist(number) }) {
                                    Text(stringResource(R.string.history_delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
