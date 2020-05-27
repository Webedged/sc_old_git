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

    public void insertStatistic(String id, long timestamp, int answerstatus, String giveanswer, String exercisemodell) {
        //Öffnet die Datenbank (Stellt verbindung her)
        open();
        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_TIMESTAMP, Long.toString(timestamp));
        values.put(SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, Integer.toString(answerstatus));
        values.put(SpeechcareSQLITEHelper.COLUMN_GIVEANSWER, giveanswer);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_MODEL, exercisemodell);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_STATISTICS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        //Schließt die Datennbank (Kappt DB verbindung)
        close();
        System.out.println("**************** " + exercisemodell);
    }

    @SuppressLint("SimpleDateFormat")
    public HashMap<String, HashMap> getAnswers() {
        HashMap<String, HashMap> answers = new HashMap<>();
        HashMap<String, Float> right = new HashMap<>(), wrong = new HashMap<>(), skipped = new HashMap<>(), exerciseModel = new HashMap<>();
        open();

        //Setzt die jeweiligen Antwort-Daten des jeweiligen Datums (-7 Tage) in seperate Arrays.
        for (int i = 6; i >= 0; i--) {
            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
            Cursor sqlQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_ANSWERSTATUS, SpeechcareSQLITEHelper.COLUMN_TIMESTAMP, SpeechcareSQLITEHelper.COLUMN_EXERCISE_MODEL}, "timestamp BETWEEN " + cal(Calendar.getInstance(), i, 0, 0, 0).getTimeInMillis() + " AND " + cal(Calendar.getInstance(), i, 23, 59, 59).getTimeInMillis() + "", null, null, null, null);
            int wrongAnswers = 0, rightAnswers = 0, skippedAnswers = 0, exercisewordforimage = 0, exerciseimageforword = 0, exercisegapsentenceforimage = 0, exercisesortcharacters = 0, exercisesortwords = 0, exercisegapwordforimage = 0;

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

            Cursor exerciseQuery = database.query(SpeechcareSQLITEHelper.TABLE_STATISTICS, new String[]{SpeechcareSQLITEHelper.COLUMN_EXERCISE_MODEL}, null, null, null, null, null);

            exerciseQuery.moveToFirst();
            while (!exerciseQuery.isAfterLast()) {
                switch (exerciseQuery.getString(0)) {
                    case "exercisewordforimage":
                        exercisewordforimage++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exercisewordforimage);
                        break;
                    case "exerciseimageforword":
                        exerciseimageforword++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exerciseimageforword);
                        break;
                    case "exercisegapsentenceforimage":
                        exercisegapsentenceforimage++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exercisegapsentenceforimage);
                        break;
                    case "exercisesortcharacters":
                        exercisesortcharacters++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exercisesortcharacters);
                        break;
                    case "exercisesortwords":
                        exercisesortwords++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exercisesortwords);
                        break;
                    case "exercisegapwordforimage":
                        exercisegapwordforimage++;
                        exerciseModel.put(exerciseQuery.getString(0), (float) exercisegapwordforimage);
                        break;
                }
                exerciseQuery.moveToNext();
            }
            exerciseQuery.close();
        }
        close();
        answers.put("wrong", wrong);
        answers.put("right", right);
        answers.put("skipped", skipped);
        answers.put("exercideModel", exerciseModel);
        System.out.println(exerciseModel);
        return answers;
    }


    private Date last7Days(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        //return den Timestamp des jeweiligen Indexes: i (z.B. i=3, Heutiger Tag -3 Tage (24.12.2020 -3 Tage = 21.12.2020))
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
        //Min und Max eines Timestamps (00:01 und 23:59)
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
