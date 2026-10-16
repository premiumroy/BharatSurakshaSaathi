package com.bharatsuraksha.saathi.ui.image

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.bharatsuraksha.saathi.data.ocr.OcrTextExtractor
import com.bharatsuraksha.saathi.data.prefs.SettingsRepository
import com.bharatsuraksha.saathi.data.repository.HistoryRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import com.bharatsuraksha.saathi.ui.common.BigButton
import com.bharatsuraksha.saathi.ui.session.LastResultStore
import com.bharatsuraksha.saathi.ui.theme.BrandGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ImageCheckState(
    val reading: Boolean = false,
    val text: String = "",
    val errorRes: Int? = null
)

@HiltViewModel
class ImageCheckViewModel @Inject constructor(
    private val ocr: OcrTextExtractor,
    private val engine: ScamEngine,
    private val settings: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImageCheckState())
    val state: StateFlow<ImageCheckState> = _state.asStateFlow()

    private val _navigate = MutableSharedFlow<Unit>()
    val navigate: SharedFlow<Unit> = _navigate.asSharedFlow()

    fun readImage(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(reading = true, errorRes = null) }
            val text = runCatching { ocr.extract(uri) }.getOrDefault("")
            _state.update {
                it.copy(
                    reading = false,
                    text = text,
                    errorRes = if (text.isBlank()) R.string.image_no_text else null
                )
            }
        }
    }

    fun check() {
        val text = _state.value.text.trim()
        if (text.isEmpty()) {
            _state.update { it.copy(errorRes = R.string.image_no_text) }
            return
        }
        viewModelScope.launch {
            val sensitivity = settings.currentSensitivity()
            val result = withContext(Dispatchers.Default) { engine.analyze(text, sensitivity) }
            LastResultStore.message = text
            LastResultStore.result = result
            historyRepository.add(text, result)
            _navigate.emit(Unit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCheckScreen(
    onBack: () -> Unit,
    onOpenResult: () -> Unit,
    viewModel: ImageCheckViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val incomingImage by com.bharatsuraksha.saathi.ui.session.IncomingImageStore.uri.collectAsStateWithLifecycle()

    LaunchedEffect(incomingImage) {
        incomingImage?.let {
            viewModel.readImage(it)
            com.bharatsuraksha.saathi.ui.session.IncomingImageStore.consume()
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.readImage(uri) }

    val captureUri = remember { mutableStateOf<Uri?>(null) }
    val takeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) captureUri.value?.let { viewModel.readImage(it) } }

    LaunchedEffect(Unit) {
        viewModel.navigate.collect { onOpenResult() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.image_title), fontWeight = FontWeight.Bold) },
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
            Text(stringResource(R.string.image_intro), style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { pickLauncher.launch(androidx.activity.result.PickVisualMediaRequest()) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.image_pick)) }
                OutlinedButton(
                    onClick = {
                        val uri = createImageUri(context)
                        captureUri.value = uri
                        takeLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.image_take)) }
            }

            if (state.reading) {
                Text(stringResource(R.string.image_reading), style = MaterialTheme.typography.bodyLarge)
            }

            state.errorRes?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }

            if (state.text.isNotBlank()) {
                Text(stringResource(R.string.image_recognized), fontWeight = FontWeight.Bold)
                Card {
                    Text(
                        text = state.text,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                BigButton(
                    text = stringResource(R.string.image_check),
                    onClick = viewModel::check
                )
            }
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
