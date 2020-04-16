package de.salait.speechcare.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.salait.speechcare.R;
import de.salait.speechcare.utility.DifferentiateCardHandler;

/**
 * Class representing the preferences for one user
 * Created by Christian.Ramthun on 11.09.13.
 */
public class Settings {
    /**
     * Speicherung in XML
     */
    protected static final String PREFS_FILE = "settings.xml";
    private static final String PREFS_IS_FIRSTSTART = "firststart";
    private static final String PREFS_EMAIL = "email";
    private static final String PREFS_IS_SOUND_ACTIVATED = "isSoundActivated";
    private static final String PREFS_IS_VIDEOHELP_ACTIVATED = "isVideohelpActivated";
    private static final String PREFS_DIFFICULTYLEVEL = "difficultyLevel";
    private static final String PREFS_REPEATKIND = "repeatKind";
    private static final String PREFS_TIMELIMIT = "timelimit";
    private static final String PREFS_IS_TIMELIMIT = "isTimelimit";
    private static final String PREFS_EXERCISELEVEL = "exerciseLimit";
    private static final String PREFS_IS_RANDOMTYPE_ACTIVATED = "isRandomTypeActivated";
    private static final String PREFS_BILDART = "bildart";
    private static final String PREFS_UUID = "uuid";
    private static final String PREFS_USERNAME = "username";
    private static final String PREFS_PASSWORD = "password";
    private static final String PREFS_USERID = "userid";
    private static final String PREFS_REPEATEXERCISEMODE = "repeatexercisemode";
    private static final String PREFS_SETTINGCHANGEALLOWED = "settingchangeallowed";
    private static final String PREFS_LOGINSTATUS = "isLogin";
    protected final Activity activity;
    public ArrayList<String> allowedExerciseTypes;
    private boolean isFirstStart;
    private String emailadressForResults;


    // Praxis-Version
    private boolean isSoundActivated;
    private boolean isVideohelpActivated;
    private String difficultyLevel;
    private int repeatKind;
    private int timelimit;
    private boolean isTimeLimit;
    private int exerciseLimit;
    private boolean isRandomTypeActivated;
    private String uuid;
    private String username;
    private String password;
    private String userid;
    private String repeatexercisemode;
    private boolean settingchangeallowed;

    public Settings(Activity a) {
        this.activity = a;
    }

    public Settings(Context context) {
        this(null);
    }

    public static String loadUuid(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        return prefs.getString(PREFS_UUID, null);
    }

    /**
     * resets the pref file to delete possible old values
     */
    public static void clearPrefs(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        if (prefs == null) {
            return;
        }
        if (!prefs.edit().clear().commit()) {
            //if (isDebug()) Toast.makeText(activity.getApplicationContext(), "save failed", Toast.LENGTH_SHORT).show();
        }
    }

