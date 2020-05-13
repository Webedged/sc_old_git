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

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_TIMESTAMP, Long.toString(timestamp));
        values.put(SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, Integer.toString(answerstatus));
        values.put(SpeechcareSQLITEHelper.COLUMN_GIVEANSWER, giveanswer);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        //Schließt die Datennbank (Kappt DB verbindung)
        close();
    }

    @SuppressLint("SimpleDateFormat")
    public HashMap<String, HashMap> getAnswers() {
        HashMap<String, HashMap> answers = new HashMap<>();
        HashMap<String, Float> right = new HashMap<>();
        HashMap<String, Float> wrong = new HashMap<>();
        HashMap<String, Float> skipped = new HashMap<>();
        open();

        for (int i = 6; i >= 0; i--) {
            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
            Cursor sqlQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, SpeechcareSQLITEHelper.COLUMN_TIMESTAMP}, "timestamp BETWEEN " + cal(Calendar.getInstance(), i, 0, 0, 0).getTimeInMillis() + " AND " + cal(Calendar.getInstance(), i, 23, 59, 59).getTimeInMillis() + "", null, null, null, null);
            int wrongAnswers = 0;
            int rightAnswers = 0;
            int skippedAnswers = 0;
            sqlQuery.moveToFirst();
            while (!sqlQuery.isAfterLast()) {
                System.out.println("DATUM: " + dateToString(sqlQuery, fmt) + " ANTWORT: " + sqlQuery.getString(0));
                if (fmt.format(last7Days(i)).equals(dateToString(sqlQuery, fmt))) {
                    switch (sqlQuery.getString(0)) {
                        case "0":
                            wrongAnswers++;
                            wrong.put(dateToString(sqlQuery, fmt), (float) wrongAnswers);
                            break;
                        case "1":
                            rightAnswers++;
                            right.put(dateToString(sqlQuery, fmt), (float) rightAnswers);
                            break;
                        case "2":
                            skippedAnswers++;
                            skipped.put(dateToString(sqlQuery, fmt), (float) skippedAnswers);
                            break;
                    }
                }
                sqlQuery.moveToNext();
            }
            sqlQuery.close();
        }
        close();
        answers.put("wrong", wrong);
        answers.put("right", right);
        answers.put("skipped", skipped);
        return answers;
    }

    private Date last7Days(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        return cal.getTime();
    }

    private String dateToString(Cursor sqlQuery, DateFormat fmt) {
        //Konvertiert den DB-Timestamp in einem Timestamp des Typen Calendar (String to Calendar)
        String time = sqlQuery.getString(1);
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
