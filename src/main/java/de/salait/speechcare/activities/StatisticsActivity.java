package de.salait.speechcare.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import de.salait.speechcare.R;

public class StatisticsActivity extends Activity {

    BarChart stackedChart;
    int[] colorClassArray = new int[]{
            Color.argb(255, 46, 204, 113),
            Color.argb(255, 231, 76, 60),
            Color.argb(255, 241, 196, 15)
    };

    String[] labelArray = new String[]{
            "Richtig",
            "Falsch",
            "Übersprungen"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        LinearLayout backG = (LinearLayout) findViewById(R.id.statisticsBack);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);

        setStats();

        final Button button = (Button) findViewById(R.id.btn_home);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void setStats() {

        stackedChart = (BarChart) findViewById(R.id.barchart);
        BarDataSet barDataSet = new BarDataSet(statValues(), null);

        BarData theData;
        barDataSet.setColors(colorClassArray);

        barDataSet.setStackLabels(labelArray);
        stackedChart.getDescription().setEnabled(false);

        BarData barData = new BarData(barDataSet);
        barData.setValueTextColor(Color.BLACK);

        barChartSettings();
        stackedChart.setData(barData);
    }

    private void barChartSettings() {
        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format1 = new SimpleDateFormat("dd.MM");
        ArrayList<String> theDates = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);

            String formatted = format1.format(cal.getTime());
            theDates.add(formatted);
        }

        stackedChart.getViewPortHandler().setMaximumScaleX(5f);
        stackedChart.getViewPortHandler().setMaximumScaleY(5f);

        XAxis xAxis = stackedChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(theDates));
        xAxis.setTypeface(font);

        YAxis yAxisLeft = stackedChart.getAxisLeft();
        yAxisLeft.setTypeface(font);

        YAxis yAxisRight = stackedChart.getAxisRight();
        yAxisRight.setTypeface(font);

        Legend legend = stackedChart.getLegend();
        legend.setTypeface(font);
    }

    private ArrayList<BarEntry> statValues() {
        ArrayList<BarEntry> resultVals = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            resultVals.add(new BarEntry(i, new float[]{getRandomNumberInRange(1, 10), getRandomNumberInRange(1, 10), getRandomNumberInRange(1, 10)}));
        }
        return resultVals;
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random rnd = new Random();
        return rnd.nextInt((max - min) + 1) + min;
    }
}
