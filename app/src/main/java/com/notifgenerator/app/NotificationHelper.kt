package com.notifgenerator.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import java.io.File

object NotificationHelper {

    const val TYPE_BIG_PICTURE = "BIG_PICTURE"
    const val TYPE_PIX = "PIX"
    const val TYPE_STANDARD = "STANDARD"

    fun getOrCreateChannel(context: Context, appName: String): String {
        val safeName = appName.trim().ifEmpty { "Notificações" }
        val channelId = "channel_${safeName.lowercase().replace(" ", "_")}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    safeName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações de $safeName"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
        return channelId
    }

    fun showNotification(
        context: Context,
        appName: String,
        title: String,
        message: String,
        type: String,
        imagePath: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = getOrCreateChannel(context, appName)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decodificar imagem salva se existir
        val bitmap: Bitmap? = imagePath?.let {
            val file = File(it)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }

        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setSubText(appName.trim().ifEmpty { "Notificação" }) // Nome alternável do App no topo
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        when (type) {
            TYPE_BIG_PICTURE -> {
                if (bitmap != null) {
                    val bigPicStyle = NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle(title)
                        .setSummaryText(message)

                    builder.setStyle(bigPicStyle)
                    builder.setLargeIcon(bitmap)
                } else {
                    builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
                }
            }

            TYPE_PIX -> {
                builder.setColor(0xFF10B981.toInt()) // Verde Pix / Bancário
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                }
                val pixStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(message)
                    .setSummaryText("Transferência Instantânea")
                builder.setStyle(pixStyle)
            }

            TYPE_STANDARD -> {
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                }
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
