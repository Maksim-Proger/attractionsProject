package project.moms.attractions.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray
)