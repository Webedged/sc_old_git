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
 * Created by speechcare on 17.04.18.
 */

public class MediaCollectionDataSource {
    protected SQLiteDatabase database;
    protected SpeechcareSQLITEHelper dbHelper;

    public MediaCollectionDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertMediacollection(String id, String title, String description, String image_media_id, String price, String status, String bildart) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_TITLE, title);
        values.put(SpeechcareSQLITEHelper.COLUMN_DESCRIPTION, description);
        values.put(SpeechcareSQLITEHelper.COLUMN_IMAGEMEDIA_ID, image_media_id);
        values.put(SpeechcareSQLITEHelper.COLUMN_PRICE, price);
        values.put(SpeechcareSQLITEHelper.COLUMN_STATUS, status);
        values.put(SpeechcareSQLITEHelper.COLUMN_BILDART, bildart);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertMediaCollectionMedia(String media_id, String mediacollection_id) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIA_ID, media_id);
        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIACOLLECTION_ID, mediacollection_id);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION_MEDIA, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void truncateTable() {

        if (tableExists(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION)) {
            database.delete(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION, null, null);
            database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION + "'", null);
        }

        if (tableExists(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION_MEDIA)) {
            database.delete(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION_MEDIA, null, null);
            database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION_MEDIA + "'", null);
        }
    }

    private boolean tableExists(String table) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
        Cursor mCursor = database.rawQuery(sql, null);
        if (mCursor.getCount() > 0) {
            return true;
        }
        mCursor.close();
        return false;
    }


    public ArrayList<String> getAvailableCardtypes(String bildart) {

        ArrayList<String> typeList = new ArrayList<String>();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION, new String[]{SpeechcareSQLITEHelper.COLUMN_ID},
                SpeechcareSQLITEHelper.COLUMN_BILDART + " = '" + bildart + "'", null, null, null, SpeechcareSQLITEHelper.COLUMN_TITLE);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            typeList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return typeList;
    }


}
