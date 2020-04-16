package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;

/**
 * Created by sandra.stuck on 20.09.13.
 */
public class AppDataSource {
    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;

    public AppDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertAppData(String id, String name, String appid) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(id));
        values.put(SpeechcareSQLITEHelper.COLUMN_NAME, name);
        values.put(SpeechcareSQLITEHelper.COLUMN_APPID, appid);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_APP, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int countAppData() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_APP, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }

    public void truncateTable() {
        database.delete(SpeechcareSQLITEHelper.TABLE_APP, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_APP + "'", null);
    }
}
