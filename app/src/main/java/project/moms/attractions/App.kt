package project.moms.attractions

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.yandex.mapkit.MapKitFactory
import project.moms.attractions.data.GalleryDataBase

class App : Application() {

    lateinit var db: GalleryDataBase
    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            GalleryDataBase::class.java,
            "db"
        ).build()

        MapKitFactory.setApiKey(API_KEY)
        MapKitFactory.initialize(this)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Notification channel"
        val descriptionText = "Notification channel for the project"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val API_KEY = "7dcdf03c-1b01-40bb-bbb6-049ea4d25642"
        const val NOTIFICATION_CHANNEL_ID = "test_channel_id"
    }
}