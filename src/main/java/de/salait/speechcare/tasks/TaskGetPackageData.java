package de.salait.speechcare.tasks;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.salait.speechcare.R;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.SpeechcareSQLITEHelper;
import de.salait.speechcare.utility.DifferentiateCardHandler;


/**
 * Created by sandra.stuck on 19.09.13.
 */
public class TaskGetPackageData extends AsyncTask<String, Integer, Boolean> {
    public static final String authUsername = "speechcare";
    public static final String authPassword = "kugelblitz";
    static final String COOKIES_HEADER = "Set-Cookie";
    private static final String secret = "g9od1m3uv.Z%$#R4KT3C!b7Ny@x=0&r!u4Y+T#]GCWko5<X_73TD9u8FWU%=7}v";
    private static final String doHandshake = "handshake/begin/";
    private static final String verifyHandshake = "handshake/verify/?response=";
    private static final String mediaBaseUrl = "media/download/";
    static java.net.CookieManager msCookieManager = new java.net.CookieManager();
    @SuppressWarnings("FieldCanBeLocal")
    private static String releasepackageID; // 6=AphasieLite , 1=AphasieVollVersion
    private final Activity activity;
    ProgressBar progressBar;
    TextView tvLoading, tvPer;
    int exCount;
    int mediaCount;
    boolean mediadownload;
    File directory;
    Settings settings;
    private String DOMAIN_ONLY;
    private Dialog dialog;


