package io.github.celeswuff.ktpwarp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.celeswuff.ktpwarp.ui.theme.KtpWarpTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App() {
    KtpWarpTheme {
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                AppTopBar(title = "ktpWarp", subtitle = "课堂派自动签到")
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
                AppContent(
                    Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel()
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        AppConnectionStatusCard(
            Modifier,
            connectionStatus,
            viewModel.lastSuccessfulServerAddress,
            viewModel::connect,
            viewModel::disconnect,
            viewModel.showConnectionFailureDialog,
            viewModel::closeConnectionFailureDialog,
            viewModel.ktpwarpServerVersion,
            viewModel.nodejsVersion,
            viewModel.schedule,
        )
        Spacer(Modifier.height(8.dp))
        App签到Card(
            Modifier,
            connectionStatus,
            viewModel.currentClass,
            viewModel.finished签到s,
            viewModel.messages,
            viewModel::submitQrcode,
            viewModel::manualCheck,
            viewModel::skip,
            viewModel::cancel
        )
        // don't put a spacer here, App签到Card already has an animated one
        AppAboutCard(Modifier)
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(modifier: Modifier = Modifier, title: String, subtitle: String? = null) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle ?: "", fontSize = 12.sp)
            }
        },
    )
}