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
        getID();

        //Öffnet die Dtenbank (Stellt verbindung her)
        open();

        //Konsolen ausgabe des Zeitstempels und dessen Status einer Antwort
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
        getAnswerStatus();

    }

    private void getID() {
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
    private void getAnswerStatus() {

        for (int i = 6; i >= 0; i--) {
            Calendar calMax = Calendar.getInstance();
            Calendar calMin = Calendar.getInstance();
            //Format für die Timestamp zu Datum Konvertierung (--31.12.2020)
            DateFormat f = new SimpleDateFormat("dd.MM.yyyy");

            //Setzt pro Index (int i) den Tag auf die niedrigste Uhrzeit (00:00:01) - als Timestamp mit der Methode getTimeInMillis()
            calMin.add(Calendar.DATE, -i);
            calMin.set(Calendar.HOUR_OF_DAY, 0);
            calMin.set(Calendar.MINUTE, 0);
            calMin.set(Calendar.SECOND, 0);

            //Setzt pro Index (int i) den Tag auf die höchste Uhrzeit (23:59:59) - als Timestamp mit der Methode getTimeInMillis()
            calMax.add(Calendar.DATE, -i);
            calMax.set(Calendar.HOUR_OF_DAY, 23);
            calMax.set(Calendar.MINUTE, 59);
            calMax.set(Calendar.SECOND, 59);

            //Setzt zur überprüfung einen log für jeden (Tag -7), je nach Index der For-Schleife
            System.out.println(f.format(calMax.getTime()) + ":::" + i + ":::" + "MIN---: " + calMin.getTimeInMillis() + " : " + "MAX---: " + calMax.getTimeInMillis());

            open();
            //Fragt die min und max Timestamps in der Query ab, die Resultate sind jeweils die Werte ziwschen min und max Timestamp
            Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, SpeechcareSQLITEHelper.COLUMN_TIMESTAMP}, "timestamp BETWEEN " + calMin.getTimeInMillis() + " AND " + calMax.getTimeInMillis() + "", null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                //Konvertiert den DB-Timestamp in einem Timestamp des Typen Calendar (String to Calendar)
                String time = cursor.getString(1);
                long timestampLong = Long.parseLong(time);
                Date d = new Date(timestampLong);
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                String s = f.format(c.getTime());

                //Zeight aktuelles Datum aus der DB und den Status der Antwort
                System.out.println("\n DATUM: " + s + ":::" + "ANSWERSTATUS: " + cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();
            close();
        }
    }

    public void truncateTable() {
        //Löscht inhalte der Statistik-Tabelle
        database.delete(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_STATISTICS + "'", null);
    }
}
