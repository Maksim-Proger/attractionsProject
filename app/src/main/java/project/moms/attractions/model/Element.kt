package project.moms.attractions.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Element(
    val name: String?,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>
) : Parcelable
