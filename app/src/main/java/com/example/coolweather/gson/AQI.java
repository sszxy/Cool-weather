package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 张翔宇 on 2017/10/17.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
