package de.salait.speechcare.dao;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.util.HashMap;

import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by bodo.grossmann on 05.11.13.
 */
public class UserSettingsDataSource {

    private SQLiteDatabase database;
    private SpeechcareSQLITEHelper dbHelper;
    private Activity a;

    public UserSettingsDataSource(Activity activity) throws IOException {
        a = activity;
        dbHelper = new SpeechcareSQLITEHelper(activity.getApplicationContext(), null, null, 0);
    }

    public void close() {
        if (dbHelper != null) dbHelper.close();
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }


    public void saveSettingsToDB(String userId, String name, String pw, int changeAllowed, int timelimit, int exlimit,
                                 String difficulty, String email, String extypes, String modus,
                                 String repmodus, int sound, int video) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("userid", userId);
        values.put("settingChangeallowed", changeAllowed);
        values.put("timelimit", timelimit);
        values.put("exceerciselimit", exlimit);
        values.put("excercisetype", extypes);
        values.put("pw", pw);
        values.put("emailstatistik", email);
        values.put("setSound", sound);
        values.put("helpvideos", video);
        values.put("modus", modus);
        values.put("repeatexercisemode", repmodus);
        values.put("difficult", difficulty);

        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean checkIfUserisInDB(String name, String pw) {

        Cursor mCount = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_USER + " WHERE name = '" + name + "' AND pw ='" + pw + "'", null);
        mCount.moveToFirst();
        return mCount.getInt(0) > 0;
    }

    public void loadUserData(String name, String pw) throws NoSuchFieldException {
        Cursor cursor = database.rawQuery("SELECT * FROM " + SpeechcareSQLITEHelper.TABLE_USER + " WHERE name = '" + name + "' AND pw ='" + pw + "'", null);
        cursor.moveToFirst();

        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            map.put(cursor.getColumnName(i), cursor.getString(i));
        }

        Settings sets = new Settings(a);

        if (map.get("settingChangeallowed") != null) {
            sets.setSettingchangeallowed((Integer.parseInt(map.get("settingChangeallowed")) == 1));
        }
        if (map.get("timelimit") != null) {
            sets.setTimelimit(Integer.parseInt(map.get("timelimit")));
        }
        if (map.get("exceerciselimit") != null) {
            sets.setExerciseLimit(Integer.parseInt(map.get("exceerciselimit")));
        }
        if (map.get("difficult") != null) {
            sets.setDifficultyLevel(map.get("difficult"));
        }
        if (map.get("userid") != null) {
            sets.setUserid(map.get("userid"));
        }
        if (map.get("emailstatistik") != null) {
            sets.setEmailadressForResults(map.get("emailstatistik"));
        }
        if (map.get("repeatexercisemode") != null) {
            sets.setRepeatexercisemode(map.get("repeatexercisemode"));
        }
        if (map.get("setSound") != null) {
            sets.setSoundActivated(map.get("setSound").equalsIgnoreCase("1"));
        }
        if (map.get("helpvideos") != null) {
            sets.setVideohelpActivated(map.get("helpvideos").equalsIgnoreCase("1"));
        }
        if (map.get("modus") != null && map.get("modus").equalsIgnoreCase("time")) {
            sets.setTimeLimit(true);
            sets.setIsExerciseLimit(false);
        } else {
            sets.setIsExerciseLimit(true);
            sets.setTimeLimit(false);
        }

        if (map.get("excercisetype") != null && map.get("excercisetype").length() > 0) {
            sets.setRandomTypeActivated(false);
            String extypes = map.get("excercisetype");
            String[] types = extypes.split(",");

            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                sets.setExerciseTypeEnabled(type, true);
            }
        } else {
            sets.setRandomTypeActivated(true);
        }

        if (map.get("name") != null) {
            sets.setUsername(map.get("name"));
        }
        if (map.get("pw") != null) {
            sets.setPassword(map.get("pw"));
        }
        sets.saveToPrefs();
        sets.saveUserToPrefs();
    }
}