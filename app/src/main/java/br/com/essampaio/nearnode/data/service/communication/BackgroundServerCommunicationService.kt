package br.com.essampaio.nearnode.data.service.communication

import android.content.Context
import android.content.Intent
import br.com.essampaio.nearnode.domain.service.CommunicationService

class BackgroundServerCommunicationService(
    private val context: Context
) : CommunicationService {

    override fun start() {
        val intent = Intent(context, CommunicationAndroidService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun stop() {
        val intent = Intent(context, CommunicationAndroidService::class.java)
        context.stopService(intent)
    }

}



