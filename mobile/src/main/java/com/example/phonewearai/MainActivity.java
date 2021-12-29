package com.example.phonewearai;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {


    private TextView heartView;
    private TextView tempView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartView = findViewById(R.id.heart);
        tempView = findViewById(R.id.temp);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msg = new String(messageEvent.getData());
        Log.i("Message received", msg);

        //https://www.py4u.net/discuss/1195822
        Map<String, String> Sensors = new Gson().fromJson(msg, new TypeToken<Map<String, String>>() {}.getType());

        String strHeartRate = Sensors.get("Heartrate");
        String strTemp = Sensors.get("Temperature");


        heartView.setText("Heartrate: "+strHeartRate);
        tempView.setText("Temperature: "+strTemp);

    }
}