package com.sujanix.cruxmdm.socket

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.sujanix.cruxmdm.features.core.utlis.UserPreferences
import com.sujanix.cruxmdm.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.Socket
import javax.inject.Singleton

@Singleton
class SocketClientImp(
    private val context: Context,
    private val preferences: UserPreferences,
    private val listener: SocketListener
) : SocketClient {

    private val TAG: String = "SocketClientImp"
    private lateinit var socketClient: Socket
    private lateinit var webSocket: WebSocket
    private lateinit var sharedPreferences: UserPreferences

    private val client = OkHttpClient()

    var socketConnected: Boolean = false
    private var deviceID: String? = ""
    private var enterPriseId: String? = ""

    init {
//        startSession()
//        connect()
    }

    override fun connect(message: Any) {
        val request = okhttp3.Request.Builder()
            .url("wss://ueoanuiqj7.execute-api.us-east-2.amazonaws.com/development/")
            .build()

        sharedPreferences = UserPreferences(context)

        CoroutineScope(Dispatchers.IO).launch {
            sharedPreferences.accessDeviceUserData.collect { userData ->
                sharedPreferences.accessDeviceId.collect { deviceId ->
                    if (deviceId != null && userData != null) {
                        Log.d(TAG, "connect: $deviceId")

                        deviceID = deviceId
                        enterPriseId = userData.enterprise_id

                        webSocket = client.newWebSocket(request, object : WebSocketListener() {

                            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                                webSocket.send(
                                    """{
                                          "action":"deviceConnect",
                                          "message":{
                                              "enterprise_id": "$enterPriseId",
                                              "device_id": "$deviceId"
                                          }
                                        }""".trimIndent())

                                Log.d(TAG, "onOpen: Connection established")
                                webSocket.send(message.toString())
                                socketConnected = true
                            }

                            override fun onMessage(webSocket: WebSocket, text: String) {
                                Log.d(TAG, "onMessage: $text")
                                receiveMessageFromSocket(text)
                            }

                            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                                Log.d(TAG, "onClosed: ")
                                context.stopService(Intent(context, LocationService::class.java))
                                Intent(context, LocationService::class.java).apply {
                                    context.startService(this)
                                }
                            }

                            override fun onFailure(
                                webSocket: WebSocket,
                                t: Throwable,
                                response: okhttp3.Response?
                            ) {
                                Log.d(TAG, "onFailure: ${t.message}")
                            }
                        })
                    }
                    else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Device ID not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun disconnect() {
        webSocket.send("""
            {
                "action":"deviceDisconnect",
                "message":{
                    "enterprise_id": "$enterPriseId",
                    "device_id":"$deviceID"
                }
            }
            """.trimIndent()
        )
        webSocket.close(200, "Goodbye!")
        socketConnected = false
    }

    override fun sendMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun setListener(listener: WebSocketListener) {
        TODO("Not yet implemented")
    }

    @Synchronized
    override fun startSession() {

//        try {
//            CoroutineScope(Dispatchers.IO).launch {
//                preferences.accessDeviceId.collect {
//                    it?.let { deviceId ->
//                        val OkHttpClientContxt = getUnsafeOkHttpClient()
//                        val options = IO.Options().apply {
////                            auth = mapOf(Pair("clientID", "1001") )
//                            extraHeaders = mapOf(
//                                Pair("clientType", listOf("MDM_ANDROID")),
//                                Pair("deviceID", listOf(deviceId))
//                            )
//                            callFactory = OkHttpClientContxt
//                            webSocketFactory = OkHttpClientContxt
//                            reconnection = true
//                        }
//                        socketClient = IO.socket(URI("http://192.168.0.183:8082"), options)
//                        socketClient.on(Socket.EVENT_CONNECT_ERROR) { error ->
//                            Log.d("WEBSOCKET", "EVENT_CONNECT_ERROR: ${error[0]}")
//                        }
//                        establishConnection()
//                        receiveMessageFromSocket()
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.d("FATAL", "getSocket: ${e.message}")
//            Toast.makeText(
//                context,
//                "There was an error connecting to the socket server",
//                Toast.LENGTH_SHORT
//            ).show()
//            e.printStackTrace()
//        }
    }

    @Synchronized
    override fun getSocket(): Socket {
        return socketClient
    }

    @Synchronized
    override fun establishConnection() {
//        socketClient.connect()
    }

    override fun isSocketConnected(): Boolean {
//        return socketClient.connected()
        return socketConnected
    }

    override fun sendMessageToSocket(message: Any) {

        try {
            Log.d(TAG, "sendMessageToSocket: $message")

            webSocket.send(message.toString())
//            if(isSocketConnected()) {
//                Log.d("WEBSOCKET", "sendMessageToSocketREPO: $message")
//                socketClient.emit(event, message)
//            }
        } catch (e: Exception){
            Log.d("WEBSOCKET", "sendMessageToSocketCATCH: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun receiveMessageFromSocket(message: String) {

        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
//        socketClient.on("INITIAL") { messages ->
//            CoroutineScope(Dispatchers.IO).launch {
//                listener.onMessageReceived(
//                    "INITIAL",
//                    messages[1].toString().replace("\\\"", "")
//                )
//            }
//        }
//
//        socketClient.on("NEW_SETTING_CHANGE") { messages ->
//            CoroutineScope(Dispatchers.IO).launch {
//                listener.onMessageReceived(
//                    "NEW_SETTING_CHANGE",
//                    messages[0].toString().replace("\\\"", "")
//                )
//            }
//        }
    }

    override fun closeConnection() {
//        socketClient.disconnect()
    }

    interface SocketListener {

        suspend fun onMessageReceived(event: String, message: String)
    }
}