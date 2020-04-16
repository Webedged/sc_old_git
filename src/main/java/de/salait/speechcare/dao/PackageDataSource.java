package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;

/**
 * Created by sandra.stuck on 20.09.13.
 */
public class PackageDataSource {
    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;


    public PackageDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }


    public void insertPackageData(String id, String name, String version, String predecessor_id, String app_id, String hash, String min_app_version, String publishdate,
                                  String created, String published) {


        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(id));
        values.put(SpeechcareSQLITEHelper.COLUMN_NAME, name);
        values.put(SpeechcareSQLITEHelper.COLUMN_VERSION, version);
        if (predecessor_id.equals("null")) {
            predecessor_id = null;
            values.put(SpeechcareSQLITEHelper.COLUMN_PREDECESSOR_ID, predecessor_id);
        }
        values.put(SpeechcareSQLITEHelper.COLUMN_APP_ID, Integer.valueOf(app_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_HASH, hash);
        values.put(SpeechcareSQLITEHelper.COLUMN_MIN_APP_VERSION, min_app_version);
        values.put(SpeechcareSQLITEHelper.COLUMN_PUBLISHDATE, publishdate);
        values.put(SpeechcareSQLITEHelper.COLUMN_CREATED, created);
        values.put(SpeechcareSQLITEHelper.COLUMN_PUBLISHED, Integer.valueOf(published));

        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_RELEASEPACKAGE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void truncateTable() {
        database.delete(SpeechcareSQLITEHelper.TABLE_RELEASEPACKAGE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_RELEASEPACKAGE + "'", null);
    }

}