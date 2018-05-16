package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoupdataService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private Button button;
    private ScrollView weatherlayout;
    private TextView titleCity;
    private TextView titleupdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView  aqitext;
    private TextView  pm25text;
    private TextView  comforttext;
    private TextView   carwashtext;
    private TextView  sporttext;
    private LinearLayout linearLayout;
    private ImageView imageView;
    public SwipeRefreshLayout refreshweather;
    private String mweatherID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT
            );
        }
        setContentView(R.layout.activity_weather);
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        button= (Button) findViewById(R.id.nab_button);
        imageView= (ImageView) findViewById(R.id.bing_pic_img);
        weatherlayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleupdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        linearLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqitext= (TextView) findViewById(R.id.aqi_text);
        pm25text= (TextView) findViewById(R.id.pm25_text);
        comforttext= (TextView) findViewById(R.id.comfort_text);
        carwashtext= (TextView) findViewById(R.id.car_wash_text);
        sporttext= (TextView) findViewById(R.id.sport_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        refreshweather= (SwipeRefreshLayout) findViewById(R.id.refresh);
        refreshweather.setColorSchemeResources(R.color.colorAccent);
        refreshweather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
             requestWeather(mweatherID);
            }
        });
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String img=prefs.getString("bing",null);
        if(img!=null){
            Glide.with(WeatherActivity.this).load(img).into(imageView);
        }
        else {
            loadbing();
        }
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mweatherID=weather.basic.weatherId;
            showweatherInfo(weather);
        }
        else {
             mweatherID=getIntent().getStringExtra("weather_id");
            requestWeather(mweatherID);
        }

    }
    public void loadbing(){
        HttpUtil.sendOKHttpRequest("http://guolin.tech/api/bing_pic", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingimg=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing",bingimg);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingimg).into(imageView);
                    }
                });
            }
        });

    }
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        refreshweather.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            Intent intent=new Intent(WeatherActivity.this, AutoupdataService.class);
                            startService(intent);
                            showweatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this,"加载天气失败",Toast.LENGTH_SHORT).show();
                        }
                        refreshweather.setRefreshing(false);
                    }
                });
            }
        });


    }

    private void showweatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updataTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleupdateTime.setText(updataTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        linearLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,linearLayout,false);
            TextView dataText= (TextView) view.findViewById(R.id.data_text);
            TextView indoText= (TextView) view.findViewById(R.id.info_text);
            TextView maxText= (TextView) view.findViewById(R.id.max_text);
            TextView minText= (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.data);
            indoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            linearLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqitext.setText(weather.aqi.city.aqi);
            pm25text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度"+weather.suggestion.comfort.info;
        String carWash="洗车指数"+weather.suggestion.carwash.info;
        String sport="运动指数"+weather.suggestion.sport.info;
        comforttext.setText(comfort);
        carwashtext.setText(carWash);
        sporttext.setText(sport);
        weatherlayout.setVisibility(View.VISIBLE);
    }
}
