package com.liautetlin.weatherapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherApp(weatherViewModel)
        }
    }
}

@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    val weatherState = viewModel.state.collectAsState().value
    val cityName = remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val searchClicked = remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val centerOffset = screenHeight / 3
    val searchBarOffset by animateDpAsState(
        targetValue = if (searchClicked.value) 0.dp else centerOffset,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = Modifier
            .offset(y = searchBarOffset)
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.city),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(cityName, viewModel, searchClicked = searchClicked)

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(80.dp))

        when (weatherState) {
            is WeatherState.Loading -> {
                CircularProgressIndicator()
            }

            is WeatherState.Success -> {
                WeatherResponseSuccess(weatherState)
            }

            is WeatherState.Error -> {
                WeatherResponseFail(weatherState, viewModel, cityName)
            }

            else -> {}

        }
    }
}

@Composable
private fun WeatherResponseFail(
    weatherState: WeatherState.Error,
    viewModel: WeatherViewModel,
    cityName: MutableState<TextFieldValue>
) {
    Text(weatherState.message)
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { viewModel.fetchWeather(cityName.value.text) }) {
        Text(text = stringResource(id = R.string.retry))
    }
}

@Composable
private fun WeatherResponseSuccess(weatherState: WeatherState.Success) {
    Text(
        weatherState.weatherResponse.name, style = TextStyle(
            color = Color(0xff333333),
            fontSize = 24.sp
        )
    )
    Text(
        stringResource(id = R.string.temperature_format, weatherState.weatherResponse.main.temp),
        style = TextStyle(
            color = Color(0xffFF5722), fontSize = 48.sp, fontWeight = FontWeight.Bold
        )
    )
    Text(
        weatherState.weatherResponse.weather[0].description, style = TextStyle(
            color = Color(0xff333333),
            fontSize = 24.sp,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: MutableState<TextFieldValue>,
    viewModel: WeatherViewModel,
    searchClicked: MutableState<Boolean>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = searchQuery.value,
        onValueChange = { searchQuery.value = it },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search),
                style = TextStyle(fontSize = 14.sp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xfff1f1f1), shape = RoundedCornerShape(50))
            .border(1.dp, Color(0xffb0b0b0), shape = RoundedCornerShape(50.dp))
            .padding(1.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            searchClicked.value = true
            keyboardController?.hide()
            viewModel.fetchWeather(searchQuery.value.text)
        }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        textStyle = TextStyle(color = Color.Black)
    )
}
