package com.notifgenerator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: "Nubank"
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Nova Notificação"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: ""
        val type = intent.getStringExtra("EXTRA_TYPE") ?: NotificationHelper.TYPE_BIG_PICTURE
        val imagePath = intent.getStringExtra("EXTRA_IMAGE_PATH")

        // Exibir notificação com nome dinâmico do app
        NotificationHelper.showNotification(
            context = context,
            appName = appName,
            title = title,
            message = message,
            type = type,
            imagePath = imagePath
        )
    }
}
