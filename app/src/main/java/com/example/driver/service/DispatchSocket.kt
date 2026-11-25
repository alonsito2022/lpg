package com.example.driver.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.driver.retrofit.ClientService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WebSocket de pedidos del chofer (reemplaza el nodo `deliveries` de Firebase RTDB).
 *
 * Se conecta a ws(s)://<BASE_URL>/ws/dispatches/driver/?token=<wsToken> y llama a
 * [onDispatchEvent] en el hilo principal cada vez que el servidor publica un cambio
 * de un pedido del chofer. Reconecta sola con espera creciente (1 s → 30 s).
 */
class DispatchSocket(
    private val token: String,
    private val onDispatchEvent: (event: String, dispatchID: Int) -> Unit,
    private val onUnauthorized: () -> Unit = {},
) {
    companion object {
        private const val TAG = "DispatchSocket"
        private const val CLOSE_UNAUTHORIZED = 4401
        private const val MAX_RETRY_MS = 30_000L

        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .pingInterval(25, TimeUnit.SECONDS)
                .build()
        }

        fun url(token: String): String {
            val base = ClientService.BASE_URL.trimEnd('/')
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://")
            return "$base/ws/dispatches/driver/?token=$token"
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: WebSocket? = null
    private var retryMs = 1_000L
    private var stopped = true

    fun start() {
        if (!stopped) return
        stopped = false
        connect()
    }

    fun stop() {
        stopped = true
        mainHandler.removeCallbacksAndMessages(null)
        socket?.close(1000, null)
        socket = null
    }

    private fun connect() {
        if (stopped) return
        val request = Request.Builder().url(url(token)).build()
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                retryMs = 1_000L
                Log.d(TAG, "conectado")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val event = json.optString("event")
                    if (event.isEmpty()) return  // p. ej. {"type":"pong"}
                    val dispatchID = json.optJSONObject("dispatch")?.optInt("dispatchID") ?: 0
                    mainHandler.post { if (!stopped) onDispatchEvent(event, dispatchID) }
                } catch (e: Exception) {
                    Log.w(TAG, "mensaje inválido: $text", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect(code)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "fallo: ${t.message}")
                handleDisconnect(response?.code() ?: 0)
            }
        })
    }

    private fun handleDisconnect(code: Int) {
        if (stopped) return
        if (code == CLOSE_UNAUTHORIZED || code == 403) {
            // Token inválido o vencido: no reintentar, hay que volver a iniciar sesión.
            stopped = true
            mainHandler.post { onUnauthorized() }
            return
        }
        val delay = retryMs
        retryMs = (retryMs * 2).coerceAtMost(MAX_RETRY_MS)
        mainHandler.postDelayed({ connect() }, delay)
    }
}
