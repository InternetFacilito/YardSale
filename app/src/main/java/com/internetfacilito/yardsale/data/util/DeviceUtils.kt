package com.internetfacilito.yardsale.data.util

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceUtils {
    
    /**
     * Obtiene un ID único para el dispositivo
     * Si no se puede obtener el ANDROID_ID, genera un UUID único
     */
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        
        return if (androidId != null && androidId != "9774d56d682e549c") {
            androidId
        } else {
            // Si no se puede obtener ANDROID_ID, usar UUID
            getOrCreateUUID(context)
        }
    }
    
    private fun getOrCreateUUID(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("device_uuid", null)
        
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("device_uuid", uuid).apply()
        }
        
        return uuid
    }
} 