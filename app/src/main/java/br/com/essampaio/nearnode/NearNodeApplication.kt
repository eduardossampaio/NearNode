package br.com.essampaio.nearnode

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import br.com.essampaio.nearnode.data.repository.MessageRepositoryImpl
import br.com.essampaio.nearnode.data.repository.ProfileRepositoryImpl
import br.com.essampaio.nearnode.database.NearNodeDatabase
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.data.service.AndroidDeviceIdentificationService
import br.com.essampaio.nearnode.data.service.nsdService.impl.NSDServiceImpl
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService

import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import br.com.essampaio.nearnode.domain.usecase.BecomeUnavailableUseCase
import br.com.essampaio.nearnode.domain.usecase.DiscoveryNearbyUseCase
import br.com.essampaio.nearnode.presentation.MainViewModel
import br.com.essampaio.nearnode.presentation.screen.chat.ChatViewModel
import br.com.essampaio.nearnode.presentation.screen.listcontact.ListContactViewModel
import br.com.essampaio.nearnode.presentation.screen.newchat.NewChatViewModel
import br.com.essampaio.nearnode.presentation.screen.registration.RegistrationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class NearNodeApplication : Application() {
    val appModule = module {
        viewModelOf(::MainViewModel)
        viewModelOf(::ViewContactsViewModel)
        viewModelOf(::RegistrationViewModel)
        viewModelOf(::ListContactViewModel)
        viewModelOf(::NewChatViewModel)
        viewModel { (contactId: String) -> ChatViewModel(contactId, get()) }

        single {
            val driver = AndroidSqliteDriver(NearNodeDatabase.Schema, get(), "nearnode.db")
            NearNodeDatabase(driver)
        }

        factory<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
        factory<MessageRepository> { MessageRepositoryImpl(get()) }
        factory<NSDService> { NSDServiceImpl(get(), get()) }
        factory<DeviceIdentificationService> { AndroidDeviceIdentificationService(get()) }
        factory { BecomeAvailableUseCase(get(),get()) }
        factory { BecomeUnavailableUseCase(get(), get()) }
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