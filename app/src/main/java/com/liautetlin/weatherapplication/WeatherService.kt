package com.liautetlin.weatherapplication

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "39e4969c37922ca3d81e1a001185da63",
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
