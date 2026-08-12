package br.com.essampaio.nearnode.domain.service.nsdService

import android.net.nsd.NsdServiceInfo
import br.com.essampaio.nearnode.data.Node

sealed class DiscoveryStatus {
    data object Discovering : DiscoveryStatus()
    //    data class Found(val serviceName: String, val ip: String, val port: Int): DiscoveryStatus()
    data class Found(val node: Node): DiscoveryStatus()
    data object Stopped: DiscoveryStatus()
    data object WaitingForRegistration: DiscoveryStatus() // Novo status para indicar a espera
}
