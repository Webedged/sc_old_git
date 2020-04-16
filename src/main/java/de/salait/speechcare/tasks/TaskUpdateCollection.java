package de.salait.speechcare.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.salait.speechcare.R;
import de.salait.speechcare.activities.MainActivity;
import de.salait.speechcare.dao.AnswerDataSource;
import de.salait.speechcare.dao.CollectionDataSource;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.dao.MediaCollectionDataSource;
import de.salait.speechcare.dao.MediaDataSource;
import de.salait.speechcare.dao.UserSettingsDataSource;
import de.salait.speechcare.data.Media;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.utility.Network;

/**
 * Created by Christian.Ramthun on 25.10.13.
 */
public class TaskUpdateCollection extends AsyncTask<String, Integer, Boolean> {
    MediaDataSource mds;
    private Activity activity;
    private Dialog dialog;
    private int statuscode;
    private boolean showDialog = true;
    private String userId;
    private int exCount;
    private int mediaCount;
    private ProgressBar progressBar;
    private TextView tvLoading, tvPer;
    private boolean mediadownload;
    private Network network;


    public TaskUpdateCollection(Activity act, boolean showDialog) {
        this.activity = act;
        this.showDialog = showDialog;
        System.out.println(" <<<  TaskUpdateCollection");
    }

