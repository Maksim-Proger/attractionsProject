package project.moms.attractions.presentation.partWithMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import project.moms.attractions.data.api.LandmarksApiService

class MapViewModelFactory(
    private val apiService: LandmarksApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}