    protected static String getDeviceUuid(Context context) {
        UUID uuid = null;
        final String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        // Use the Android ID unless it's broken, in which case
        // fallback on deviceId,
        // unless it's not available, then fallback on a random
        // number which we store to a prefs file
        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId
                        .getBytes("utf8"));
            } else {
                //final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();   auskommentiert weil zusaetzliche Berechtigung benoetigt
                //uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"))
                //                        : UUID.randomUUID();
                uuid = UUID.randomUUID();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return uuid.toString();
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void saveUUID() {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        if (prefs.getString(PREFS_UUID, null) != null) return;
        prefs.edit().putString(PREFS_UUID, Settings.getDeviceUuid(activity)).commit();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserid() {

        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getRepeatexercisemode() {
        return repeatexercisemode;
    }

    /**
     * one of: "norepeat", "repeatuntilcorrect", "showcorrect"
     *
     * @param repeatexercisemode
     */
    public void setRepeatexercisemode(String repeatexercisemode) {
        this.repeatexercisemode = repeatexercisemode;
    }

    public String getBildArt() {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        return prefs.getString(PREFS_BILDART, "FOTO");
    }

    public void setPrefsBildart(String bildart) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        prefs.edit().putString(PREFS_BILDART, bildart).commit();
    }

    /**
     * gibt an, ob der Benutzer seine Einstellunge bearbeiten darf oder nicht
     *
     * @return
     */
    public boolean isSettingchangeallowed() {
        return settingchangeallowed;
    }

    // Praxis-Version END

    public void setSettingchangeallowed(boolean settingchangeallowed) {
        this.settingchangeallowed = settingchangeallowed;
    }

    /**
     * loads the user related data into object
     */
    public void loadUserprefs() {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        setUsername(prefs.getString(PREFS_USERNAME, ""));
        setPassword(prefs.getString(PREFS_PASSWORD, ""));
    }

    public boolean saveUserToPrefs() {
        boolean saveSucceeded = true;
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        saveSucceeded &= prefs.edit().putString(PREFS_USERNAME, getUsername()).commit();
        saveSucceeded &= prefs.edit().putString(PREFS_PASSWORD, getPassword()).commit();

        prefs.edit().putBoolean(PREFS_LOGINSTATUS, saveSucceeded).commit();

        return saveSucceeded;
    }

    public void setUserLoginStatus(Boolean isLogin) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        prefs.edit().putBoolean(PREFS_LOGINSTATUS, isLogin).commit();
    }

    public boolean isLogin() {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        return prefs.getBoolean(PREFS_LOGINSTATUS, false);
    }

    public boolean isFirstStart() {
        return isFirstStart;
    }

    public void setFirstStart(boolean firstStart) {
        isFirstStart = firstStart;
    }

    public String getEmailadressForResults() {
        return emailadressForResults;
    }

    public void setEmailadressForResults(String emailadressForResults) {
        this.emailadressForResults = emailadressForResults;
    }

    public boolean isSoundActivated() {
        return isSoundActivated;
    }

    public void setSoundActivated(boolean soundActivated) {
        isSoundActivated = soundActivated;
    }

    public boolean isVideohelpActivated() {
        return isVideohelpActivated;
    }

    public void setVideohelpActivated(boolean videohelpActivated) {
        isVideohelpActivated = videohelpActivated;
    }

    /**
     * @return "easy", "medium" or "hard"
     */
    public String getDifficultyLevel() {

        return difficultyLevel;
    }

    /**
     * @param difficultyLevel has to be of: "easy", "medium" or "hard"
     */
    public void setDifficultyLevel(String difficultyLevel) throws NoSuchFieldException {
        this.difficultyLevel = "";
        if (!difficultyLevel.equalsIgnoreCase(Exercise.DIFFICULTY_LEVEL_EASY) && !difficultyLevel.equalsIgnoreCase(Exercise.DIFFICULTY_LEVEL_MEDIUM) && !difficultyLevel.equalsIgnoreCase(Exercise.DIFFICULTY_LEVEL_HARD)) {
            throw new NoSuchFieldException("param \"difficultyLevel\" must be one of: \"easy\", \"medium\" or \"hard\"");

        }
        this.difficultyLevel = difficultyLevel;
    }

    public int getCardCountForExercise() {
        if (difficultyLevel.equalsIgnoreCase("easy")) {
            return 1;
        } else if (difficultyLevel.equalsIgnoreCase("medium")) {
            return 2;
        } else if (difficultyLevel.equalsIgnoreCase("hard")) {
            return 4;
        }
        return 1;
    }

    public int getKindOfRepeat() {

        return repeatKind;
    }

    public void setKindOfRepeat(int repeatKind) {

        this.repeatKind = repeatKind;
    }

    public int getTimelimit() {
        return timelimit;
    }

    public void setTimelimit(int timelimit) {
        this.timelimit = timelimit;
    }

    public boolean isTimeLimit() {
        return isTimeLimit;
    }

    public void setTimeLimit(boolean timeLimit) {
        isTimeLimit = timeLimit;
    }

    public boolean isExerciseLimit() {
        return !isTimeLimit;            // exerciseLimit ist die Verneinung von isTimeLimit
    }

    public void setIsExerciseLimit(boolean exerciseLimit) {
        isTimeLimit = !exerciseLimit;   // exerciseLimit ist die Verneinung von isTimeLimit
    }

    public int getExerciseLimit() {
        if (exerciseLimit < 1) exerciseLimit = 1;
        return exerciseLimit;
    }

    public void setExerciseLimit(int exerciseLimit) {
        if (exerciseLimit < 1) exerciseLimit = 1;
        this.exerciseLimit = exerciseLimit;
    }

    public boolean isRandomTypeActivated() {
        return isRandomTypeActivated;
    }

    public void setRandomTypeActivated(boolean randomTypeActivated) {
        isRandomTypeActivated = randomTypeActivated;
    }


    // Praxis-Erweiterung


//    public String getUuid() {
//        return this.uuid;
//    }
//
//    public static String loadUuid(Activity activity) {
//        SharedPreferences prefs = activity.getSharedPreferences( PREFS_FILE, 0);
//        return prefs.getString(PREFS_UUID, null);
//    }
//
//    public void setUuid(String uuid) {
//        this.uuid = uuid;
//    }
//
//    public void saveUUID(){
//        final SharedPreferences prefs = activity.getSharedPreferences( PREFS_FILE, 0);
//        if (prefs.getString(PREFS_UUID, null) != null) return;
//        prefs.edit().putString(PREFS_UUID, Settings.getDeviceUuid(activity)).commit();
//    }

    public void setExerciseTypeEnabled(String typID, boolean isEnabled) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(typID, isEnabled);
        editor.commit();
    }

    public boolean isExerciseTypeEnabled(String typID) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        return prefs.getBoolean(typID, true);
    }

    public void loadFromPrefs() throws NoSuchFieldException, IOException {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);


        DifferentiateCardHandler differentiateCardHandler = new DifferentiateCardHandler();

        final List<String> thisExerciseTypes = differentiateCardHandler.getExerciseTypes(activity, getBildArt());

        setFirstStart(prefs.getBoolean(PREFS_IS_FIRSTSTART, true));
        setEmailadressForResults(prefs.getString(PREFS_EMAIL, ""));
        setSoundActivated(prefs.getBoolean(PREFS_IS_SOUND_ACTIVATED, true));
        setVideohelpActivated(prefs.getBoolean(PREFS_IS_VIDEOHELP_ACTIVATED, true));
        setDifficultyLevel(prefs.getString(PREFS_DIFFICULTYLEVEL, "easy"));
        setKindOfRepeat(prefs.getInt(PREFS_REPEATKIND, 3));
        setTimelimit(prefs.getInt(PREFS_TIMELIMIT, 3));
        setTimeLimit(prefs.getBoolean(PREFS_IS_TIMELIMIT, true));
        setExerciseLimit(prefs.getInt(PREFS_EXERCISELEVEL, 1));
        setRandomTypeActivated(prefs.getBoolean(PREFS_IS_RANDOMTYPE_ACTIVATED, true));
