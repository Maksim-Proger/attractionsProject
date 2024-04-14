package project.moms.attractions.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import project.moms.attractions.model.Photo

@Dao
interface GalleryDao {

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<Photo>>

    @Insert
    suspend fun insert(photo: Photo): Long

}