package io.github.celeswuff.ktpwarp.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.celeswuff.ktpwarp.network.entity.Class_
import io.github.celeswuff.ktpwarp.network.entity.WebsocketMessage
import io.github.celeswuff.ktpwarp.network.entity.签到HistoryData
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService
import io.github.celeswuff.ktpwarp.network.service.KtpwarpService.ConnectionStatus
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("settings", 0)
    private var serverAddress = ""
    var lastSuccessfulServerAddress =
        sharedPreferences.getString("lastSuccessfulServerAddress", "") ?: ""

    private var service = KtpwarpService.create(viewModelScope)

    val connectionStatus = service.connectionStatus

    private val _showConnectionFailureDialog = mutableStateOf(false)
    val showConnectionFailureDialog: State<Boolean> = _showConnectionFailureDialog

    var ktpwarpServerVersion: String? = null
    var nodejsVersion: String? = null
    var schedule: List<Class_>? = null
    var currentClass: Class_? = null
    var finished签到s: List<签到HistoryData>? = null

    val messages = mutableStateListOf<WebsocketMessage>()

    init {
        viewModelScope.launch {
            service.connectionStatus.collect {
                if (it == ConnectionStatus.CONNECTED) {
                    ktpwarpServerVersion = service.ktpwarpServerVersion
                    nodejsVersion = service.nodejsVersion
                    schedule = service.schedule
                    currentClass = service.currentClass
                    finished签到s = service.finished签到s

                    updateLastSuccessfulServerAddress()
                }
            }
        }

        viewModelScope.launch {
            service.messages.collect { messages.add(it) }
        }
    }

    private fun updateLastSuccessfulServerAddress() {
        sharedPreferences
            .edit()
            .putString("lastSuccessfulServerAddress", serverAddress)
            .apply()
    }

    fun connect(serverAddress: String) {
        this.serverAddress = serverAddress

        // must clear messages at there instead of "on connect", or the pending签到 messages may be dropped
        messages.clear()

        viewModelScope.launch {
            runCatching {
                service.connect(serverAddress)
            }.onFailure {
                disconnect()
                it.printStackTrace()
                _showConnectionFailureDialog.value = true
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            service.disconnect()
        }
    }

    fun closeConnectionFailureDialog() {
        _showConnectionFailureDialog.value = false
    }

    fun skip() {
        viewModelScope.launch { service.skip() }
    }

    fun cancel() {
        viewModelScope.launch { service.cancel() }
    }

    fun manualCheck() {
        viewModelScope.launch { service.manualCheck() }
    }

    fun submitQrcode(urlString: String) {
        viewModelScope.launch { service.submitQrcode(urlString) }
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }
}
