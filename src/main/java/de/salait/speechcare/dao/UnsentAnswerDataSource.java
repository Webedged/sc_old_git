package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.util.ArrayList;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by sandra.stuck on 01.11.13.
 */
public class UnsentAnswerDataSource {
    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;

    public UnsentAnswerDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertUnsentAnswer(String json) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_JSONANSWER, json);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_UNSENTANSWER, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void deleteUnsentAnswer(String jsonString) {
        database.delete(SpeechcareSQLITEHelper.TABLE_UNSENTANSWER, SpeechcareSQLITEHelper.COLUMN_JSONANSWER + " = '" + jsonString + "'", null);
    }

    public ArrayList<String> getAllNotSendData() {
        ArrayList<String> data = new ArrayList<String>();

        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_UNSENTANSWER,
                SpeechcareSQLITEHelper.COLUMN_TABLE_UNSENTANSWER, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String d = cursor.getString(0);
            data.add(d);
            //Log.i("jsonstring",d);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();

        return data;
    }


    public int countUnsentAnsers() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_UNSENTANSWER, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }

}
