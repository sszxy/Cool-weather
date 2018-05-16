package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 张翔宇 on 2017/10/14.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response){
    if(!TextUtils.isEmpty(response)){
        try {
            JSONArray allProvince=new JSONArray(response);
            for(int i=0;i<allProvince.length();i++){
                JSONObject provinceobject=allProvince.getJSONObject(i);
                Province province=new Province();
                province.setProvinceName(provinceobject.getString("name"));
                province.setProvinceCode(provinceobject.getInt("id"));
                province.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    return false;
}
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityobject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setProvinceId(provinceId);
                    city.setCityName(cityobject.getString("name"));
                    city.setCityCode(cityobject.getInt("id"));
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountryResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountries=new JSONArray(response);
                for(int i=0;i<allCountries.length();i++){
                    JSONObject countryobject=allCountries.getJSONObject(i);
                    Country country=new Country();
                    country.setCityId(cityId);
                    country.setCountryName(countryobject.getString("name"));
                    country.setWeatherId(countryobject.getString("weather_id"));
                    country.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

