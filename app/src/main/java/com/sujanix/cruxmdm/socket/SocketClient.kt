package com.sujanix.cruxmdm.socket

import io.socket.client.Socket
import javax.inject.Singleton

@Singleton
interface SocketClient {

    fun startSession()

    fun getSocket(): Socket

    fun establishConnection()

    fun isSocketConnected(): Boolean

    fun sendMessageToSocket(event: String, message: Any)

    fun receiveMessageFromSocket()

    fun closeConnection()
}