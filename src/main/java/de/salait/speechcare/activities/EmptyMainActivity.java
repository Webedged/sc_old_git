package de.salait.speechcare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.salait.speechcare.data.Settings;

/**
 * Created by speechcare on 01.03.18.
 */

public class EmptyMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent activityIntent;
        Settings sets = new Settings(this);

        if (sets.isLogin()) {
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();

    }

}
