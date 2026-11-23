package com.notifgenerator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Nova Notificação"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: ""
        val type = intent.getStringExtra("EXTRA_TYPE") ?: NotificationHelper.TYPE_BIG_PICTURE
        val imagePath = intent.getStringExtra("EXTRA_IMAGE_PATH")

        // Assegurar canal criado
        NotificationHelper.createNotificationChannel(context)

        // Exibir notificação
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            type = type,
            imagePath = imagePath
        )
    }
}
