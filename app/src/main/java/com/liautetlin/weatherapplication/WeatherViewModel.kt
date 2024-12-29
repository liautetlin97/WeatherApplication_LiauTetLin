package com.liautetlin.weatherapplication

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherService: WeatherService
) : ViewModel(), LifecycleObserver {

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val state: StateFlow<WeatherState> get() = _state

    fun fetchWeather(city: String) {
        _state.value = WeatherState.Loading
        viewModelScope.launch {
            try {
                val response = weatherService.getWeather(city)
                _state.value = WeatherState.Success(response)
            } catch (e: Exception) {
                _state.value = WeatherState.Error("Failed to load weather data")
            }
        }
    }
}

sealed class WeatherState {
    data object Idle : WeatherState()
    data object Loading : WeatherState()
    data class Success(val weatherResponse: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}
