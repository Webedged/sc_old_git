package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by Christian.Ramthun on 04.11.13.
 */
public class CollectionDataSource {
    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;


    public CollectionDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * schreibt eine Collection fuer einen Benutzer in die Collection-Tabelle oder fuehrt ein Update durch
     *
     * @param id      collection id
     * @param date    ?
     * @param enddate Gueltigkeitsdatum der Collection
     * @param userid  Id des Benutzers
     */
    public boolean insertCollectionData(String id, String date, String enddate, String userid) {

        boolean collectionShouldUpdate = false;

        int countEntry = 0;
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_COLLECTION, SpeechcareSQLITEHelper.COLUMN_TABLE_COLLECTION, "user_id = '" + userid + "' AND id = '" + id + "'", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            if (!date.equals(cursor.getString(1))) {
                collectionShouldUpdate = true;
            }
            countEntry++;
            cursor.moveToNext();
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(id));
        values.put(SpeechcareSQLITEHelper.COLUMN_DATE, date);
        values.put(SpeechcareSQLITEHelper.COLUMN_ENDDATE, enddate);
        values.put(SpeechcareSQLITEHelper.COLUMN_USER_ID, Integer.valueOf(userid));

        if (countEntry > 0) {
            database.update(SpeechcareSQLITEHelper.TABLE_COLLECTION, values, "user_id = '" + userid + "' AND id = '" + id + "'", null);
        } else {
            collectionShouldUpdate = true;
            database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_COLLECTION, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return collectionShouldUpdate;
    }

    /**
     * stellt die verbindenden Eintraege zwischen Collections und Uebungen in der CollectionExercise-Tabelle her
     *
     * @param collection_id
     * @param exercise_id
     */
    public void insertCollectionExercise(String collection_id, String exercise_id) {
        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_COLLECTION_ID, Integer.valueOf(collection_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, Integer.valueOf(exercise_id));
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_COLLECTION_EXERCISE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertCollectionMedia(String collection_id, String mediaColl_id, String art) {
        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_COLLECTION_ID, Integer.valueOf(collection_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIACOLLECTION_ID, Integer.valueOf(mediaColl_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_BILDART, art);
        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_COLLECTION_MEDIACOLLECTION, null, values, SQLiteDatabase.CONFLICT_REPLACE);


    }

    public void truncateTable() {
        if (tableExists(SpeechcareSQLITEHelper.TABLE_COLLECTION_MEDIACOLLECTION)) {
            database.delete(SpeechcareSQLITEHelper.TABLE_COLLECTION_MEDIACOLLECTION, null, null);
        }
        if (tableExists(SpeechcareSQLITEHelper.TABLE_COLLECTION_EXERCISE)) {

            database.delete(SpeechcareSQLITEHelper.TABLE_COLLECTION_EXERCISE, null, null);
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

    /**
     * entfernt eine Collection aus allen betroffenen Tabellen (Collection, collectionExercise)
     *
     * @param collection_id
     */
    public void eraseCollection(String collection_id) {
        this.deleteCollection(collection_id);
        this.deleteCollectionExercise(collection_id);
    }

    /**
     * loescht alle Eintraege zu einer Collection aus der Collection-Tabelle
     *
     * @param collection_id
     */
    public void deleteCollection(String collection_id) {
        database.delete(SpeechcareSQLITEHelper.TABLE_COLLECTION, SpeechcareSQLITEHelper.COLUMN_ID + " = '" + collection_id + "'", null);
    }

    /**
     * loescht alle Eintraege zu einer Collection aus der CollectionExercise-Tabelle
     *
     * @param collection_id
     */
    public void deleteCollectionExercise(String collection_id) {
        database.delete(SpeechcareSQLITEHelper.TABLE_COLLECTION_EXERCISE, SpeechcareSQLITEHelper.COLUMN_COLLECTION_ID + " = '" + collection_id + "'", null);
    }

    public List<String> getCollectionsForUser(String userid) {
        List<String> collectionList = new ArrayList<String>();

        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_COLLECTION, SpeechcareSQLITEHelper.COLUMN_TABLE_COLLECTION, "user_id = '" + userid + "'", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            collectionList.add(String.valueOf(cursor.getInt(0)));
            cursor.moveToNext();
        }
        cursor.close();
        return collectionList;
    }

    public void deleteCollectionForUser(String colid, String userid) {

        // delete collection
        database.delete(SpeechcareSQLITEHelper.TABLE_COLLECTION, "id = '" + colid + "' AND user_id = '" + userid + "'", null);

        // delete repeatExercise
        database.delete(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, "collection_id = '" + colid + "' AND user_id = '" + userid + "'", null);
    }


//    public void insertCollectionMediacollection(String collection_id, String mediacollection_id){
//        ContentValues values = new ContentValues();
//        values.put(SpeechcareSQLITEHelper.COLUMN_COLLECTION_ID, Integer.valueOf(collection_id));
//        values.put(SpeechcareSQLITEHelper.COLUMN_MEDIACOLLECTION_ID, Integer.valueOf(mediacollection_id));
//        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_COLLECTION_MEDIACOLLECTION, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//    }
}
