package com.notifgenerator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: ""
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: ""
        val type = intent.getStringExtra("EXTRA_TYPE") ?: NotificationHelper.TYPE_STANDARD
        val imagePath = intent.getStringExtra("EXTRA_IMAGE_PATH")

        // Exibir notificação
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
