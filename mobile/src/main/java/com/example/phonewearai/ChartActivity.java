package com.example.phonewearai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Intent intent = getIntent();
        chart = findViewById(R.id.chart);

        ArrayList<Integer> heartList = intent.getIntegerArrayListExtra("heart");
        ArrayList<Integer> stepsList = intent.getIntegerArrayListExtra("steps");
        ArrayList<Integer> cadenceList = intent.getIntegerArrayListExtra("cadence");
        ArrayList<Float> speedList = (ArrayList<Float>) intent.getSerializableExtra("speed");

        Log.i("",heartList.toString());
        Log.i("",stepsList.toString());
        Log.i("",cadenceList.toString());
        Log.i("",speedList.toString());

        ArrayList<BarEntry> yValues = addValues(heartList);

        BarDataSet dataSet = new BarDataSet(yValues, "heartrate");
        BarData data = new BarData(dataSet);
        data.setValueTextSize(10f);
        chart.setData(data);
    }

    private ArrayList<BarEntry> addValues(ArrayList<Integer> dataList){
        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int xValue = i==0 ? 5 : 5+i*5;
            values.add(new BarEntry(xValue, dataList.get(i)));
        }
        return values;
    }

}