package de.salait.speechcare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.dao.MediaDataSource;
import de.salait.speechcare.data.Media;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.tasks.TaskGetPackageData;
import de.salait.speechcare.tasks.TaskMediaDownload;
import de.salait.speechcare.tasks.TaskUpdateCollection;
import de.salait.speechcare.tasks.TaskUserlogin;
import de.salait.speechcare.utility.ButtonHighlighterOnTouchListener;
import de.salait.speechcare.utility.DifferentiateCardHandler;
import de.salait.speechcare.utility.DisplayNextView;
import de.salait.speechcare.utility.FlipAnimation;
import de.salait.speechcare.utility.Network;
import de.salait.speechcare.utility.Utils;


public class MainActivity extends Activity implements View.OnClickListener {

    protected ImageView imageViewFacebook;
    protected ImageView imageViewTwitter;
    protected ImageView imageViewSpeechCare;
    protected ImageView imageViewYoutube;

    protected RelativeLayout ll_training;
    protected RelativeLayout ll_info;
    protected RelativeLayout rl_blau_1;
    protected RelativeLayout rl_blau_2;
    protected RelativeLayout rl_blau_3;
    protected RelativeLayout rl_gruen_1;
    protected RelativeLayout rl_gruen_2;
    protected RelativeLayout rl_gruen_3;
    protected boolean isFirstBox = false;
    protected LinearLayout ll_bottom;
    protected Settings settings;

    protected TextView tv_impressum;
    protected TextView textViewlinks;
    protected TextView textViewlinksoben;
    protected TextView textViewlinksunten;
    protected TextView textViewMitte;
    protected TextView textViewMitteoben;
    protected TextView textviewmitteUnten;
    protected TextView textviewrechts;
    protected TextView textViewrechtsoben;
    protected TextView textViewrechtsunten;
    protected TextView textviewTraining;
    protected TextView textViewlinksoben2;
    protected TextView textViewlinks2;
    protected TextView tv_difficulty_level;
    protected TextView tv_dauer_umpfang;
    protected TextView tv_typenauswahl;
    protected TextView textViewlinksunten2;
    protected TextView textviewmitteoben2;
    protected TextView textViewmitteunten2;
    protected TextView textViewrechtsoben2;
    protected TextView textViewrechts2;
    protected TextView textViewrechtsunten2;
    protected TextView textViewinfo;
    protected TextView textViewStatistic;
    protected boolean hasLoggedIn;
    DifferentiateCardHandler differentiateCardHandler;
    private TaskMediaDownload taskMediaDownload;
    // PLUS Veriosn VARIABLES
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        differentiateCardHandler = new DifferentiateCardHandler();


        ExerciseDataSource eds = null;
        try {
            eds = new ExerciseDataSource(this);
        } catch (IOException e) {
            e.printStackTrace();

        }

