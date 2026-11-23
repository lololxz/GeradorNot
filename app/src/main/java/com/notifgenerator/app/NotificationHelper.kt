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

    const val CHANNEL_ID = "custom_notifications_channel"
    private const val CHANNEL_NAME = "Notificações Personalizadas"
    private const val CHANNEL_DESC = "Canal para notificações geradas com imagens e alertas"

    const val TYPE_BIG_PICTURE = "BIG_PICTURE"
    const val TYPE_PIX = "PIX"
    const val TYPE_STANDARD = "STANDARD"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        type: String,
        imagePath: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir o app ao clicar na notificação
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        when (type) {
            TYPE_BIG_PICTURE -> {
                // Notificação com imagem grande expandida
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
                // Notificação estilo Bancária / Pix
                builder.setSubText("Pix Recebido • Banco")
                builder.setColor(0xFF10B981.toInt()) // Verde Pix
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
                // Notificação padrão com avatar/ícone
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                }
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