    public TaskGetPackageData(Activity c) {
        activity = c;
        mediadownload = false;
        System.out.println(" <<< TaskGetPackageData");
        ContextWrapper contextWrapper = new ContextWrapper(activity);
        directory = contextWrapper.getFilesDir();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        releasepackageID = activity.getResources().getString(R.string.app_id);
        DOMAIN_ONLY = activity.getResources().getString(R.string.serverURL);
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progressdialog);
        progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar1);
        tvLoading = (TextView) dialog.findViewById(R.id.tv1);
        tvPer = (TextView) dialog.findViewById(R.id.tvper);
        dialog.show();

    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (dialog != null) {
            dialog.dismiss();
        }
        settings = loadSettings();
        settings.setFirstStart(false);
        settings.saveToPrefs();
        activity.recreate();
        super.onPostExecute(result);
    }

    /**
     * laedt die Einstellungen aus der Prefs.xml
     *
     * @return gibt ein vollstaendig geladenes Settingsobjekt zurueck oder null im Fehlerfall
     */
    private Settings loadSettings() {
        settings = new Settings(activity);
        try {
            settings.loadFromPrefs();
            return settings;
        } catch (NoSuchFieldException e) {

        } catch (IOException e) {

        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0] == -1) {
            if (activity.getResources().getString(R.string.app_id).equals("5") || activity.getResources().getString(R.string.app_id).equals("7")) {
                tvLoading.setText(activity.getResources().getString(R.string.label_init_import_kids));
            } else {
                tvLoading.setText(activity.getResources().getString(R.string.label_init_import));
            }

        } else if (mediadownload == false) {
            tvLoading.setText(activity.getResources().getString(R.string.label_import_ex) + " " + values[0] + " " + activity.getResources().getString(R.string.von) + " " + exCount);
            tvPer.setText(values[0] * 100 / exCount + " %");
            progressBar.setProgress(values[0] * 100 / exCount);
        } else {
            tvLoading.setText(activity.getResources().getString(R.string.label_download_media) + " (" + values[0] + " / " + mediaCount + ")");
            tvPer.setText(values[0] * 100 / mediaCount + " %");
            progressBar.setProgress(values[0] * 100 / mediaCount);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        // DO Handshake
        try {
            publishProgress(-1);
            System.out.println("**** doInBackground Start");
            String url = DOMAIN_ONLY + doHandshake;

            URL myURL = new URL(url);
            System.out.println("**** myURL " + myURL);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            System.out.println("**** httpConn ");
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");
            int statusCode = httpConn.getResponseCode();
            System.out.println("**** STATUSCODE DOHANDSHAKE " + String.valueOf(statusCode));
            Log.i("STATUSCODE DOHANDSHAKE", String.valueOf(statusCode));
            if (statusCode == 200) {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String entityStr = response.toString();

                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);


                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }


                verifyHandshake(entityStr);

            } else {
                // TODO ERROR HANDLING
            }

        } catch (IOException e) {
            System.out.println("**** IOException " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void verifyHandshake(String response) {

        try {


            String subUrl = md5String(response + String.valueOf(quersumme(response)) + secret);
            URL myURL = new URL(DOMAIN_ONLY + verifyHandshake + subUrl);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");


            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                httpConn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }


            int statusCode = httpConn.getResponseCode();

            if (statusCode == 200) {

                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);


                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }


                importFromWeb();
            } else {
                // TODO ERROR HANDLING
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void importFromWeb() {


        DifferentiateCardHandler differentiateCardHandler = new DifferentiateCardHandler();
        String apiMethod = differentiateCardHandler.getApiMethod(activity);
        try {
            URL myURL = new URL(DOMAIN_ONLY + apiMethod + releasepackageID);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");

            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                httpConn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }

            int statusCode = httpConn.getResponseCode();
            // System.out.println("<<<< importFromWeb statuscode "+statusCode);
            if (statusCode == 200) {
                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }


                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String entityStr = response.toString();


                SpeechcareSQLITEHelper databasebHelper = null;
                try {
                    databasebHelper = new SpeechcareSQLITEHelper(activity, null, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                SQLiteDatabase db = databasebHelper.getWritableDatabase();
                db.beginTransaction();


                JSONObject jsonObj = new JSONObject(entityStr);
                ContentValues packaecv = new ContentValues();
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(jsonObj.getString("id")));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_NAME, jsonObj.getString("name"));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_VERSION, jsonObj.getString("version"));
                if (jsonObj.getString("predecessor_id").equals("null")) {

                    packaecv.put(SpeechcareSQLITEHelper.COLUMN_PREDECESSOR_ID, "");
                }
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_APP_ID, Integer.valueOf(jsonObj.getString("app_id")));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_HASH, jsonObj.getString("hash"));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_MIN_APP_VERSION, jsonObj.getString("min_app_version"));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_PUBLISHDATE, jsonObj.getString("publishdate"));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_CREATED, jsonObj.getString("created"));
                packaecv.put(SpeechcareSQLITEHelper.COLUMN_PUBLISHED, Integer.valueOf(jsonObj.getString("published")));

                db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_RELEASEPACKAGE, null, packaecv, SQLiteDatabase.CONFLICT_REPLACE);

                // APP DATA
                JSONObject appObj = jsonObj.getJSONObject("app");
                ContentValues appcv = new ContentValues();
                appcv.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(appObj.getString("id")));
                appcv.put(SpeechcareSQLITEHelper.COLUMN_NAME, appObj.getString("name"));
                appcv.put(SpeechcareSQLITEHelper.COLUMN_APPID, appObj.getString("appid"));
                db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_APP, null, appcv, SQLiteDatabase.CONFLICT_REPLACE);


                JSONArray exerciseJArray = jsonObj.getJSONArray("exercises");

                exCount = exerciseJArray.length();
                System.out.println("<<<< START importAllExerciseData");

                ContentValues values_tbl_exercise = new ContentValues();
                ContentValues values_tbl_exercisetype = new ContentValues();
                ContentValues values_tbl_answer = new ContentValues();
                ContentValues values_tbl_media = new ContentValues();
                ContentValues values_tbl_exercisemodel = new ContentValues();
                for (int i = 0; exerciseJArray.length() > i; i++) {
                    boolean showLog = false;
                    publishProgress(i);

                    // Table Exercise
                    JSONObject exObj = exerciseJArray.getJSONObject(i);
                    values_tbl_exercise.clear();
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(exObj.getString("id")));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_TITLE, exObj.getString("title"));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_QUESTION, exObj.getString("question"));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_QUESTION_MEDIA_ID, Integer.valueOf(exObj.getString("question_media_id")));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_HELPVIDEO, exObj.getString("helpVideo"));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_HELPVIDIO_MEDIA_ID, Integer.valueOf(exObj.getString("helpVideo_media_id")));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_EXERCISETYPE_ID, Integer.valueOf(exObj.getString("exercisetype_id")));
                    values_tbl_exercise.put(SpeechcareSQLITEHelper.COLUMN_DIFFICULTY, exObj.getString("difficulty"));
                    db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_EXERCISE, null, values_tbl_exercise, SQLiteDatabase.CONFLICT_REPLACE);

                    // Table Exercisetype
                    JSONObject extype = exObj.getJSONObject("exercisetype");
                    values_tbl_exercisetype.clear();
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_ID, extype.getString("id"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_NUMBER, extype.getString("number"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_TITLE, extype.getString("title"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_TITLE, extype.getString("exercise_title"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_DESCRIPTION, extype.getString("description"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_MODEL, extype.getString("exercise_model"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_DEACTIVATED, extype.getString("deactivated"));
                    values_tbl_exercisetype.put(SpeechcareSQLITEHelper.COLUMN_GRAMMARLAYER, extype.getString("grammarlayer"));
                    db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, null, values_tbl_exercisetype, SQLiteDatabase.CONFLICT_REPLACE);


                    // Table Answer with Answermedia
                    JSONObject exDetail = exObj.getJSONObject(extype.getString("exercise_model"));
                    if (exDetail.has("answers")) {
                        JSONArray answers = exDetail.getJSONArray("answers");

                        for (int c = 0; answers.length() > c; c++) {
                            JSONObject a = answers.getJSONObject(c);
                            values_tbl_answer.clear();

                            if (activity.getResources().getString(R.string.app_id).equals("3") && !a.isNull("help_media_id")) {

                                values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_HELP_MEDIA_ID, a.getInt("help_media_id"));
                            }

                            values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_ID, a.getInt("id"));
                            values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, a.getInt("exercise_id"));
                            values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_VALUE, a.getString("value"));
                            values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_SORT, a.getInt("sort"));
                            values_tbl_answer.put(SpeechcareSQLITEHelper.COLUMN_MEDIA_ID, a.getInt("media_id"));
                            db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_ANSWER, null, values_tbl_answer, SQLiteDatabase.CONFLICT_REPLACE);


                            values_tbl_media.clear();
                            if (a.has("media")) {
                                values_tbl_media = mediaValuesFromJson(a.getJSONObject("media"));
                                if (values_tbl_media.size() > 0) {
                                    db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values_tbl_media, SQLiteDatabase.CONFLICT_IGNORE);
                                }
                            }
                            values_tbl_media.clear();
                            if (a.has("help_media")) {
                                values_tbl_media = mediaValuesFromJson(a.getJSONObject("help_media"));
                                if (values_tbl_media.size() > 0) {
                                    db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values_tbl_media, SQLiteDatabase.CONFLICT_IGNORE);
                                }

                            }


                        }


                        exDetail.remove("answers");
                    }

                    // Table For exercisemodel
                    values_tbl_exercisemodel.clear();

                    Iterator<?> keys = exDetail.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        try {
                            values_tbl_exercisemodel.put(key, exDetail.getString(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (values_tbl_exercisemodel.size() > 0) {
                        db.insertWithOnConflict(extype.getString("exercise_model"), null, values_tbl_exercisemodel, SQLiteDatabase.CONFLICT_REPLACE);
                    }


                    // Table Media
                    values_tbl_media.clear();
                    if (exObj.has("question_media")) {
                        values_tbl_media = mediaValuesFromJson(exObj.getJSONObject("question_media"));
                        if (values_tbl_media.size() > 0) {
                            db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values_tbl_media, SQLiteDatabase.CONFLICT_IGNORE);
                        }

                    }

                    values_tbl_media.clear();
                    if (exObj.has("helpVideo_media")) {
                        values_tbl_media = mediaValuesFromJson(exObj.getJSONObject("helpVideo_media"));
                        if (values_tbl_media.size() > 0) {
                            db.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_MEDIA, null, values_tbl_media, SQLiteDatabase.CONFLICT_IGNORE);
                        }
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();
                if (databasebHelper != null) databasebHelper.close();

                System.out.println("<<<< DONE importAllExerciseData");
           /*     MediaDataSource mds = new MediaDataSource(activity);
                mds.open();
                mediadownload=true;
                List<Media> mediaList;
                mediaList=mds.getMediaData();
                mds.close();
                mediaCount=mediaList.size();
                for (int i=0; i<mediaList.size(); i++) {

                    Media m =mediaList.get(i);
                    if(m.getFilname().length()==0 || m.getFilname().equals("null")){
                       m.setFilname(m.getOriginalfile());
                    }
                    publishProgress(i);
                    downloadMedia(m);

                }*/

            } else {
                // TODO ERROR HANDLING
            }


        } catch (JSONException e) {
            // TODO ERROR HANDLING
            e.printStackTrace();
        } catch (ClientProtocolException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }


    }

    public ContentValues mediaValuesFromJson(JSONObject mediaObj) {

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
        return values;
    }


    private String md5String(String s) throws NoSuchAlgorithmException {
        String[] strArray = new String[]{s};
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(strArray[0].getBytes());
        byte[] result = md5.digest();

        /* Ausgabe */
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < result.length; i++) {
            if (result[i] <= 15 && result[i] >= 0) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & result[i]));
        }
        //System.out.println("MD5: " + hexString.toString());
        return hexString.toString();
    }


    private int quersumme(String hexString) {
        int qsumme = 0;

        int i = 0;
        while (i < hexString.length()) {
            String s = hexString.substring(i, i + 1);
            //Log.i("SUBSTRING", s);
            int zahl = Integer.parseInt(s, 16);
            //Log.i("ZAHL",String.valueOf(zahl));

            qsumme = qsumme + zahl;
            i++;
        }

        //Log.i("qsumme",String.valueOf(qsumme) );
        return qsumme;

    }


}
