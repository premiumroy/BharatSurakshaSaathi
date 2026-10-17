package com.bharatsuraksha.saathi.ui.upi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.LatticeOnDark
import com.bharatsuraksha.saathi.ui.theme.Saffron
import com.bharatsuraksha.saathi.ui.theme.Sindoor
import com.bharatsuraksha.saathi.ui.theme.Space2
import com.bharatsuraksha.saathi.ui.theme.Space3
import com.bharatsuraksha.saathi.ui.theme.Space4
import com.bharatsuraksha.saathi.ui.theme.motionAllowed
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * The UPI QR scanner. CameraX preview with the BUNDLED ML Kit barcode model, so
 * it works with no network. A QR image can also be picked from the gallery.
 */
@Composable
fun UpiScanScreen(onBack: () -> Unit, onScanned: () -> Unit) {
    val context = LocalContext.current
    val motion = motionAllowed()

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var deniedOnce by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        granted = ok
        if (!ok) deniedOnce = true
    }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            busy = true
            val value = scanImage(context, uri)
            busy = false
            if (value != null) {
                com.bharatsuraksha.saathi.ui.session.UpiScanStore.raw = value
                onScanned()
            }
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Space4),
            verticalArrangement = Arrangement.spacedBy(Space3)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Text(
                    text = stringResource(R.string.upi_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when {
                !granted && !deniedOnce -> PermissionRationale(
                    onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onSkip = { deniedOnce = true }
                )

                !granted -> DeniedPanel(
                    onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )

                else -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = 260.dp)
                    ) {
                        CameraPreview(
                            onFound = { value ->
                                if (!busy) {
                                    busy = true
                                    com.bharatsuraksha.saathi.ui.session.UpiScanStore.raw = value
                                    onScanned()
                                }
                            }
                        )
                        LatticeScanFrame(motion = motion, modifier = Modifier.fillMaxSize())
                    }
                    Text(
                        text = stringResource(R.string.upi_scan_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    pickLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.upi_pick_image)) }

            OutlinedButton(
                onClick = { onBack() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text(stringResource(R.string.upi_type_manual)) }

            Text(
                text = stringResource(R.string.upi_offline_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.upi_never_verified),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CameraPreview(onFound: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor, QrAnalyzer(onFound))
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                // Camera unavailable: the gallery and manual paths still work.
            }
        }, mainExecutor)

        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            runCatching { executor.shutdown() }
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

/** Feeds camera frames to the bundled ML Kit QR reader. */
private class QrAnalyzer(private val onFound: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { codes ->
                codes.firstOrNull()?.rawValue?.takeIf { it.isNotBlank() }?.let(onFound)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

/** Reads a QR out of a picked gallery image, fully offline. */
private fun scanImage(context: Context, uri: Uri): String? {
    val bitmap: Bitmap = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.isMutableRequired = false
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        return null
    }

    val input = InputImage.fromBitmap(bitmap, 0)
    val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )
    return try {
        val task = scanner.process(input)
        // Bounded wait; the model is on-device so this returns quickly.
        val deadline = System.currentTimeMillis() + 4000
        while (!task.isComplete && System.currentTimeMillis() < deadline) {
            Thread.sleep(40)
        }
        task.result.firstOrNull()?.rawValue
    } catch (e: Exception) {
        null
    } finally {
        runCatching { scanner.close() }
    }
}

/** An animated lattice frame with corner brackets and a sweeping line. */
@Composable
private fun LatticeScanFrame(motion: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "sweep"
    )
    val y = if (motion) sweep else 0.5f

    Canvas(modifier) {
        // faint lattice
        val step = 40.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(LatticeOnDark, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            x += step
        }
        var yy = 0f
        while (yy < size.height) {
            drawLine(LatticeOnDark, Offset(0f, yy), Offset(size.width, yy), 1.dp.toPx())
            yy += step
        }

        // corner brackets
        val inset = 28.dp.toPx()
        val arm = 54.dp.toPx()
        val stroke = 4.dp.toPx()
        val corners = listOf(
            Offset(inset, inset) to listOf(Offset(arm, 0f), Offset(0f, arm)),
            Offset(size.width - inset, inset) to listOf(Offset(-arm, 0f), Offset(0f, arm)),
            Offset(inset, size.height - inset) to listOf(Offset(arm, 0f), Offset(0f, -arm)),
            Offset(size.width - inset, size.height - inset) to listOf(Offset(-arm, 0f), Offset(0f, -arm))
        )
        corners.forEach { (origin, arms) ->
            arms.forEach { armOffset ->
                drawLine(
                    color = Saffron,
                    start = origin,
                    end = Offset(origin.x + armOffset.x, origin.y + armOffset.y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }

        // sweeping line
        val lineY = size.height * y
        drawLine(
            color = Saffron,
            start = Offset(inset, lineY),
            end = Offset(size.width - inset, lineY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawRect(
            color = Saffron.copy(alpha = 0.08f),
            topLeft = Offset(inset, lineY - 18.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width - inset * 2, 36.dp.toPx())
        )
    }
}

@Composable
private fun PermissionRationale(onAllow: () -> Unit, onSkip: () -> Unit) {
    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Text(
                text = stringResource(R.string.upi_perm_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.upi_perm_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onAllow,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text(stringResource(R.string.upi_perm_allow)) }
            TextButton(onClick = onSkip) { Text(stringResource(R.string.upi_perm_not_now)) }
        }
    }
}

@Composable
private fun DeniedPanel(onRetry: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Space4), verticalArrangement = Arrangement.spacedBy(Space3)) {
            Text(
                text = stringResource(R.string.upi_denied_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Sindoor
            )
            Text(
                text = stringResource(R.string.upi_denied_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.upi_perm_allow)) }
        }
    }
}
