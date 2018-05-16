package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 张翔宇 on 2017/10/15.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTRY=2;
    private ProgressDialog progressDialog;
    private TextView textView;
    private Button backbutton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectprovince;
    private City selectcity;
    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        textView=(TextView) view.findViewById(R.id.title_text);

        backbutton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.listview);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectprovince=provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY){
                    selectcity=cityList.get(position);
                    queryCountries();
                }
                else if (currentLevel==LEVEL_COUNTRY){
                    String weatherId=countryList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                    }

                    else if(getActivity()instanceof WeatherActivity){
                        WeatherActivity activity= (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.refreshweather.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }
               }
            }
        });
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTRY){
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }

            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        textView.setText("中国");
        backbutton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }
        else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }
    private void queryCities(){
        textView.setText(selectprovince.getProvinceName());
        backbutton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceId=?",String.valueOf(selectprovince.getId())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else{
            int provinceCode=selectprovince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }
    private void queryCountries(){
        textView.setText("中国");
        backbutton.setVisibility(View.VISIBLE);
        countryList= DataSupport.where("cityId=?",String.valueOf(selectcity.getId())).find(Country.class);
        if(countryList.size()>0) {
            datalist.clear();
            for (Country country : countryList) {
                datalist.add(country.getCountryName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }
        else{
            int provinceCode=selectprovince.getProvinceCode();
            int cityCode=selectcity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }

    }
   private void queryFromServer(String address,final String type){
       showProgressDialog();
       HttpUtil.sendOKHttpRequest(address, new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               getActivity().runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       closeProgressDialog();
                       Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                   }
               });

           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
               boolean result=false;
               if("province".equals(type)){
                   result= Utility.handleProvinceResponse(responseText);
               }
               else if("city".equals(type)){
                   result=Utility.handleCityResponse(responseText,selectprovince.getId());
               }
               else if("country".equals(type)){
                   result=Utility.handleCountryResponse(responseText,selectcity.getId());
               }
               if(result){
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           closeProgressDialog();
                           if("province".equals(type)){
                               queryProvinces();
                           }
                           else if("city".equals(type)){
                               queryCities();
                           }
                           else if("country".equals(type)){
                               queryCountries();
                           }
                       }
                   });
               }
           }
       });

   }
   private void showProgressDialog(){
       if(progressDialog==null){
           progressDialog=new ProgressDialog(getActivity());
           progressDialog.setMessage("正在加载");
           progressDialog.setCanceledOnTouchOutside(false);
       }
       progressDialog.show();
   }

   private void closeProgressDialog(){
       if(progressDialog!=null)
       {
           progressDialog.dismiss();
       }
   }
}
