package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.example.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 671c6ef98bc098a4ccc0aa3877a6fc1d --> Api key

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fetchWeatherData("cairo")
        searchCity()
    }

    private fun wallpaperChangeAccordingToCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Clear", "Sunny" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimation.setAnimation(R.raw.sun)
            }
            "Partly Clouds", "Clouds", "Overcast" , "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimation.setAnimation(R.raw.cloud)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimation.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimation.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimation.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimation.playAnimation()
    }


    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiInterface = retrofit.create(ApiInterface::class.java)

        val response = apiInterface.getWeatherData(cityName, "0b38e71e9ff470237fab6f61687fa991", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val temperature = responseBody.main.temp.toString()
                        val humidity = responseBody.main.humidity
                        val windSpeed = responseBody.wind.speed
                        val sunRise = responseBody.sys.sunrise.toLong()
                        val sunSet = responseBody.sys.sunset.toLong()
                        val seaLevel = responseBody.main.pressure
                        val maxTemp = responseBody.main.temp_max
                        val minTemp = responseBody.main.temp_min
                        val condition = responseBody.weather.firstOrNull()?.main?: "UnKnown"


                        binding.temp.text = "$temperature °C"
                        binding.caseText.text = condition
                        binding.maxText.text = "Max $maxTemp C"
                        binding.minText.text = "Min $minTemp C"
                        binding.humidity.text = "$humidity %"
                        binding.sea.text = "$seaLevel hPa"
                        binding.windSpeed.text = "$windSpeed m/s"
                        binding.sunrise.text = "${time(sunRise)}"
                        binding.sunset.text = "${time(sunSet)}"
                        binding.conditions.text = condition
                        binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.city.text = "$cityName"

                        wallpaperChangeAccordingToCondition(condition)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                Log.e("TAG", "Error: ${t.message}")
            }
        })
    }

    private fun searchCity() {
        val search = binding.searchCity
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    private fun dayName(timestamp: Long) :String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}