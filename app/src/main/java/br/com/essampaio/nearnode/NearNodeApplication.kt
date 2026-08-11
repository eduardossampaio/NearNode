package br.com.essampaio.nearnode

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class NearNodeApplication : Application() {
    val appModule = module {
        viewModelOf(::ViewContactsViewModel)

        factory { NSDHelper(get()) }
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