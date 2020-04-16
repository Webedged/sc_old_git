package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.salait.speechcare.data.Media;
import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by sandra.stuck on 23.09.13.
 */
public class MediaDataSource {
    protected SQLiteDatabase database;
    protected SpeechcareSQLITEHelper dbHelper;

    public MediaDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void insertMediaData(int id, String title, String type, String source, String license, String invalidationdate, String originalfile, String filename, int deactivated, String created, String status) {
        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_TITLE, title);
        values.put(SpeechcareSQLITEHelper.COLUMN_TYPE, type);
        values.put(SpeechcareSQLITEHelper.COLUMN_SOURCE, source);
        values.put(SpeechcareSQLITEHelper.COLUMN_LICENSE, license);
        values.put(SpeechcareSQLITEHelper.COLUMN_INVALIDATIONDATE, invalidationdate);
        values.put(SpeechcareSQLITEHelper.COLUMN_ORIGINALFILE, originalfile);
        values.put(SpeechcareSQLITEHelper.COLUMN_FILENAME, filename);
        values.put(SpeechcareSQLITEHelper.COLUMN_DEACTIVATED, deactivated);
        values.put(SpeechcareSQLITEHelper.COLUMN_CREATED, created);
        values.put(SpeechcareSQLITEHelper.COLUMN_STATUS, status);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }


    public void insertQuestionMedia(JSONObject mediaObj) {


        if (mediaObj.has("status")) {
            mediaObj.remove("status");
        }
        ContentValues values = new ContentValues();
        Iterator<?> keys = mediaObj.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();

            try {
                values.put(key, mediaObj.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (values.size() > 0) {
            database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

    }


    public List<Media> getMediaData() {
        List<Media> mediaList = new ArrayList<Media>();

        Cursor c = database.rawQuery("SELECT " + SpeechcareSQLITEHelper.COLUMN_ID + ", " /*+ SpeechcareSQLITEHelper.COLUMN_ORIGINALFILE + ", "*/ + SpeechcareSQLITEHelper.COLUMN_FILENAME + " FROM " + SpeechcareSQLITEHelper.TABLE_MEDIA + " WHERE " + SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS + " = 0", null);
        Media media;
        c.moveToFirst();

        while (!c.isAfterLast()) {
            media = new Media();
            media.setId(c.getString(0));
            //media.setOriginalfile(c.getString(1));
            media.setFilname(c.getString(1));
            mediaList.add(media);
            c.moveToNext();
        }
        c.close();
        return mediaList;
    }


    public boolean unloadMediaExits() {
        open();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_MEDIA + " WHERE " + SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS + " = 0", null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        if (count > 0) {
            return true;
        }
        return false;
    }

    public void handleColumnDownloadSuccess() {
        open();
        Cursor c = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String[]> result = new ArrayList<String[]>();
        int i = 0;
        result.add(c.getColumnNames());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String[] temp = new String[c.getColumnCount()];
            for (i = 0; i < temp.length; i++) {
                temp[i] = c.getString(i);
                System.out.println("** DB TABLE - " + temp[i]);


                Cursor c1 = database.rawQuery(
                        "SELECT * FROM " + temp[i], null);
                c1.moveToFirst();
                String[] COLUMNS = c1.getColumnNames();
                for (int j = 0; j < COLUMNS.length; j++) {
                    c1.move(j);
                    System.out.println("     ** DB COLUMN - " + COLUMNS[j]);
                }
            }
            result.add(temp);
        }
        close();
    }

    public boolean ColumnExits(String table, String column) {
        boolean found = false;
        open();
        Cursor c1 = database.rawQuery(
                "SELECT * FROM " + table, null);
        c1.moveToFirst();
        String[] COLUMNS = c1.getColumnNames();

        for (int j = 0; j < COLUMNS.length; j++) {
            c1.move(j);
            System.out.println("     ** DB COLUMN - " + COLUMNS[j]);
            if (COLUMNS[j].equals(column)) {
                //System.out.println("     ** DB COLUMN equal ");
                found = true;
            }
        }

        c1.close();
        close();

        return found;
    }

    public void addColoumDownloadSuccessToMedia() {

        if (!ColumnExits(SpeechcareSQLITEHelper.TABLE_MEDIA, SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS)) {

            //System.out.println("     ** DB Add column ");
            open();
            database.execSQL("ALTER TABLE " + SpeechcareSQLITEHelper.TABLE_MEDIA + " ADD COLUMN " + SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS + " INTEGER DEFAULT 0");
            close();
        }
    }


    public void truncateTable() {
        database.delete(SpeechcareSQLITEHelper.TABLE_MEDIA, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_MEDIA + "'", null);
    }


    // PRAXIS VERIONEN METHODEN

    /**
     * laedt alle Media-Daten, die noch nicht heruntergeladen wurden
     *
     * @return
     */
    public List<Media> getMediaDataForPraxis() {
        List<Media> mediaList = new ArrayList<Media>();

        Cursor c = database.rawQuery("SELECT " + SpeechcareSQLITEHelper.COLUMN_ID + ", " /*+ SpeechcareSQLITEHelper.COLUMN_ORIGINALFILE + ", "*/ + SpeechcareSQLITEHelper.COLUMN_FILENAME + ", " + SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS + " FROM " + SpeechcareSQLITEHelper.TABLE_MEDIA, null);
        Media media;
        c.moveToFirst();

        while (!c.isAfterLast()) {
            int success = c.getInt(2);
            if (success == 1) {// wurde bereits erfolgreich heruntergeladen, also nicht in die Liste
                c.moveToNext();
                continue;
            }

            media = new Media();
            media.setId(c.getString(0));
            media.setFilname(c.getString(1));

            mediaList.add(media);
            c.moveToNext();
        }
        c.close();
        return mediaList;
    }

    /**
     * vermerkt, ob eine Mediendatei erfolgreich heruntergeladen wurde
     *
     * @param media
     */
    public void saveSuccess(Media media) {
        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS, 1);
        database.updateWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, values, SpeechcareSQLITEHelper.COLUMN_ID + " = '" + media.getId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);

//        Cursor c = database.rawQuery("SELECT " + SpeechcareSQLITEHelper.COLUMN_ID + ", " /*+ SpeechcareSQLITEHelper.COLUMN_ORIGINALFILE + ", "*/ + SpeechcareSQLITEHelper.COLUMN_FILENAME + ", " + SpeechcareSQLITEHelper.COLUMN_DOWNLOADSUCCESS  + " FROM " + SpeechcareSQLITEHelper.TABLE_MEDIA, null);
//
//        c.moveToFirst();
//        List<Media> mediaList = new ArrayList<Media>();
//        while (!c.isAfterLast()){
//            int success = c.getInt(2);
//            if (success == 1){
//                c.moveToNext();
//                continue; // wurde bereits erfolgreich heruntergeladen, also nicht in die Liste
//            }
//
//
//            media = new Media();
//            media.setId(c.getString(0));
//            media.setFilname(c.getString(1));
//
//            mediaList.add(media);
//            c.moveToNext();
//        }
//
//        c.close();

    }


    public void insertQuestionMediaForPraxis(JSONObject mediaObj) {
        if (mediaObj.has("status")) {
            mediaObj.remove("status");
        }
        ContentValues values = new ContentValues();
        Iterator<?> keys = mediaObj.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                values.put(key, mediaObj.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (values.size() > 0) {
            database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }

    }

}