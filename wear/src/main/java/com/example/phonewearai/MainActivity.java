package com.example.phonewearai;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.phonewearai.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView heartView;
    private TextView tempView;
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeart;
    private Sensor mTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        heartView = findViewById(R.id.Heart);
        tempView = findViewById(R.id.Temp);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // HEARTRATE INITIATION
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null){
            mHeart = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            Log.i("Sensor", "Herz Sensor initiiert");
        }
        else{
            heartView.setText("Heartrate is not available");
            Log.e("Sensor", "Herz Sensor nicht gefunden");
        }

        // TEMPERATURE INITIATION
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            Log.i("Sensor", "Temperatur Sensor initiiert");
        }
        else{
            tempView.setText("Temperature is not available");
            Log.e("Sensor", "Temperatur Sensor nicht gefunden");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mHeart, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            float heartRate = event.values[0];
            heartView.setText("Heartrate: " + heartRate);
            Log.i("HeartRate", "Puls hat sich geaendert: "+Float.toString(heartRate));
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            float temp = event.values[0];
            tempView.setText("Temperature: "+ temp);
            Log.i("Temperature", "Temperatur hat sich geaendert: "+Float.toString(temp));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

