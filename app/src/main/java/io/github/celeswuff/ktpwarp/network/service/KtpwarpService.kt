package io.github.celeswuff.ktpwarp.network.service

import io.github.celeswuff.ktpwarp.network.entity.Class_
import io.github.celeswuff.ktpwarp.network.entity.WebsocketMessage
import io.github.celeswuff.ktpwarp.network.entity.签到HistoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

interface KtpwarpService {
    companion object {
        fun create(
            scope: CoroutineScope,
            context: CoroutineContext = Dispatchers.IO,
        ): KtpwarpService = KtpwarpServiceImpl(scope, context)
    }

    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING
    }

    val connectionStatus: StateFlow<ConnectionStatus>

    val ktpwarpServerVersion: String?

    val nodejsVersion: String?

    val schedule : List<Class_>?

    val currentClass: Class_?

    val finished签到s: List<签到HistoryData>?

    val messages: SharedFlow<WebsocketMessage>

    suspend fun connect(urlString: String)

    suspend fun disconnect()

    suspend fun manualCheck()

    suspend fun submitQrcode(urlString: String)

    suspend fun skip()

    suspend fun cancel()
}
