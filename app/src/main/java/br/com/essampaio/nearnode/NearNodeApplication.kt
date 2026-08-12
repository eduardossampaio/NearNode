package br.com.essampaio.nearnode

import android.app.Application
import br.com.essampaio.nearnode.data.service.AndroidDeviceIdentificationService
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.impl.NSDServiceImpl
import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import br.com.essampaio.nearnode.domain.usecase.BecomeUnavailableUseCase
import br.com.essampaio.nearnode.domain.usecase.DiscoveryNearbyUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class NearNodeApplication : Application() {
    val appModule = module {
        viewModelOf(::ViewContactsViewModel)

        factory<NSDService> { NSDServiceImpl(get(), get()) }
        factory<DeviceIdentificationService> { AndroidDeviceIdentificationService(get()) }
        factory { BecomeAvailableUseCase(get()) }
        factory { BecomeUnavailableUseCase(get()) }
        factory { DiscoveryNearbyUseCase(get()) }
    }
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@NearNodeApplication)
            modules(appModule)
        }
    }
}