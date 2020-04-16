package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by sandra.stuck on 23.09.13.
 */
public class AnswerDataSource {
    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;

    public AnswerDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertAnswer(int answerid, int exercise_id, String value, int sort, int media_id) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, answerid);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, exercise_id);
        values.put(SpeechcareSQLITEHelper.COLUMN_VALUE, value);
        values.put(SpeechcareSQLITEHelper.COLUMN_SORT, sort);
        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIA_ID, media_id);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_ANSWER, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void insertsevAnswer(int answerid, int exercise_id, String value, int sort, int media_id, int help_media_id) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, answerid);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, exercise_id);
        values.put(SpeechcareSQLITEHelper.COLUMN_VALUE, value);
        values.put(SpeechcareSQLITEHelper.COLUMN_SORT, sort);
        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIA_ID, media_id);
        values.put(SpeechcareSQLITEHelper.COLUMN_HELP_MEDIA_ID, help_media_id);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_ANSWER, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public String getAnswerIdForValueAndExID(String value, String exID) {

        String id = null;
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, new String[]{"id"},
                "value = '" + value + "' AND exercise_id = '" + exID + "'", null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            id = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return id;
    }

    public void truncateTable() {
        database.delete(SpeechcareSQLITEHelper.TABLE_ANSWER, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_ANSWER + "'", null);
    }
}
