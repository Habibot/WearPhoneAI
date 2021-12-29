package com.example.phonewearai;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.phonewearai.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView heartView;
    private TextView tempView;
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeart;
    private Sensor mTemperature;


    private String strHeartRate;
    private String strTemp;

    JSONObject jsonMsg = new JSONObject();

    private String transcriptionNodeId;

    //credits https://github.com/Bilbobx182
    private static final String SET_MESSAGE_CAPABILITY = "setString";
    public static final String SET_MESSAGE_PATH = "/setString";


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

            try {
                jsonMsg.put("Heartrate", "unknown");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            heartView.setText("Heartrate is not available");
            Log.e("Sensor", "Herz Sensor nicht gefunden");

            try {
                jsonMsg.put("Heartrate", "NULL");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // TEMPERATURE INITIATION
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            Log.i("Sensor", "Temperatur Sensor initiiert");

            try {
                jsonMsg.put("Temperature","unknown");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            tempView.setText("Temperature is not available");
            Log.e("Sensor", "Temperatur Sensor nicht gefunden");

            try {
                jsonMsg.put("Temperature", "NULL");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mHeart, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRate = event.values[0];
            strHeartRate = Float.toString(heartRate);

            try {
                updateJSON();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            beginSendMessageToPhone(jsonMsg.toString());

            heartView.setText("Heartrate: " + strHeartRate);
            Log.i("HeartRate", "Puls hat sich geaendert: " + strHeartRate);

        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            float temp = event.values[0];
            strTemp = Float.toString(temp);

            try {
                updateJSON();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            beginSendMessageToPhone(jsonMsg.toString());


            tempView.setText("Temperature: "+ strTemp);
            //Log.i("Temperature", "Temperatur hat sich geaendert: "+ strTemp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //credits https://github.com/Bilbobx182
    private void beginSendMessageToPhone(String msg) {

        AsyncTask.execute(() -> {

            CapabilityInfo capabilityInfo;
            try {

                capabilityInfo = Tasks.await(
                        Wearable.getCapabilityClient(getBaseContext()).getCapability(SET_MESSAGE_CAPABILITY, CapabilityClient.FILTER_REACHABLE));
                updateTranscriptionCapability(capabilityInfo);
                requestTranscription(msg.getBytes());

                // END REFERENCE

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }


    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }



    //credits https://github.com/Bilbobx182
    private void requestTranscription(final byte[] message) {

        AsyncTask.execute(() -> {
            if (transcriptionNodeId != null) {
                final Task<Integer> sendTask = Wearable.getMessageClient(getBaseContext()).sendMessage(transcriptionNodeId, SET_MESSAGE_PATH, message);

                sendTask.addOnSuccessListener(dataItem -> Log.d("MESSAGESTATE", "SUCCESS"));
                sendTask.addOnFailureListener(dataItem -> Log.d("MESSAGESTATE", "FAILURE"));
                sendTask.addOnCompleteListener(task -> Log.d("MESSAGESTATE", "COMPLETE"));
            }
        });
    }

    private void updateJSON() throws JSONException {
        jsonMsg.put("Heartrate", strHeartRate);
        jsonMsg.put("Temperature", strTemp);
    }
}