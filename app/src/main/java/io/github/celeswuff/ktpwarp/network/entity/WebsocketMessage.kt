@file:Suppress("PropertyName")

package io.github.celeswuff.ktpwarp.network.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
sealed interface WebsocketMessage {
    companion object {
        private val _json = Json { ignoreUnknownKeys = true }

        fun fromJson(json: String): WebsocketMessage {
            return _json.decodeFromString(json)
        }
    }
}

@Serializable
data class Class_(
    val friendlyName: String,
    val classId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val latitude: String? = null,
    val longitude: String? = null
)

@Serializable
data class 签到HistoryData(
    val type: String,
    val data: 签到Data,
    val results: List<签到HistoryResultData>? = null,
) {
    @Serializable
    data class 签到HistoryResultData(
        val type: String,
        val data: 签到HistoryResultMessageData,
    ) {
        @Serializable
        data class 签到HistoryResultMessageData(
            val friendlyName: String,
            val failureMessage: String? = null,
        )
    }
}

@Serializable
data class 签到Data(
    val class_: Class_? = null,
    val delaySeconds: Int? = null,
    val 签到Id: String? = null
) : WebsocketMessage

@Serializable
@SerialName("welcome")
data class WelcomeMessage(val data: WelcomeData) : WebsocketMessage {
    @Serializable
    data class WelcomeData(
        val ktpwarpServerVersion: String,
        val nodejsVersion: String,
        val classCount: Int,
        val 互动答题ClassCount: Int,
        val schedule: List<Class_>? = null,
        val currentClass: Class_? = null,
        val finished签到s: List<签到HistoryData>? = null,
        val pending签到: 签到HistoryData? = null,
    )
}

@Serializable
@SerialName("manualCheck")
object ManualCheckMessage : WebsocketMessage

@Serializable
@SerialName("skip")
object SkipMessage : WebsocketMessage

@Serializable
@SerialName("cancel")
object CancelMessage : WebsocketMessage

@Serializable
@SerialName("submitQrcode")
data class SubmitQrcodeMessage(val data: SubmitQrcodeData) : WebsocketMessage {
    @Serializable
    data class SubmitQrcodeData(val ticketid: String, val expire: String, val sign: String)
}

// obsolete since 1.1.0
@Serializable
@SerialName("restart")
object RestartMessage : WebsocketMessage

@Serializable
@SerialName("adios")
object AdiosMessage : WebsocketMessage

@Serializable
@SerialName("skipSuccess")
object SkipSuccessMessage : WebsocketMessage

@Serializable
@SerialName("cancelSuccess")
object CancelSuccessMessage : WebsocketMessage

@Serializable
@SerialName("new数字签到")
data class New数字签到Message(val data: 签到Data) : WebsocketMessage

@Serializable
@SerialName("newGps签到")
data class NewGps签到Message(val data: 签到Data) : WebsocketMessage

@Serializable
@SerialName("newQrcode签到")
data class NewQrcode签到Message(val data: 签到Data) : WebsocketMessage

@Serializable
@SerialName("new签入签出签到")
data class New签入签出签到Message(val data: 签到Data) : WebsocketMessage

@Serializable
@SerialName("数字签到code")
data class 数字签到CodeMessage(val data: 数字签到CodeData) : WebsocketMessage {
    @Serializable
    data class 数字签到CodeData(val code: String)
}

@Serializable
@SerialName("receivedQrcode")
object ReceivedQrcodeMessage : WebsocketMessage

@Serializable
@SerialName("签到success")
data class 签到SuccessMessage(val data: 签到SuccessData) : WebsocketMessage {
    @Serializable
    data class 签到SuccessData(val friendlyName: String)
}

@Serializable
@SerialName("签到failure")
data class 签到FailureMessage(val data: 签到FailureData) : WebsocketMessage {
    @Serializable
    data class 签到FailureData(val friendlyName: String, val failureMessage: String)
}

@Serializable
@SerialName("incoming互动答题")
data class Incoming互动答题Message(val data: Incoming互动答题Data) : WebsocketMessage {
    @Serializable
    data class Incoming互动答题Data(val class_: Class_)
}
