package io.github.celeswuff.ktpwarp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.celeswuff.ktpwarp.network.entity.Class_
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConnectionStatusCard(
    modifier: Modifier = Modifier,
    connectionStatus: ConnectionStatus,
    serverAddress: String,
    onConnect: (serverAddress: String) -> Unit,
    onDisconnect: () -> Unit,
    showConnectionFailureDialog: State<Boolean>,
    closeConnectionFailureDialog: () -> Unit,
    ktpwarpServerVersion: String?,
    nodejsVersion: String?,
    schedule: List<Class_>?,
) {
    val scope = rememberCoroutineScope()

    var serverAddressState by rememberSaveable { mutableStateOf(serverAddress) }

    val openServerAddressInputDialog = remember { mutableStateOf(false) }
    val openSystemInformationDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (serverAddressState.isNotBlank()) {
            onConnect(serverAddressState)
        }
    }

    OutlinedCard(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "状态", style = MaterialTheme.typography.headlineSmall)
            Crossfade(targetState = connectionStatus, label = "") { state ->
                Text(
                    when (state) {
                        ConnectionStatus.CONNECTED -> "已连接"
                        ConnectionStatus.DISCONNECTED -> "未连接"
                        ConnectionStatus.CONNECTING -> "正在连接"
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Button(onClick = {
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        scope.launch { onDisconnect() }
                    } else {
                        openServerAddressInputDialog.value = true
                    }
                }, enabled = connectionStatus != ConnectionStatus.CONNECTING) {
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> "断开连接"
                            ConnectionStatus.DISCONNECTED -> "连接"
                            ConnectionStatus.CONNECTING -> "正在连接"
                        }
                    )
                }

                if (openServerAddressInputDialog.value) {
                    AlertDialog(
                        modifier = Modifier.imePadding(),
                        onDismissRequest = { openServerAddressInputDialog.value = false },
                        title = { Text(text = "服务器地址") },
                        text = {
                            TextField(
                                value = serverAddressState,
                                onValueChange = { serverAddressState = it },
                                placeholder = {
                                    Text(
                                        text = "wss://...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                openServerAddressInputDialog.value = false
                                scope.launch { onConnect(serverAddressState) }
                            }) {
                                Text(text = "继续")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { openServerAddressInputDialog.value = false }) {
                                Text(text = "取消")
                            }
                        }
                    )
                }

                val isConnected = connectionStatus == ConnectionStatus.CONNECTED

                AnimatedVisibility(
                    visible = isConnected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Button(onClick = {
                        openSystemInformationDialog.value = true
                    }) {
                        Text(text = "系统信息")
                    }
                }
            }
        }

        if (openSystemInformationDialog.value) {
            AlertDialog(
                onDismissRequest = { openSystemInformationDialog.value = false },
                title = { Text(text = "系统信息") },
                text = {
                    Column {
                        Text(text = "软件版本", fontWeight = Bold)
                        Text(text = "ktpwarp-server ${ktpwarpServerVersion ?: "版本未知"}")
                        Text(text = "Node.js ${nodejsVersion ?: "版本未知"}")
                        Text(text = "")

                        Text(text = "课程表", fontWeight = Bold)
                        Text(text = schedule?.joinToString("\n  ") {
                            val dayOfWeekMap = mapOf(
                                0 to "日",
                                1 to "一",
                                2 to "二",
                                3 to "三",
                                4 to "四",
                                5 to "五",
                                6 to "六",
                            )

                            "${it.friendlyName}，每周${dayOfWeekMap[it.dayOfWeek]}，${it.startTime} 至 ${it.endTime}"
                        } ?: "不可用")
                    }
                },
                confirmButton = {
                    TextButton({ openSystemInformationDialog.value = false }) {
                        Text(text = "好")
                    }
                }
            )
        }

        if (showConnectionFailureDialog.value) {
            AlertDialog(
                onDismissRequest = { closeConnectionFailureDialog() },
                title = { Text(text = "连接失败") },
                text = { Text(text = "无法连接到服务器，请检查输入的地址是否正确。") },
                confirmButton = {
                    TextButton(onClick = { closeConnectionFailureDialog() }) {
                        Text(text = "好")
                    }
                }
            )
        }
    }
}