    public TaskUpdateCollection(Activity act) {
        this(act, true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showDialog) {

            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.progressdialog);
            progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar1);
            tvLoading = (TextView) dialog.findViewById(R.id.tv1);
            tvPer = (TextView) dialog.findViewById(R.id.tvper);
            dialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (showDialog) {
            if (values[0] == -1) {
                tvLoading.setText(activity.getResources().getString(R.string.label_init_import));
            } else if (!mediadownload) {
                tvLoading.setText(activity.getResources().getString(R.string.label_import_ex) + " " + values[0] + " " + activity.getResources().getString(R.string.von) + " " + exCount);
                tvPer.setText(values[0] * 100 / exCount + " %");
                progressBar.setProgress(values[0] * 100 / exCount);
            } else {
                tvLoading.setText(activity.getResources().getString(R.string.label_download_media) + " (" + values[0] + " / " + mediaCount + ")");
                tvPer.setText(values[0] * 100 / mediaCount + " %");
                progressBar.setProgress(values[0] * 100 / mediaCount);
            }
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Settings settingsPraxis = new Settings(activity);
        settingsPraxis.loadUserprefs();

        JSONObject updateColObj = new JSONObject();
        try {
            updateColObj.put("AppId", activity.getResources().getString(R.string.app_id));
            updateColObj.put("Device", Settings.loadUuid(activity));
            //updateColObj.put("Username", settingsPraxis.getUsername());
            updateColObj.put("Username", params[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String responseBody = "";
        network = new Network(activity);
        HttpURLConnection response = network.updateCollection(updateColObj);

        try {
            if (Network.STATUSCODE_200 != response.getResponseCode()) {
                // die updateCollection-Schnittstelle konnte keine korrekte Antwort liefern, also koennen wir nicht weiter verarbeiten

                statuscode = response.getResponseCode();
                return false;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.getInputStream()));
            String inputLine;
            StringBuffer body = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }
            in.close();
            responseBody = body.toString();

            JSONObject obj = new JSONObject(responseBody);
            userId = obj.getString("UserId");
            List<String> updateArray = new ArrayList<String>();
            JSONArray coll = obj.getJSONArray("Collection");

            if (coll != null) {

                checkForDeletedCollections(coll, userId);
                updateArray = saveCollection(coll, userId);

            }


            JSONObject sets = obj.getJSONObject("Initialsetting");
            if (sets != null) saveSettings(sets);

            mds = new MediaDataSource(activity);
            mds.open();
            for (int i = 0; i < updateArray.size(); i++) {

                saveExportCollection(network.exportCollection(updateArray.get(i)));
                if (statuscode != Network.STATUSCODE_200) return false;
            }


            // MEDIA ermitteln und herunterladen
            mediadownload = true;
            downloadMedia();
            mds.close();

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("<< json error: " + e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("<< io error: " + e.getLocalizedMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("<< ex error: " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (showDialog && dialog != null) {
            dialog.dismiss();
        }
        if (!result) {
            alertUser();
        }
        if (result) {
            Settings sets = new Settings(activity);
            try {
                sets.loadFromPrefs();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sets.setFirstStart(false);
            sets.saveToPrefs();
            MainActivity a = (MainActivity) activity;
            a.updateUI();


        }

        super.onPostExecute(result);
    }

    private void saveSettings(JSONObject settingsObj) throws JSONException, NoSuchFieldException {

        if (showDialog == false) return;

        Settings settings = new Settings(activity);

        int timeLimit = 3;
        int exLimit = 10;
        String difficulty = "medium";
        String email = "";
        String repeatModus = "norepeat";
        int sound = 1;
        int video = 1;
        String extypes = "";
        String modus = "time";

        settings.setUserid(settingsObj.getString("user_id"));

        if (settingsObj.isNull("excerciselimit") == false)
            exLimit = settingsObj.getInt("excerciselimit");
        settings.setExerciseLimit(exLimit);

        if (settingsObj.isNull("timelimit") == false) timeLimit = settingsObj.getInt("timelimit");
        settings.setTimelimit(timeLimit);

        if (settingsObj.isNull("difficult") == false)
            difficulty = settingsObj.getString("difficult");
        settings.setDifficultyLevel(difficulty);

        if (settingsObj.isNull("emailstatistik") == false)
            email = settingsObj.getString("emailstatistik");
        settings.setEmailadressForResults(email);

        if (settingsObj.isNull("repeatexercisemode") == false)
            repeatModus = settingsObj.getString("repeatexercisemode");
        settings.setRepeatexercisemode(repeatModus);

        if (settingsObj.isNull("setsound") == false) sound = settingsObj.getInt("setsound");
        if (settingsObj.isNull("allowhelpvideos") == false)
            video = settingsObj.getInt("allowhelpvideos");
        settings.setSoundActivated((sound == 1));
        settings.setVideohelpActivated((video == 1));

        if (settingsObj.isNull("excercisetypes") == false) {
            settings.setRandomTypeActivated(false);

            extypes = settingsObj.getString("excercisetypes");
            String[] types = extypes.split(",");

            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                settings.setExerciseTypeEnabled(type, true);
            }
        } else {
            settings.setRandomTypeActivated(true);
        }

        if (!settingsObj.isNull("modus")) {

            modus = settingsObj.getString("modus");
            if (modus.equalsIgnoreCase("excercise")) {
                settings.setIsExerciseLimit(true);
                settings.setTimeLimit(false);
            } else {
                settings.setTimeLimit(true);
                settings.setIsExerciseLimit(false);
            }
        } else {
            settings.setTimeLimit(true);
            settings.setIsExerciseLimit(false);
        }

        int changeSettings = settingsObj.getInt("settingchangeallowed");
        settings.setSettingchangeallowed((changeSettings == 1));
        settings.saveToPrefs();

        Settings sets = new Settings(activity);
        sets.loadUserprefs();

        UserSettingsDataSource ds = null;
        try {
            ds = new UserSettingsDataSource(activity);
            ds.open();
            ds.saveSettingsToDB(settingsObj.getString("user_id"), sets.getUsername(), sets.getPassword(), changeSettings, timeLimit, exLimit, difficulty, email, extypes, modus, repeatModus, sound, video);
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void checkForDeletedCollections(JSONArray collectionArr, String user_id) throws JSONException, IOException {

        CollectionDataSource collectionDataSource = new CollectionDataSource(activity);
        collectionDataSource.open();
        List<String> userColArray = collectionDataSource.getCollectionsForUser(user_id);
        collectionDataSource.close();

        List<String> validColIdArray = new ArrayList<String>();
        for (int i = 0; i < collectionArr.length(); i++) {
            JSONObject c = collectionArr.getJSONObject(i);
            validColIdArray.add(c.getString("CollectionId"));
        }

        for (String colId : userColArray) {
            if (validColIdArray.contains(colId) == false) {
                collectionDataSource.open();
                collectionDataSource.deleteCollectionForUser(colId, user_id);
                collectionDataSource.close();

            }
        }

    }


    private List<String> saveCollection(JSONArray collectionArr, String user_id) throws JSONException, IOException {

        CollectionDataSource collectionDataSource = new CollectionDataSource(activity);
        collectionDataSource.open();
        List<String> newCollections = new ArrayList<String>();
        for (int i = 0; i < collectionArr.length(); i++) {

            JSONObject collectionsinfoObj = (JSONObject) collectionArr.get(i);
            if (collectionDataSource.insertCollectionData(collectionsinfoObj.getString("CollectionId"), collectionsinfoObj.getString("Date"), collectionsinfoObj.getString("Enddate"), user_id)) {

                newCollections.add(collectionsinfoObj.getString("CollectionId"));
            }
        }
        collectionDataSource.close();

        return newCollections;
    }

    private void saveExportCollection(HttpURLConnection response) {

        try {
            System.out.println("<<<< TaskUPDAteCollection saveExportCollection StatusCode " + response.getResponseCode());

            statuscode = response.getResponseCode();
            if (statuscode != Network.STATUSCODE_200) {
                // die saveExportCollection-Schnittstelle konnte keine korrekte Antwort liefern, also koennen wir nicht weiter verarbeiten
                return;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.getInputStream()));
            String inputLine;
            StringBuffer responseString = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseString.append(inputLine);
            }
            in.close();
            String entityStr = responseString.toString();

            JSONObject collectionObj = new JSONObject(entityStr);

            // EXERCISE DATA
            ExerciseDataSource eds = new ExerciseDataSource(activity);
            JSONArray exerciseJArray = new JSONArray();
            if (collectionObj.has("exercises")) {
                exerciseJArray = collectionObj.getJSONArray("exercises");
            }

            System.out.println("<<<<< exerciseJArray length " + exerciseJArray.length());
            eds.open();


            //  MEDIACOLLECTION DATA ->   BILDERKARTE
            JSONArray mediacollectionsArray = new JSONArray();
            if (collectionObj.has("mediacollection")) {
                mediacollectionsArray = collectionObj.getJSONArray("mediacollection");
            }


            System.out.println("<<<<< mediacollectionsArray length " + mediacollectionsArray.length());

            exCount = exerciseJArray.length() + mediacollectionsArray.length();

            CollectionDataSource collectionDataSource = new CollectionDataSource(activity);
            collectionDataSource.open();


            for (int i = 0; exerciseJArray.length() > i; i++) {

                publishProgress(i);
                JSONObject exObj = exerciseJArray.getJSONObject(i);
                eds.insertExerciseData(exObj.getString("id"), exObj.getString("title"), exObj.getString("question"),
                        exObj.getString("question_media_id"), exObj.getString("helpVideo"), exObj.getString("helpVideo_media_id"),
                        exObj.getString("exercisetype_id"), exObj.getString("difficulty"));

                JSONObject exType = exObj.getJSONObject("exercisetype");
                eds.insertExerciseTYPEData(exType.getString("id"), exType.getString("number"), exType.getString("title"),
                        exType.getString("exercise_title"), exType.getString("description"), exType.getString("exercise_model"),
                        exType.getString("deactivated"), exType.getString("grammarlayer"));


                JSONObject exDetail = exObj.getJSONObject(exType.getString("exercise_model"));
                if (exDetail.has("answers")) {
                    JSONArray answers = exDetail.getJSONArray("answers");
                    AnswerDataSource awds = new AnswerDataSource(activity);
                    awds.open();

                    for (int c = 0; answers.length() > c; c++) {
                        JSONObject a = answers.getJSONObject(c);
                        awds.insertAnswer(a.getInt("id"), a.getInt("exercise_id"), a.getString("value"), a.getInt("sort"), a.getInt("media_id"));
                        if (a.has("media")) {
                            mds.insertQuestionMediaForPraxis(a.getJSONObject("media"));
                        }
                    }
                    awds.close();
                    exDetail.remove("answers");
                }

                eds.insertExerciseModelTable(exDetail, exType.getString("exercise_model"));
                if (exObj.has("question_media")) {
                    mds.insertQuestionMediaForPraxis(exObj.getJSONObject("question_media"));
                }
                if (exObj.has("helpVideo_media")) {
                    mds.insertQuestionMediaForPraxis(exObj.getJSONObject("helpVideo_media"));
                }

                collectionDataSource.insertCollectionExercise(collectionObj.getString("id"), exObj.getString("id"));
            }
            eds.close();
            MediaCollectionDataSource mediaCollectionDS = new MediaCollectionDataSource(activity);
            mediaCollectionDS.open();

            for (int i = 0; mediacollectionsArray.length() > i; i++) {

                publishProgress(i);

                JSONObject exObj = mediacollectionsArray.getJSONObject(i);
                collectionDataSource.insertCollectionMedia(collectionObj.getString("id"), exObj.getString("id"), exObj.getString("art"));

                mediaCollectionDS.insertMediacollection(exObj.getString("id"), exObj.getString("title"), exObj.getString("description"), exObj.getString("image_media_id"), exObj.getString("price"), exObj.getString("status"), exObj.getString("art"));

                JSONArray media = exObj.getJSONArray("media");
                for (int c = 0; media.length() > c; c++) {
                    JSONObject mediaDict = media.getJSONObject(c);
                    mediaCollectionDS.insertMediaCollectionMedia(mediaDict.getString("id"), exObj.getString("id"));
                    mds.insertMediaData(mediaDict.getInt("id"), mediaDict.getString("title"), mediaDict.getString("type"), mediaDict.getString("source"), mediaDict.getString("license"), mediaDict.getString("invalidationdate"), mediaDict.getString("originalfile"), mediaDict.getString("filename"), mediaDict.getInt("deactivated"), mediaDict.getString("created"), mediaDict.getString("status"));

                }

            }
            mediaCollectionDS.close();


            collectionDataSource.close();


        } catch (IOException e) {
            System.out.println("<<<<< IOException " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("<<<<< JSONException " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void downloadMedia() {
        List<Media> mediaList = mds.getMediaDataForPraxis();

        mediaCount = mediaList.size();

        for (int i = 0; i < mediaList.size(); i++) {

            Media m = mediaList.get(i);
            if (m.getFilname().length() == 0 || m.getFilname().equals("null")) {
                m.setFilname(m.getOriginalfile());
            }
            publishProgress(i);
            boolean dSuccess = network.downloadMedia(m);
            if (dSuccess) {          // erfolgreichen Download und Speicherung vermerken
                mds.saveSuccess(m);
            }
        }
    }

    private void alertUser() {
        String msg = "unbekannter Fehler";
        if (statuscode == Network.STATUSCODE_404) {
            msg = "Es konnten keine Collections gefunden werden";
        }
        if (statuscode == Network.STATUSCODE_400) {
            msg = activity.getString(R.string.dialog_bad_request);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("").setMessage(msg).setNeutralButton("OK", null).show();
    }


}
