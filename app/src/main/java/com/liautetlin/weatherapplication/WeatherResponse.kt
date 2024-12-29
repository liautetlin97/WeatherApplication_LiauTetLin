package com.liautetlin.weatherapplication

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind,
)

data class Wind(
    @SerializedName("speed")
    val speed: Double
)

data class Main(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("humidity")
    val humidity: Double,
    @SerializedName("pressure")
    val pressure: Int,
)

data class Weather(
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
)
