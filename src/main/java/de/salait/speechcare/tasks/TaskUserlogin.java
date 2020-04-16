package de.salait.speechcare.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Window;

import org.json.JSONException;
import org.json.JSONObject;

import de.salait.speechcare.R;
import de.salait.speechcare.activities.MainActivity;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.utility.Network;

/**
 * Created by Christian.Ramthun on 23.10.13.
 */
public class TaskUserlogin extends AsyncTask<String, Integer, Boolean> {
    private final Activity activity;
    boolean isBackgroundLoginIN;
    private ProgressDialog dialog;
    private String username;
    private String pw;
    private int statuscode;

    public TaskUserlogin(Activity a, String username, String pw, boolean isBgLogin) {
        this.activity = a;
        this.username = username;
        this.pw = pw;
        this.isBackgroundLoginIN = isBgLogin;
        System.out.println(" <<<  TaskUserlogin");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isBackgroundLoginIN) {
            dialog = new ProgressDialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setMessage("Überprüfe Anmeldedaten");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {

        JSONObject jsonLoginObj = new JSONObject();
        try {
            jsonLoginObj.put("Password", pw);
            jsonLoginObj.put("Username", username);

            JSONObject jsonDeviceObj = new JSONObject();
            jsonDeviceObj.put("Devicename", Settings.loadUuid(activity));
            jsonDeviceObj.put("OS", Build.DEVICE + " " + Build.VERSION.SDK_INT);
            jsonDeviceObj.put("Description", "");

            jsonLoginObj.put("Device", jsonDeviceObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Network network = new Network(activity);
        statuscode = network.loginUser(jsonLoginObj);
        System.out.println(" <<<  TaskUserlogin statuscode " + statuscode);
        System.out.println(" <<<  TaskUserlogin json " + jsonLoginObj);
        if (statuscode != Network.STATUSCODE_200) return false;
        saveUser();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (!result) {
            alertUser();
        }
        if (result) {    // 200, user konnte eingeloggt werden
            if (!isBackgroundLoginIN) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("login", true);
                activity.startActivity(intent);
            }
        }
        super.onPostExecute(result);
    }


    private void saveUser() {
        Settings settings = new Settings(activity);
        if (isBackgroundLoginIN) {
            settings.setUserLoginStatus(true);
        }
        settings.setUsername(username);
        settings.setPassword(pw);
        settings.saveUserToPrefs();

    }

    private void alertUser() {
        String msg = "login";
        if (statuscode == Network.STATUSCODE_404) {
            msg = activity.getResources().getString(R.string.dialog_check_usercredentials);
        }
        if (statuscode == Network.STATUSCODE_400) {
            msg = activity.getString(R.string.dialog_bad_request);
            //msg = activity.getResources().getString(R.string.dialog_bad_request);
        }
        if (statuscode == 401) {
            msg = "401";
            //msg = activity.getResources().getString(R.string.dialog_bad_request);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("").setMessage(msg).setNeutralButton("OK", null).show();
    }


}
