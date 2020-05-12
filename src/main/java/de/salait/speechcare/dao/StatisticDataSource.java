package de.salait.speechcare.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;

public class StatisticDataSource {

    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;

    public StatisticDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertStatistic(String id, long timestamp, int answerstatus, String giveanswer) {
        //Öffnet die Dtenbank (Stellt verbindung her)
        open();

        //Konsolen ausgabe des aktuellen Zeitstempels und dessen Status einer Antwort
        /*System.out.println("---TIMESTAMP: " + timestamp);
        System.out.println("---ANSWERSTATUS: " + answerstatus);*/

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_TIMESTAMP, Long.toString(timestamp));
        values.put(SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, Integer.toString(answerstatus));
        values.put(SpeechcareSQLITEHelper.COLUMN_GIVEANSWER, giveanswer);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        //Schließt die Datennbank (Kappt DB verbindung)
        close();

        System.out.println("***********" + getAnswers(getDates()) + "***********");
    }

    @SuppressLint("SimpleDateFormat")
    public ArrayList<String> getDates() {
        ArrayList<String> dates = new ArrayList<>();
        open();
        for (int i = 6; i >= 0; i--) {
            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");

            Cursor sqlQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_TIMESTAMP}, "timestamp BETWEEN " + cal(Calendar.getInstance(), i, 0, 0, 0).getTimeInMillis() + " AND " + cal(Calendar.getInstance(), i, 23, 59, 59).getTimeInMillis() + "", null, null, null, null);
            sqlQuery.moveToFirst();

            while (!sqlQuery.isAfterLast()) {
                dates.add(dateToString(sqlQuery, fmt));
                sqlQuery.moveToNext();
            }
            sqlQuery.close();
        }
        close();
        return dates;
    }

    @SuppressLint("SimpleDateFormat")
    public HashMap<String, HashMap> getAnswers(ArrayList<String> dates) {
        int rightAnswers = 0;
        int wrongAnswers = 0;
        HashMap<String, HashMap> answers = new HashMap<>();
        HashMap<String, Float> right = new HashMap<>();
        HashMap<String, Float> wrong = new HashMap<>();
        open();
        for (int i = 6; i >= 0; i--) {
            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");

            Cursor sqlQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS}, null, null, null, null, null);
            sqlQuery.moveToFirst();
            while (!sqlQuery.isAfterLast()) {
                //System.out.println("+++++" + fmt.format(last7Days(i).getTime()) + sqlQuery.getString(0));
                if (dates.contains(fmt.format(last7Days(i).getTime())) && sqlQuery.getString(0).equals("1")) {
                    rightAnswers++;
                    right.put(fmt.format(last7Days(i).getTime()), (float) rightAnswers);
                } else if (sqlQuery.getString(0).equals("1")) {
                    right.put(fmt.format(last7Days(i).getTime()), (float) 0);
                }

                if (dates.contains(fmt.format(last7Days(i).getTime())) && sqlQuery.getString(0).equals("0")) {
                    wrongAnswers++;
                    wrong.put(fmt.format(last7Days(i).getTime()), (float) wrongAnswers);
                } else if (sqlQuery.getString(0).equals("0")) {
                    wrong.put(fmt.format(last7Days(i).getTime()), (float) 0);
                }
                sqlQuery.moveToNext();
            }
            sqlQuery.close();
            answers.put("right", right);
            answers.put("wrong", wrong);
        }
        close();
        return answers;
    }

    private Date last7Days(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        return cal.getTime();
    }

    private String dateToString(Cursor sqlQuery, DateFormat fmt) {
        //Konvertiert den DB-Timestamp in einem Timestamp des Typen Calendar (String to Calendar)
        String time = sqlQuery.getString(0);
        long timestampLong = Long.parseLong(time);
        Date d = new Date(timestampLong);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return fmt.format(c.getTime());
    }

    private Calendar cal(Calendar cal, int i, int hour, int minute, int second) {
        cal.add(Calendar.DATE, -i);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        return cal;
    }

    public void truncateTable() {
        //Löscht inhalte der Statistik-Tabelle
        database.delete(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_STATISTICS + "'", null);
    }
}
