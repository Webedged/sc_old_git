package de.salait.speechcare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.UserSettingsDataSource;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.tasks.TaskUserlogin;
import de.salait.speechcare.utility.Network;


public class LoginActivity extends Activity {

    private final Activity activity = this;
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setDeviceUUID();

        // TextView tv_ver = (TextView) findViewById(R.id.tv_version);
        // tv_ver.setText(getString(R.string.app_ver));

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvForgotPassword = (TextView) findViewById(R.id.tv_forgotPassword);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(getString(R.string.url_forgot_password));
            }
        });

        tvRegister = (TextView) findViewById(R.id.tv_register);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(getString(R.string.url_register));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }


    protected void onResume() {
        super.onResume();

        etUsername.setText("");
        etPassword.setText("");
    }

    private void login() {

        if (Network.isNetworkOnline(this)) {
            TaskUserlogin taskUserlogin = new TaskUserlogin(activity, etUsername.getText().toString(), etPassword.getText().toString(), false);
            taskUserlogin.execute();
        } else {
            boolean isIndb = false;
            UserSettingsDataSource ds = null;
            try {
                ds = new UserSettingsDataSource(activity);
                ds.open();
                isIndb = ds.checkIfUserisInDB(etUsername.getText().toString(), etPassword.getText().toString());
                ds.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isIndb == true) {
                ds.open();
                try {
                    ds.loadUserData(etUsername.getText().toString(), etPassword.getText().toString());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                ds.close();

                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
            } else {
                alertNetworkConnection();
            }
        }
    }

    private void alertNetworkConnection() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("").setMessage("Es ist keine Netzwerkverbindung vorhanden.").setNeutralButton("OK", null).show();
    }

    private void setDeviceUUID() {
        Settings settings = new Settings(activity);
        settings.saveUUID();
    }

    protected void openBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

}
