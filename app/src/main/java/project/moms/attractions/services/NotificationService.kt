package project.moms.attractions.services

import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import project.moms.attractions.App
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import project.moms.attractions.R

// TODO добавить запрос разрешения на получение уведомлений у пользователя.
class NotificationService(
    val context: Context
) {

    fun createNotification() {
        val notification = NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("")
            .setContentText("Ваша локация определена.")
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED ) {return}
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1000
    }
}