package project.moms.attractions.data.api

import okhttp3.RequestBody
import project.moms.attractions.model.LandmarkResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LandmarksApiService {
    @POST("interpreter")
    fun getLandmarks(@Body requestBody: RequestBody) : Call<LandmarkResponse>
}