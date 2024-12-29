package com.liautetlin.weatherapplication

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
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
            .background(Color(0xFFD1E8E2))
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .offset(y = searchBarOffset),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = stringResource(id = R.string.city),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(cityName, viewModel, searchClicked = searchClicked)

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
fun SearchBar(
    searchQuery: MutableState<TextFieldValue>,
    viewModel: WeatherViewModel,
    searchClicked: MutableState<Boolean>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = searchQuery.value,
        onValueChange = { searchQuery.value = it },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search),
                style = TextStyle(fontSize = 14.sp)
            )
        },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            searchClicked.value = true
            keyboardController?.hide()
            viewModel.fetchWeather(searchQuery.value.text)
        }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.LightGray,
        ),
        textStyle = TextStyle(color = Color.Black)
    )
}

@Composable
private fun WeatherResponseFail(
    weatherState: WeatherState.Error,
    viewModel: WeatherViewModel,
    cityName: MutableState<TextFieldValue>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Spacer(modifier = Modifier.height(16.dp))
    Text(weatherState.message)
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = {
        viewModel.fetchWeather(cityName.value.text)
        keyboardController?.hide()
    }) {
        Text(text = stringResource(id = R.string.retry))
    }
}

@Composable
private fun WeatherResponseSuccess(weatherState: WeatherState.Success) {
    val iconCode = weatherState.weatherResponse.weather.firstOrNull()?.icon
    val imageUrl = iconCode?.let { stringResource(R.string.weather_image_url, it) }

    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = "",
        modifier = Modifier
            .padding(top = 16.dp)
            .size(150.dp)
    )

    Text(
        weatherState.weatherResponse.name, style = TextStyle(
            color = Color(0xff333333),
            fontSize = 24.sp
        )
    )
    Text(
        stringResource(
            id = R.string.temperature_format,
            weatherState.weatherResponse.main.temp.toInt()
        ),
        modifier = Modifier
            .padding(top = 5.dp),
        style = TextStyle(
            color = Color(0xffFF5722), fontSize = 48.sp, fontWeight = FontWeight.Bold
        )
    )
    Text(
        text = weatherState.weatherResponse.weather.firstOrNull()?.description ?: "",
        style = TextStyle(
            color = Color(0xff333333),
            fontSize = 24.sp
        ), modifier = Modifier
            .padding(top = 5.dp)
    )

    WeatherInfoCard(
        stringResource(
            id = R.string.windSpeed_format,
            weatherState.weatherResponse.wind.speed
        ),
        stringResource(
            id = R.string.pressure_format,
            weatherState.weatherResponse.main.pressure
        ),
        stringResource(
            id = R.string.temperature_format,
            weatherState.weatherResponse.main.feelsLike.toInt()
        ),
        stringResource(
            id = R.string.humidity_format,
            weatherState.weatherResponse.main.humidity.toInt()
        )
    )
}

@Composable
fun WeatherInfoCard(
    windSpeed: String,
    pressure: String,
    feelsLike: String,
    humidity: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherInfoBox(title = stringResource(R.string.wind_speed), value = windSpeed)
            VerticalDivider(color = Color.Gray.copy(alpha = 0.3f),modifier = Modifier.padding(horizontal = 5.dp), thickness = 1.dp)
            WeatherInfoBox(title = stringResource(R.string.pressure), value = pressure)
        }

        HorizontalDivider(modifier = Modifier.padding())

        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherInfoBox(title = stringResource(R.string.feels_like), value = feelsLike)
            VerticalDivider(modifier = Modifier.padding(horizontal = 5.dp), thickness = 1.dp)
            WeatherInfoBox(title = stringResource(R.string.humidity), value = humidity)
        }
    }
}

@Composable
fun RowScope.WeatherInfoBox(title: String, value: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .padding(10.dp)
    ) {
        value?.let {
            Text(
                text = it, style = TextStyle(
                    fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
            )
        } ?: ""
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title, style = TextStyle(
                fontSize = 13.sp
            )
        )
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}