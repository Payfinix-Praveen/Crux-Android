package com.sujanix.cruxmdm.socket

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.sujanix.cruxmdm.socket.UnSafeOkHttpClient.getUnsafeOkHttpClient
import com.sujanix.cruxmdm.util.UserPreferences
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Singleton

@Singleton
class SocketClientImp(
    private val context: Context,
    private val preferences: UserPreferences,
    private val listener: SocketListener
) : SocketClient {

    private lateinit var socketClient: Socket

    init {
        startSession()
    }

    @Synchronized
    override fun startSession() {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                preferences.accessDeviceId.collect {
                    it?.let { deviceId ->
                        val OkHttpClientContxt = getUnsafeOkHttpClient()
                        val options = IO.Options().apply {
                            extraHeaders = mapOf(
                                Pair("clientType", listOf("MDM_ANDROID")),
                                Pair("deviceID", listOf(deviceId))
                            )
                            callFactory = OkHttpClientContxt
                            webSocketFactory = OkHttpClientContxt
                            reconnection = true
                        }
                        socketClient = IO.socket(URI("https://payzark.com"), options)
                        socketClient.on(Socket.EVENT_CONNECT_ERROR) { error ->
                            Log.d("WEBSOCKET", "startSession: ${error[0]}")
                        }
                        establishConnection()
                        receiveMessageFromSocket()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("FATAL", "getSocket: ${e.message}")
            Toast.makeText(
                context,
                "There was an error connecting to the socket server",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    @Synchronized
    override fun getSocket(): Socket {
        return socketClient
    }

    @Synchronized
    override fun establishConnection() {
        socketClient.connect()
    }

    override fun isSocketConnected(): Boolean {
        return socketClient.connected()
    }

    override fun sendMessageToSocket(event: String, message: Any) {

        try {
            if(isSocketConnected()) {
                Log.d("WEBSOCKET", "sendMessageToSocketREPO: $message")
                socketClient.emit(event, message)
            }
        } catch (e: Exception){
            Log.d("WEBSOCKET", "sendMessageToSocketCATCH: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun receiveMessageFromSocket() {

        socketClient.on("INITIAL") { messages ->
            CoroutineScope(Dispatchers.IO).launch {
                listener.onMessageReceived(
                    "INITIAL",
                    messages[1].toString().replace("\\\"", "")
                )
            }
        }

        socketClient.on("NEW_SETTING_CHANGE") { messages ->
            CoroutineScope(Dispatchers.IO).launch {
                listener.onMessageReceived(
                    "NEW_SETTING_CHANGE",
                    messages[0].toString().replace("\\\"", "")
                )
            }
        }
    }

    override fun closeConnection() {
        socketClient.disconnect()
    }

    interface SocketListener {

        suspend fun onMessageReceived(event: String, message: String)
    }

}