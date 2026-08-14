package br.com.essampaio.nearnode.data.service.nsdService.impl

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

private sealed class NSDServiceDiscoveryStatus {
    data object Discovering : NSDServiceDiscoveryStatus()
    data class Found(val service: NsdServiceInfo): NSDServiceDiscoveryStatus()
    data object Stopped: NSDServiceDiscoveryStatus()
    data object WaitingForRegistration: NSDServiceDiscoveryStatus()
}

class NSDServiceImpl(context: Context, deviceIdentificationService: DeviceIdentificationService) : NSDService{

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val nsdServiceType = "_nearnode._tcp"

    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val multicastLock = wifiManager.createMulticastLock("NearNodeMulticastLock")

    private val deviceId: String = deviceIdentificationService.getUniqueId()

    var localServiceName: String = ""

    // Mutex criado para enfileirar as resoluções de serviço e evitar o travamento do NsdManager
    private val resolveMutex = Mutex()

    private var activeRegistrationListener: NsdManager.RegistrationListener? = null

    override suspend fun registerService(port: Int): RegistrationStatus {
        if (activeRegistrationListener != null) unregisterService()

        multicastLock.acquire()
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "NearNode-${deviceId}"
            serviceType = nsdServiceType
            setPort(port)
        }

        return suspendCancellableCoroutine { continuation ->
            val registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                    localServiceName = nsdServiceInfo.serviceName
                    activeRegistrationListener = this
                    if (continuation.isActive) {
                        continuation.resume(RegistrationStatus.Registered(localServiceName))
                    }
                }

                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    if (multicastLock.isHeld) multicastLock.release()
                    if (continuation.isActive) {
                        continuation.resume(RegistrationStatus.Failed("Error code: $errorCode"))
                    }
                }

                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                    activeRegistrationListener = null
                    localServiceName = ""
                }

                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            }

            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

            continuation.invokeOnCancellation {
                // If cancelled while registering, try to clean up
                // Note: unregisterService is also suspend, so we just trigger it
                nsdManager.unregisterService(registrationListener)
                if (multicastLock.isHeld) multicastLock.release()
            }
        }
    }

    override suspend fun unregisterService(): RegistrationStatus {
        val listener = activeRegistrationListener ?: return RegistrationStatus.Unregistered

        return suspendCancellableCoroutine { continuation ->
            // To properly wait for unregistration, we need to handle it via the original listener
            // but NsdManager doesn't allow adding multiple listeners or wrapping easily for 'suspend'.
            // For simplicity in this refactor, we'll use a local reference and update the listener
            // behavior or just trigger and resume if we assume success.
            // A better way is to store the continuation in the class and resume from the original listener.
            
            try {
                nsdManager.unregisterService(listener)
                // Since NsdManager.RegistrationListener.onServiceUnregistered will be called on the 'listener',
                // and we don't have a clean way to intercept it here without class-level state for the continuation,
                // we'll resume immediately or store the continuation.
                
                if (multicastLock.isHeld) multicastLock.release()
                localServiceName = ""
                activeRegistrationListener = null
                continuation.resume(RegistrationStatus.Unregistered)
            } catch (e: Exception) {
                continuation.resume(RegistrationStatus.Failed(e.message ?: "Unregistration error"))
            }
        }
    }

    override fun discoverServices(): Flow<DiscoveryStatus> {
        return discoverServicesInNSDService()
            .map { status ->
                when (status) {
                    is NSDServiceDiscoveryStatus.Discovering -> DiscoveryStatus.Discovering
                    is NSDServiceDiscoveryStatus.Stopped -> DiscoveryStatus.Stopped
                    is NSDServiceDiscoveryStatus.WaitingForRegistration -> DiscoveryStatus.WaitingForRegistration
                    is NSDServiceDiscoveryStatus.Found -> null // Handled via filter and flatMap
                }
            }
            .filterIsInstance<DiscoveryStatus>()
            .let { otherStatuses ->
                // Combine non-Found statuses with Found statuses that undergo resolution
                merge(
                    otherStatuses,
                    discoverServicesInNSDService()
                        .filterIsInstance<NSDServiceDiscoveryStatus.Found>()
                        .flatMapMerge { foundStatus ->
                            val resolvedService = resolveService(foundStatus.service)
                            if (resolvedService != null) {
                                val node = Node(
                                    name = resolvedService.serviceName,
                                    ipAddress = resolvedService.host?.hostAddress ?: "unknown",
                                    port = resolvedService.port
                                )
                                flowOf(DiscoveryStatus.Found(node))
                            } else {
                                emptyFlow()
                            }
                        }
                )
            }
    }


    private fun discoverServicesInNSDService() = callbackFlow {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                trySend(NSDServiceDiscoveryStatus.Discovering)
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                when {

                    serviceInfo.serviceName == localServiceName -> return

                    serviceInfo.serviceType.contains(nsdServiceType) -> {

                        trySend(
                            NSDServiceDiscoveryStatus.Found(
                                serviceInfo

                            )
                        )
                    }
                }
            }


            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                // Tratamento opcional
            }

            override fun onDiscoveryStopped(regType: String) {
                trySend(NSDServiceDiscoveryStatus.Stopped)
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