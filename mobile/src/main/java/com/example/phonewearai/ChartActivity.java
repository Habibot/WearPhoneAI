package com.example.phonewearai;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    private ScatterChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        Intent intent = getIntent();
        chart = findViewById(R.id.scatterchart);

        ArrayList<Integer> heartList = intent.getIntegerArrayListExtra("heart");
       // ArrayList<Integer> stepsList = intent.getIntegerArrayListExtra("steps");
        ArrayList<Integer> cadenceList = intent.getIntegerArrayListExtra("cadence");
        ArrayList<Float> speedList = (ArrayList<Float>) intent.getSerializableExtra("speed");

//        Log.i("",heartList.toString());
//        Log.i("",stepsList.toString());
//        Log.i("",cadenceList.toString());
//        Log.i("",speedList.toString());
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setMaxHighlightDistance(50f);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        chart.setMaxVisibleValueCount(200);
        chart.setPinchZoom(true);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXOffset(5f);


        ArrayList<Entry> heartValues = addIntValues(heartList);
        ArrayList<Entry> cadenceValues = addIntValues(cadenceList);
        ArrayList<Entry> speedValues = addFloatValues(speedList);

        ScatterDataSet scatterHeartSet = new ScatterDataSet(heartValues,"Heartrate");
        scatterHeartSet.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        scatterHeartSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);

        ScatterDataSet scatterCadenceSet = new ScatterDataSet(cadenceValues, "Cadence");
        scatterCadenceSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        scatterCadenceSet.setScatterShapeHoleColor(ColorTemplate.COLORFUL_COLORS[3]);
        scatterCadenceSet.setScatterShapeHoleRadius(3f);
        scatterCadenceSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);

        ScatterDataSet scatterSpeedSet = new ScatterDataSet(speedValues, "Speed");
        scatterSpeedSet.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        scatterSpeedSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(scatterHeartSet);
        dataSets.add(scatterCadenceSet);
        dataSets.add(scatterSpeedSet);

        ScatterData data = new ScatterData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    private ArrayList<Entry> addIntValues(ArrayList<Integer> dataList){
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int xValue = i==0 ? 5 : 5+i*5;
            values.add(new Entry(i, dataList.get(i)));
        }
        return values;
    }
    private ArrayList<Entry> addFloatValues(ArrayList<Float> dataList){
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int xValue = i==0 ? 5 : 5+i*5;
            values.add(new Entry(i, dataList.get(i)));
        }
        return values;
    }

}