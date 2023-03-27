package io.github.celeswuff.ktpwarp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.celeswuff.ktpwarp.network.entity.CancelSuccessMessage
import io.github.celeswuff.ktpwarp.network.entity.Class_
import io.github.celeswuff.ktpwarp.network.entity.Incoming互动答题Message
import io.github.celeswuff.ktpwarp.network.entity.NewGps签到Message
import io.github.celeswuff.ktpwarp.network.entity.NewQrcode签到Message
import io.github.celeswuff.ktpwarp.network.entity.New数字签到Message
import io.github.celeswuff.ktpwarp.network.entity.New签入签出签到Message
import io.github.celeswuff.ktpwarp.network.entity.ReceivedQrcodeMessage
import io.github.celeswuff.ktpwarp.network.entity.SkipSuccessMessage
import io.github.celeswuff.ktpwarp.network.entity.WebsocketMessage
import io.github.celeswuff.ktpwarp.network.entity.数字签到CodeMessage
import io.github.celeswuff.ktpwarp.network.entity.签到FailureMessage
import io.github.celeswuff.ktpwarp.network.entity.签到HistoryData
import io.github.celeswuff.ktpwarp.network.entity.签到SuccessMessage
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch

@Composable
fun App签到Card(
    modifier: Modifier = Modifier,
    connectionStatus: ConnectionStatus,
    currentClass: Class_?,
    finished签到s: List<签到HistoryData>?,
    messages: SnapshotStateList<WebsocketMessage>,
    onSubmitQrcode: (urlString: String) -> Unit,
    onManualCheck: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current!!

    val isConnected = connectionStatus == ConnectionStatus.CONNECTED

    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        scope.launch {
            when (result) {
                is QRResult.QRSuccess -> if (result.content.rawValue.startsWith("https://w.ketangpai.com/checkIn/checkinCodeResult")) {
                    onSubmitQrcode(result.content.rawValue)
                } else {
                    snackbarHostState.showSnackbar("不是课堂派二维码。")
                }

                QRResult.QRUserCanceled -> {}
                QRResult.QRMissingPermission -> snackbarHostState.showSnackbar("没有摄像头权限，不能打开扫码器。")
                is QRResult.QRError -> snackbarHostState.showSnackbar("${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}\n\n是什么呢")
            }
        }
    }

    AnimatedVisibility(
        visible = isConnected,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        OutlinedCard(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "签到", style = MaterialTheme.typography.headlineSmall)

                if (currentClass != null) {
                    Text("正在进行的课程：${currentClass.friendlyName}")
                    Text("有新的签到任务时，就会显示在这里。")
                } else {
                    Text("没有正在进行的课程。")
                    Text("将不会自动发现新签到，如果有正在进行的签到，请点按“手动检查签到”。")
                }

                if (!finished签到s.isNullOrEmpty()) {
                    Divider()
                    Text("已完成的其他签到", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    val 签到NameMap = mapOf(
                        "newGps签到" to "GPS 签到",
                        "newQrcode签到" to "二维码签到",
                        "new数字签到" to "数字签到",
                        "new签入签出签到" to "签入签出签到",
                    )

                    var counterForAddingSpacers = finished签到s.size
                    finished签到s.forEach { 签到 ->
                        Text("签到类型：${签到NameMap[签到.type]}")
                        签到.results?.forEach { result ->
                            Text("${result.data.friendlyName}：${result.data.failureMessage ?: "签到成功"}")
                        }
                        if (counterForAddingSpacers-- > 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                var showSkipAndCancelButton = false

                messages.forEach { message ->
                    when (message) {
                        is NewGps签到Message -> {
                            Divider()
                            Text("有新的签到任务", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("课程 ${message.data.class_!!.friendlyName} 发现 GPS 签到，将在 ${message.data.delaySeconds} 秒后签到。")
                            showSkipAndCancelButton = true
                        }

                        is NewQrcode签到Message -> {
                            Divider()
                            Text("有新的签到任务", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("课程 ${message.data.class_?.friendlyName} 发现二维码签到，点按下面的“扫描二维码”来进行扫码。")
                        }

                        is New数字签到Message -> {
                            Divider()
                            Text("有新的签到任务", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("课程 ${message.data.class_!!.friendlyName} 发现数字签到，将在 ${message.data.delaySeconds} 秒后签到。")
                            showSkipAndCancelButton = true
                        }

                        is New签入签出签到Message -> {
                            Divider()
                            Text("有新的签到任务", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("课程 ${message.data.class_!!.friendlyName} 发现签入签出签到，将在 ${message.data.delaySeconds} 秒后签入（或签出）。")
                            showSkipAndCancelButton = true
                        }

                        is SkipSuccessMessage -> {
                            Text("已跳过等待。")
                            showSkipAndCancelButton = false
                        }

                        is CancelSuccessMessage -> {
                            Text("已取消签到。")
                            showSkipAndCancelButton = false
                        }

                        is 数字签到CodeMessage -> {
                            Text("签到码为 ${message.data.code}。")
                            showSkipAndCancelButton = false
                        }

                        is ReceivedQrcodeMessage -> {
                            Text("已接收到二维码。")
                            showSkipAndCancelButton = false
                        }

                        is 签到SuccessMessage -> {
                            Text("${message.data.friendlyName}：签到成功")
                            showSkipAndCancelButton = false
                        }

                        is 签到FailureMessage -> {
                            Text("${message.data.friendlyName}：${message.data.failureMessage}")
                            showSkipAndCancelButton = false
                        }

                        is Incoming互动答题Message -> {
                            Divider()
                            Text("课程 ${message.data.class_.friendlyName} 发现互动答题。")
                        }

                        else -> {}
                    }
                }

                // this is a hack to make the buttons not jump when the animation ends
                // https://stackoverflow.com/a/69935571/10144204
                Column(verticalArrangement = Arrangement.Top) {
                    AnimatedVisibility(
                        visible = showSkipAndCancelButton,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Button(onClick = {
                                showSkipAndCancelButton = false
                                onSkip()
                            }) {
                                Text(text = "跳过等待")
                            }
                            Button(
                                onClick = {
                                    showSkipAndCancelButton = false
                                    onCancel()
                                },
                            ) {
                                Text(text = "取消签到")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Button(onClick = { scanQrCodeLauncher.launch(null) }) {
                        Text(text = "扫描二维码")
                    }
                    Button(
                        onClick = {
                            onManualCheck()
                            scope.launch { snackbarHostState.showSnackbar("正在手动检查签到，若有新的签到将会通知您。") }
                        },
                    ) {
                        Text(text = "手动检查签到")
                    }
                }
            }
        }
    }
}