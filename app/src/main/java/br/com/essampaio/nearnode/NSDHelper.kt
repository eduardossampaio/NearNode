package br.com.essampaio.nearnode

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.provider.Settings.Secure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume


sealed class RegistrationStatus {
    data class Registered(val registrationServiceName: String): RegistrationStatus()
    data class Failed(val cause: String): RegistrationStatus()
    data object Unregistered: RegistrationStatus()
}

sealed class DiscoveryStatus {
    data object Discovering : DiscoveryStatus()
//    data class Found(val serviceName: String, val ip: String, val port: Int): DiscoveryStatus()
    data class Found(val service: NsdServiceInfo): DiscoveryStatus()
    data object Stopped: DiscoveryStatus()
    data object WaitingForRegistration: DiscoveryStatus() // Novo status para indicar a espera
}

class NSDHelper(context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val nsdServiceType = "_nearnode._tcp"

    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val multicastLock = wifiManager.createMulticastLock("NearNodeMulticastLock")

    private val android_id: String? = Secure.getString(
        context.getContentResolver(),
        Secure.ANDROID_ID
    )

    var localServiceName: String = ""

    // Mutex criado para enfileirar as resoluções de serviço e evitar o travamento do NsdManager
    private val resolveMutex = Mutex()

    fun registerService(port: Int): Flow<RegistrationStatus> = callbackFlow {
        multicastLock.acquire()
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "NearNode-${android_id}"
            serviceType = nsdServiceType
            setPort(port)
        }

        val registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                localServiceName = nsdServiceInfo.serviceName
                trySend(RegistrationStatus.Registered(localServiceName))
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                trySend(RegistrationStatus.Failed("Error code: $errorCode"))
            }
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                trySend(RegistrationStatus.Unregistered)
            }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

        awaitClose {
//            multicastLock.release()
            nsdManager.unregisterService(registrationListener)
        }
    }

    fun discoverServices() = callbackFlow {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                trySend(DiscoveryStatus.Discovering)
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                when {
                    // Ignora o próprio serviço anunciado
                    serviceInfo.serviceName == localServiceName -> return
                    // Verifica se o tipo de serviço é o procurado
                    serviceInfo.serviceType.contains(nsdServiceType) -> {

                                    trySend(DiscoveryStatus.Found(
                                        serviceInfo

                                    ))
                    }
                }
            }


            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                // Tratamento opcional
            }

            override fun onDiscoveryStopped(regType: String) {
                trySend(DiscoveryStatus.Stopped)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {

            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {

            }
        }

        nsdManager.discoverServices(nsdServiceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }
    fun stopService() {
//        multicastLock.release()
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(p0: String?) {
//                TODO("Not yet implemented")
            }

            override fun onDiscoveryStopped(p0: String?) {
//                TODO("Not yet implemented")
            }

            override fun onServiceFound(p0: NsdServiceInfo?) {
//                TODO("Not yet implemented")
            }

            override fun onServiceLost(p0: NsdServiceInfo?) {
//                TODO("Not yet implemented")
            }

            override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
//                TODO("Not yet implemented")
            }

            override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
//                TODO("Not yet implemented")
            }

        }
        nsdManager.stopServiceDiscovery(discoveryListener)
    }
    suspend fun resolveService(serviceInfo: NsdServiceInfo): NsdServiceInfo? = resolveMutex.withLock {
        suspendCancellableCoroutine { continuation ->
            val resolveListener = object : NsdManager.ResolveListener {
                override fun onServiceResolved(resolvedServiceInfo: NsdServiceInfo) {
                    if (continuation.isActive) {
                        continuation.resume(resolvedServiceInfo)
                    }
                }

                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }

            nsdManager.resolveService(serviceInfo, resolveListener)

            continuation.invokeOnCancellation {
                // O NsdManager infelizmente não tem um método "cancelResolveService"
            }
        }
    }
}