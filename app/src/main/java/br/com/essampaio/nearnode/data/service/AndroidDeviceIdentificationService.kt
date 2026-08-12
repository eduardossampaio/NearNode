package br.com.essampaio.nearnode.data.service

import android.content.Context
import android.provider.Settings
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService

class AndroidDeviceIdentificationService(private val context: Context) : DeviceIdentificationService {
    override fun getUniqueId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }
}