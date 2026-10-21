package com.example.appjuntavecinos.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

// El "trabajador": Android lo ejecuta aunque la app esté cerrada
class AvisoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Junta Zaror"
        val mensaje = inputData.getString("mensaje") ?: ""

        // Canal de notificación (obligatorio desde Android 8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel("avisos", "Avisos", NotificationManager.IMPORTANCE_DEFAULT)
            applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        // Solo se muestra si el usuario dio el permiso
        val permitido = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

        if (permitido) {
            val notificacion = NotificationCompat.Builder(applicationContext, "avisos")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(applicationContext).notify(1, notificacion)
        }
        return Result.success()
    }
}

// Se llama desde cualquier parte: "muestra este aviso en X segundos"
fun programarAviso(context: Context, titulo: String, mensaje: String, segundos: Long = 10) {
    val tarea = OneTimeWorkRequestBuilder<AvisoWorker>()
        .setInitialDelay(segundos, TimeUnit.SECONDS)
        .setInputData(workDataOf("titulo" to titulo, "mensaje" to mensaje))
        .build()
    WorkManager.getInstance(context).enqueue(tarea)
}
