package de.salait.speechcare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.data.ImageCardExercise;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.TrainingSingleton;
import de.salait.speechcare.utility.DifferentiateCardHandler;

public class EvaluationActivity extends Activity {
    protected final Activity activity = this;
    protected ExerciseDataSource datasource;
    protected TextView tv_zeitdauer;
    protected TextView tv_solved;
    protected TextView tv_failed;
    protected TextView tv_inWiedervorlage;
    protected TextView tv_time_elapsed;
    protected TextView tv_solved_exercises;
    protected TextView tv_unsolved_exercises;
    protected TextView tv_exercises_to_repeat;
    protected TextView tv_send_result;
    protected EditText et_email;
    protected Button btn_home;
    protected Button btn_repeat;
    protected Button btn_sendEmail;
    protected Button btn_backToMain;

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(Editable email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout backG = (LinearLayout) findViewById(R.id.evalsBack);
        //Drawable d = new BitmapDrawable(getResources(), Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.hintergrund_blank, displaySize.x, displaySize.y));
        // backG.setBackground(d);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);

        try {
            datasource = new ExerciseDataSource(this);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_could_not_open_or_create_database), Toast.LENGTH_LONG).show();
        }
        tv_zeitdauer = (TextView) findViewById(R.id.tv_zeitdauer);
        tv_solved = (TextView) findViewById(R.id.tv_solved);
        tv_failed = (TextView) findViewById(R.id.tv_failed);
        tv_inWiedervorlage = (TextView) findViewById(R.id.tv_inWiedervorlage);
        tv_time_elapsed = (TextView) findViewById(R.id.tv_time_elapsed);
        tv_solved_exercises = (TextView) findViewById(R.id.tv_solved_exercises);
        tv_unsolved_exercises = (TextView) findViewById(R.id.tv_unsolved_exercises);
        tv_exercises_to_repeat = (TextView) findViewById(R.id.tv_exercises_to_repeat);
        tv_send_result = (TextView) findViewById(R.id.tv_send_result);
        et_email = (EditText) findViewById(R.id.et_sendEmailTo);
        btn_home = (Button) findViewById(R.id.btn_home);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainMenu();
            }
        });
        btn_repeat = (Button) findViewById(R.id.btn_repeat);
        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRepeat();
            }
        });
        btn_sendEmail = (Button) findViewById(R.id.btn_sendEmail);
        btn_sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
        btn_backToMain = (Button) findViewById(R.id.btn_backToMain);
        btn_backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainMenu();
            }
        });


//Schriftart
        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");
        tv_zeitdauer.setTypeface(font);
        tv_solved.setTypeface(font);
        tv_failed.setTypeface(font);
        tv_inWiedervorlage.setTypeface(font);
        tv_time_elapsed.setTypeface(font);
        tv_solved_exercises.setTypeface(font);
        tv_unsolved_exercises.setTypeface(font);
        tv_exercises_to_repeat.setTypeface(font);
        tv_send_result.setTypeface(font);
        btn_repeat.setTypeface(font);
        btn_sendEmail.setTypeface(font);
        btn_backToMain.setTypeface(font);

        tv_zeitdauer.setPaintFlags(tv_zeitdauer.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_solved.setPaintFlags(tv_solved.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_failed.setPaintFlags(tv_failed.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_inWiedervorlage.setPaintFlags(tv_inWiedervorlage.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_time_elapsed.setPaintFlags(tv_time_elapsed.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_solved_exercises.setPaintFlags(tv_solved_exercises.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_unsolved_exercises.setPaintFlags(tv_unsolved_exercises.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_exercises_to_repeat.setPaintFlags(tv_exercises_to_repeat.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_send_result.setPaintFlags(tv_send_result.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btn_repeat.setPaintFlags(btn_repeat.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btn_sendEmail.setPaintFlags(btn_sendEmail.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btn_backToMain.setPaintFlags(btn_backToMain.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        tv_zeitdauer.setText(TrainingSingleton.getInstance().getElapsedTime());

        if (getResources().getBoolean(R.bool.isCardApp)) {
            tv_solved.setText(String.valueOf(TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT)));
            tv_failed.setText(String.valueOf(TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_WRONG)));
        } else {
            tv_solved.setText(String.valueOf(TrainingSingleton.getInstance().getCorrectAnswers().size()));
            tv_failed.setText(String.valueOf(TrainingSingleton.getInstance().getWrongAnswers().size()));
        }


        Settings settings = new Settings(this);
        try {
            settings.loadFromPrefs();
            et_email.setText(settings.getEmailadressForResults());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Toast.makeText(this, "Email konnte nicht aus den Settings geladen werden. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Email konnte nicht aus den Settings geladen werden.", Toast.LENGTH_SHORT).show();
        }

        /** hiding the keyboard**/
        //LinearLayout ll_auswertung = (LinearLayout) findViewById(R.id.ll_auswertung);
        //ll_auswertung.requestFocus();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setRepeatLabel();
    }

    protected void setRepeatLabel() {

        DifferentiateCardHandler differentiateCardHandler = new DifferentiateCardHandler();
        Settings sets = new Settings(this);
        tv_inWiedervorlage.setText(differentiateCardHandler.getWiedervorlageLblCountTxt(this, sets));

    }

    protected void startRepeat() {
        int count = 0;
        Settings sets = new Settings(this);
        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {

            try {
                sets.loadFromPrefs();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sets.loadUserprefs();

        }

        datasource.open();
        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
            count = datasource.getAllRepeatExercises(sets.getUserid()).size();
        } else {
            count = datasource.getAllRepeatExerciseIds().size();
        }
        datasource.close();

        if (count > 0) {
            Intent intent = getTrainingIntent();
            intent.putExtra("repetition", true);
            startActivity(intent);
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setTitle("").setMessage("Derzeit keine Aufgaben in der Wiedervorlage.").setNeutralButton("OK", null).show();
        }
    }

    private void sendEmail() {


        if (isEmailValid(et_email.getText())) {
            Settings settings = new Settings(this);
            settings.saveEmail(String.valueOf(et_email.getText()));


            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{String.valueOf(et_email.getText())});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Ihr Trainingsergebnis " + getResources().getString(R.string.app_name));
            DifferentiateCardHandler differentiateCardHandler = new DifferentiateCardHandler();

            String emailBody = differentiateCardHandler.getEmailBodyTxt(this, getIntent().getBooleanExtra("repetition", false));

            emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
            emailIntent.setType("text/text");

          /*  emailIntent.setType("application/image");
            Uri uri=Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo_speechcare_blau_250);
            emailIntent.putExtra(Intent.EXTRA_STREAM,uri );*/

            startActivity(Intent.createChooser(emailIntent, "E-Mail senden..."));
            finish();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("").setMessage("Bitte geben Sie eine gültige E-Mail Adresse an!").setNeutralButton("OK", null).show();
        }


    }

    private void backToMainMenu() {
        Intent intent = getMainActivityIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TrainingSingleton.getInstance().abortTraining();
        startActivity(intent);
        this.finish();
    }

    protected Intent getMainActivityIntent() {
        return new Intent(this, MainActivity.class);
    }

    protected Intent getTrainingIntent() {

        return new Intent(this, TrainingActivity.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.evaluation, menu);
        return true;
    }

}
