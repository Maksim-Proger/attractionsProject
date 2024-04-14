package project.moms.attractions

import android.app.Application
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
    }

    companion object {
        private const val API_KEY = "7dcdf03c-1b01-40bb-bbb6-049ea4d25642"
    }
}