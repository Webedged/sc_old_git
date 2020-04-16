package de.salait.speechcare.tasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.UnsentAnswerDataSource;
import de.salait.speechcare.data.Answer;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.utility.Network;

/**
 * Created by sandra.stuck on 01.11.13.
 */
public class TaskSendAnswer extends AsyncTask<String, Integer, Boolean> {

    public static final String url = Network.DOMAIN + "service/setAnswer/";
    public static final String authUsername = "speechcare";
    public static final String authPassword = "scportal";
    public static String appID;
    private final Activity activity;
    private Answer givenAnswer;
    private Boolean sendSuccess;
    private JSONObject jsonbody;
    private UnsentAnswerDataSource dataSource;

    public TaskSendAnswer(Activity c, Answer answer) {
        activity = c;
        givenAnswer = answer;
        System.out.println(" <<<  TaskSendAnswer");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        appID = activity.getResources().getString(R.string.app_id);//PraxisAphasie ="101"
        try {
            dataSource = new UnsentAnswerDataSource(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (sendSuccess == false) {

            dataSource.open();
            dataSource.insertUnsentAnswer(jsonbody.toString());
            dataSource.close();
        }


        super.onPostExecute(result);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected Boolean doInBackground(String... strings) {

        try {

            URL myURL = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
            httpConn.setDoOutput(true);
            jsonbody = getJsonObject();
            System.out.println("<< sending: " + jsonbody.toString());

            String se = jsonbody.toString();
            OutputStream os = httpConn.getOutputStream();
            byte[] input = se.getBytes("utf-8");
            os.write(input, 0, input.length);


            int statusCode = httpConn.getResponseCode();

            if (statusCode == 200) {


                sendSuccess = true;
                dataSource.open();
                int unsendcount = dataSource.countUnsentAnsers();
                dataSource.close();

                if (unsendcount > 0) {
                    sentUnsendData();
                }
            } else {
                sendSuccess = false;
            }

        } catch (UnsupportedEncodingException e) {
            sendSuccess = false;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            sendSuccess = false;
            e.printStackTrace();
        } catch (IOException e) {
            sendSuccess = false;
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject getJsonObject() {

        JSONObject postJson = new JSONObject();
        JSONArray answerjsArray = new JSONArray();

        JSONObject answerjs = new JSONObject();

        try {
            answerjs.put("AnswerId", givenAnswer.getAnswerID());
            answerjs.put("Answertext", givenAnswer.getAnswerTxt());
            answerjs.put("Answertime", givenAnswer.getAnswerTime());
            answerjs.put("CollectionId", givenAnswer.getCollectionID());
            if (givenAnswer.getSolved() != null)
                answerjs.put("Correct", givenAnswer.getIsCorrect());
            answerjs.put("Duration", givenAnswer.getDuration());
            answerjs.put("exerciseID", givenAnswer.getExerciseID());
            answerjs.put("givenAnswer", givenAnswer.getGivenAnswer());
            answerjs.put("solved", givenAnswer.getSolved());
            answerjsArray.put(answerjs);

            postJson.put("Answer", answerjsArray);
            Settings settings = new Settings(activity);
            settings.loadUserprefs();
            postJson.put("Username", settings.getUsername());
            postJson.put("Device", Settings.loadUuid(activity));
            postJson.put("AppId", appID);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return postJson;
    }

    private int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    private void sentUnsendData() {

        dataSource.open();
        List<String> notSendQArray = dataSource.getAllNotSendData();
        dataSource.close();

        for (int i = 0; i < notSendQArray.size(); i++) {

            try {
                JSONObject jObject = new JSONObject(notSendQArray.get(i));
//				Log.i("notSendQArray.get(i)", notSendQArray.get(i).toString());

                URL myURL = new URL(url);
                HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
                String credentials = authUsername + ":" + authPassword;
                String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
                httpConn.setRequestProperty("Authorization", basicAuth);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
                httpConn.setDoOutput(true);

                Network network = new Network(activity);
                network.addCookieToPostRequest(httpConn);
                httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
                httpConn.setDoOutput(true);


                String se = jObject.toString();
                OutputStream os = httpConn.getOutputStream();
                byte[] input = se.getBytes("utf-8");
                os.write(input, 0, input.length);


                int statusCode = httpConn.getResponseCode();


                if (statusCode == 200) {
                    dataSource.open();
                    dataSource.deleteUnsentAnswer(notSendQArray.get(i));
                    dataSource.close();
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
}
