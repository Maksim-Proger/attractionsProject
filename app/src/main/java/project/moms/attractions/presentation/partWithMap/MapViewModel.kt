package project.moms.attractions.presentation.partWithMap

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.map.PlacemarkMapObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import project.moms.attractions.data.api.LandmarksApiService
import project.moms.attractions.model.Element
import project.moms.attractions.model.LandmarkResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel(
    private val apiService: LandmarksApiService
) : ViewModel() {

    private var _permissionToastShown = false
    var permissionToastShown: Boolean = false

    init {
        _permissionToastShown = false
    }

    val landmarkData: MutableLiveData<List<Element>> by lazy {
        MutableLiveData<List<Element>>()
    }

    fun fetchLandmarks() {
        val requestBody = createRequestBody()
        val call = apiService.getLandmarks(requestBody)

        call.enqueue(object : Callback<LandmarkResponse> {
            override fun onResponse(
                call: Call<LandmarkResponse>,
                response: Response<LandmarkResponse>
            ) {
                if (response.isSuccessful) {
                    val landmarkResponse = response.body()
                    landmarkResponse?.elements?.let {
                        Log.d("MapViewModel", "Received landmarks: $it")
                        landmarkData.value = it
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch landmarks: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LandmarkResponse>, t: Throwable) {
                Log.e("MainActivity", "Failed to fetch landmarks: ${t.message}")
            }
        })
    }

    private fun createRequestBody(): RequestBody {
        val query = "[out:json];node[\"tourism\"=\"museum\"](55.55,37.35,55.95,37.85);out;"
        return RequestBody.create("application/x-www-form-urlencoded"
            .toMediaTypeOrNull(), "data=$query")
    }
}