package com.example.phonewearai;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherChecker {

    private static String weathAPI = "c198627a1303756c85ea29900f1eaa7c";
    private static String strLat;
    private static String strLng;
    private static String strElevation;

    public static void callWeather(String lat, String lng, Context context, TextView tempView, TextView cityView, TextView eleView){
        strLat = lat;
        strLng = lng;

        updateWeather(context, tempView, cityView);
        updateElevation(context, eleView);
    }

    private static void updateWeather(Context context, TextView tempView, TextView cityView){
        String weathURL = "https://api.openweathermap.org/data/2.5/weather?lat="+strLat+"&lon="+strLng+"&appid="+weathAPI;
        Log.i("weathURL", weathURL);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, weathURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15; // calvin temperature
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15; // calvin temperature
                    int pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    String wind = jsonObjectWind.getString("speed");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");
                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                    String countryName = jsonObjectSys.getString("country");
                    String cityName = jsonResponse.getString("name");

                    String strTemp = Double.toString(Math.round(temp));

                    tempView.setText("Temperature: "+strTemp);
                    cityView.setText("City: "+cityName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cityView.setText("Weather: API ERROR");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private static void updateElevation(Context context, TextView eleView){
        String altURL = "https://api.opentopodata.org/v1/aster30m?locations="+strLat+","+strLng;
        Log.i("altURL", altURL);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, altURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("results");
                    JSONObject jsonObjectElevation = jsonArray.getJSONObject(0);

                    double elevation;
                    elevation = jsonObjectElevation.getDouble("elevation");
                    strElevation = Double.toString(elevation);
                    eleView.setText("elevation: "+strElevation);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                eleView.setText("elevation: API ERROR");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
