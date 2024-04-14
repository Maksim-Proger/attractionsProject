package project.moms.attractions.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://overpass-api.de/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: LandmarksApiService = retrofit.create(LandmarksApiService::class.java)
}