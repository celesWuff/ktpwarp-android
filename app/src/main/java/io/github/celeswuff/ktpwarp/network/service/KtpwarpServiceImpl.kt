package io.github.celeswuff.ktpwarp.network.service

import android.net.Uri
import io.github.celeswuff.ktpwarp.network.entity.AdiosMessage
import io.github.celeswuff.ktpwarp.network.entity.CancelMessage
import io.github.celeswuff.ktpwarp.network.entity.Class_
import io.github.celeswuff.ktpwarp.network.entity.ManualCheckMessage
import io.github.celeswuff.ktpwarp.network.entity.NewGps签到Message
import io.github.celeswuff.ktpwarp.network.entity.NewQrcode签到Message
import io.github.celeswuff.ktpwarp.network.entity.New数字签到Message
import io.github.celeswuff.ktpwarp.network.entity.New签入签出签到Message
import io.github.celeswuff.ktpwarp.network.entity.SkipMessage
import io.github.celeswuff.ktpwarp.network.entity.SubmitQrcodeMessage
import io.github.celeswuff.ktpwarp.network.entity.WebsocketMessage
import io.github.celeswuff.ktpwarp.network.entity.WelcomeMessage
import io.github.celeswuff.ktpwarp.network.entity.签到FailureMessage
import io.github.celeswuff.ktpwarp.network.entity.签到HistoryData
import io.github.celeswuff.ktpwarp.network.entity.签到SuccessMessage
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus.CONNECTED
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus.CONNECTING
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus.DISCONNECTED
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class KtpwarpServiceImpl(
    private val scope: CoroutineScope,
    private val context: CoroutineContext = Dispatchers.IO,
) : KtpwarpService {
    private val client: HttpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null

    private var _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(DISCONNECTED)
    override val connectionStatus = _connectionStatus.asStateFlow()

    override var ktpwarpServerVersion: String? = null
        private set

    override var nodejsVersion: String? = null
        private set

    override var schedule: List<Class_>? = emptyList()
        private set

    override var currentClass: Class_? = null
        private set

    override var finished签到s: List<签到HistoryData>? = emptyList()
        private set

    private val _messages = MutableSharedFlow<WebsocketMessage>()
    override val messages = _messages.asSharedFlow()

    private fun Frame.toMessage(): WebsocketMessage {
        require(this is Frame.Text)
        return WebsocketMessage.fromJson(readText())
    }

    private fun reset() {
        session = null
        _connectionStatus.value = DISCONNECTED
        ktpwarpServerVersion = null
        nodejsVersion = null
        schedule = emptyList()
        currentClass = null
        finished签到s = emptyList()
    }

    private fun requireConnect() {
        runCatching {
            check(connectionStatus.value == CONNECTED) { "WebSocket is not connected" }
            check(session != null) { "WebSocket session is null" }
        }.onFailure {
            reset()
            throw it
        }
    }

    private suspend fun setupWelcomeMessage(message: WelcomeMessage) {
        ktpwarpServerVersion = message.data.ktpwarpServerVersion
        nodejsVersion = message.data.nodejsVersion
        schedule = message.data.schedule
        currentClass = message.data.currentClass
        finished签到s = message.data.finished签到s

        if (message.data.pending签到 != null) {
            val pending签到Metadata = message.data.pending签到.data
            when (message.data.pending签到.type) {
                "new数字签到" -> _messages.emit(New数字签到Message(pending签到Metadata))
                "newGps签到" -> _messages.emit(NewGps签到Message(pending签到Metadata))
                "newQrcode签到" -> _messages.emit(NewQrcode签到Message(pending签到Metadata))
                "new签入签出签到" -> _messages.emit(New签入签出签到Message(pending签到Metadata))
                else -> {}
            }

            val pending签到Results = message.data.pending签到.results
            if (pending签到Results != null) {
                for (result in pending签到Results) {
                    when (result.type) {
                        "签到success" -> _messages.emit(
                            签到SuccessMessage(
                                签到SuccessMessage.签到SuccessData(result.data.friendlyName)
                            )
                        )

                        "签到failure" -> _messages.emit(
                            签到FailureMessage(
                                签到FailureMessage.签到FailureData(
                                    result.data.friendlyName,
                                    result.data.failureMessage!!
                                )
                            )
                        )

                        else -> {}
                    }
                }
            }
        }
    }

    override suspend fun connect(urlString: String) {
        withContext(context) {
            runCatching {
                _connectionStatus.value = CONNECTING
                session = client.webSocketSession(urlString) {}
            }.fold(
                onSuccess = {
                    runCatching {
                        session!!.incoming.consumeEach { frame ->
                            when (val message = frame.toMessage()) {
                                is AdiosMessage -> disconnect()
                                is WelcomeMessage -> {
                                    setupWelcomeMessage(message)
                                    _connectionStatus.value = CONNECTED
                                }

                                else -> _messages.emit(message)
                            }
                        }
                    }.onFailure { disconnect() }
                },
                onFailure = {
                    _connectionStatus.value = DISCONNECTED
                    throw it
                }
            )
        }
    }

    override suspend fun disconnect() {
        session?.close()
        reset()
    }

    private suspend fun sendMessage(message: WebsocketMessage) {
        runCatching {
            requireConnect()
            withContext(context) {
                session!!.send(Frame.Text(Json.encodeToString(message)))
            }
        }.onFailure {
            disconnect()
        }
    }

    override suspend fun manualCheck() {
        sendMessage(ManualCheckMessage)
    }

    override suspend fun submitQrcode(urlString: String) {
        val url = Uri.parse(urlString)
        val ticketid = url.getQueryParameter("ticketid")!!
        val expire = url.getQueryParameter("expire")!!
        val sign = url.getQueryParameter("sign")!!
        sendMessage(
            SubmitQrcodeMessage(
                SubmitQrcodeMessage.SubmitQrcodeData(
                    ticketid,
                    expire,
                    sign
                )
            )
        )
    }

    override suspend fun skip() {
        sendMessage(SkipMessage)
    }

    override suspend fun cancel() {
        sendMessage(CancelMessage)
    }
}
