package project.moms.attractions.data

import androidx.room.Database
import androidx.room.RoomDatabase
import project.moms.attractions.model.Photo

@Database(
    entities = [Photo::class],
    version = 1
)
abstract class GalleryDataBase : RoomDatabase() {
    abstract fun galleryDao() : GalleryDao
}