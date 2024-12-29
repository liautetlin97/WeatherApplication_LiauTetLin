package com.liautetlin.weatherapplication

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherViewModelTest {
    private lateinit var viewModel: WeatherViewModel
    private val weatherService: WeatherService = mockk()
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel(weatherService)

    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun `test fetchWeather success`() = runTest {
        val weatherResponse = WeatherResponse(
            name = "City",
            main = Main(temp = 25.0, humidity = 65.0, pressure = 1012, feelsLike = 25.0),
            weather = listOf(Weather(description = "clear sky", icon = "11d")),Wind(speed = 15.0),
        )
        coEvery { weatherService.getWeather("Selangor") } returns weatherResponse

        viewModel.fetchWeather("Selangor")

        assert(viewModel.state.value is WeatherState.Success)
        val successState = viewModel.state.value as WeatherState.Success
        assertEquals("City", successState.weatherResponse.name)
        assertEquals(25.0, successState.weatherResponse.main.temp, 0.1)
    }

    @Test
    fun `test fetchWeather failure`() = runTest {
        coEvery { weatherService.getWeather("UnknownCity") } throws Exception("Network error")

        viewModel.fetchWeather("UnknownCity")

        assert(viewModel.state.value is WeatherState.Error)
        val errorState = viewModel.state.value as WeatherState.Error
        assertEquals("Failed to load weather data", errorState.message)

    }

    @Test
    fun `test initial state is Idle`() {
        assertTrue(viewModel.state.value is WeatherState.Idle)
    }
}