//        setUuid(prefs.getString(PREFS_UUID, getDeviceUuid(activity)));
        allowedExerciseTypes = new ArrayList<String>();
        if (isRandomTypeActivated() == false) {
            for (int i = 0, size = thisExerciseTypes.size(); i < size; i++) {

                boolean enabled = isExerciseTypeEnabled(thisExerciseTypes.get(i));
                if (isExerciseTypeEnabled(thisExerciseTypes.get(i)) == true) {
                    allowedExerciseTypes.add(thisExerciseTypes.get(i));
                }

            }
        }
        if (allowedExerciseTypes.size() == 0) {
            allowedExerciseTypes.addAll(thisExerciseTypes);
        }
        if (activity.getResources().getBoolean(R.bool.isPlusVersion)) {
            final SharedPreferences prefse = activity.getSharedPreferences(PREFS_FILE, 0);
            setUserid(prefse.getString(PREFS_USERID, "1"));
            setRepeatexercisemode(prefse.getString(PREFS_REPEATEXERCISEMODE, ""));
            setSettingchangeallowed(prefse.getBoolean(PREFS_SETTINGCHANGEALLOWED, false));
        }
    }

    public void saveEmail(String emailadress) {
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);
        prefs.edit().putString(PREFS_EMAIL, emailadress).commit();
    }

    public boolean saveToPrefs() {

        boolean saveSucceeded = true;
        final SharedPreferences prefs = activity.getSharedPreferences(PREFS_FILE, 0);


        saveSucceeded &= prefs.edit().putString(PREFS_EMAIL, getEmailadressForResults()).commit();

        saveSucceeded &= prefs.edit().putBoolean(PREFS_IS_FIRSTSTART, isFirstStart()).commit();
        saveSucceeded &= prefs.edit().putBoolean(PREFS_IS_SOUND_ACTIVATED, isSoundActivated()).commit();
        saveSucceeded &= prefs.edit().putBoolean(PREFS_IS_VIDEOHELP_ACTIVATED, isVideohelpActivated()).commit();

        saveSucceeded &= prefs.edit().putString(PREFS_DIFFICULTYLEVEL, getDifficultyLevel()).commit();
        saveSucceeded &= prefs.edit().putInt(PREFS_REPEATKIND, getKindOfRepeat()).commit();
        saveSucceeded &= prefs.edit().putInt(PREFS_TIMELIMIT, getTimelimit()).commit();
        saveSucceeded &= prefs.edit().putBoolean(PREFS_IS_TIMELIMIT, isTimeLimit()).commit();
        saveSucceeded &= prefs.edit().putInt(PREFS_EXERCISELEVEL, getExerciseLimit()).commit();

        saveSucceeded &= prefs.edit().putBoolean(PREFS_IS_RANDOMTYPE_ACTIVATED, isRandomTypeActivated()).commit();

//        saveSucceeded &= prefs.edit().putString(PREFS_UUID, getUuid()).commit();

        //if (isDebug()) {	if(saveSucceeded) { Toast.makeText(activity.getApplicationContext(), "save succeeded", Toast.LENGTH_SHORT).show();} else {Toast.makeText(activity.getApplicationContext(), "save failed", Toast.LENGTH_SHORT).show();}		}

        if (activity.getResources().getBoolean(R.bool.isPlusVersion)) {
            saveSucceeded &= prefs.edit().putString(PREFS_USERID, getUserid()).commit();
            saveSucceeded &= prefs.edit().putString(PREFS_REPEATEXERCISEMODE, getRepeatexercisemode()).commit();
            saveSucceeded &= prefs.edit().putBoolean(PREFS_SETTINGCHANGEALLOWED, isSettingchangeallowed()).commit();
        }

        return saveSucceeded;
    }


}
