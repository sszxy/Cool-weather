package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 张翔宇 on 2017/10/17.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    @SerializedName("cw")
    public Carwash carwash;
    public Sport sport;
    public class Sport{
        @SerializedName("txt")
        public String info;

    }
    public class Comfort{
        @SerializedName("txt")
        public String info;

    }
    public class Carwash{
        @SerializedName("txt")
        public String info;

    }
}
