package br.com.essampaio.nearnode.data.service.communication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.com.essampaio.nearnode.R
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import io.ktor.server.routing.routing
import io.ktor.server.routing.get
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.http.HttpStatusCode

class CommunicationAndroidService : Service() {

    private val profileRepository: ProfileRepository by inject()
    private var server: SocketIOServer? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val CHANNEL_ID = "communication_service_channel"
    private val NOTIFICATION_ID = 1

    // Trocamos o SocketIOServer pelo ApplicationEngine do Ktor
    // Trocamos o SocketIOServer pelo ApplicationEngine do Ktor
    private var ktorServer: ApplicationEngine? = null

    private fun startServer() {
        if (ktorServer != null) return

        serviceScope.launch {
            try {
                android.util.Log.d("NearNodeServer", "Iniciando Ktor CIO Engine na porta 9876...")

                ktorServer = embeddedServer(CIO, port = 9876, host = "0.0.0.0") {

                    // Instala o conversor JSON
                    install(ContentNegotiation) {
                        gson { setPrettyPrinting() }
                    }

                    // Define as Rotas
                    routing {
                        get("/info") {
                            // Essa coroutine já roda de forma segura no Ktor
                            val profile = profileRepository.getCurrentProfile().firstOrNull()

                            if (profile != null) {
                                val info = mapOf(
                                    "id" to profile.id,
                                    "userName" to profile.username,
                                    "profilePicture" to "",
                                    "status" to profile.status.name
                                )
                                // O Ktor magicamente transforma o mapOf em JSON e envia
                                call.respond(HttpStatusCode.OK, info)
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Perfil não encontrado"))
                            }
                        }
                    }
                }

                // wait = false é OBRIGATÓRIO no Android para não travar a thread de background
                ktorServer?.start(wait = false)

                android.util.Log.d("NearNodeServer", "Ktor Server ONLINE e escutando na porta 9876!")
            } catch (e: Exception) {
                android.util.Log.e("NearNodeServer", "CRASH Ktor", e)
            }
        }
    }

    private fun stopServer() {
        // Dá 1 segundo para encerrar requisições ativas e 2 segundos para matar o processo
        ktorServer?.stop(1000, 2000)
        ktorServer = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startServer()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopServer()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Communication Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Communication service is running")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .build()
    }
}
