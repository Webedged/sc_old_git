package de.salait.speechcare.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Christian.Ramthun on 12.09.13.
 */
public class SpeechcareSQLITEHelper extends SQLiteOpenHelper {
    public static final String TABLE_EXERCISE = "exercise";
    public static final String TABLE_EXERCISETYPE = "exercisetype";
    public static final String TABLE_EXERCISEGAPSENTENCEFORIMAGE = "exercisegapsentenceforimage";
    //public static final String COLUMN__ID = "_id";
    public static final String TABLE_EXERCISEGAPWORDFORIMAGE = "exercisegapwordforimage";
    public static final String TABLE_EXERCISEIMAGEFORWORD = "exerciseimageforword";
    public static final String TABLE_EXERCISEMIRRORWITHAUDIO = "exercisemirrorwithaudio";
    public static final String TABLE_EXERCISESORTCHARACTERS = "exercisesortcharacters";
    public static final String TABLE_EXERCISESORTWORDS = "exercisesortwords";
    public static final String TABLE_EXERCISEWORDFORIMAGE = "exercisewordforimage";
    public static final String TABLE_EXERCISEIMAGEFORVIDEO = "exerciseimageforvideo";
    public static final String TABLE_EXERCISEWORDFORVIDEO = "exercisewordforvideo";
    public static final String TABLE_ANSWER = "answer";
    public static final String TABLE_APP = "app";
    public static final String TABLE_MEDIA = "media";
    public static final String TABLE_RELEASEPACKAGE = "releasepackage";
    public static final String TABLE_RELEASEPACKAGE_EXERCISE = "releasepackage_exercise";
    public static final String TABLE_REPEATEXERCISE = "repeatexercise";
    public static final String TABLE_REPEATCARDEXERCISE = "repeatimagecards";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EXERCISE_ID = "exercise_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_HELPVIDEO = "helpVideo";
    public static final String COLUMN_QUESTION_MEDIA_ID = "question_media_id";
    public static final String COLUMN_HELPVIDIO_MEDIA_ID = "helpVideo_media_id";
    public static final String COLUMN_EXERCISETYPE_ID = "exercisetype_id";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_CORRECT_ANSWER = "correct_answer";
    public static final String COLUMN_GAPS = "gaps";
    public static final String COLUMN_ADDITIONALS_WORDS = "additional_words";
    public static final String COLUMN_ADDITIONALS_CHARS = "additional_chars";
    public static final String COLUMN_EXERCISE_TITLE = "exercise_title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_EXERCISE_MODEL = "exercise_model";
    public static final String COLUMN_DEACTIVATED = "deactivated";
    public static final String COLUMN_GRAMMARLAYER = "grammarlayer";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_SORT = "sort";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_LICENSE = "license";
    public static final String COLUMN_INVALIDATIONDATE = "invalidationdate";
    public static final String COLUMN_ORIGINALFILE = "originalfile";
    public static final String COLUMN_MEDIA_ID = "media_id";
    public static final String COLUMN_HELP_MEDIA_ID = "help_media_id";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_PREDECESSOR_ID = "predecessor_id";
    public static final String COLUMN_APP_ID = "app_id";
    public static final String COLUMN_HASH = "hash";
    public static final String COLUMN_MIN_APP_VERSION = "min_app_version";
    public static final String COLUMN_PUBLISHDATE = "publishdate";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_PUBLISHED = "published";
    public static final String COLUMN_APPID = "appid";
    /**
     * table columns
     **/
    public static final String[] COLUMN_TABLE_EXERCISE = {COLUMN_ID, COLUMN_TITLE, COLUMN_QUESTION, COLUMN_HELPVIDEO, COLUMN_QUESTION_MEDIA_ID, COLUMN_HELPVIDIO_MEDIA_ID, COLUMN_EXERCISETYPE_ID, COLUMN_DIFFICULTY};
    public static final String[] COLUMN_TABLE_EXERCISETYPE = {COLUMN_ID, COLUMN_TITLE, COLUMN_EXERCISE_TITLE, COLUMN_DESCRIPTION, COLUMN_EXERCISE_MODEL, COLUMN_DEACTIVATED, COLUMN_GRAMMARLAYER};
    public static final String[] COLUMN_TABLE_EXERCISEGAPSENTENCEFORIMAGE = {COLUMN_ID, COLUMN_CORRECT_ANSWER, COLUMN_GAPS, COLUMN_ADDITIONALS_WORDS};
    public static final String[] COLUMN_TABLE_EXERCISEGAPWORDFORIMAGE = {COLUMN_ID, COLUMN_CORRECT_ANSWER, COLUMN_GAPS, COLUMN_ADDITIONALS_CHARS};
    public static final String[] COLUMN_TABLE_EXERCISEIMAGEFORWORD = {COLUMN_ID, COLUMN_CORRECT_ANSWER};
    public static final String[] COLUMN_TABLE_EXERCISEIMAGEFORVIDEO = {COLUMN_ID, COLUMN_CORRECT_ANSWER};
    public static final String[] COLUMN_TABLE_EXERCISEWORDFORVIDEO = {COLUMN_ID, COLUMN_CORRECT_ANSWER};
    public static final String[] COLUMN_TABLE_EXERCISEMIRRORWITHAUDIO = {COLUMN_ID, COLUMN_CORRECT_ANSWER};
    public static final String[] COLUMN_TABLE_EXERCISESORTCHARACTERS = {COLUMN_ID, COLUMN_CORRECT_ANSWER, COLUMN_ADDITIONALS_CHARS};
    public static final String[] COLUMN_TABLE_EXERCISESORTWORDS = {COLUMN_ID, COLUMN_CORRECT_ANSWER, COLUMN_ADDITIONALS_WORDS};
    public static final String[] COLUMN_TABLE_EXERCISEWORDFORIMAGE = {COLUMN_ID, COLUMN_CORRECT_ANSWER};
    public static final String[] COLUMN_TABLE_ANSWER = {COLUMN_ID, COLUMN_EXERCISE_ID, COLUMN_SORT, COLUMN_MEDIA_ID, COLUMN_VALUE};
    public static final String[] COLUMN_TABLE_SEVANSWER = {COLUMN_ID, COLUMN_EXERCISE_ID, COLUMN_SORT, COLUMN_MEDIA_ID, COLUMN_VALUE, COLUMN_HELP_MEDIA_ID};
    public static final String[] COLUMN_TABLE_APP = {COLUMN_ID, COLUMN_NAME, COLUMN_APPID,};
    public static final String[] COLUMN_TABLE_MEDIA = {COLUMN_ID, COLUMN_TITLE, COLUMN_TYPE, COLUMN_SOURCE, COLUMN_LICENSE, COLUMN_INVALIDATIONDATE, COLUMN_ORIGINALFILE, COLUMN_FILENAME, COLUMN_DEACTIVATED, COLUMN_CREATED};
    public static final String[] COLUMN_TABLE_RELEASEPACKAGE = {COLUMN_ID, COLUMN_NAME, COLUMN_VERSION, COLUMN_PREDECESSOR_ID, COLUMN_APP_ID, COLUMN_HASH, COLUMN_MIN_APP_VERSION, COLUMN_PUBLISHDATE, COLUMN_CREATED, COLUMN_PUBLISHED};
    public static final String[] COLUMN_TABLE_RELEASEPACKAGE_EXERCISE = {COLUMN_ID};
    public static final String[] COLUMN_TABLE_REPEATEXERCISE = {COLUMN_EXERCISE_ID};
    // Praxis Versionen Values
    public static final String TABLE_UNSENTANSWER = "completedexercise";
    public static final String TABLE_COLLECTION = "collection";
    public static final String TABLE_COLLECTION_MEDIACOLLECTION = "collection_mediacollection";
    public static final String TABLE_COLLECTION_EXERCISE = "collection_exercise";
    public static final String TABLE_USER = "user";
    public static final String TABLE_COMPLETEDEXERCISE = "completedexercise";
    public static final String TABLE_MEDIACOLLECTION = "mediacollection";
    public static final String TABLE_MEDIACOLLECTION_MEDIA = "mediacollection_media";
    public static final String COLUMN_JSONANSWER = "jsonstring";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_ENDDATE = "enddate";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_COLLECTION_ID = "collection_id";
    public static final String COLUMN_MEDIACOLLECTION_ID = "mediacollection_id";
    public static final String COLUMN_DOWNLOADSUCCESS = "downloadSuccess";
    public static final String COLUMN_BILDART = "bildart";
    public static final String COLUMN_IMAGEMEDIA_ID = "image_media_id";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_STATUS = "status";
    //public static final String[] COLUMN_TABLE_UNSENTANSWER = {COLUMN_ID, COLUMN_JSONANSWER};
    public static final String[] COLUMN_TABLE_UNSENTANSWER = {COLUMN_JSONANSWER};
    public static final String[] COLUMN_TABLE_COLLECTION = {COLUMN_ID, COLUMN_DATE, COLUMN_ENDDATE, COLUMN_USER_ID};
    public static final String[] COLUMN_TABLE_COLLECTION_EXERCISE = {COLUMN_COLLECTION_ID, COLUMN_EXERCISE_ID};
    public static final String[] COLUMN_TABLE_COLLECTION_MEDIACOLLECTION = {COLUMN_COLLECTION_ID, COLUMN_MEDIACOLLECTION_ID};
    public static final String[] COLUMN_TABLE_REPEATEXERCISE_PraxisV = {COLUMN_EXERCISE_ID, COLUMN_USER_ID, COLUMN_COLLECTION_ID};
    public static final String[] COLUMN_TABLE_MEDIACOLLECTION = {COLUMN_ID, COLUMN_TITLE, COLUMN_IMAGEMEDIA_ID};
    private static final String DATABASE_NAME = "speechcare.sqlite";
    private static final int DATABASE_VERSION = 13;
    private static final String DATABASE_CREATE = "";
    /**
     * create statements
     **/

