package com.wstef.api

import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class WebSocketManager(
    private val url: String,
    private val onPaymentUpdate: (PaymentData) -> Unit,
    private val onDisconnect: () -> Unit
) : WebSocketListener() {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    init {
        connect()
    }

    private fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        Log.d("WebSocket", "Conexão Fechada")

        webSocket?.close(1000, "Closing connection")
    }

    fun send(message: String) {
        Log.d("WebSocket", "Mensagem enviada: $message")

        webSocket?.send(message)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Conexão aberta")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "Mensagem recebida: $text")
        // Exemplo de mensagem: {"event":"payment_update","valorTotal":120.0,"descontos":20.0,"totalPagar":100.0}
        val json = JSONObject(text)
        if (json.optString("event") == "payment_update") {
            val data = PaymentData(
                valorTotal = json.optDouble("valorTotal", 0.0),
                descontos = json.optDouble("descontos", 0.0),
                totalPagar = json.optDouble("totalPagar", 0.0)
            )
            onPaymentUpdate(data)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("WebSocket", "Mensagem binária recebida: ${bytes.utf8()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Conexão fechando: $code/$reason")

        webSocket.close(1000, null)
        onDisconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Falha: ${t.message}")

        onDisconnect()
    }
}

data class PaymentData(
    val valorTotal: Double = 0.0,
    val descontos: Double = 0.0,
    val totalPagar: Double = 0.0
)
