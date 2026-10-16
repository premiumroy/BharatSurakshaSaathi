package com.bharatsuraksha.saathi.ui.tips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bharatsuraksha.saathi.R
import com.bharatsuraksha.saathi.ui.theme.BrandGreen

private data class Tip(val titleRes: Int, val bodyRes: Int, val neverRes: Int)

private val TIPS = listOf(
    Tip(R.string.tips_digital_arrest_title, R.string.tips_digital_arrest_body, R.string.tips_digital_arrest_never),
    Tip(R.string.tips_kyc_title, R.string.tips_kyc_body, R.string.tips_kyc_never),
    Tip(R.string.tips_job_title, R.string.tips_job_body, R.string.tips_job_never),
    Tip(R.string.tips_lottery_title, R.string.tips_lottery_body, R.string.tips_lottery_never),
    Tip(R.string.tips_loan_title, R.string.tips_loan_body, R.string.tips_loan_never),
    Tip(R.string.tips_sextortion_title, R.string.tips_sextortion_body, R.string.tips_sextortion_never)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tips_title), fontWeight = FontWeight.Bold) },
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
            Text(stringResource(R.string.tips_intro), style = MaterialTheme.typography.bodyLarge)
            TIPS.forEach { tip ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(tip.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(stringResource(tip.bodyRes), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.tips_what_real_never),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(stringResource(tip.neverRes), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
