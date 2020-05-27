package de.salait.speechcare.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.StatisticDataSource;

public class StatisticsActivity extends Activity implements View.OnClickListener {
    ArrayList<HashMap> right = new ArrayList<>();
    ArrayList<HashMap> wrong = new ArrayList<>();
    ArrayList<HashMap> skipped = new ArrayList<>();
    ArrayList<HashMap> exerciseModel = new ArrayList<>();
    StatisticDataSource statisticDataSource;
    Button btnActive;
    LinearLayout viewStat;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        RelativeLayout backG = findViewById(R.id.statisticsBack);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);

        final Button trainingstats = findViewById(R.id.trainingStats);
        trainingstats.setOnClickListener(this);
        btnActive = trainingstats;
        viewStat = findViewById(R.id.trainingStatsView);

        final Button intensitaetstats = findViewById(R.id.intensitätStats);
        intensitaetstats.setOnClickListener(this);

        final Button typstats = findViewById(R.id.typStats);
        typstats.setOnClickListener(this);

        final Button listexcersisestats = findViewById(R.id.listExcersiseStats);
        listexcersisestats.setOnClickListener(this);

        final Button btn_home = findViewById(R.id.btn_home);
        btn_home.setOnClickListener(this);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        final TextView header = findViewById(R.id.headerStatistics);
        header.setText("7 Tagesstatistik von " + fmt.format(last7Days(6)) + " bis " + fmt.format(last7Days(0)));
        header.setTextSize(18f);

        //Lädt die Statistikdaten in seperaten ArrayLists
        try {
            statisticDataSource = new StatisticDataSource(this);
            wrong.add(statisticDataSource.getAnswers().get("wrong"));
            right.add(statisticDataSource.getAnswers().get("right"));
            skipped.add(statisticDataSource.getAnswers().get("skipped"));
            exerciseModel.add(statisticDataSource.getAnswers().get("exercideModel"));
            System.out.println("MODEL LAST 7 DAYS" + exerciseModel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setStats();
    }

    public void onClick(View view) {
        Button tmpBtn = btnActive;
        LinearLayout tmpViewStat = viewStat;
        switch (view.getId()) {
            case R.id.btn_home:
                onBackPressed();
                break;
            case R.id.trainingStats:
                btnActive = findViewById(R.id.trainingStats);
                viewStat = findViewById(R.id.trainingStatsView);
                loadNewStats(btnActive, tmpBtn, viewStat, tmpViewStat);
                break;
            case R.id.intensitätStats:
                btnActive = findViewById(R.id.intensitätStats);
                viewStat = findViewById(R.id.intensitätStatsView);
                loadNewStats(btnActive, tmpBtn, viewStat, tmpViewStat);
                break;
            case R.id.typStats:
                btnActive = findViewById(R.id.typStats);
                viewStat = findViewById(R.id.typStatsView);
                loadNewStats(btnActive, tmpBtn, viewStat, tmpViewStat);
                break;
            case R.id.listExcersiseStats:
                btnActive = findViewById(R.id.listExcersiseStats);
                viewStat = findViewById(R.id.listExcersiseStatsView);
                loadNewStats(btnActive, tmpBtn, viewStat, tmpViewStat);
                break;
        }
    }

    @SuppressLint("ResourceType")
    private void loadNewStats(Button btnActive, Button tmpBtn, LinearLayout viewStat, LinearLayout tmpViewStat) {
        if (btnActive != tmpBtn) {
            viewStat.setVisibility(View.VISIBLE);
            tmpViewStat.setVisibility(View.GONE);
            btnActive.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
            tmpBtn.setBackgroundColor(getResources().getColor(R.color.grey));
            setStats();
        }
    }

    BarChart stackedChart;
    PieChart pieChart;
    //RGB-Werte für die Statistik-Balken
    int[] colorArray = new int[]{
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
        switch (viewStat.getId()) {
            case R.id.trainingStatsView:
                stackedChart = findViewById(R.id.trainingStatsChart);
                //Setzt die rnd-values aus der Array-Liste (statValues) in die Statistik
                BarDataSet barDataSet = new BarDataSet(trainingStatValues(), null);

                //Setzt die Farben der Richtig, Falsch, Übersprungen "Balken"
                barDataSet.setColors(colorArray);

                //Setzt die werte der Legende
                barDataSet.setStackLabels(labelArray);

                //Gneriert die Staistik
                BarData barData = new BarData(barDataSet);
                barData.setValueTextColor(Color.WHITE);
                barData.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return value == 0.0f ? "" : Math.floor(value) > 1 ? (int) Math.floor(value) + ". Aufgaben" : (int) Math.floor(value) + ". Aufgabe";
                    }
                });

                chartSettings();

                //Setzt alle Daten der Statistik
                stackedChart.setData(barData);
                break;
            case R.id.intensitätStatsView:
                pieChart = findViewById(R.id.intensitätStatsChart);
                pieChart.setHoleRadius(20f);
                pieChart.setTransparentCircleRadius(25f);
                pieChart.setUsePercentValues(true);


                PieDataSet pieDataSet = new PieDataSet(intensitaetStatValue(), null);

                pieDataSet.setColors(colorArray);
                pieDataSet.setValueTextSize(18f);
                pieDataSet.setValueLinePart1Length(0.75f);
                pieDataSet.setValueLinePart2Length(0.75f);
                pieDataSet.setSliceSpace(3f);
                pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);


                PieData pieData = new PieData(pieDataSet);

                pieData.setValueTextColor(Color.BLACK);
                pieData.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return value == 0.0f ? "" : Math.round(value) + "%";
                    }
                });

                chartSettings();

                pieChart.setData(pieData);
                break;

            case R.id.typStatsView:
                System.out.println(viewStat);
                break;
        }
    }

    private ArrayList<BarEntry> trainingStatValues() {
        ArrayList<BarEntry> resultVals = new ArrayList<>();
        //x dient dazu um den aktuellen Tag als index anzusprechen
        int x = 6;
        for (int i = 0; i < 7; i++) {
            //Setzt die werte in die ArrayList in den jeweiligen Index
            resultVals.add(new BarEntry(x, new float[]{(float) getStats(i, right), (float) getStats(i, wrong), (float) getStats(i, skipped)}));
            x--;
        }
        return resultVals;
    }

    private ArrayList<PieEntry> intensitaetStatValue() {
        ArrayList<PieEntry> resultVals = new ArrayList<>();
        String tmpExerciseModel = "";
        for (int i = 0; i < exerciseModel.get(0).size(); i++) {
            //TODO: Richtig übersetzen lassen
            switch (models[i]) {
                case "exercisewordforimage":
                    tmpExerciseModel = getString(R.string.label_wortbild);
                    break;
                case "exerciseimageforword":
                    tmpExerciseModel = getString(R.string.label_bildwort);
                    break;
                case "exercisegapsentenceforimage":
                    tmpExerciseModel = getString(R.string.label_satzbild);
                    break;
                case "exercisesortcharacters":
                case "exercisegapwordforimage":
                    tmpExerciseModel = getString(R.string.label_buchstabensortieren);
                    break;
                case "exercisesortwords":
                    tmpExerciseModel = getString(R.string.label_sortierensatzebene);
                    break;
            }
            //Setzt die werte in die ArrayList in den jeweiligen Index
            System.out.println("INDEXXX: " + i + "modelSize" + exerciseModel.get(0).size());
            resultVals.add(new PieEntry((float) getStats(i, exerciseModel), tmpExerciseModel));
        }

        return resultVals;
    }

    String[] models = {"exercisewordforimage", "exerciseimageforword", "exercisegapsentenceforimage", "exercisesortcharacters", "exercisesortwords", "exercisegapwordforimage"};

    private Object getStats(int i, ArrayList<HashMap> array) {
        switch (viewStat.getId()) {
            case R.id.trainingStatsView:
                @SuppressLint("SimpleDateFormat") DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
                //erhaltener Timestamp wird in einem format konvertiert
                String date = fmt.format(last7Days(i).getTime());
                //Zieht sich von Index: i das datum, wenn das Array ein wert mit diesem datum enthält returnt er einen (float > 0) wenn nicht dann returnt er einen (float = 0)
                return array.get(0).get(date) != null ? array.get(0).get(date) : (float) 0;

            case R.id.intensitätStatsView:
                return array.get(0).get(models[i]) != null ? array.get(0).get(models[i]) : (float) 0;
        }
        return (float) 0;
    }

    private void chartSettings() {
        switch (viewStat.getId()) {
            case R.id.trainingStatsView:

                stackedChart.setExtraOffsets(10f, 20f, 10f, 5f);
                stackedChart.getDescription().setEnabled(false);
                stackedChart.setDrawValueAboveBar(false);
                //X & Y Achse kann um maximal 5 stufen gezoomt werden
                stackedChart.getViewPortHandler().setMaximumScaleX(5f);
                stackedChart.getViewPortHandler().setMaximumScaleY(5f);

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

                //X-Achse erhält das Datum der letzten 7 Tage
                XAxis xAxis = stackedChart.getXAxis();
                xAxis.setTextSize(14f);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(theDates));
                xAxis.setTypeface(font);

                //Einstellung der yAchse-Links
                YAxis yAxisLeft = stackedChart.getAxisLeft();
                yAxisLeft.setTextSize(14f);
                yAxisLeft.setTypeface(font);
                yAxisLeft.setLabelCount(5);
                yAxisLeft.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) Math.floor(value));
                    }
                });

                //Einstellung der yAchse-Rechts
                YAxis yAxisRight = stackedChart.getAxisRight();
                yAxisRight.setEnabled(false);

                //Einstellung der Legende
                Legend legend = stackedChart.getLegend();
                legend.setTextSize(18f);
                legend.setTypeface(font);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                break;

            case R.id.intensitätStatsView:
                pieChart.setExtraOffsets(10f, 20f, 10f, 5f);
                pieChart.getDescription().setEnabled(false);
                pieChart.setEntryLabelTextSize(14f);
                pieChart.setEntryLabelColor(Color.BLACK);
                legend = pieChart.getLegend();
                legend.setEnabled(false);
                break;
        }
    }

    private Date last7Days(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        //return den Timestamp des jeweiligen Indexes: i (Unformattiert)
        return cal.getTime();
    }
}