        List<Media> mlist;
        if (eds.exerciseDataExits()) {
            MediaDataSource mds = null;
            try {
                mds = new MediaDataSource(this);
                mds.addColoumDownloadSuccessToMedia();
                mds.open();
                mlist = mds.getMediaData();
                mds.close();
                taskMediaDownload = new TaskMediaDownload(MainActivity.this, mlist);
                taskMediaDownload.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // TextView tv_ver = (TextView) findViewById(R.id.tv_version);
        // tv_ver.setText(getString(R.string.app_ver));

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout backG = (LinearLayout) findViewById(R.id.statisticsBack);
        //  Drawable d = new BitmapDrawable(getResources(), Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.hintergrund, displaySize.x, displaySize.y));
        //backG.setBackground(d);

        backG.setBackgroundResource(R.drawable.hintergrund);

        imageViewFacebook = (ImageView) findViewById(R.id.imageViewFacebook);
        imageViewTwitter = (ImageView) findViewById(R.id.imageViewTwitter);
        imageViewSpeechCare = (ImageView) findViewById(R.id.imageViewSpeechCare);
        imageViewYoutube = (ImageView) findViewById(R.id.imageViewYoutube);

        ll_training = (RelativeLayout) findViewById(R.id.ll_training);
        ll_info = (RelativeLayout) findViewById(R.id.ll_info);
        rl_blau_1 = (RelativeLayout) findViewById(R.id.rl_blau_1);
        rl_blau_2 = (RelativeLayout) findViewById(R.id.rl_blau_2);
        rl_blau_3 = (RelativeLayout) findViewById(R.id.rl_blau_3);
        rl_gruen_1 = (RelativeLayout) findViewById(R.id.rl_gruen_1);
        rl_gruen_2 = (RelativeLayout) findViewById(R.id.rl_gruen_2);
        rl_gruen_3 = (RelativeLayout) findViewById(R.id.rl_gruen_3);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);

        rl_blau_1.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_blau_1, this));
        rl_blau_2.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_blau_2, this));
        rl_blau_3.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_blau_3, this));
        rl_gruen_1.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_gruen_1, this));
        rl_gruen_2.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_gruen_2, this));
        rl_gruen_3.setOnTouchListener(new ButtonHighlighterOnTouchListener(rl_gruen_3, this));

        rl_blau_1.setVisibility(View.GONE);
        rl_blau_2.setVisibility(View.GONE);
        rl_blau_3.setVisibility(View.GONE);
        ll_training.setVisibility(View.GONE);
        rl_blau_1.setOnClickListener(this);
        rl_blau_2.setOnClickListener(this);
        rl_blau_3.setOnClickListener(this);
        rl_gruen_1.setOnClickListener(this);
        rl_gruen_2.setOnClickListener(this);
        rl_gruen_3.setOnClickListener(this);
        imageViewFacebook.setOnClickListener(this);
        imageViewSpeechCare.setOnClickListener(this);
        imageViewTwitter.setOnClickListener(this);
        imageViewYoutube.setOnClickListener(this);


        //Schriftart
        tv_impressum = (TextView) findViewById(R.id.tv_impressum);
        textViewlinks = (TextView) findViewById(R.id.textViewLinks);
        textViewlinksoben = (TextView) findViewById(R.id.textViewLinksOben);
        textViewlinksunten = (TextView) findViewById(R.id.textViewLinksUnten);
        textViewMitte = (TextView) findViewById(R.id.textViewMitte);
        textViewMitteoben = (TextView) findViewById(R.id.textViewMitteOben);
        textviewmitteUnten = (TextView) findViewById(R.id.textViewMitteUnten);
        textviewrechts = (TextView) findViewById(R.id.textViewRechts);
        textViewrechtsoben = (TextView) findViewById(R.id.textViewRechtsOben);
        textViewrechtsunten = (TextView) findViewById(R.id.textViewRechtsUnten);
        textviewTraining = (TextView) findViewById(R.id.textViewTraining);
        textViewlinksoben2 = (TextView) findViewById(R.id.textViewLinksOben2);
        textViewlinks2 = (TextView) findViewById(R.id.textViewLinks2);
        tv_difficulty_level = (TextView) findViewById(R.id.tv_difficulty_level);
        tv_dauer_umpfang = (TextView) findViewById(R.id.tv_dauer_umfang);
        tv_typenauswahl = (TextView) findViewById(R.id.tv_typenauswahl);
        textViewlinksunten2 = (TextView) findViewById(R.id.textViewLinksUnten2);
        textviewmitteoben2 = (TextView) findViewById(R.id.textViewMitteOben2);
        textViewmitteunten2 = (TextView) findViewById(R.id.textViewMitteUnten2);
        textViewrechtsoben2 = (TextView) findViewById(R.id.textViewRechtsOben2);
        textViewrechts2 = (TextView) findViewById(R.id.textViewRechts2);
        textViewrechtsunten2 = (TextView) findViewById(R.id.textViewRechtsUnten2);
        textViewinfo = (TextView) findViewById(R.id.textViewInfo);
        textViewStatistic = (TextView) findViewById(R.id.textViewStatistic);


        Typeface font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");
        tv_impressum.setTypeface(font);
        textViewlinks.setTypeface(font);
        textViewlinksoben.setTypeface(font);
        textViewlinksunten.setTypeface(font);
        textViewMitte.setTypeface(font);
        textViewMitteoben.setTypeface(font);
        textviewmitteUnten.setTypeface(font);
        textviewrechts.setTypeface(font);
        textViewrechtsoben.setTypeface(font);
        textViewrechtsunten.setTypeface(font);
        textviewTraining.setTypeface(font);
        textViewlinksoben2.setTypeface(font);
        textViewlinks2.setTypeface(font);
        tv_difficulty_level.setTypeface(font);
        tv_dauer_umpfang.setTypeface(font);
        tv_typenauswahl.setTypeface(font);
        textViewlinksunten2.setTypeface(font);
        textviewmitteoben2.setTypeface(font);
        textViewmitteunten2.setTypeface(font);
        textViewrechtsoben2.setTypeface(font);
        textViewrechts2.setTypeface(font);
        textViewrechtsunten2.setTypeface(font);
        textViewinfo.setTypeface(font);
        textViewStatistic.setTypeface(font);

        tv_impressum.setPaintFlags(tv_impressum.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinks.setPaintFlags(textViewlinks.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinksoben.setPaintFlags(textViewlinksoben.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinksunten.setPaintFlags(textViewlinksunten.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewMitte.setPaintFlags(textViewMitte.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewMitteoben.setPaintFlags(textViewMitteoben.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textviewmitteUnten.setPaintFlags(textviewmitteUnten.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textviewrechts.setPaintFlags(textviewrechts.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewrechtsoben.setPaintFlags(textViewrechtsoben.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewrechtsunten.setPaintFlags(textViewrechtsunten.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textviewTraining.setPaintFlags(textviewTraining.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinksoben2.setPaintFlags(textViewlinksoben2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinks2.setPaintFlags(textViewlinks2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_difficulty_level.setPaintFlags(tv_difficulty_level.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_dauer_umpfang.setPaintFlags(tv_dauer_umpfang.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tv_typenauswahl.setPaintFlags(tv_typenauswahl.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewlinksunten2.setPaintFlags(textViewlinksunten2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textviewmitteoben2.setPaintFlags(textviewmitteoben2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewmitteunten2.setPaintFlags(textViewmitteunten2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewrechtsoben2.setPaintFlags(textViewrechtsoben2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewrechts2.setPaintFlags(textViewrechts2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewrechtsunten2.setPaintFlags(textViewrechtsunten2.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewinfo.setPaintFlags(textViewinfo.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textViewStatistic.setPaintFlags(textViewStatistic.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        tv_impressum.setOnClickListener(this);

        ll_info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isFirstBox) {
                    ll_bottom.setVisibility(View.INVISIBLE);
                    applyRotation(0, -90);
                    isFirstBox = !isFirstBox;
                } else {
                    ll_bottom.setVisibility(View.VISIBLE);
                    applyRotation(0, 90);
                    isFirstBox = !isFirstBox;
                }
            }
        });

        if (getResources().getBoolean(R.bool.isCardApp) == true) {
            Settings setting = new Settings(this);
            if (!setting.isLogin()) {
                setting.saveUUID();
                TaskUserlogin taskUserlogin = new TaskUserlogin(this, "BilderKarte", "blekrtIDR", true);
                taskUserlogin.execute();
            }
        }

        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
            settings = loadSettings();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                hasLoggedIn = extras.getBoolean("login");
            }
            ImageButton lo = (ImageButton) findViewById(R.id.logoutBtn);
            lo.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.btlogout, 42, 42));

            final Activity act = this;
            lo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /** Logout
                     *  TODO: Daten bereinigen
                     */
                    settings = loadSettings();
                    settings.setUserLoginStatus(false);
                    Intent loginIntent = new Intent(act, LoginActivity.class);
                    act.startActivity(loginIntent);
                    act.finish();

                }
            });

            try {
                settings.loadFromPrefs();
                settings.loadUserprefs();
                user = settings.getUsername();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateUI();
            // TODO Check THIS -> IS A Perfomance PROBLEM
            if (Network.isNetworkOnline(this)) {
                TaskUpdateCollection taskUpdateCollection = new TaskUpdateCollection(this, hasLoggedIn);
                if (user != null) {
                    taskUpdateCollection.execute(user);
                    hasLoggedIn = false;
                }
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (differentiateCardHandler == null) {
            differentiateCardHandler = new DifferentiateCardHandler();
        }

        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
            try {
                settings = loadSettings();
                settings.loadFromPrefs();
                settings.loadUserprefs();
                user = settings.getUsername();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        fillRightBox();
        fillLeftBox();
    }

    protected void fillLeftBox() {
        settings = loadSettings();

        if (settings.isFirstStart()) {
            rl_gruen_1.setOnClickListener(null);
            rl_gruen_1.setEnabled(false);
            rl_gruen_1.setAlpha(0.5f);
            rl_gruen_3.setOnClickListener(null);
            rl_gruen_3.setEnabled(false);
            rl_gruen_3.setAlpha(0.5f);
            return;
        } else {
            rl_gruen_1.setOnClickListener(this);
            rl_gruen_1.setEnabled(true);
            rl_gruen_1.setAlpha(1f);
            rl_gruen_3.setOnClickListener(this);
            rl_gruen_3.setEnabled(true);
            rl_gruen_3.setAlpha(1f);
        }

        TextView tv_difficulty_level = (TextView) findViewById(R.id.tv_difficulty_level);

        tv_difficulty_level.setText(differentiateCardHandler.getDifficultyLevelLblTxt(this, settings));


        // Ausgabe der gewählten zeit in der MainActivity
        TextView tv_dauer_umfang = (TextView) findViewById(R.id.tv_dauer_umfang);

        if (settings.isTimeLimit()) {
            tv_dauer_umfang.setText(getResources().getString(R.string.label_time) + " " + settings.getTimelimit() + " " + getString(R.string.label_minutes));
            if (settings.getTimelimit() < 2) {
                tv_dauer_umfang.setText(getResources().getString(R.string.label_time) + " " + settings.getTimelimit() + " " + getString(R.string.label_minute));
            }
        } else {
            tv_dauer_umfang.setText(getResources().getString(R.string.label_umfang) + " " + settings.getExerciseLimit() + " " + getString(R.string.label_aufgaben));
            if (settings.getExerciseLimit() < 2) {
                tv_dauer_umfang.setText(getResources().getString(R.string.label_umfang) + " " + settings.getExerciseLimit() + " " + getString(R.string.label_aufgabe));
            }
        }


        if (settings.isRandomTypeActivated()) {
            tv_typenauswahl.setText(getResources().getString(R.string.label_versionselection) + " " + getString(R.string.label_zufalltypenauswahl));
        } else {
            tv_typenauswahl.setText(getResources().getString(R.string.label_versionselection) + " " + differentiateCardHandler.getSelectedTypsLblTxt(this, settings));
        }

        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
            TextView user_tv = (TextView) findViewById(R.id.textView_Username);
            user_tv.setText(user);

        }
    }


    protected void fillRightBox() {
        TextView tv_wiedervorlage = (TextView) findViewById(R.id.textViewRechts2);
        tv_wiedervorlage.setText(String.format(getString(R.string.label_x_exercises_in_repeatition), countRepetition()));
    }

    /**
     * Anzahl der Wiedervorlagen laden
     *
     * @return Anzahl der Wiedervorlagen laden
     */
    protected int countRepetition() {
        settings = loadSettings();
        return differentiateCardHandler.getRepeatExercisesCount(this, settings);

    }


    /**
     * laedt die Einstellungen aus der Prefs.xml
     *
     * @return gibt ein vollstaendig geladenes Settingsobjekt zurueck oder null im Fehlerfall
     */
    protected Settings loadSettings() {

        settings = new Settings(this);
        try {
            settings.loadFromPrefs();
            return settings;
        } catch (NoSuchFieldException e) {
            System.out.println("<<< " + getString(R.string.error_could_not_load_settings) + e.getMessage());
            Toast.makeText(this, getString(R.string.error_could_not_load_settings) + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            System.out.println("<<< " + getString(R.string.error_could_not_load_database) + e.getMessage());
            Toast.makeText(this, getString(R.string.error_could_not_load_database) + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            System.out.println("<<< Exception" + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void applyRotation(float start, float end) {
        final float centerX = ll_info.getWidth() / 2.0f;
        final float centerY = ll_info.getHeight() / 2.0f;

        final FlipAnimation rotation = new FlipAnimation(start, end, centerX, centerY);

        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstBox, ll_training, ll_info, rl_blau_1, rl_blau_2, rl_blau_3, rl_gruen_1, rl_gruen_2, rl_gruen_3));
        if (isFirstBox) {
            ll_training.startAnimation(rotation);
            rl_blau_1.startAnimation(rotation);
            rl_blau_2.startAnimation(rotation);
            rl_blau_3.startAnimation(rotation);

        } else {
            ll_info.startAnimation(rotation);
            rl_gruen_1.startAnimation(rotation);
            rl_gruen_2.startAnimation(rotation);
            rl_gruen_3.startAnimation(rotation);
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rl_blau_1:
                showPopupUeberspeechcare();
                break;
            case R.id.rl_blau_2:
                showPopupMoreapps();
                break;
            case R.id.rl_blau_3:
                showPopupTrainingstipps();
                break;
            case R.id.tv_impressum:
                showPopupImpressum();
                break;
            case R.id.rl_gruen_1:
                startSettingsActivity();
                break;
            case R.id.rl_gruen_2:
                startTraining(false);
                break;
            case R.id.rl_gruen_3:
                startTraining(true);
                break;
            case R.id.imageViewFacebook:
                openBrowser(getString(R.string.url_facebook));
                break;
            case R.id.imageViewTwitter:
                openBrowser(getString(R.string.url_twitter));
                break;
            case R.id.imageViewSpeechCare:
                openBrowser(getString(R.string.url_speechcare));
                break;
            case R.id.imageViewYoutube:
                openBrowser(getString(R.string.url_youtube));
                break;
            case R.id.imageViewStatistic:
                startStatisticActivity();
                break;
            case R.id.textViewStatistic:
                startStatisticActivity();
            break;
            default:
                break;
        }
    }

    /**
     * startet ein Trainingslauf entweder neu oder als Wiederholung
     *
     * @param wiederholung gibt an, ob ein Wiederholung gestartet werden soll oder nicht
     */

    protected void startTraining(boolean wiederholung) {
        settings = loadSettings();
        if (settings.isFirstStart() && getResources().getBoolean(R.bool.isCardApp) == true) {

            TaskUpdateCollection taskUpdateCollection = new TaskUpdateCollection(this, true);
            taskUpdateCollection.execute("BilderKarte");

        } else if (settings.isFirstStart()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.alertTitleNoExerciseData));
            builder.setMessage(getResources().getString(R.string.alertMsgNoExerciseData));

            builder.setPositiveButton(getResources().getString(R.string.alertBtnActionNoExerciseData), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startTaskGetPackageData();

                }
            });
            builder.setNegativeButton(getResources().getString(R.string.alertBtnBreakNoExerciseData), null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (getResources().getBoolean(R.bool.isCardApp) == true) {


            if (wiederholung) {
                if (countRepetition() > 0) {
                    Intent intent = new Intent(this, CardsActivity.class);
                    intent.putExtra("repetition", true);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("").setMessage("Derzeit keine Aufgaben in der Wiedervorlage.").setNeutralButton("OK", null).show();
                }
            } else {
                Intent intent = new Intent(this, CardsActivity.class);
                startActivity(intent);
            }

        } else {
            if (wiederholung) {

                ExerciseDataSource ds = null;
                int count = 0;
                try {
                    ds = new ExerciseDataSource(this);
                    ds.open();
                    if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
                        settings.loadUserprefs();
                        count = ds.getAllRepeatExercises(settings.getUserid()).size();
                    } else {
                        count = ds.getAllRepeatExerciseIds().size();
                    }
                    ds.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (count > 0) {
                    Intent intent = getTrainingIntent();
                    intent.putExtra("repetition", true);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("").setMessage("Derzeit keine Aufgaben in der Wiedervorlage.").setNeutralButton("OK", null).show();
                }
            } else {
                if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
                    System.out.println("<<< isPlusVersion");
                    ExerciseDataSource ds = null;
                    try {
                        ds = new ExerciseDataSource(this);
                        ds.open();
                        settings.loadUserprefs();
                        boolean hasLizense = ds.userHasLicense(settings.getUserid());
                        ds.close();

                        if (hasLizense == true) {
                            System.out.println("<<< hasLizense");
                            Intent intent = getTrainingIntent();
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                            alert.setTitle("").setMessage("Sie haben derzeit keine gültige Lizenz.").setNeutralButton("OK", null).show();
                        }
                    } catch (IOException e) {
                        System.out.println("<<< IOException " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                } else {
                    ExerciseDataSource eds = null;
                    try {
                        eds = new ExerciseDataSource(this);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    if (eds.exerciseDataExits()) {

                        MediaDataSource mds = null;
                        try {
                            mds = new MediaDataSource(this);
                            if (mds.unloadMediaExits()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage(getResources().getString(R.string.unloadMediaInfo));

                                builder.setNeutralButton("okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Intent intent = getTrainingIntent();
                                        startActivity(intent);
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();


                            } else {
                                Intent intent = getTrainingIntent();
                                startActivity(intent);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Intent intent = getTrainingIntent();
                            startActivity(intent);
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getResources().getString(R.string.alertTitleNoExerciseData));
                        builder.setMessage(getResources().getString(R.string.alertMsgNoExerciseData));

                        builder.setPositiveButton(getResources().getString(R.string.alertBtnActionNoExerciseData), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startTaskGetPackageData();
                            }
                        });
                        builder.setNegativeButton(getResources().getString(R.string.alertBtnBreakNoExerciseData), null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                ;

            }
        }
    }

    protected Intent getTrainingIntent() {
        taskMediaDownload.cancel(true);
        return new Intent(this, TrainingActivity.class);
    }


    protected void showPopupUeberspeechcare() {
        Intent intent = new Intent(this, Ueberspeechcare.class);
        startActivity(intent);
    }

    protected void showPopupMoreapps() {
        Intent intent2 = new Intent(this, Moreapps.class);
        startActivity(intent2);
    }


    protected void showPopupTrainingstipps() {
        Intent intent3 = new Intent(this, Trainingstipps.class);
        startActivity(intent3);
    }

    protected void showPopupImpressum() {
        Intent intent4 = new Intent(this, Impressum.class);
        startActivity(intent4);
    }

    protected void startStatisticActivity() {
        Intent intent6 = new Intent(this, StatisticsActivity.class);
        startActivity(intent6);
    }

    protected void startSettingsActivity() {
        Intent intent5 = new Intent(this, SettingsActivity.class);
        intent5.putExtra("allowedToChange", true);
        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {

            if (settings.isSettingchangeallowed() == false) {
                intent5.putExtra("allowedToChange", false);
            }

        }

        startActivity(intent5);
    }

    protected void openBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(i);
    }


    public void updateUI() {

        try {
            settings.loadFromPrefs();
            settings.loadUserprefs();
            user = settings.getUsername();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fillLeftBox();
        fillRightBox();
    }


    public void startTaskGetPackageData() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)) {

            TaskGetPackageData taskGetPackageData = new TaskGetPackageData(MainActivity.this);
            taskGetPackageData.execute();
        } else {
            // ALERT
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.alertTitleNoInternet)).setMessage(getResources().getString(R.string.alertMsgNoInternet)).setNeutralButton("OK", null).show();

        }

    }

}
