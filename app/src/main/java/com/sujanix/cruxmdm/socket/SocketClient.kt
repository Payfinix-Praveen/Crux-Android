package com.sujanix.cruxmdm.socket

import okhttp3.WebSocketListener
import java.net.Socket
import javax.inject.Singleton

@Singleton
interface SocketClient {

    fun connect(message: Any)

    fun disconnect()

    fun sendMessage(message: String)

    fun setListener(listener: WebSocketListener)

    fun startSession()

    fun getSocket(): Socket

    fun establishConnection()

    fun isSocketConnected(): Boolean

    fun sendMessageToSocket(message: Any)

    fun receiveMessageFromSocket(message: String)

    fun closeConnection()
}