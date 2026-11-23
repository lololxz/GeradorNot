package com.notifgenerator.app

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.notifgenerator.app.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cachedImagePath: String? = null

    // Seletor de fotos da galeria
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleSelectedImage(it) }
        }

    // Permissão de notificação para Android 13+ (API 33)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permissão de notificação concedida!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permissão necessária para exibir notificações na tela.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestNotificationPermission()
        setupListeners()
        setupPresets()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun setupPresets() {
        binding.btnPresetLimpo.setOnClickListener {
            binding.etAppName.setText("")
            Toast.makeText(this, "Subtexto limpo! A notificação não terá nenhum texto extra.", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetNubank.setOnClickListener {
            binding.etAppName.setText("Nubank")
            binding.etTitle.setText("Transferência recebida")
            binding.etMessage.setText("Você recebeu uma transferência Pix de R$ 1.500,00.")
            Toast.makeText(this, "Preset Nubank aplicado!", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetWhatsApp.setOnClickListener {
            binding.etAppName.setText("WhatsApp")
            binding.etTitle.setText("Contato")
            binding.etMessage.setText("Oi! Tudo bem?")
            binding.rbStandard.isChecked = true
            Toast.makeText(this, "Preset WhatsApp aplicado!", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetInstagram.setOnClickListener {
            binding.etAppName.setText("Instagram")
            binding.etTitle.setText("Nova publicação")
            binding.etMessage.setText("Publicou uma nova foto.")
            binding.rbBigPicture.isChecked = true
            Toast.makeText(this, "Preset Instagram aplicado!", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetMercadoPago.setOnClickListener {
            binding.etAppName.setText("Mercado Pago")
            binding.etTitle.setText("Pix recebido!")
            binding.etMessage.setText("Você recebeu um Pix na sua conta.")
            Toast.makeText(this, "Preset Mercado Pago aplicado!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.layoutSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnTriggerNotification.setOnClickListener {
            triggerNotification()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

                if (bitmap != null) {
                    val cacheFile = File(cacheDir, "temp_notification_image.png")
                    val out = FileOutputStream(cacheFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.flush()
                    out.close()

                    cachedImagePath = cacheFile.absolutePath

                    binding.ivPreview.setImageBitmap(bitmap)
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.placeholderContainer.visibility = View.GONE
                    binding.btnSelectImage.text = "Alterar Foto Selecionada"

                    Toast.makeText(this, "Foto selecionada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao carregar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerNotification() {
        // Pega EXATAMENTE o que o usuário digitou, sem forçar textos automáticos!
        val appName = binding.etAppName.text?.toString()?.trim() ?: ""
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        val message = binding.etMessage.text?.toString()?.trim() ?: ""

        val type = when (binding.rgNotifType.checkedRadioButtonId) {
            R.id.rbBigPicture -> NotificationHelper.TYPE_BIG_PICTURE
            R.id.rbPix -> NotificationHelper.TYPE_PIX
            else -> NotificationHelper.TYPE_STANDARD
        }

        val delaySeconds = when (binding.rgTimer.checkedRadioButtonId) {
            R.id.rbTimer5 -> 5
            R.id.rbTimer10 -> 10
            R.id.rbTimer30 -> 30
            else -> 0
        }

        if (delaySeconds == 0) {
            NotificationHelper.showNotification(
                context = this,
                appName = appName,
                title = title,
                message = message,
                type = type,
                imagePath = cachedImagePath
            )
            Toast.makeText(this, "Notificação disparada!", Toast.LENGTH_SHORT).show()
        } else {
            scheduleNotification(appName, title, message, type, delaySeconds)
        }
    }

    private fun scheduleNotification(
        appName: String,
        title: String,
        message: String,
        type: String,
        delaySeconds: Int
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("EXTRA_APP_NAME", appName)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
            putExtra("EXTRA_TYPE", type)
            putExtra("EXTRA_IMAGE_PATH", cachedImagePath)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = SystemClock.elapsedRealtime() + (delaySeconds * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Toast.makeText(
                this,
                "⏰ Notificação agendada para daqui a $delaySeconds segundos! Bloqueie a tela para testar.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Toast.makeText(
                this,
                "Notificação agendada para daqui a $delaySeconds segundos!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
