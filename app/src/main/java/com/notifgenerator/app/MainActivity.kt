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

    // Seletor de imagens da galeria
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

        // Criar o canal de notificação logo ao iniciar
        NotificationHelper.createNotificationChannel(this)

        checkAndRequestNotificationPermission()
        setupListeners()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun setupListeners() {
        // Escolha da imagem
        binding.layoutSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Sugestões automáticas ao mudar o tipo de notificação
        binding.rgNotifType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPix -> {
                    if (binding.etTitle.text.isNullOrBlank() || binding.etTitle.text.toString() == "Notícia Urgente") {
                        binding.etTitle.setText("Nubank")
                        binding.etMessage.setText("Você recebeu uma transferência Pix de R$ 1.500,00 de Banco Central.")
                    }
                }
                R.id.rbBigPicture -> {
                    if (binding.etTitle.text.toString() == "Nubank") {
                        binding.etTitle.setText("Nova Foto em Destaque")
                        binding.etMessage.setText("Veja a imagem completa que acaba de ser compartilhada com você.")
                    }
                }
                R.id.rbStandard -> {
                    if (binding.etTitle.text.toString() == "Nova Foto em Destaque") {
                        binding.etTitle.setText("Mensagem Importante")
                        binding.etMessage.setText("Você recebeu uma nova mensagem com foto.")
                    }
                }
            }
        }

        // Botão disparar notificação
        binding.btnTriggerNotification.setOnClickListener {
            triggerNotification()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                // Carregar bitmap com escala segura
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2 // Evitar estouro de memória
                }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

                if (bitmap != null) {
                    // Salvar temporariamente no cache interno
                    val cacheFile = File(cacheDir, "temp_notification_image.png")
                    val out = FileOutputStream(cacheFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.flush()
                    out.close()

                    cachedImagePath = cacheFile.absolutePath

                    // Atualizar preview visual na tela
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
        val title = binding.etTitle.text.toString().trim().ifEmpty { "Notificação de Teste" }
        val message = binding.etMessage.text.toString().trim().ifEmpty { "Mensagem de teste gerada." }

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
            // Disparo imediato
            NotificationHelper.showNotification(
                context = this,
                title = title,
                message = message,
                type = type,
                imagePath = cachedImagePath
            )
            Toast.makeText(this, "Notificação enviada com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            // Agendamento com AlarmManager para funcionar mesmo com tela bloqueada
            scheduleNotification(title, message, type, delaySeconds)
        }
    }

    private fun scheduleNotification(title: String, message: String, type: String, delaySeconds: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, NotificationReceiver::class.java).apply {
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
            // Fallback caso não tenha permissão de alarme exato no Android 14+
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
