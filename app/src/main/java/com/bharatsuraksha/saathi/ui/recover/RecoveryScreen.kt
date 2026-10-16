package com.bharatsuraksha.saathi.ui.recover

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.data.db.EvidenceEntity
import com.bharatsuraksha.saathi.data.pdf.EvidencePdfExporter
import com.bharatsuraksha.saathi.data.repository.EvidenceRepository
import com.bharatsuraksha.saathi.ui.common.BigButton
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val evidenceRepository: EvidenceRepository,
    private val pdfExporter: EvidencePdfExporter
) : ViewModel() {

    val items: StateFlow<List<EvidenceEntity>> = evidenceRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addEvidence(
        title: String,
        amount: String,
        whenText: String,
        numbers: String,
        notes: String,
        screenshotUri: Uri?
    ) {
        viewModelScope.launch {
            val path = screenshotUri?.let { withContext(Dispatchers.IO) { copyToFiles(context, it) } }
            evidenceRepository.add(
                EvidenceEntity(
                    title = title.ifBlank { "Scam incident" },
                    amount = amount,
                    whenText = whenText,
                    numbers = numbers,
                    notes = notes,
                    screenshotPath = path,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAll() = viewModelScope.launch { evidenceRepository.deleteAll() }

    fun delete(id: Long) = viewModelScope.launch { evidenceRepository.delete(id) }

    fun exportPdf(onReady: (Uri?) -> Unit) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching { pdfExporter.export(evidenceRepository.all()) }.getOrNull()
            }
            val uri = file?.let {
                runCatching {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                }.getOrNull()
            }
            onReady(uri)
        }
    }
}

private fun copyToFiles(context: Context, uri: Uri): String? = runCatching {
    val dir = File(context.filesDir, "evidence").apply { mkdirs() }
    val dest = File(dir, "img_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    }
    dest.absolutePath
}.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(
    onBack: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var whenText by remember { mutableStateOf("") }
    var numbers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var screenshot by remember { mutableStateOf<Uri?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) screenshot = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recover_title), fontWeight = FontWeight.Bold) },
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
            Text(stringResource(R.string.recover_intro), style = MaterialTheme.typography.bodyLarge)

            StepCard(
                title = stringResource(R.string.recover_step1_title),
                body = stringResource(R.string.recover_step1_body),
                actionLabel = stringResource(R.string.recover_call_1930),
                onAction = { dial(context, "1930") }
            )
            StepCard(
                title = stringResource(R.string.recover_step2_title),
                body = stringResource(R.string.recover_step2_body),
                actionLabel = stringResource(R.string.recover_call_bank),
                onAction = { dial(context, "") }
            )
            StepCard(
                title = stringResource(R.string.recover_step3_title),
                body = stringResource(R.string.recover_step3_body),
                actionLabel = stringResource(R.string.recover_open_cybercrime),
                onAction = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
            StepCard(
                title = stringResource(R.string.recover_step4_title),
                body = stringResource(R.string.recover_step4_body),
                actionLabel = null,
                onAction = null
            )

            // ---- Evidence checklist ----
            Text(stringResource(R.string.recover_checklist_title), fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recover_field_title)) }, singleLine = true
            )
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recover_field_amount)) }, singleLine = true
            )
            OutlinedTextField(
                value = whenText, onValueChange = { whenText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recover_field_when)) }, singleLine = true
            )
            OutlinedTextField(
                value = numbers, onValueChange = { numbers = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recover_field_numbers)) }, singleLine = true
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recover_field_notes)) }
            )
            OutlinedButton(
                onClick = { pickLauncher.launch(androidx.activity.result.PickVisualMediaRequest()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.recover_add_screenshot) +
                        if (screenshot != null) " ✓" else ""
                )
            }
            BigButton(
                text = stringResource(R.string.recover_save),
                onClick = {
                    viewModel.addEvidence(title, amount, whenText, numbers, notes, screenshot)
                    title = ""; amount = ""; whenText = ""; numbers = ""; notes = ""; screenshot = null
                }
            )

            // ---- Saved evidence ----
            Text(stringResource(R.string.recover_saved_items), fontWeight = FontWeight.Bold)
            if (items.isEmpty()) {
                Text(stringResource(R.string.recover_empty), style = MaterialTheme.typography.bodyLarge)
            } else {
                items.forEach { item ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold)
                                if (item.amount.isNotBlank()) Text(item.amount)
                                if (item.whenText.isNotBlank()) Text(item.whenText)
                                if (item.numbers.isNotBlank()) Text(item.numbers)
                            }
                            TextButton(onClick = { viewModel.delete(item.id) }) {
                                Text(stringResource(R.string.history_delete))
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportPdf { uri ->
                                if (uri != null) {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(share, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.recover_export_pdf)) }
                    OutlinedButton(
                        onClick = { viewModel.deleteAll() },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.recover_delete_all)) }
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    title: String,
    body: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyLarge)
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun dial(context: Context, number: String) {
    context.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
