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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        //Test-Ausgabe der ID's um zu überprüfen ob sich die Tabelle auch mit Trainingsdaten füllt
        getTimestamps();

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

    }

    private void getTimestamps() {
        open();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_TIMESTAMP}, null, null, null, null, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            //Show my ID
            System.out.println("TIMESTAMPS in DB (row: " + i + ") :: " + cursor.getString(0));
            cursor.moveToNext();
            i++;
        }
        cursor.close();
        close();
    }

    @SuppressLint("SimpleDateFormat")
    public void getAnswers() {
        open();
        for (int i = 6; i >= 0; i--) {

            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");

            //Setzt zur überprüfung einen log für jeden (Tag -7), je nach Index der For-Schleife
            System.out.println(fmt.format(cal(Calendar.getInstance(), i, 0, 0, 0).getTime()) + ":::" + i + ":::" + "MIN---: " + cal(Calendar.getInstance(), i, 0, 0, 0).getTimeInMillis() + " : " + "MAX---: " + cal(Calendar.getInstance(), i, 23, 59, 59).getTimeInMillis());


            //Fragt die min und max Timestamps in der Query ab, die Resultate sind jeweils die Werte ziwschen min und max Timestamp
            Cursor sqlQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, SpeechcareSQLITEHelper.COLUMN_TIMESTAMP}, "timestamp BETWEEN " + cal(Calendar.getInstance(), i, 0, 0, 0).getTimeInMillis() + " AND " + cal(Calendar.getInstance(), i, 23, 59, 59).getTimeInMillis() + "", null, null, null, null);
            sqlQuery.moveToFirst();
            while (!sqlQuery.isAfterLast()) {
                //Zeigt aktuelles Datum aus der DB und den Status der Antwort
                System.out.println("\n DATUM: " + strToDate(sqlQuery, fmt) + ":::" + "ANSWERSTATUS: " + getAnswersCount(sqlQuery, fmt, i));
                //System.out.println("RICHTIG: " + rightMap.get("11.05.2020") + "++++++++++++++++++++++++++++++++++++");
                sqlQuery.moveToNext();
            }

            sqlQuery.close();
        }
        close();
    }

    private Calendar cal(Calendar cal, int i, int hour, int minute, int second) {
        cal.add(Calendar.DATE, -i);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        return cal;
    }

    private int right = 1;
    private int wrong = 1;
    private int skipped = 1;

    private Map<String, Integer> rightMap = new HashMap<>();
    private Map<String, Integer> wrongMap = new HashMap<>();
    private Map<String, Integer> skippedMap = new HashMap<>();

    private String getAnswersCount(Cursor sqlQuery, DateFormat fmt, int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        switch (sqlQuery.getString(0)) {
            case "1":
                if (strToDate(sqlQuery, fmt).equals(fmt.format(cal.getTime())))
                    rightMap.put(strToDate(sqlQuery, fmt), right++);
                break;
            case "0":
                if (strToDate(sqlQuery, fmt).equals(fmt.format(cal.getTime())))
                    wrongMap.put(strToDate(sqlQuery, fmt), wrong++);
                break;
            case "2":
                if (strToDate(sqlQuery, fmt).equals(fmt.format(cal.getTime())))
                    skippedMap.put(strToDate(sqlQuery, fmt), skipped++);
                break;
            default:
                rightMap.put(strToDate(sqlQuery, fmt), null);
                wrongMap.put(strToDate(sqlQuery, fmt), null);
                skippedMap.put(strToDate(sqlQuery, fmt), null);
                break;
        }
        return sqlQuery.getString(0);
    }

    public float[] getDatas(String date) {
        return new float[]{6, 3, 1};
    }

    private String strToDate(Cursor sqlQuery, DateFormat fmt) {
        //Konvertiert den DB-Timestamp in einem Timestamp des Typen Calendar (String to Calendar)
        String time = sqlQuery.getString(1);
        long timestampLong = Long.parseLong(time);
        Date d = new Date(timestampLong);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return fmt.format(c.getTime());
    }

    public void truncateTable() {
        //Löscht inhalte der Statistik-Tabelle
        database.delete(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_STATISTICS + "'", null);
    }
}
