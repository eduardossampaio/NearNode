package br.com.essampaio.nearnode

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import br.com.essampaio.nearnode.data.repository.MessageRepositoryImpl
import br.com.essampaio.nearnode.data.repository.ProfileRepositoryImpl
import br.com.essampaio.nearnode.database.NearNodeDatabase
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.data.service.AndroidDeviceIdentificationService
import br.com.essampaio.nearnode.data.service.communication.BackgroundServerCommunicationService
import br.com.essampaio.nearnode.data.service.communication.RestfulServerNodeService
import br.com.essampaio.nearnode.data.service.nsdService.impl.NSDServiceImpl
import br.com.essampaio.nearnode.domain.service.CommunicationService
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import br.com.essampaio.nearnode.domain.service.RemoteNodeService
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService

import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import br.com.essampaio.nearnode.domain.usecase.BecomeUnavailableUseCase
import br.com.essampaio.nearnode.domain.usecase.DiscoveryNearbyUseCase
import br.com.essampaio.nearnode.domain.usecase.ListContactsUseCase
import br.com.essampaio.nearnode.presentation.MainViewModel
import br.com.essampaio.nearnode.presentation.screen.chat.ChatViewModel
import br.com.essampaio.nearnode.presentation.screen.listcontact.ListContactViewModel
import br.com.essampaio.nearnode.presentation.screen.newchat.NewChatViewModel
import br.com.essampaio.nearnode.presentation.screen.registration.RegistrationViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

class NearNodeApplication : Application() {
    val appModule = module {
        viewModelOf(::MainViewModel)
//        viewModelOf(::ViewContactsViewModel)
        viewModelOf(::RegistrationViewModel)
        viewModelOf(::ListContactViewModel)
        viewModelOf(::NewChatViewModel)
        viewModel { (contactId: String) -> ChatViewModel(contactId, get()) }

        single {
            val driver = AndroidSqliteDriver(NearNodeDatabase.Schema, get(), "nearnode.db")
            NearNodeDatabase(driver)
        }
        single<Gson> {
            GsonBuilder().create()
        }

        // 2. Injeta o OkHttpClient customizado para redes P2P (Singleton)
        single<OkHttpClient> {
            OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS) // Desiste rápido se o outro celular sumir
                .readTimeout(3, TimeUnit.SECONDS)
                .build()
        }
        factory<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
        factory<MessageRepository> { MessageRepositoryImpl(get()) }
        factory<NSDService> { NSDServiceImpl(get(), get()) }
        factory<CommunicationService> { BackgroundServerCommunicationService(get()) }
        factory<RemoteNodeService> { RestfulServerNodeService(get(), get()) }
        factory<DeviceIdentificationService> { AndroidDeviceIdentificationService(get()) }
        factory { BecomeAvailableUseCase(get(), get(), get()) }
        factory { BecomeUnavailableUseCase(get(), get(), get()) }
        factory { DiscoveryNearbyUseCase(get(), get(), get()) }
        factory { ListContactsUseCase(get(),get()) }
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