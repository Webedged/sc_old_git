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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.StatisticDataSource;

public class StatisticsActivity extends Activity {
    ArrayList<HashMap> right = new ArrayList<>();
    ArrayList<HashMap> wrong = new ArrayList<>();
    ArrayList<HashMap> skipped = new ArrayList<>();
    StatisticDataSource statisticDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        LinearLayout backG = findViewById(R.id.statisticsBack);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);

        try {
            statisticDataSource = new StatisticDataSource(this);
            wrong.add(statisticDataSource.getAnswers().get("wrong"));
            right.add(statisticDataSource.getAnswers().get("right"));
            skipped.add(statisticDataSource.getAnswers().get("skipped"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setStats();

        final Button button = findViewById(R.id.btn_home);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    BarChart stackedChart;
    //RGB-Werte für die Statistik-Balken
    int[] colorClassArray = new int[]{
            Color.argb(255, 46, 204, 113),
            Color.argb(255, 231, 76, 60),
            Color.argb(255, 241, 196, 15)
    };

    //Labels für die Legende
    String[] labelArray = new String[]{
            "Richtig",
            "Falsch",
            "Übersprungen"
    };


    private void setStats() {

        stackedChart = findViewById(R.id.barchart);
        //Setzt die rnd-values aus der Array-Liste (statValues) in die Statistik
        BarDataSet barDataSet = new BarDataSet(statValues(), null);

        //Setzt die Farben der Richtig, Falsch, Übersprungen "Balken"
        barDataSet.setColors(colorClassArray);

        //Setzt die werte der Legende
        barDataSet.setStackLabels(labelArray);
        stackedChart.getDescription().setEnabled(false);

        //Gneriert die Staistik
        BarData barData = new BarData(barDataSet);
        barData.setValueTextColor(Color.BLACK);

        barChartSettings();

        //Setzt alle Daten der Statistik
        stackedChart.setData(barData);
    }

    private void barChartSettings() {
        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt = new SimpleDateFormat("dd.MM");
        ArrayList<String> theDates = new ArrayList<>();

        //Packt die letzten 7 Tage als Datum in ein Array
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);

            String formatted = fmt.format(cal.getTime());
            theDates.add(formatted);
        }

        //X & Y Achse kann um maximal 5 stufen gezoomt werden
        stackedChart.getViewPortHandler().setMaximumScaleX(5f);
        stackedChart.getViewPortHandler().setMaximumScaleY(5f);

        //X-Achse erhält die Daten der letzten 7 Tage (Daten == Datum)
        XAxis xAxis = stackedChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(theDates));
        xAxis.setTypeface(font);

        //Y-Achse und die Legende bekommen ein Font
        YAxis yAxisLeft = stackedChart.getAxisLeft();
        yAxisLeft.setTypeface(font);

        YAxis yAxisRight = stackedChart.getAxisRight();
        yAxisRight.setTypeface(font);

        Legend legend = stackedChart.getLegend();
        legend.setTypeface(font);
    }

    private Date last7Days(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        return cal.getTime();
    }

    private Object getStats(int i, ArrayList<HashMap> array) {
        @SuppressLint("SimpleDateFormat") DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        String date = fmt.format(last7Days(i).getTime());
        return array.get(0).get(date) != null ? array.get(0).get(date) : (float) 0;
    }

    //setzt die rnd-values in einer Array-Liste
    private ArrayList<BarEntry> statValues() {
        ArrayList<BarEntry> resultVals = new ArrayList<>();
        int x = 6;
        for (int i = 0; i < 7; i++) {
            resultVals.add(new BarEntry(x, new float[]{(float) getStats(i, right), (float) getStats(i, wrong), (float) getStats(i, skipped)}));
            x--;
        }
        return resultVals;
    }

}
