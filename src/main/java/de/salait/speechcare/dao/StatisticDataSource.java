package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;

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

        //Schließt die Dtenbank (Kappt DB verbindung)
        close();
    }

    private void getID() {
        open();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ID}, null, null, null, null, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            //Show my ID
            System.out.println("ID in DB (row: " + i + ")" + cursor.getString(0));
            cursor.moveToNext();
            i++;
        }
        cursor.close();
        close();
    }

    public void truncateTable() {
        //Löscht inhalte der Statistik-Tabelle
        database.delete(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_STATISTICS + "'", null);
    }
}