    private static final String CREATE_TABLE_EXERCISE = "CREATE TABLE 'exercise' ('id' INTEGER PRIMARY KEY  NOT NULL ,'title' VARCHAR,'question' VARCHAR,'helpVideo' VARCHAR,'question_media_id' INTEGER,'helpVideo_media_id' INTEGER, 'exercisetype_id' INTEGER, 'difficulty' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISETYPE = "CREATE TABLE 'exercisetype' ('id' INTEGER PRIMARY KEY  NOT NULL , 'number' INTEGER, 'title' VARCHAR, 'exercise_title' VARCHAR, 'description' TEXT, 'icon' VARCHAR, 'exercise_model' VARCHAR, 'deactivated' INTEGER, 'grammarlayer' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISEGAPSENTENCEFORIMAGE = "CREATE TABLE 'exercisegapsentenceforimage' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' VARCHAR,'gaps' VARCHAR,'additional_words' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISEGAPWORDFORIMAGE = "CREATE TABLE 'exercisegapwordforimage' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' VARCHAR,'gaps' VARCHAR,'additional_chars' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISEIMAGEFORWORD = "CREATE TABLE 'exerciseimageforword' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' INTEGER)";
    private static final String CREATE_TABLE_EXERCISEMIRRORWITHAUDIO = "CREATE TABLE 'exercisemirrorwithaudio' ('id' INTEGER PRIMARY KEY  NOT NULL , 'compare_level' INTEGER, 'min_time_over_level_at_once' INTEGER, 'min_time_over_level_total' INTEGER)";
    private static final String CREATE_TABLE_EXERCISESORTCHARACTERS = "CREATE TABLE 'exercisesortcharacters' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' VARCHAR,'additional_chars' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISESORTWORDS = "CREATE TABLE 'exercisesortwords' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' VARCHAR,'additional_words' VARCHAR)";
    private static final String CREATE_TABLE_EXERCISEWORDFORIMAGE = "CREATE TABLE 'exercisewordforimage' ('id' INTEGER PRIMARY KEY  NOT NULL ,'correct_answer' INTEGER)"; // das zuerst umsetzen
    private static final String CREATE_TABLE_ANSWER = "CREATE TABLE 'answer' ('id' INTEGER PRIMARY KEY  NOT NULL ,'exercise_id' INTEGER NOT NULL ,'sort' INTEGER NOT NULL ,'media_id' INTEGER, 'value' VARCHAR)";
    private static final String CREATE_TABLE_APP = "CREATE TABLE 'app' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' VARCHAR, 'appid' VARCHAR)";
    private static final String CREATE_TABLE_MEDIA = "CREATE TABLE 'media' ('id' INTEGER PRIMARY KEY  NOT NULL , 'title' VARCHAR, 'type' VARCHAR, 'source' TEXT, 'license' TEXT, 'invalidationdate' DATETIME, 'originalfile' TEXT, 'filename' TEXT, 'deactivated' INTEGER, 'created' DATETIME)";
    private static final String CREATE_TABLE_RELEASEPACKAGE = "CREATE TABLE 'releasepackage' ('id' INTEGER PRIMARY KEY  NOT NULL , 'name' VARCHAR, 'version' VARCHAR, 'predecessor_id' INTEGER, 'app_id' INTEGER, 'hash' VARCHAR, 'min_app_version' VARCHAR, 'publishdate' DATETIME, 'created' DATETIME, 'published' INTEGER)";
    private static final String CREATE_TABLE_RELEASEPACKAGE_EXERCISE = "CREATE TABLE 'releasepackage_exercise' ('releasepackage_id' INTEGER NOT NULL , 'exercise_id' INTEGER NOT NULL , PRIMARY KEY ('releasepackage_id', 'exercise_id'))";
    private static final String CREATE_TABLE_REPEATEXERCISE = "CREATE TABLE 'repeatexercise' ('exercise_id' INTEGER PRIMARY KEY  NOT NULL )";
    public SQLiteDatabase myDataBase;
    private Context context;
    private String DB_PATH; /*= "/data/data/"
                        + context.getApplicationContext().getPackageName()
                        + "/databases/";
    //private String DB_PATH = mycontext.getApplicationContext().getPackageName()+"/databases/";
                                                                        /**  Database entities **/


    public SpeechcareSQLITEHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) throws IOException {
        super(context, DATABASE_NAME, factory, (version > 0) ? version : DATABASE_VERSION);
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.context = context;
        if (!databaseExists()) {
            copydatabase();
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        /*db.execSQL(this.CREATE_TABLE_ANSWER);
        db.execSQL(this.CREATE_TABLE_APP);
        db.execSQL(this.CREATE_TABLE_EXERCISE);
        usw.
        */
        /*try {
            //db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER); 								// f�r Testzwecke kann dies benutzt werden
            db.execSQL(DATABASE_CREATE);
            //Toast.makeText(context, "Create DB statement successful.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(context, "Create DB statement failed.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/
        Log.i("", "");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.i("", "");

    }
/** Methoden zum erstellen der Datenbank aus der Testdatenbank**/

    /**
     * checks if the DB exists
     *
     * @return
     */
    private boolean databaseExists() {

        boolean dbExists = false;
        try {
            String myPath = DB_PATH + DATABASE_NAME;
            File dbfile = new File(myPath);
            //dbExists = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READWRITE);
            dbExists = dbfile.exists();
        } catch (SQLiteException e) {
            //System.out.println("Database doesn't exist");
        }
        return dbExists;
    }


    /*public void createDatabase() throws IOException {

        if(databaseExists()) {
            //System.out.println(" Database exists.");
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }*/

    /**
     * Kopiert die Test-Datenbank aus dem Assets-Ordner ins App-Verzeichnis
     *
     * @throws IOException
     */
    private void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = context.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outfilename = DB_PATH + DATABASE_NAME;

        File file = new File(DB_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(DB_PATH + DATABASE_NAME);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer)) > 0) {
            myoutput.write(buffer, 0, length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }


    /*public void opendatabase()  {
        String mypath = DB_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }*/

}
