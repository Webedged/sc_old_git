package de.salait.speechcare.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.AnswerDataSource;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.dao.StatisticDataSource;
import de.salait.speechcare.data.Answer;
import de.salait.speechcare.data.Exercise;
import de.salait.speechcare.data.Media;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.TrainingSingleton;
import de.salait.speechcare.tasks.TaskMediaDownload;
import de.salait.speechcare.tasks.TaskSendAnswer;
import de.salait.speechcare.utility.ButtonHighlighterOnTouchListener;
import de.salait.speechcare.utility.Utils;


public class TrainingActivity extends Activity {

    protected final static String GRAMMARLAYER_SENTENCE = "sentence";
    protected final static long SECOND = 60;
    protected final static long MILLISECOND = 1000;
    protected final static int MARGINDIF = 50;
    protected final Activity activity = this;
    protected File directory;
    protected boolean isSoundActivated;
    protected boolean isVideoActivated;
    protected ProgressDialog dialog;
    protected ExerciseDataSource datasource;
    protected List<Exercise> exerciseList;
    protected TextView tv_timer;
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv_timer.setText((String) msg.obj);
        }
    };
    protected AtomicBoolean isRunning = new AtomicBoolean(false);
    protected long mStartTime = 0L;
    protected long mStopTime;
    protected TextView tvTitle;
    protected RelativeLayout rl_exerciseField;
    protected RelativeLayout rl_media;
    protected RelativeLayout rl_videoField;
    protected RelativeLayout rl_trainingField;
    protected ImageView iv_media;
    protected ImageView iv_play;
    protected VideoView vv_helpVideo;
    protected Typeface tfLTElight;
    protected ImageButton btn_nextExercise;
    protected ImageButton btn_nextExercise2;
    protected ImageButton btn_repeat;
    protected ImageButton btn_previousExercise;
    protected Button btnResize;
    protected Button btnImageResize;
    protected LinearLayout progressView;
    protected boolean isVideoScaledUp;
    protected boolean isImagecaledUp;
    protected boolean isTimelimit;
    protected Point displaySize;
    protected float density;
    private StatisticDataSource statisticDataSource;
    /**
     * gibt an, ob Wiedervorlagenlauf oder neues Training
     */
    protected boolean isRepetition;

    /**
     * Liste aller Antwortfelder
     */
    protected List<TextView> answerfieldsList;
    /**
     * vom Benutzer gegebene Antwort
     */
    protected String givenAnswer = "";
    protected List<String> givenAnswerList;
    protected Boolean isCorrect;
    protected String answerID;
    protected int imageleftMargin;
    protected int helpMediaYPosition;


    // PLUS VERSION VALUES
    protected MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer m) {
            try {
                if (m.isPlaying()) {
                    m.stop();
                    m.release();
                    m = new MediaPlayer();
                }
                m.setVolume(100f, 100f);
                m.setLooping(false);
                m.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Answer currentAnswer;
    private String userid;
    private long startTime;
    private String exerciseType;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(displaySize);
        } else {
            display.getSize(displaySize);
        }

        try {
            statisticDataSource = new StatisticDataSource(this);
        } catch (IOException e) {
            System.out.println(e);
        }

        if (getResources().getBoolean(R.bool.isPlusVersion) == true) {
            Settings settingsPraxis = new Settings(activity);
            String collID = "";
            try {
                settingsPraxis.loadFromPrefs();
                userid = settingsPraxis.getUserid();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        density = getResources().getDisplayMetrics().density;
        //System.out.println(">>> density "+ String.valueOf(density));


        tfLTElight = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        LinearLayout backG = findViewById(R.id.trainBack);
       /* Drawable d = new BitmapDrawable(getResources(), Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.hintergrund_blank, displaySize.x, displaySize.y));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            backG.setBackground(d);
        } else {
            backG.setBackgroundDrawable(d);
        }*/
        backG.setBackgroundResource(R.drawable.hintergrund_blank);


        ContextWrapper c = new ContextWrapper(this);
        directory = c.getFilesDir();
        isRepetition = getIntent().getBooleanExtra("repetition", false);

        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTypeface(tfLTElight);
        tvTitle.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
        Button btnHome = findViewById(R.id.btn_home);

        btnResize = findViewById(R.id.videoresize_button);
        btnImageResize = findViewById(R.id.imageresize_button);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                onHomePressed();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(R.string.dialog_abort_training).setPositiveButton("Ja", dialogClickListener)
                        .setNegativeButton("Nein", dialogClickListener).show();
            }
        });
        btnResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVideoResizePressed();
            }
        });
        btnImageResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImageResizePressed();
            }
        });
        tv_timer = findViewById(R.id.tv_timer);
        rl_exerciseField = findViewById(R.id.rl_exercise_field);
        rl_videoField = findViewById(R.id.rl_helpVideo);
        rl_trainingField = findViewById(R.id.ll_training);
        rl_media = findViewById(R.id.rl_media);
        iv_play = findViewById(R.id.playbtn_imageView);
        iv_media = findViewById(R.id.iv_media);
        progressView = findViewById(R.id.trainingProgressView);

        vv_helpVideo = findViewById(R.id.vv_helpVideo);
        vv_helpVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!vv_helpVideo.isPlaying()) {
                    iv_play.setVisibility(View.INVISIBLE);
                    vv_helpVideo.setBackgroundColor(Color.TRANSPARENT);
                    vv_helpVideo.start();
                }
                return false;
            }
        });
        /*vv_helpVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                iv_play.setVisibility(View.VISIBLE);
                if (TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exercisewordforvideo") || TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")){
                    loadHelpVideo(TrainingSingleton.getInstance().getCurrentExercise().getQuestionMedia(), false);
                }
            }
        });*/

        btn_nextExercise = findViewById(R.id.btn_next_exercise);
        btn_nextExercise.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_nextExercise));
        btn_nextExercise2 = findViewById(R.id.btn_next_exercise2);
        btn_nextExercise2.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_nextExercise2));
        btn_repeat = findViewById(R.id.btn_repeat);
        btn_repeat.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_repeat));

        btn_nextExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadNextExercise();
            }
        });


        btn_nextExercise2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadNextExercise();
            }
        });

        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadExercise(TrainingSingleton.getInstance().getCurrentExercise());
            }
        });
        btn_previousExercise = findViewById(R.id.btn_previous_exercise);
        btn_previousExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadPreviousExercise();
            }
        });
        btn_previousExercise.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_previousExercise));
        try {
            datasource = new ExerciseDataSource(this);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_could_not_open_or_create_database), Toast.LENGTH_LONG).show();
        }


        configLayout();

        /** Trainingslauf erstellen **/
        openDatasource();
        try {
            createTraining();
        } catch (Exception e) {
            Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (!getResources().getString(R.string.app_name).toLowerCase().contains("Aphasie".toLowerCase())) {
            btnImageResize.setVisibility(View.GONE);
        }

    }

    /**
     * oeffnet die Datenbank
     */
    protected void openDatasource() {
        try {
            datasource.open();
        } catch (SQLiteException sqlEx) {
            Toast.makeText(this, getString(R.string.error_could_not_open_database) + sqlEx.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void configLayout() {

        //progress height = 30
        //header height = 90

        //  createTrainingProgressBar();

        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) rl_trainingField.getLayoutParams();
        p.height = displaySize.y - 90 - 30;
        p.width = displaySize.x;
        rl_trainingField.setLayoutParams(p);

        RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) rl_videoField.getLayoutParams();
        p1.height = (int) (p.height * 0.34f);
        p1.width = (int) (displaySize.x * 0.30f);
        rl_videoField.setLayoutParams(p1);

        RelativeLayout.LayoutParams v = (RelativeLayout.LayoutParams) vv_helpVideo.getLayoutParams();
        v.height = p1.height - 35;
        v.width = (int) (v.height * ((float) 4 / 3));
        vv_helpVideo.setLayoutParams(v);

        RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) rl_media.getLayoutParams();
        p2.height = (int) (v.height * ((float) 4 / 3)) + 25;
        p2.width = (int) (v.height * ((float) 4 / 3)) + 25;
        rl_media.setLayoutParams(p2);
        imageleftMargin = p2.leftMargin;


        RelativeLayout.LayoutParams e = (RelativeLayout.LayoutParams) rl_exerciseField.getLayoutParams();
        e.width = displaySize.x - p2.width - (2 * imageleftMargin);
        e.height = displaySize.y - 90 - 50;
        rl_exerciseField.setLayoutParams(e);


        RelativeLayout.LayoutParams pl = (RelativeLayout.LayoutParams) iv_play.getLayoutParams();
        pl.rightMargin = v.width / 2 - pl.width / 2;
        iv_play.setLayoutParams(pl);


        HorizontalScrollView progressScrollView = findViewById(R.id.progressScrollView);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) progressScrollView.getLayoutParams();
        lp.leftMargin = p2.leftMargin;
        progressScrollView.setLayoutParams(lp);


        helpMediaYPosition = v.height + v.bottomMargin;

        if (loadSettings().getKindOfRepeat() == 1) {
            // Keine Wiederholung
            btn_nextExercise2.setVisibility(View.GONE);
            btn_repeat.setVisibility(View.GONE);
        } else {

            btn_nextExercise.setVisibility(View.GONE);
        }


        // System.out.println("<< displaySize.y " +String.valueOf(displaySize.y )+ " displaySize.x " +String.valueOf(displaySize.x));
        // System.out.println("<< rl_videoField.h " +String.valueOf(p1.height )+ " rl_videoField.w " +String.valueOf(p1.width));
        // System.out.println("<< vv_helpVideo.h " +String.valueOf(v.height )+ " vv_helpVideo.w " +String.valueOf(v.width));


    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
    }

    @Override
    protected void onStop() {
        isRunning.set(false);
        super.onStop();
        datasource.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onHomePressed();
    }

    protected void onHomePressed() {
        Intent intent = getMainActivityIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }


    /************************************************/
    /** Allgemeine Methoden fuer den Trainingslauf **/
    /************************************************/

    /**
     * verkleinert oder vergroessert das Bild
     */
    protected void onImageResizePressed() {
        //
        if (isImagecaledUp) {
            RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) rl_media.getLayoutParams();
            p1.height = (int) (displaySize.x * 0.30f);
            p1.width = (int) (displaySize.x * 0.30f);
            rl_media.setLayoutParams(p1);

            RelativeLayout.LayoutParams v = (RelativeLayout.LayoutParams) iv_media.getLayoutParams();
            v.height = (int) (v.height * ((float) 4 / 3));
            v.width = (int) (v.height * ((float) 4 / 3));
            iv_media.setLayoutParams(v);

            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(R.drawable.ico_fullsize, this.getTheme());
            } else {
                drawable = getResources().getDrawable(R.drawable.ico_fullsize);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                btnImageResize.setBackground(drawable);
            } else {

                btnImageResize.setBackgroundDrawable(drawable);
            }

            rl_exerciseField.setVisibility(View.VISIBLE);
            rl_videoField.setVisibility(View.VISIBLE);

        } else {
            RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) rl_media.getLayoutParams();
            p1.height = (rl_trainingField.getLayoutParams().height - 20);
            p1.width = (rl_trainingField.getLayoutParams().width - 100);
            rl_media.setLayoutParams(p1);

            RelativeLayout.LayoutParams v = (RelativeLayout.LayoutParams) iv_media.getLayoutParams();
            v.height = p1.height - 35;
            v.width = (int) (v.height * ((float) 4 / 3));
            iv_media.setLayoutParams(v);

            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(R.drawable.ico_resize, this.getTheme());
            } else {
                drawable = getResources().getDrawable(R.drawable.ico_resize);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                btnImageResize.setBackground(drawable);
            } else {

                btnImageResize.setBackgroundDrawable(drawable);
            }
            rl_exerciseField.setVisibility(View.INVISIBLE);
            rl_videoField.setVisibility(View.INVISIBLE);
        }

        isImagecaledUp = !isImagecaledUp;
    }

    /**
     * verkleinert oder vergroessert das Video
     */
    protected void onVideoResizePressed() {

        if (isVideoScaledUp) {
            RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) rl_videoField.getLayoutParams();
            p1.height = (int) (rl_trainingField.getLayoutParams().height * 0.34f);
            p1.width = (int) (displaySize.x * 0.30f);
            rl_videoField.setLayoutParams(p1);

            RelativeLayout.LayoutParams v = (RelativeLayout.LayoutParams) vv_helpVideo.getLayoutParams();
            v.height = p1.height - 35;
            v.width = (int) (v.height * ((float) 4 / 3));
            vv_helpVideo.setLayoutParams(v);

            RelativeLayout.LayoutParams pl = (RelativeLayout.LayoutParams) iv_play.getLayoutParams();
            pl.rightMargin = v.width / 2 - pl.width / 2;
            iv_play.setLayoutParams(pl);

            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(R.drawable.ico_fullsize, this.getTheme());
            } else {
                drawable = getResources().getDrawable(R.drawable.ico_fullsize);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnResize.setBackground(drawable);

            } else {
                btnResize.setBackgroundDrawable(drawable);

            }

            rl_exerciseField.setVisibility(View.VISIBLE);
        } else {
            RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) rl_videoField.getLayoutParams();
            p1.height = (rl_trainingField.getLayoutParams().height - 20);
            p1.width = (rl_trainingField.getLayoutParams().width - 100);
            rl_videoField.setLayoutParams(p1);

            RelativeLayout.LayoutParams v = (RelativeLayout.LayoutParams) vv_helpVideo.getLayoutParams();
            v.height = p1.height - 35;
            v.width = (int) (v.height * ((float) 4 / 3));
            vv_helpVideo.setLayoutParams(v);

            RelativeLayout.LayoutParams pl = (RelativeLayout.LayoutParams) iv_play.getLayoutParams();
            pl.rightMargin = v.width / 2 - pl.width / 2;
            iv_play.setLayoutParams(pl);

            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(R.drawable.ico_resize, this.getTheme());
            } else {
                drawable = getResources().getDrawable(R.drawable.ico_resize);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnResize.setBackground(drawable);

            } else {
                btnResize.setBackgroundDrawable(drawable);

            }
            rl_exerciseField.setVisibility(View.INVISIBLE);
        }

        isVideoScaledUp = !isVideoScaledUp;
    }

    /**
     * startet Timer, entweder fuer das zeitbasierte Training oder um im Hintergrund die Zeit zu messen
     */
    protected void startTimer() {
        Settings settings = loadSettings();
        isTimelimit = settings.isTimeLimit();

        if (mStartTime == 0L) { //  && isTimelimit
            mStartTime = SystemClock.uptimeMillis();
            //mStopTime = SystemClock.uptimeMillis() + (1 * SECOND * MILLISECOND);
            mStopTime = mStartTime + (settings.getTimelimit() * SECOND * MILLISECOND);
        }
        TimerTask timerTask = new TimerTask();
        timerTask.start();
        isRunning.set(true);
    }

    protected void playSoundPageFlip() {
        if (!isSoundActivated) return;
        try {
            AssetFileDescriptor afd = getAssets().openFd("page-flip-2.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_could_not_load_sound) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * aktualisiert die Fortschrittsanzeige
     *
     * @param ex
     */
    protected void updateTrainingProgressBar(Exercise ex) {

        HorizontalScrollView progressScrollView = findViewById(R.id.progressScrollView);

        int index = TrainingSingleton.getInstance().exerciseList.indexOf(ex);

        if (progressView.getChildCount() == 0) return;

        LinearLayout ll = (LinearLayout) progressView.getChildAt(index);

        ImageView aI = (ImageView) ll.getChildAt(0);

        if (ex.answerstatus == Exercise.WRONG_ANSWER_GIVEN) {            // falsche Antwort
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.ico_wrong, (int) (19 * density), (int) (19 * density));
            aI.setImageBitmap(bm);
            if (bm != null) {
                // bm.recycle();
                bm = null;
            }
            ll.setBackgroundColor(Color.DKGRAY);
        } else if (ex.answerstatus == Exercise.CORRECT_ANSWER_GIVEN) {     // korrekte Antwort
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.ico_right, (int) (19 * density), (int) (19 * density));
            aI.setImageBitmap(bm);
            if (bm != null) {
                //  bm.recycle();
                bm = null;
            }
            ll.setBackgroundColor(Color.DKGRAY);
        } else if (ex.answerstatus == Exercise.NO_ANSWER_GIVEN) {          // noch keine Antwort
            aI.setImageBitmap(null);
            if (index == TrainingSingleton.getInstance().getExerciseIndex()) {
                ll.setBackgroundColor(Color.BLUE);
            } else {
                ll.setBackgroundColor(Color.DKGRAY);
            }
        }

        if (index != TrainingSingleton.getInstance().exerciseList.size() - 1) {
            LinearLayout ll2 = (LinearLayout) progressView.getChildAt(index + 1);
            ll2.setBackgroundColor(Color.DKGRAY);
        }

        if (index > 3) {
            progressScrollView.scrollTo((index - 3) * 20, 0);
        }
    }

    @SuppressLint("ResourceAsColor")
    protected void createTrainingProgressBar() {

        HorizontalScrollView progressScrollView = findViewById(R.id.progressScrollView);

        if (progressView.getChildCount() > 0)
            progressView.removeAllViews();

        for (int i = 0; i < TrainingSingleton.getInstance().exerciseList.size(); i++) {

            LinearLayout ll = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            params.setMargins(3, 1, 1, 1);

            ll.setLayoutParams(params);
            ll.getLayoutParams().height = (int) (19 * density);
            ll.getLayoutParams().width = (int) (19 * density);

            ImageView aI = new ImageView(this);

            aI.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams imagepara = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            imagepara.setMargins(1, 1, 1, 1);
            aI.setLayoutParams(imagepara);
            aI.setBackgroundColor(Color.LTGRAY);
            ll.addView(aI);

            if (i == 0) {
                ll.setBackgroundColor(Color.BLUE);
            } else {
                ll.setBackgroundColor(Color.DKGRAY);
            }


            progressView.addView(ll);

            progressScrollView.setHorizontalScrollBarEnabled(false);
            progressScrollView.setVerticalScrollBarEnabled(false);
        }
    }

    /**
     * beendet den Trainingslauf bzw. startet/leitet auf die Auswertung
     */
    protected void finishTraining() {
        // benoetigte Zeit ermitteln
        long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
        String elapsedTimeString;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        if (seconds < 10) {
            elapsedTimeString = "" + minutes + ":0" + seconds;
        } else {
            elapsedTimeString = "" + minutes + ":" + seconds;
        }

        TrainingSingleton.getInstance().finishTraining(elapsedTimeString);

        insertWrongAnswers();
        isRunning.set(false);
        startEvaluation();
    }

    protected void insertWrongAnswers() {
        openDatasource();
        if (getResources().getBoolean(R.bool.isPlusVersion)) {

            datasource.insertIntoRepeatExercise(TrainingSingleton.getInstance().getWrongAnswers(), userid);

        } else {
            datasource.insertIntoRepeatExercise(TrainingSingleton.getInstance().getWrongAnswers());
        }
        datasource.close();
    }

    /**
     * Weiterleitung auf Auswertungs-Ansicht
     */
    protected void startEvaluation() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        Intent intent = getEvaluationActivityIntent();
        intent.putExtra("repetition", isRepetition);
        startActivity(intent);
        this.finish();
    }

    protected Intent getEvaluationActivityIntent() {
        return new Intent(this, EvaluationActivity.class);
    }

    protected Intent getMainActivityIntent() {
        return new Intent(this, MainActivity.class);
    }

    /**
     * initialisert alle Werte fuer einen Trainingslauf anhand der Settings
     */
    protected void createTraining() {
        Settings settings = loadSettings();

        if (datasource == null) try {
            datasource = new ExerciseDataSource(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isRepetition) {
            if (getResources().getBoolean(R.bool.isPlusVersion)) {

                exerciseList = datasource.getAllRepeatExercises(userid);
            } else {
                exerciseList = datasource.getAllRepeatExercises();
            }

        } else {

            // Uebungen aus der Datenbank laden
            String exercisetypes = "AND exercisetype_id in (";
            for (String exerciseString : settings.allowedExerciseTypes) {
                exercisetypes += exerciseString + ",";
            }

            exercisetypes = exercisetypes.substring(0, exercisetypes.length() - 1) + ")";

            if (getResources().getBoolean(R.bool.isPlusVersion)) {

                exerciseList = datasource.getAllExercisesOfDifficultyForUser(settings.getDifficultyLevel(), exercisetypes, userid);
            } else {
                exerciseList = datasource.getAllExercisesOfDifficulty(settings.getDifficultyLevel(), exercisetypes);
            }

        }
        if (exerciseList.size() > 0) {
            // Trainingsverlauf initiieren
            TrainingSingleton.getInstance().createTraining(settings, exerciseList);

            loadExercise(TrainingSingleton.getInstance().getFirstExercise());

            createTrainingProgressBar();
        } else {
            finish();
        }

    }

    /**
     * springt zur naechsten uebung
     */
    protected void loadNextExercise() {

        if (getResources().getBoolean(R.bool.isPlusVersion)) {
            try {
                setAnswerData();
                sendAnswer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus == 2) {// keine Anwort gegeben, also falsch
            TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 0;
            onWrongAnswerClicked(null);
        }


        //Zeigt mir relevante werte zum überprüfen der gegebenen Antwort an + Modell (Variiert je nach Modell-Typ)
        /*System.out.println("MODELL: " + TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell());
        System.out.println("GIVENANSWER: " + givenAnswer);
        System.out.println("GIVENANSWERLIST: " + givenAnswerList);
        System.out.println("ANSWERID: " + answerID);*/

        //Fügt die Antwort in die DB ein
        statisticDataSource.insertStatistic(TrainingSingleton.getInstance().getCurrentExercise().getId(), System.currentTimeMillis(), TrainingSingleton.getInstance().getCurrentExercise().answerstatus, selectedAnswer());

        if (TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforword") || TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")) {
            RelativeLayout ll_training = findViewById(R.id.ll_training);
            View exv = findViewById(R.id.exercise_layout);
            ll_training.removeView(exv);
        }
        loadExercise(TrainingSingleton.getInstance().getNextExercise());
    }

    private String selectedAnswer() {
        String tmpAnswer = "";
        switch (TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell()) {
            case "exercisewordforimage":
            case "exerciseimageforword":
                tmpAnswer = answerID;
                break;
            case "exercisegapsentenceforimage":
            case "exercisesortcharacters":
            case "exercisesortwords":
            case "exercisegapwordforimage":
                tmpAnswer = givenAnswer;
                break;
        }
        return tmpAnswer;
    }

    /**
     * springt zur vorhergehenden uebung
     */
    protected void loadPreviousExercise() {

        if (TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforword") || TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")) {
            RelativeLayout ll_training = findViewById(R.id.ll_training);
            View exv = findViewById(R.id.exercise_layout);
            ll_training.removeView(exv);
        }

        TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 2;

        updateTrainingProgressBar(TrainingSingleton.getInstance().getCurrentExercise());

        Exercise prevExercise = TrainingSingleton.getInstance().getPreviousExercise();
        if (prevExercise == null) return;

        loadExercise(prevExercise);
    }

    /*******************************************/
    /** Methoden für bestimmte ExerciseModels **/
    /*******************************************/

    /**
     * WordForImage
     **/

    protected void loadExercise(Exercise exercise) {

        if (getResources().getBoolean(R.bool.isPlusVersion)) {

            if (exercise != null) {
                for (int i = 0; i < exercise.getWrongAnswers().size(); i++) {
                    Log.i("ExerciseWrongAnswer", exercise.getWrongAnswers().get(i));
                }
                String collID = "";
                try {

                    ExerciseDataSource exerciseDataSourcePraxis = new ExerciseDataSource(activity);
                    exerciseDataSourcePraxis.open();
                    collID = exerciseDataSourcePraxis.getCollectionIDForExercise(exercise.getId(), userid);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                currentAnswer = new Answer();
                currentAnswer.setExerciseID(exercise.getId());
                currentAnswer.setCollectionID(collID);
                currentAnswer.setIsCorrect(false);
                currentAnswer.setSolved(null);
                currentAnswer.setAnswerID(null);
                currentAnswer.setAnswerTime("");
                currentAnswer.setDuration("");
                currentAnswer.setGivenAnswer("");

                isCorrect = null;
                //answerID="";
                startTime = System.currentTimeMillis();
                exerciseType = exercise.getExerciseModell();
            }
        }


        if (vv_helpVideo.isPlaying()) {
            vv_helpVideo.stopPlayback();
        }

        if (isVideoScaledUp) {
            onVideoResizePressed();
        }
        if (isImagecaledUp) {
            onImageResizePressed();
        }
        rl_exerciseField.setVisibility(View.VISIBLE);


        clearImageviewsForRLayout(rl_exerciseField);

        if (exercise == null) {
            finishTraining();
            return;
        }

        TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 2;

        updateEvaluationbar();
        updateTrainingProgressBar(exercise);

        prepareExerciseField();
        setExerciseTitle(exercise.getTitle());
        openDatasource();

        System.out.println("<< ExerciseModell" + exercise.getExerciseModell());

        //TODO: If-Statemants wechseln mit switch-case

        if (exercise.getExerciseModell().equalsIgnoreCase("exercisesortcharacters")) {
            datasource.getExerciseSortCharacters(exercise);
            createExerciseSortCharacters(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exercisegapwordforimage")) {
            datasource.getExerciseGapWordForImage(exercise);
            createExerciseGapWordForImage(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exerciseimageforword")) {
            datasource.getExerciseImageForWord(exercise);
            createExerciseImageForWord(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exercisewordforimage")) {
            datasource.getExerciseWordForImage(exercise);
            createExerciseWordForImage(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exercisesortwords")) {
            datasource.getExerciseSortWords(exercise);
            createExerciseSortWords(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exercisegapsentenceforimage")) {
            datasource.getExerciseGapSentenceForImage(exercise);
            createExerciseGapSentenceForImage(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")) {
            datasource.getExerciseImageForVideo(exercise);
            createExerciseImageForVideo(exercise);
        } else if (exercise.getExerciseModell().equalsIgnoreCase("exercisewordforvideo")) { // fds!°!gfdgdf
            datasource.getExerciseWordForVideo(exercise);
            createExerciseWordForVideo(exercise);
        } else {
            // do nothing - what has happened?
        }

        // forVideo-Uebungen haben ihre VideoInformation in der QuestionMedia, nicht in HelpVideo
        if (exercise.getExerciseModell().equalsIgnoreCase("exercisewordforvideo") || exercise.getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")) {
            loadHelpVideo(exercise.getQuestionMedia(), true);
        } else {
            loadHelpVideo(exercise.getHelpVideo(), false);
        }
        System.out.println("** TMP exercise getQuestionMedia " + exercise.getQuestionMedia());
        System.out.println("** TMP exercise getQuestionMediaid " + exercise.getQuestionMediaID());
        System.out.println("** TMP exercise getHelpVideo " + exercise.getHelpVideo());
        System.out.println("** TMP exercise getHelpVideoMediaID " + exercise.getHelpVideoMediaID());

        List<Media> mediaList = new ArrayList<Media>();
        if (exercise.getHelpVideo() != null) {
            File tmp = new File(directory + "/" + exercise.getHelpVideo());
            if (!tmp.exists()) {
                Media m = new Media();
                m.setId(exercise.getHelpVideoMediaID());
                m.setFilname(exercise.getHelpVideo());
                mediaList.add(m);
            }
        }
        if (exercise.getQuestionMedia() != null) {
            File tmp = new File(directory + "/" + exercise.getQuestionMedia());
            if (!tmp.exists()) {
                Media m = new Media();
                m.setId(exercise.getQuestionMediaID());
                m.setFilname(exercise.getQuestionMedia());
                mediaList.add(m);
            }
        }

        if (mediaList.size() > 0) {
            System.out.println("** TMP taskMediaDownload.execute()");
            TaskMediaDownload taskMediaDownload = new TaskMediaDownload(this, mediaList);
            taskMediaDownload.execute();
        }


        rl_videoField.bringToFront();
        btn_previousExercise.bringToFront();
        btn_nextExercise.bringToFront();

        // Zurueck-Button ausblenden, wenn erste Uebung
        if (TrainingSingleton.getInstance().getExerciseIndex() == 0) {
            btn_previousExercise.setVisibility(View.INVISIBLE);
        } else {
            btn_previousExercise.setVisibility(View.VISIBLE);
        }
    }

    /**
     * erstellt das Trainingsfeld fuer die WordForImage-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseWordForImage(Exercise exercise) {


        boolean isSentence = false;

        if (exercise.getGrammarLayer().equalsIgnoreCase(GRAMMARLAYER_SENTENCE)) {
            isSentence = true;
        } else if (exercise.getWrongAnswers().size() > 0) {
            if (spacecount(exercise.getWrongAnswers().get(0)) > 0) {
                isSentence = true;
            }
        }


        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);

        // zufaellige Position fuer richtige Antwort ermitteln
        Random random = new Random();
        int correctAnswerpos = random.nextInt(exercise.getWrongAnswers().size());

        ArrayList<String> answerList = (ArrayList<String>) exercise.getWrongAnswers().clone();
        answerList.add(correctAnswerpos, exercise.getCorrectAnswer());

        if (isSentence) {
            System.out.println("<< isSentence");
            int tvMaxWidth = 0;

            for (String answer : answerList) {
                TextView tv = new TextView(this);

                tv.setMaxLines(1);
                tv.setMinLines(1);
                tv.setText(answer);
                //tv.setPadding(15,5,15,5);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tv.setTypeface(tfLTElight);
                tv.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
                tv.setTextColor(getResources().getColor(R.color.extext_enabled));

                tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                if (tv.getMeasuredWidth() > tvMaxWidth) tvMaxWidth = tv.getMeasuredWidth();
            }

            tvMaxWidth = tvMaxWidth + 20;
            int leftMargin = (rl_exerciseField.getLayoutParams().width - tvMaxWidth - 30 - 250) / 2;
            int topMargin = (rl_exerciseField.getLayoutParams().height - (answerList.size() * 50)) / (answerList.size() + 1);

            for (String answer : answerList) {
                LinearLayout answer_lo = new LinearLayout(this);
                answer_lo.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams alo_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                alo_p.leftMargin = leftMargin;
                alo_p.topMargin = (int) (topMargin / density);
                answer_lo.setLayoutParams(alo_p);

                final TextView tv = new TextView(this);
                LinearLayout.LayoutParams tv_p = new LinearLayout.LayoutParams(tvMaxWidth, (int) (50 * density));
                tv.setLayoutParams(tv_p);

                tv.setMaxLines(1);
                tv.setMinLines(1);
                tv.setText(answer);
                //tv.setPadding(15,5,15,5);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tv.setTypeface(tfLTElight);
                tv.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
                tv.setTextColor(getResources().getColor(R.color.extext_enabled));

                final ImageView iv = new ImageView(this);

                iv.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(150, (int) (50 * density));
                iv_p.leftMargin = 20;
                iv.setLayoutParams(iv_p);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                tv.setTag(answer);

                if (answerList.indexOf(answer) == correctAnswerpos) {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, 150, (int) (50 * density));
                    iv.setImageBitmap(bm);
                    if (bm != null) {
                        //  bm.recycle();
                        bm = null;
                    }
                    tv.setOnTouchListener(new ButtonHighlighterOnTouchListener(tv));
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                                return;
                            TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 1;
                            iv.setVisibility(View.VISIBLE);
                            onCorrectAnswerClicked(null);
                            isCorrect = true;
                            answerID = String.valueOf(tv.getTag());
                        }
                    });
                } else {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, 150, (int) (50 * density));
                    iv.setImageBitmap(bm);
                    if (bm != null) {
                        // bm.recycle();
                        bm = null;
                    }
                    tv.setOnTouchListener(new ButtonHighlighterOnTouchListener(tv));
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                                return;
                            TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 0;
                            iv.setVisibility(View.VISIBLE);
                            onWrongAnswerClicked(null);
                            isCorrect = false;
                            answerID = String.valueOf(tv.getTag());
                        }
                    });
                }

                answer_lo.addView(tv);
                answer_lo.addView(iv);
                exerciseLayout.addView(answer_lo);
            }
        } else {

            int tvMaxWidth = exerciseLayout.getLayoutParams().width / 2 - 60; //(int)(250/1.5);
            int maxFontsize = 25;
            for (String answer : answerList) {

                TextView tv = new TextView(this);

                tv.setMaxLines(1);
                tv.setMinLines(1);
                tv.setText(answer);
                // tv.setPadding(15,5,15,5);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (maxFontsize * density));
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tv.setTypeface(tfLTElight);
                tv.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
                tv.setTextColor(getResources().getColor(R.color.extext_enabled));

                tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);


                while (tv.getMeasuredWidth() > tvMaxWidth) {
                    maxFontsize = maxFontsize - 1;
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((maxFontsize) * density));
                    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    tv.setTypeface(tfLTElight);
                    tv.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
                    tv.setTextColor(getResources().getColor(R.color.extext_enabled));

                    tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                }


            }


            int linecount = 0;
            if (answerList.size() % 2 == 1) {
                linecount = (answerList.size() / 2) + 1;
            } else {
                linecount = answerList.size() / 2;
            }

            int leftMargin = (rl_exerciseField.getLayoutParams().width - (tvMaxWidth * 2)) / 3;
            int topMargin = (rl_exerciseField.getLayoutParams().height - (linecount * 110)) / (linecount + 1);

            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams l_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            line.setLayoutParams(l_p);

            for (String answer : answerList) {
                LinearLayout answer_lo = new LinearLayout(this);
                answer_lo.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams alo_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                alo_p.leftMargin = leftMargin;
                alo_p.topMargin = (int) (topMargin / density);
                answer_lo.setLayoutParams(alo_p);

                final TextView tv = new TextView(this);

                LinearLayout.LayoutParams tv_p = new LinearLayout.LayoutParams(tvMaxWidth, (int) (50 * density));
                tv.setLayoutParams(tv_p);

                tv.setMaxLines(1);
                tv.setMinLines(1);
                tv.setText(answer);
                //tv.setPadding(15,5,15,5);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (maxFontsize));
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                tv.setTypeface(tfLTElight);
                tv.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
                tv.setTextColor(getResources().getColor(R.color.extext_enabled));
                tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                tv.setTag(answer);

                final ImageView iv = new ImageView(this);
                iv.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(tvMaxWidth, 50);

                iv_p.topMargin = 10;
                iv_p.gravity = Gravity.CENTER_HORIZONTAL;
                iv.setLayoutParams(iv_p);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                if (answerList.indexOf(answer) == correctAnswerpos) {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, tvMaxWidth, 50);
                    iv.setImageBitmap(bm);
                    if (bm != null) {
                        //  bm.recycle();
                        bm = null;
                    }
                    tv.setOnTouchListener(new ButtonHighlighterOnTouchListener(tv));
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                                return;
                            TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 1;
                            iv.setVisibility(View.VISIBLE);
                            onCorrectAnswerClicked(null);
                            isCorrect = true;
                            answerID = String.valueOf(tv.getTag());
                        }
                    });
                } else {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, tvMaxWidth, 50);
                    iv.setImageBitmap(bm);
                    if (bm != null) {
                        // bm.recycle();
                        bm = null;
                    }
                    tv.setOnTouchListener(new ButtonHighlighterOnTouchListener(tv));
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                                return;
                            TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 0;
                            iv.setVisibility(View.VISIBLE);
                            onWrongAnswerClicked(null);
                            isCorrect = false;
                            answerID = String.valueOf(tv.getTag());
                        }
                    });
                }

                answer_lo.addView(tv);
                answer_lo.addView(iv);

                if (answerList.indexOf(answer) % 2 == 0) {
                    line = new LinearLayout(this);
                    line.setOrientation(LinearLayout.HORIZONTAL);
                    line.setLayoutParams(l_p);
                    line.addView(answer_lo);
                    if (answerList.indexOf(answer) == answerList.size() - 1)
                        exerciseLayout.addView(line);
                } else {
                    line.addView(answer_lo);
                    exerciseLayout.addView(line);
                }
            }
        }

        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }

    /**
     * ImageForWord
     **/

    protected int spacecount(String s) {
        int i;
        int c;
        for (i = 0, c = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ' ')
                c++;
        }
        return c;
    }


    /**     SortWords      **/

    /**
     * erstellt das Trainingsfeld fuer die ImageforWord-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseImageForWord(Exercise exercise) {


        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_trainingField.getLayoutParams().width, rl_trainingField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);
        exerciseLayout.setId(R.id.exercise_layout);

        rl_media.setVisibility(View.GONE);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ll_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_p.gravity = Gravity.CENTER_HORIZONTAL;
        ll_question.setLayoutParams(ll_p);

        LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lla_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lla_p.gravity = Gravity.CENTER_HORIZONTAL;
        lla_p.topMargin = 10;
        ll_answer.setLayoutParams(lla_p);

        // zufaellige Position fuer richtige Antwort ermitteln
        Random random = new Random();
        final int correctAnswerpos = random.nextInt(exercise.getWrongAnswers().size() + 1);

        ArrayList<String> answerList = (ArrayList<String>) exercise.getWrongAnswers().clone();
        answerList.add(correctAnswerpos, exercise.getQuestionMedia());

        int maxHeight = (int) ((rl_trainingField.getLayoutParams().height * 0.6f) - 50 - 40);
        int imagewidth = (e_p.width - 140 - (answerList.size() * 15)) / answerList.size();
        if (imagewidth > maxHeight) imagewidth = maxHeight;


        for (int i = 0; i < answerList.size(); i++) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(imagewidth, imagewidth);
            iv_p.leftMargin = 30;
            iv.setLayoutParams(iv_p);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bitmap = Utils.decodeSampledBitmapFromPath(directory + "/" + answerList.get(i), imagewidth, imagewidth);
            iv.setImageBitmap(bitmap);

            final ImageView answer_iv = new ImageView(this);
            LinearLayout.LayoutParams aiv_p = new LinearLayout.LayoutParams(imagewidth, 50);
            aiv_p.leftMargin = 30;
            answer_iv.setVisibility(View.INVISIBLE);
            answer_iv.setLayoutParams(aiv_p);
            answer_iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            answer_iv.setTag(answerList.get(i));

            if (i == correctAnswerpos) {
                Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, imagewidth, 50);
                answer_iv.setImageBitmap(bm);
                if (bm != null) {
                    //  bm.recycle();
                    bm = null;
                }

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vi) {
                        if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                            return;
                        setCurrentExerciseAsCorrectAnswered();
                        isCorrect = true;
                        answerID = String.valueOf(answer_iv.getTag());
                        answer_iv.setVisibility(View.VISIBLE);
                        updateEvaluationbar();
                    }
                });
            } else {
                Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, imagewidth, 50);
                answer_iv.setImageBitmap(bm);
                if (bm != null) {
                    // bm.recycle();
                    bm = null;
                }
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vi) {
                        if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus < 2)
                            return;
                        setCurrentExerciseAsWrongAnswered();
                        isCorrect = false;
                        answerID = String.valueOf(answer_iv.getTag());
                        answer_iv.setVisibility(View.VISIBLE);
                        updateEvaluationbar();
                    }
                });
            }

            ll_answer.addView(answer_iv);
            ll_question.addView(iv);
        }

        exerciseLayout.addView(ll_question);
        exerciseLayout.addView(ll_answer);

        TextView word = new TextView(this);
        word.setMaxLines(1);
        word.setMinLines(1);
        word.setText(exercise.getQuestion());
        word.setBackgroundColor(Color.WHITE);
        word.setGravity(Gravity.CENTER);
        word.setTextSize(TypedValue.COMPLEX_UNIT_PX, (50 - 16) * density);
        word.setTypeface(tfLTElight);
        word.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
        word.setTextColor(getResources().getColor(R.color.extext_enabled));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.topMargin = 70;
        word.setPadding(20, 5, 20, 5);
        word.setLayoutParams(params);

        exerciseLayout.addView(word);

        rl_trainingField.addView(exerciseLayout);
    }

    /**
     * erstellt das Trainingsfeld fuer die SortWord-Exercises
     *
     * @param exercise Uebung
     */
    @SuppressLint("ResourceAsColor")
    protected void createExerciseSortWords(Exercise exercise) {


        String[] correctAnswer = exercise.getCorrectAnswer().split(" ");
        List<String> w = Arrays.asList(correctAnswer);
        List<String> wordsList = new ArrayList<String>();
        wordsList.addAll(w);
        Collections.shuffle(wordsList);


        List<TextView> questionfieldsList = new ArrayList<TextView>();
        answerfieldsList = new ArrayList<TextView>();

        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, (int) (rl_exerciseField.getLayoutParams().height * 0.45f));
        q_p.leftMargin = imageleftMargin;
        ll_question.setLayoutParams(q_p);
        exerciseLayout.addView(ll_question);

        LinearLayout ll_feedback = new LinearLayout(this);
        ll_feedback.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fb_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height * 0.1f));
        fb_p.leftMargin = imageleftMargin;
        ll_feedback.setLayoutParams(fb_p);

        LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams a_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        a_p.topMargin = 5;
        a_p.leftMargin = imageleftMargin;
        ll_answer.setLayoutParams(a_p);

        exerciseLayout.addView(ll_answer);
        exerciseLayout.addView(ll_feedback);

        final ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setId(R.id.feedback_image);
        iv.setVisibility(View.INVISIBLE);
        ll_feedback.addView(iv);

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (q_p.height / 3) - 5);
        l_p.topMargin = 5;
        line.setLayoutParams(l_p);

        int usedWidth = 0;
        for (String word : wordsList) {

            TextView qbtn = new TextView(this);
            qbtn.setMaxLines(1);
            qbtn.setMinLines(1);
            qbtn.setText(word);
            qbtn.setBackgroundColor(Color.WHITE);
            qbtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            qbtn.setTypeface(tfLTElight);
            qbtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            qbtn.setTextColor(getResources().getColor(R.color.extext_enabled));

            Random r = new Random();
            int i1 = r.nextInt((l_p.height / 2));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, l_p.height / 2);
            params.topMargin = i1;
            qbtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            qbtn.setPadding(15, 5, 15, 5);
            params.leftMargin = 20;
            qbtn.setLayoutParams(params);

            qbtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(qbtn));
            qbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateWordsAnswerfields(((TextView) v));
                    v.setEnabled(false);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.extext_disabled));
                }
            });

            questionfieldsList.add(qbtn);

            qbtn.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int btnwidth = qbtn.getMeasuredWidth();

            if (usedWidth + 20 + btnwidth > (rl_exerciseField.getLayoutParams().width) - btnwidth) {
                ll_question.addView(line);
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            line.addView(qbtn);
            usedWidth = usedWidth + btnwidth + 20;

        }

        ll_question.addView(line);

        LinearLayout answer_line = new LinearLayout(this);
        answer_line.setOrientation(LinearLayout.HORIZONTAL);
        answer_line.setLayoutParams(l_p);

        usedWidth = 0;

        for (String a_word : correctAnswer) {
            TextView aBtn = new TextView(this);
            aBtn.setMaxLines(1);
            aBtn.setMinLines(1);
            aBtn.setText(a_word);
            aBtn.setBackgroundColor(Color.WHITE);
            aBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            aBtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            aBtn.setTypeface(tfLTElight);
            aBtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            aBtn.setPadding(15, 5, 15, 5);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (l_p.height / 2));
            params.leftMargin = 20;
            params.gravity = Gravity.CENTER_VERTICAL;
            aBtn.setLayoutParams(params);

            aBtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(aBtn));
            aBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((TextView) v).getText().toString().equalsIgnoreCase("")) return;

                    if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus != 2) {
                        return;
                    }
                    // Rueckgaengigmachen des gesperrten Feldes. Die Referenz zum Feld haben wir im Tag der Antwort gespeichert
                    int ipos = answerfieldsList.indexOf(v);
                    givenAnswerList.remove(ipos);
                    givenAnswerList.add(ipos, "");
                    TextView tvQ = (TextView) v.getTag();
                    tvQ.setEnabled(true);
                    tvQ.setTextColor(getResources().getColor(R.color.extext_enabled));

                    // Loeschen des Antwortextes
                    ((TextView) v).setText("");
                }
            });

            answerfieldsList.add(aBtn);

            aBtn.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int btnwidth = aBtn.getMeasuredWidth();

            params.width = aBtn.getMeasuredWidth();
            aBtn.setLayoutParams(params);
            aBtn.setText("");

            if (usedWidth + 20 + btnwidth > (rl_exerciseField.getLayoutParams().width) - btnwidth) {
                ll_answer.addView(answer_line);
                answer_line = new LinearLayout(this);
                answer_line.setOrientation(LinearLayout.HORIZONTAL);
                answer_line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            answer_line.addView(aBtn);
            usedWidth = usedWidth + btnwidth + 20;
        }

        ll_answer.addView(answer_line);

        ll_answer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int llwidth = ll_answer.getMeasuredWidth();
        LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(llwidth - 20, 50);
        iv_p.leftMargin = 20;
        Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, llwidth - 20, 50);
        iv.setImageBitmap(bm);
        if (bm != null) {
            //  bm.recycle();
            bm = null;
        }
        iv.setLayoutParams(iv_p);


        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }

    /**
     * SortCharacters
     **/

    protected void updateWordsAnswerfields(TextView tvSource) {

        CharSequence text = tvSource.getText();
        for (int i = 0; i < answerfieldsList.size(); i++) {
            TextView textView = answerfieldsList.get(i);
            if (textView.getText().toString().equalsIgnoreCase("")) {
                textView.setText(text);
                textView.setTag(tvSource);
                if (givenAnswerList.size() >= (i + 1) && givenAnswerList.get(i).equalsIgnoreCase(""))
                    givenAnswerList.remove(i);
                givenAnswerList.add(i, String.valueOf(text));
                //givenAnswer += " " + text;
                //givenAnswer = givenAnswer.trim();
                break;
            }
        }

        String answerString = "";
        for (String s : givenAnswerList) {
            answerString += " " + s;
        }
        answerString = answerString.trim();

        if (answerString.length() != TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().length())
            return;

        if (TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().equalsIgnoreCase(answerString)) {
            ImageView iv = findViewById(R.id.feedback_image);
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, 250, 50);
            iv.setImageBitmap(bm);
            if (bm != null) {
                //bm.recycle();
                bm = null;
            }
            iv.setVisibility(View.VISIBLE);
            setCurrentExerciseAsCorrectAnswered();
            isCorrect = true;
            givenAnswer = answerString;
        } else if (!TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().equalsIgnoreCase(answerString)) {
            ImageView iv = findViewById(R.id.feedback_image);
            iv.setVisibility(View.VISIBLE);
            setCurrentExerciseAsWrongAnswered();
            isCorrect = false;
            givenAnswer = answerString;
        }


        updateEvaluationbar();
    }

    /**
     * erstellt das Trainingsfeld fuer die SortCharacter-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseSortCharacters(Exercise exercise) {

        System.out.println("<< dens " + density);
        int charactersCount = exercise.getCorrectAnswer().length();

        // Frage shuffeln
        List<Character> characters = new ArrayList<Character>();
        for (char c : exercise.getCorrectAnswer().toCharArray()) {
            characters.add(c);
        }

        StringBuilder shuffledQuestion = new StringBuilder(charactersCount);
        while (characters.size() != 0) {
            int randPicker = (int) (Math.random() * characters.size());
            shuffledQuestion.append(characters.remove(randPicker));
        }

        List<TextView> questionfieldsList = new ArrayList<TextView>();
        answerfieldsList = new ArrayList<TextView>();

        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, (int) (rl_exerciseField.getLayoutParams().height * 0.45f));
        q_p.leftMargin = imageleftMargin;
        ll_question.setLayoutParams(q_p);
        exerciseLayout.addView(ll_question);

        LinearLayout ll_feedback = new LinearLayout(this);
        ll_feedback.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fb_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height * 0.1f));
        ll_feedback.setLayoutParams(fb_p);

        LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams a_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        a_p.leftMargin = imageleftMargin;
        a_p.topMargin = 5;
        ll_answer.setLayoutParams(a_p);

        exerciseLayout.addView(ll_answer);
        exerciseLayout.addView(ll_feedback);

        final ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setId(R.id.feedback_image);
        iv.setVisibility(View.INVISIBLE);
        ll_feedback.addView(iv);

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ((q_p.height / 3) - 5));
        l_p.topMargin = 5;
        line.setLayoutParams(l_p);

        int usedWidth = 0;
        for (int i = 0; i < charactersCount; i++) {
            String buchst = String.valueOf(shuffledQuestion.toString().charAt(i));
            TextView qbtn = new TextView(this);
            qbtn.setMaxLines(1);
            qbtn.setMinLines(1);
            qbtn.setText(buchst);
            qbtn.setBackgroundColor(Color.WHITE);
            qbtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            qbtn.setTypeface(tfLTElight);
            qbtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            qbtn.setTextColor(getResources().getColor(R.color.extext_enabled));

            Random r = new Random();
            int i1 = r.nextInt((l_p.height / 2));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(l_p.height / 2, l_p.height / 2);
            params.topMargin = i1;
            qbtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            // qbtn.setPadding(5, 5, 5, 5);
            params.leftMargin = 20;
            qbtn.setLayoutParams(params);

            qbtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(qbtn));
            qbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCharactersAnswerfields(v);
                    v.setEnabled(false);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.extext_disabled));
                }
            });

            questionfieldsList.add(qbtn);

            if (usedWidth + 20 + (l_p.height / 2) > (rl_exerciseField.getLayoutParams().width) - (l_p.height / 2)) {
                ll_question.addView(line);
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            line.addView(qbtn);
            usedWidth = usedWidth + (l_p.height / 2) + 20;

        }

        ll_question.addView(line);

        LinearLayout answer_line = new LinearLayout(this);
        answer_line.setOrientation(LinearLayout.HORIZONTAL);
        answer_line.setLayoutParams(l_p);

        usedWidth = 0;

        for (int i = 0; i < charactersCount; i++) {
            TextView aBtn = new TextView(this);
            aBtn.setMaxLines(1);
            aBtn.setMinLines(1);
            aBtn.setBackgroundColor(Color.WHITE);
            aBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            aBtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            aBtn.setTypeface(tfLTElight);
            aBtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            // aBtn.setPadding(5,5,5,5);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(l_p.height / 2, l_p.height / 2);
            params.leftMargin = 20;
            params.gravity = Gravity.CENTER_VERTICAL;
            aBtn.setLayoutParams(params);

            givenAnswer += "*";

            aBtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(aBtn));
            aBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!givenAnswer.contains("*")) return;
                    // Rueckgaengigmachen des gesperrten Feldes. Die Referenz zum Feld haben wir im Tag der Antwort gespeichert
                    TextView tvQ = (TextView) v.getTag();
                    if (tvQ == null) return;

                    int ipos = answerfieldsList.indexOf(v);
                    StringBuilder sb = new StringBuilder(givenAnswer);
                    sb.setCharAt(ipos, '*');
                    givenAnswer = sb.toString();
                    ((TextView) v).setText("");

                    tvQ.setEnabled(true);
                    tvQ.setTextColor(getResources().getColor(R.color.extext_enabled));
                    v.setTag(null);
                }
            });

            answerfieldsList.add(aBtn);

            if (usedWidth + 20 + (l_p.height / 2) > (rl_exerciseField.getLayoutParams().width) - (l_p.height / 2)) {
                ll_answer.addView(answer_line);
                answer_line = new LinearLayout(this);
                answer_line.setOrientation(LinearLayout.HORIZONTAL);
                answer_line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            answer_line.addView(aBtn);
            usedWidth = usedWidth + (l_p.height / 2) + 20;
        }

        ll_answer.addView(answer_line);

        ll_answer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int llwidth = ll_answer.getMeasuredWidth();
        LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(llwidth - 20, 50);
        //iv_p.leftMargin = 20;
        iv_p.leftMargin = imageleftMargin + 20;
        Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, llwidth - 20, 50);
        iv.setImageBitmap(bm);
        if (bm != null) {
            // bm.recycle();
            bm = null;
        }
        iv.setLayoutParams(iv_p);
        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }


    /**
     * GapWordForImage
     **/

    protected void updateCharactersAnswerfields(View v) {
        CharSequence text = ((TextView) v).getText();
        for (int i = 0; i < answerfieldsList.size(); i++) {
            TextView textView = answerfieldsList.get(i);
            if (textView.getText().toString().equalsIgnoreCase("")) {
                textView.setText(text);
                textView.setTag(v);
                StringBuilder sb = new StringBuilder(givenAnswer);
                sb.setCharAt(i, text.charAt(0));
                givenAnswer = sb.toString();
                break;
            }
        }
        if (givenAnswer.contains("*")) return;
        if (givenAnswer.length() == answerfieldsList.size() && TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().equalsIgnoreCase(givenAnswer)) {
            ImageView iv = findViewById(R.id.feedback_image);
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, 250, 50);
            iv.setImageBitmap(bm);
            if (bm != null) {
                // bm.recycle();
                bm = null;
            }

            iv.setVisibility(View.VISIBLE);
            setCurrentExerciseAsCorrectAnswered();
        } else if (givenAnswer.length() == answerfieldsList.size() && !TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().equalsIgnoreCase(givenAnswer)) {
            ImageView iv = findViewById(R.id.feedback_image);
            iv.setVisibility(View.VISIBLE);
            setCurrentExerciseAsWrongAnswered();
        }
        updateEvaluationbar();
    }

    /**     ImageForVideo      **/

    /**
     * erstellt das Trainingsfeld fuer die GapWordForImage-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseGapWordForImage(Exercise exercise) {

        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);

        exerciseLayout.setLayoutParams(e_p);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, (int) (rl_exerciseField.getLayoutParams().height * 0.45f));
        q_p.leftMargin = imageleftMargin;
        ll_question.setLayoutParams(q_p);
        exerciseLayout.addView(ll_question);

        LinearLayout ll_feedback = new LinearLayout(this);
        ll_feedback.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fb_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height * 0.1f));
        fb_p.leftMargin = imageleftMargin;
        ll_feedback.setLayoutParams(fb_p);


        LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams a_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        a_p.leftMargin = imageleftMargin;
        a_p.topMargin = 25;

        ll_answer.setLayoutParams(a_p);
        exerciseLayout.addView(ll_answer);
        exerciseLayout.addView(ll_feedback);

        // Luecken aus der korrekten Antwort in den Array mit den zusätzlichen Chars
        // Luecken in Zahlen umwandeln
        List<Integer> gapPositions = new ArrayList<Integer>();
        for (int i = 0; i < exercise.getGaps().length(); i++) {
            if (!exercise.getGaps().substring(i, i + 1).equalsIgnoreCase(",")) {
                gapPositions.add(Integer.valueOf(exercise.getGaps().substring(i, i + 1)));
            }
        }


        StringBuilder givenanswerSB = new StringBuilder(exercise.getCorrectAnswer());
        //Lueckenchars zu den addional_chars hinzufuegen
        String additionalChars = exercise.getAdditionalChars();
        for (int i = 0; i < exercise.getCorrectAnswer().length(); i++) {
            if (gapPositions.contains(i)) {
                additionalChars += exercise.getCorrectAnswer().substring(i, i + 1);

                //correctAnswer += exercise.getCorrectAnswer().substring(i, i+1);
                givenanswerSB.replace(i, i + 1, "*");
            }
        }
        givenAnswer = givenanswerSB.toString();

        // Antwortmoeglichkeiten in zufaellige Reihenfolge
        List<Character> characters = new ArrayList<Character>();
        for (char c : additionalChars.toCharArray()) {
            characters.add(c);
        }
        StringBuilder gapsChars = new StringBuilder(exercise.getAdditionalChars().length());
        while (characters.size() != 0) {
            int randPicker = (int) (Math.random() * characters.size());
            gapsChars.append(characters.remove(randPicker));
        }

        final ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setId(R.id.feedback_image);
        iv.setVisibility(View.INVISIBLE);
        ll_feedback.addView(iv);

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ((q_p.height / 3) - 5));
        l_p.topMargin = 5;
        line.setLayoutParams(l_p);

        int usedWidth = 0;
        final int breite = 0;
        for (int i = 0; i < gapsChars.toString().length(); i++) {

            TextView qbtn = new TextView(this);
            qbtn.setMaxLines(1);
            qbtn.setMinLines(1);
            qbtn.setText(String.valueOf(gapsChars.charAt(i)));
            qbtn.setBackgroundColor(Color.WHITE);
            qbtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            qbtn.setTypeface(tfLTElight);
            qbtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            qbtn.setTextColor(getResources().getColor(R.color.extext_enabled));

            Random r = new Random();
            int i1 = r.nextInt((l_p.height / 2));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(l_p.height / 2, l_p.height / 2);
            params.topMargin = i1;
            qbtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            // qbtn.setPadding(5,5,5,5);
            params.leftMargin = 20;
            qbtn.setLayoutParams(params);

            qbtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(qbtn));
            qbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateGapWordAnswerfields(((TextView) v));
                    v.setEnabled(false);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.extext_disabled));
                }

                private void updateGapWordAnswerfields(TextView tv) {
                    CharSequence text = tv.getText();
                    for (int i1 = 0; i1 < answerfieldsList.size(); i1++) {
                        TextView textView = answerfieldsList.get(i1);
                        if (textView.getText().toString().equalsIgnoreCase("")) {
                            textView.setText(text);
                            textView.setTag(tv);
                            StringBuilder sb = new StringBuilder(givenAnswer);
                            sb.setCharAt(i1, text.charAt(0));
                            givenAnswer = sb.toString();
                            break;
                        }
                    }

                    if (givenAnswer.contains("*")) return;

                    if (givenAnswer.equalsIgnoreCase(TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer())) {
                        Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, iv.getLayoutParams().width, 50);
                        iv.setImageBitmap(bm);
                        if (bm != null) {
                            // bm.recycle();
                            bm = null;
                        }
                        setCurrentExerciseAsCorrectAnswered();
                        iv.setVisibility(View.VISIBLE);
                    } else {
                        setCurrentExerciseAsWrongAnswered();
                        iv.setVisibility(View.VISIBLE);
                    }
                    updateEvaluationbar();
                }
            });

            if (usedWidth + 20 + (l_p.height / 2) > (rl_exerciseField.getLayoutParams().width) - (l_p.height / 2)) {
                ll_question.addView(line);
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            line.addView(qbtn);
            usedWidth = usedWidth + (l_p.height / 2) + 20;

        }

        ll_question.addView(line);


        LinearLayout answer_line = new LinearLayout(this);
        answer_line.setOrientation(LinearLayout.HORIZONTAL);
        answer_line.setLayoutParams(l_p);

        usedWidth = 0;

        for (int i = 0; i < exercise.getCorrectAnswer().length(); i++) {
            TextView aBtn = new TextView(this);
            aBtn.setMaxLines(1);
            aBtn.setMinLines(1);
            aBtn.setText(String.valueOf(exercise.getCorrectAnswer().charAt(i)));
            if (gapPositions.contains(i)) aBtn.setText("");
            aBtn.setBackgroundColor(Color.WHITE);
            aBtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            aBtn.setTypeface(tfLTElight);
            aBtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            //aBtn.setPadding(5,5,5,5);

            int width = ((rl_exerciseField.getLayoutParams().width - (20 * exercise.getCorrectAnswer().length())) / exercise.getCorrectAnswer().length());
            aBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);

            if (width > l_p.height / 2) width = l_p.height / 2;
            aBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
            params.leftMargin = 20;
            params.gravity = Gravity.CENTER_VERTICAL;
            aBtn.setLayoutParams(params);

            if (gapPositions.contains(i)) {
                aBtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(aBtn));
                aBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!givenAnswer.contains("*")) return;
                        // Rueckgaengigmachen des gesperrten Feldes. Die Referenz zum Feld haben wir im Tag der Antwort gespeichert
                        TextView tvQ = (TextView) v.getTag();
                        if (tvQ == null)
                            return;            // kein v gespeichert, vermutlich kein Antwortbuchstabe

                        int ipos = answerfieldsList.indexOf(v);
                        StringBuilder sb = new StringBuilder(givenAnswer);
                        sb.setCharAt(ipos, '*');
                        givenAnswer = sb.toString();
                        ((TextView) v).setText("");

                        tvQ.setEnabled(true);
                        tvQ.setTextColor(getResources().getColor(R.color.extext_enabled));
                        v.setTag(null);
                    }
                });
            }


            answerfieldsList.add(aBtn);


            if (usedWidth + 10 + l_p.height / 2 > rl_exerciseField.getLayoutParams().width - (l_p.height / 2)) {
                ll_answer.addView(answer_line);
                answer_line = new LinearLayout(this);
                answer_line.setOrientation(LinearLayout.HORIZONTAL);
                answer_line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            answer_line.addView(aBtn);
            usedWidth = (usedWidth + l_p.height / 2 + 20);
        }

        ll_answer.addView(answer_line);


        ll_answer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int llwidth = ll_answer.getMeasuredWidth();
        LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(llwidth - 20, 50);
        iv_p.leftMargin = 20;
        iv.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, llwidth - 20, 50));
        iv.setLayoutParams(iv_p);


        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }


    /**     WordForVideo      **/

    /**
     * erstellt das Trainingsfeld fuer die ImageForVideo-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseImageForVideo(Exercise exercise) {

        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_trainingField.getLayoutParams().width, rl_trainingField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);
        exerciseLayout.setId(R.id.exercise_layout);

        rl_media.setVisibility(View.GONE);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ll_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_p.gravity = Gravity.CENTER_HORIZONTAL;
        ll_question.setLayoutParams(ll_p);

        final LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lla_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lla_p.gravity = Gravity.CENTER_HORIZONTAL;
        lla_p.topMargin = 10;
        ll_answer.setLayoutParams(lla_p);

        ArrayList<HashMap> answerList = exercise.getifvAnswers();

        int maxHeight = (int) ((rl_trainingField.getLayoutParams().height * 0.6f) - 50 - 40);
        int imageWidth = (e_p.width - 140 - (answerList.size() * 15)) / answerList.size();
        if (imageWidth > maxHeight) imageWidth = maxHeight;

        for (HashMap answer : answerList) {
            System.out.println("<<< ANSWER " + answer);
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(imageWidth, imageWidth);
            iv_p.leftMargin = 10;
            iv.setLayoutParams(iv_p);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(Color.rgb(255, 255, 255));
            Bitmap bitmap = Utils.decodeSampledBitmapFromPath(directory + "/" + answer.get("media"), imageWidth, imageWidth);
            iv.setImageBitmap(bitmap);
            final String help_media = (String) answer.get("help_media");


            final ImageView answer_iv = new ImageView(this);
            LinearLayout.LayoutParams aiv_p = new LinearLayout.LayoutParams(imageWidth, 50);
            aiv_p.leftMargin = 10;
            answer_iv.setVisibility(View.INVISIBLE);
            answer_iv.setLayoutParams(aiv_p);
            answer_iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            answer_iv.setTag(answer);

            if (TrainingSingleton.getInstance().getCurrentExercise().getQuestionMedia().equalsIgnoreCase((String) answer.get("help_media"))) {
                Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, imageWidth, 50);
                answer_iv.setImageBitmap(bm);
                if (bm != null) {
                    // bm.recycle();
                    bm = null;
                }

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vi) {
                        setCurrentExerciseAsCorrectAnswered();
                        isCorrect = true;
                        answerID = String.valueOf(answer_iv.getTag());
                        for (int i = 0; i < ll_answer.getChildCount(); i++) {
                            ll_answer.getChildAt(i).setVisibility(View.INVISIBLE);
                        }
                        answer_iv.setVisibility(View.VISIBLE);
                        updateEvaluationbar();
                        loadHelpVideo(help_media, true);
                    }
                });
            } else {
                Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, imageWidth, 50);
                answer_iv.setImageBitmap(bm);
                if (bm != null) {
                    // bm.recycle();
                    bm = null;
                }

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vi) {
                        setCurrentExerciseAsWrongAnswered();
                        isCorrect = false;
                        answerID = String.valueOf(answer_iv.getTag());
                        for (int i = 0; i < ll_answer.getChildCount(); i++) {
                            ll_answer.getChildAt(i).setVisibility(View.INVISIBLE);
                        }
                        answer_iv.setVisibility(View.VISIBLE);
                        updateEvaluationbar();
                        loadHelpVideo(help_media, true);
                    }
                });
            }


            ll_answer.addView(answer_iv);
            ll_question.addView(iv);
        }

        exerciseLayout.addView(ll_question);
        exerciseLayout.addView(ll_answer);

        rl_trainingField.addView(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }


    /**     GapSentenceForImage      **/

    /**
     * erstellt das Trainingsfeld fuer die WordForVideo-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseWordForVideo(Exercise exercise) {
        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.HORIZONTAL);

        //LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height*0.45f));
        LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_question.setLayoutParams(q_p);
        //exerciseLayout.addView(ll_question);

        LinearLayout ll_feedback = new LinearLayout(this);
        ll_feedback.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fb_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height * 0.1f));
        ll_feedback.setLayoutParams(fb_p);
//        ll_feedback.setBackgroundColor(Color.GREEN);


        final LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams a_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        a_p.topMargin = 5;
        ll_answer.setLayoutParams(a_p);
//        ll_answer.setBackgroundColor(Color.BLUE);


        ArrayList<HashMap> answerList = exercise.getifvAnswers();


        int maxHeight = (int) ((rl_trainingField.getLayoutParams().height * 0.6f) - 50 - 40);
        int imageWidth = (e_p.width - 140 - (answerList.size() * 15)) / answerList.size();
        if (imageWidth > maxHeight) imageWidth = maxHeight;

        System.out.println("<< imageWidth " + imageWidth);

        int iv_pWidht = (rl_exerciseField.getLayoutParams().width / 2) - 60;
        System.out.println("<< iv_pWidht " + iv_pWidht);

        LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(iv_pWidht, 100);
        iv_p.leftMargin = 20;
        iv_p.topMargin = 25;

        LinearLayout.LayoutParams iv_fb = new LinearLayout.LayoutParams(iv_pWidht, 252);
        iv_fb.leftMargin = 20;
        iv_fb.topMargin = 25;

        final ImageView iv_answerCorrect = new ImageView(this);
        Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, 350, 100);
        iv_answerCorrect.setImageBitmap(bm);
        if (bm != null) {
            // bm.recycle();
            bm = null;
        }
        iv_answerCorrect.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv_answerCorrect.setLayoutParams(iv_p);
        iv_answerCorrect.setTag(answerList.get(0));
        iv_answerCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {
                ImageView iv_feedback = (ImageView) ll_answer.getChildAt(0);
                if (String.valueOf(((HashMap) vi.getTag()).get("id")).equalsIgnoreCase(TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer())) {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.richtig, 350, 252);
                    iv_feedback.setImageBitmap(bm);
                    if (bm != null) {
                        //  bm.recycle();
                        bm = null;
                    }
                    setCurrentExerciseAsCorrectAnswered();
                    isCorrect = true;
                } else {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.falsch, 350, 252);
                    iv_feedback.setImageBitmap(bm);
                    if (bm != null) {
                        // bm.recycle();
                        bm = null;
                    }
                    setCurrentExerciseAsWrongAnswered();
                }

                //answerID = String.valueOf(iv_answerCorrect.getTag());
                for (int i = 0; i < ll_answer.getChildCount(); i++) {
                    ll_answer.getChildAt(i).setVisibility(View.INVISIBLE);
                }
                iv_feedback.setVisibility(View.VISIBLE);
                updateEvaluationbar();
            }
        });
        ll_question.addView(iv_answerCorrect);
        ImageView iv_feedbackright = new ImageView(this);
        iv_feedbackright.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv_feedbackright.setLayoutParams(iv_fb);
        ll_answer.addView(iv_feedbackright);


        final ImageView iv_answerWrong = new ImageView(this);
        Bitmap bm2 = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, imageWidth, 100);
        iv_answerWrong.setImageBitmap(bm2);
        if (bm2 != null) {
            //  bm2.recycle();
            bm2 = null;
        }
        iv_answerWrong.setLayoutParams(iv_p);
        iv_answerWrong.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv_answerWrong.setTag(answerList.get(1));
        iv_answerWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {
                ImageView iv_feedback = (ImageView) ll_answer.getChildAt(1);
                if (String.valueOf(((HashMap) vi.getTag()).get("id")).equalsIgnoreCase(TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer())) {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.richtig, 350, 252);
                    iv_feedback.setImageBitmap(bm);
                    if (bm != null) {
                        // bm.recycle();
                        bm = null;
                    }
                    setCurrentExerciseAsCorrectAnswered();
                    isCorrect = true;
                } else {
                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.falsch, 350, 252);
                    iv_feedback.setImageBitmap(bm);
                    if (bm != null) {
                        // bm.recycle();
                        bm = null;
                    }
                    setCurrentExerciseAsWrongAnswered();
                }

                //answerID = String.valueOf(iv_answerCorrect.getTag());
                for (int i = 0; i < ll_answer.getChildCount(); i++) {
                    ll_answer.getChildAt(i).setVisibility(View.INVISIBLE);
                }
                iv_feedback.setVisibility(View.VISIBLE);
                updateEvaluationbar();
            }
        });
        ll_question.addView(iv_answerWrong);
        ImageView iv_feedbackleft = new ImageView(this);
        iv_feedbackleft.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv_feedbackleft.setLayoutParams(iv_fb);
        ll_answer.addView(iv_feedbackleft);

        exerciseLayout.addView(ll_question);
        exerciseLayout.addView(ll_answer);
        exerciseLayout.addView(ll_feedback);

        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getHelpVideo());
    }


    /**     allgemeine Methoden für Übungen **/

    /**
     * erstellt das Trainingsfeld fuer die GapWordForImage-Exercises
     *
     * @param exercise Uebung
     */
    protected void createExerciseGapSentenceForImage(Exercise exercise) {

        LinearLayout exerciseLayout = new LinearLayout(this);
        exerciseLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams e_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, rl_exerciseField.getLayoutParams().height);
        exerciseLayout.setLayoutParams(e_p);

        LinearLayout ll_question = new LinearLayout(this);
        ll_question.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams q_p = new LinearLayout.LayoutParams(rl_exerciseField.getLayoutParams().width, (int) (rl_exerciseField.getLayoutParams().height * 0.45f));
        q_p.leftMargin = imageleftMargin;
        ll_question.setLayoutParams(q_p);
        exerciseLayout.addView(ll_question);

        LinearLayout ll_feedback = new LinearLayout(this);
        ll_feedback.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams fb_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (rl_exerciseField.getLayoutParams().height * 0.1f));
        fb_p.leftMargin = imageleftMargin;
        ll_feedback.setLayoutParams(fb_p);

        LinearLayout ll_answer = new LinearLayout(this);
        ll_answer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams a_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        a_p.topMargin = 5;
        a_p.leftMargin = imageleftMargin;
        ll_answer.setLayoutParams(a_p);

        exerciseLayout.addView(ll_answer);
        exerciseLayout.addView(ll_feedback);

        final ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setId(R.id.feedback_image);
        iv.setVisibility(View.INVISIBLE);
        ll_feedback.addView(iv);


        // Luecken aus der korrekten Antwort in den Array mit den zusätzlichen Chars
        // Luecken in Zahlen umwandeln
        List<Integer> gapPositions = new ArrayList<Integer>();
        for (int i = 0; i < exercise.getGaps().length(); i++) {
            if (!exercise.getGaps().substring(i, i + 1).equalsIgnoreCase(",")) {
                gapPositions.add(Integer.valueOf(exercise.getGaps().substring(i, i + 1)));
            }
        }


        //Antwortsatz in Liste ueberfuehren und Lueckenwort(correctanswer) ermitteln
        String[] words = exercise.getCorrectAnswer().split(" ");
        List<String> sentenceWordList = new ArrayList<String>();
        final List<String> correctAnswersList = new ArrayList<String>();
        List<String> gapWordList = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
            if (gapPositions.contains(i)) {  // wenn es ein Lueckenwort ist..
                correctAnswersList.add(words[i]);
                gapWordList.add(words[i]);
                sentenceWordList.add("");
            } else {
                sentenceWordList.add(words[i]);
            }
        }

        //additional_words mit Falschantworten ergaenzen
        String[] addWords = exercise.getAdditionalWords().split(" ");
        Collections.addAll(gapWordList, addWords);
        Collections.shuffle(gapWordList);

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (q_p.height / 3) - 5);
        l_p.topMargin = 5;
        line.setLayoutParams(l_p);

        int usedWidth = 0;
        int maxWidth = 0;

        for (String word : gapWordList) {

            TextView qbtn = new TextView(this);
            qbtn.setMaxLines(1);
            qbtn.setMinLines(1);
            qbtn.setText(word);
            qbtn.setBackgroundColor(Color.WHITE);
            qbtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            qbtn.setTypeface(tfLTElight);
            qbtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            qbtn.setTextColor(getResources().getColor(R.color.extext_enabled));

            Random r = new Random();
            int i1 = r.nextInt((l_p.height / 2));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, l_p.height / 2);
            params.topMargin = i1;
            qbtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            qbtn.setPadding(15, 5, 15, 5);
            params.leftMargin = 20;
            qbtn.setLayoutParams(params);

            qbtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(qbtn));
            qbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateGapWordAnswerfields(((TextView) v));
                    v.setEnabled(false);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.extext_disabled));
                }

                private void updateGapWordAnswerfields(TextView tv) {

                    for (TextView textView : answerfieldsList) {
                        if (textView.getText().toString().equalsIgnoreCase("")) {
                            textView.setText(String.valueOf(tv.getText()));
                            givenAnswerList.add(String.valueOf(tv.getText()));

                            if (givenAnswerList.size() == correctAnswersList.size()) {
                                givenAnswer = "";
                                boolean isAnswerCorrect = true;
                                for (int i1 = 0; i1 < givenAnswerList.size(); i1++) {
                                    if (!givenAnswerList.get(i1).equalsIgnoreCase(correctAnswersList.get(i1))) {

                                        isAnswerCorrect &= false;
                                    }
                                    givenAnswer += givenAnswerList.get(i1);
                                }

                                if (isAnswerCorrect) {
                                    Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackright, 250, 50);
                                    iv.setImageBitmap(bm);
                                    if (bm != null) {
                                        // bm.recycle();
                                        bm = null;
                                    }
                                    setCurrentExerciseAsCorrectAnswered();
                                    iv.setVisibility(View.VISIBLE);
                                } else {
                                    setCurrentExerciseAsWrongAnswered();
                                    iv.setVisibility(View.VISIBLE);
                                }
                                isCorrect = isAnswerCorrect;
                                updateEvaluationbar();
                            }
                            break;
                        }
                    }
                }
            });


            qbtn.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int btnwidth = qbtn.getMeasuredWidth();

            if (qbtn.getMeasuredWidth() > maxWidth) maxWidth = qbtn.getMeasuredWidth();


            if (usedWidth + 20 + btnwidth > (rl_exerciseField.getLayoutParams().width) - btnwidth) {
                ll_question.addView(line);
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            line.addView(qbtn);
            usedWidth = usedWidth + btnwidth + 20;

        }

        ll_question.addView(line);


        LinearLayout answer_line = new LinearLayout(this);
        answer_line.setOrientation(LinearLayout.HORIZONTAL);
        answer_line.setLayoutParams(l_p);

        usedWidth = 0;

        for (String a_word : sentenceWordList) {
            TextView aBtn = new TextView(this);
            aBtn.setMaxLines(1);
            aBtn.setMinLines(1);
            aBtn.setText(a_word);
            aBtn.setBackgroundColor(Color.WHITE);
            aBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((l_p.height / 2) - 16) / density);
            aBtn.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            aBtn.setTypeface(tfLTElight);
            aBtn.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
            aBtn.setPadding(15, 5, 15, 5);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (l_p.height / 2));
            params.leftMargin = 20;
            params.gravity = Gravity.CENTER_VERTICAL;
            aBtn.setLayoutParams(params);
            System.out.println("<<<<< word " + a_word + " wordLength " + a_word.length());

            if (a_word.length() == 0) {

                aBtn.setOnTouchListener(new ButtonHighlighterOnTouchListener(aBtn));
                aBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (((TextView) v).getText().toString().equalsIgnoreCase("")) return;

                        if (TrainingSingleton.getInstance().getCurrentExercise().answerstatus != 2) {
                            return;
                        }
                        // Rueckgaengigmachen des gesperrten Feldes. Die Referenz zum Feld haben wir im Tag der Antwort gespeichert
                        int ipos = answerfieldsList.indexOf(v);
                        givenAnswerList.remove(ipos);
                        givenAnswerList.add(ipos, "");
                        TextView tvQ = (TextView) v.getTag();
                        tvQ.setEnabled(true);
                        tvQ.setTextColor(getResources().getColor(R.color.extext_enabled));

                        // Loeschen des Antwortextes
                        ((TextView) v).setText("");
                    }
                });
            }
            answerfieldsList.add(aBtn);

            aBtn.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int btnwidth = aBtn.getMeasuredWidth();

            if (gapPositions.contains(sentenceWordList.indexOf(a_word))) {
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) aBtn.getLayoutParams();
                p.width = maxWidth;
                aBtn.setLayoutParams(p);
                aBtn.setText("");
                btnwidth = maxWidth;
            }

            if (usedWidth + 20 + btnwidth > (rl_exerciseField.getLayoutParams().width) - btnwidth) {
                ll_answer.addView(answer_line);
                answer_line = new LinearLayout(this);
                answer_line.setOrientation(LinearLayout.HORIZONTAL);
                answer_line.setLayoutParams(l_p);
                usedWidth = 0;
            }

            answer_line.addView(aBtn);
            usedWidth = (usedWidth + btnwidth + 20);
        }

        ll_answer.addView(answer_line);

        ll_answer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int llwidth = ll_answer.getMeasuredWidth();
        LinearLayout.LayoutParams iv_p = new LinearLayout.LayoutParams(llwidth - 20, 50);
        iv_p.leftMargin = 20;
        Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.answerfeedbackwrong, llwidth - 20, 50);
        iv.setImageBitmap(bm);
        if (bm != null) {
            // bm.recycle();
            bm = null;
        }
        iv.setLayoutParams(iv_p);

        addExerciseLayoutToField(exerciseLayout);
        loadQuestionMedia(exercise.getQuestionMedia());
    }

    /**
     * setzt den Title der Uebung
     *
     * @param title zu setzender Titel
     */
    protected void setExerciseTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * bereitet einige Variablen fuer eine neue Uebung vor
     */
    protected void prepareExerciseField() {
        rl_exerciseField.removeAllViews();
        answerfieldsList = new ArrayList<TextView>();
        givenAnswer = "";
        givenAnswerList = new ArrayList<String>();
        iv_media.setImageBitmap(null);
        RelativeLayout rl_media = findViewById(R.id.rl_media);
        rl_media.setVisibility(View.VISIBLE);
    }

    /**
     * fügt das erstellte Übungsfeld dem Gesamtlayout(rl_exerciseField) hinzu
     *
     * @param exerciseLayout bearbeitetes Layout, das nun angezeigt werden soll
     */
    protected void addExerciseLayoutToField(View exerciseLayout) {
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        param.addRule(RelativeLayout.CENTER_HORIZONTAL);
        exerciseLayout.setLayoutParams(param);
        rl_exerciseField.addView(exerciseLayout, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * richtige Antwort wurde ausgewaehlt -> Erfolg festhalten
     */
    protected void onCorrectAnswerClicked(View view) {
        isCorrect = true;
        if (view != null) ((ImageView) view.getTag()).setVisibility(View.VISIBLE);
        setCurrentExerciseAsCorrectAnswered();
        updateEvaluationbar();
    }

    protected void setCurrentExerciseAsCorrectAnswered() {
        TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 1;
        String correctAnswerID = TrainingSingleton.getInstance().getCurrentExercise().getId();
        updateTrainingProgressBar(TrainingSingleton.getInstance().getCurrentExercise());
        datasource.deleteFromRepeatExercise(correctAnswerID);
    }

    /**
     * falsche Antwort - Ergebnis festhalten
     */
    protected void onWrongAnswerClicked(View view) {
        isCorrect = false;
        if (view != null) ((ImageView) view.getTag()).setVisibility(View.VISIBLE);
        if (view == null) isCorrect = null;
        setCurrentExerciseAsWrongAnswered();
        updateEvaluationbar();
    }

    protected void setCurrentExerciseAsWrongAnswered() {
        String wrongAnswerID = TrainingSingleton.getInstance().getCurrentExercise().getId();
        TrainingSingleton.getInstance().getCurrentExercise().answerstatus = 0;
        updateTrainingProgressBar(TrainingSingleton.getInstance().getCurrentExercise());

        if (loadSettings().getKindOfRepeat() == 3) {
            datasource.insertIntoRepeatExercise(wrongAnswerID);
        }
    }

    /**
     * aktualisiert die Auswertungsanzeige im Footer
     */
    protected void updateEvaluationbar() {
       /* LinearLayout llGameAnswrs = (LinearLayout) findViewById(R.id.ll_game_answers);
        llGameAnswrs.removeAllViews();
        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 10);
        for(Exercise exercise : TrainingSingleton.getInstance().getExerciseList()){
            //ImageView ivAnswer = new ImageView(this);
            View view = getLayoutInflater().inflate(R.layout.iv_gameprogress,llGameAnswrs, false);
            ImageView ivAnswer = (ImageView) view.findViewById(R.id.imageView);
            if (TrainingSingleton.getInstance().getWrongAnswers().contains(exercise.getId())){
                ivAnswer.setImageResource(R.drawable.ico_wrong);
            }else
            if (TrainingSingleton.getInstance().getCorrectAnswers().contains(exercise.getId())){
                ivAnswer.setImageResource(R.drawable.ico_right);
            } else {
               ivAnswer.setBackgroundColor(Color.GREEN);
            }
            ViewGroup.LayoutParams vgP = new ViewGroup.LayoutParams(15,15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(vgP);
            //params.width = 46;
            //params.height = 46;
            ivAnswer.setLayoutParams(params);
            llGameAnswrs.addView(view);
        }*/

        TextView tv_wrongAnswers = findViewById(R.id.tv_exercises_wrong);
        int wrong = TrainingSingleton.getInstance().getWrongAnswers().size();
        tv_wrongAnswers.setText(String.valueOf(wrong));

        TextView tv_correctAnswers = findViewById(R.id.tv_exercises_correct);
        int correct = TrainingSingleton.getInstance().getCorrectAnswers().size();
        tv_correctAnswers.setText(String.valueOf(correct));

        TextView tv_exercisesStatus = findViewById(R.id.tv_exercise_status);
        String exerciseStatus = String.valueOf(correct + wrong);
        if (!isTimelimit)
            exerciseStatus += "/" + TrainingSingleton.getInstance().getExerciseLimit();
        tv_exercisesStatus.setText(exerciseStatus); //  +"/"+TrainingSingleton.getInstance().getExerciseLimit()
    }

    /**
     * tries to find the questionMedia and loads it into the ImageView
     *
     * @param questionMedia Dateiname des Bild, das angezeigt werden soll
     */
    protected void loadQuestionMedia(String questionMedia) {


        Bitmap bitmap = Utils.decodeSampledBitmapFromPath(directory + "/" + questionMedia, rl_media.getLayoutParams().width, rl_media.getLayoutParams().height);

        iv_media.setImageBitmap(bitmap);
//        try {
//            InputStream ims = getAssets().open(questionMedia);
//            Drawable d = Drawable.createFromStream(ims, null);
//            iv_media.setImageDrawable(d);
//        }
//        catch(IOException ex) {
//            Toast.makeText(this, getString(R.string.error_could_not_load_question_media) + ex.getMessage(), Toast.LENGTH_LONG).show();
//        }
    }

    /**
     * tries to find a helpVideo and loads into the videoView
     *
     * @param helpvideo Dateiname des Videos, das geladen werden soll
     */
    protected void loadHelpVideo(String helpvideo, boolean startVideo) {


        File tmp = new File(directory + "/" + helpvideo);
        if (tmp.exists()) {

            if (!isVideoActivated && !TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exercisewordforvideo") && !TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforvideo")) {
                rl_videoField.setVisibility(View.INVISIBLE);
                return;
            }

            vv_helpVideo.setVideoURI(null);
            if (helpvideo == null) return;
            try {
                String urlPath = (directory + "/" + helpvideo);

                vv_helpVideo.setOnPreparedListener(preparedListener);
                vv_helpVideo.setVideoURI(Uri.parse(urlPath));
                if (!startVideo) {
                    iv_play.setVisibility(View.VISIBLE);
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(urlPath, MediaStore.Video.Thumbnails.MINI_KIND);
                    if (thumbnail == null) {
                        vv_helpVideo.seekTo(0); // wenn kein Vorschaubild, dann mit spulen versuchen
                    } else {
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), thumbnail);
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            vv_helpVideo.setBackgroundDrawable(bitmapDrawable);
                        } else {
                            vv_helpVideo.setBackground(bitmapDrawable);
                        }
                    }
                } else {
                    iv_play.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error_could_not_load_helpvideo) + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            //if(TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exercisewordforvideo") || TrainingSingleton.getInstance().getCurrentExercise().getExerciseModell().equalsIgnoreCase("exerciseimageforvideo"))
            if (startVideo) {


                vv_helpVideo.start();

            }
        }
    }


    /**
     * laedt die Einstellungen aus der Prefs.xml
     *
     * @return gibt ein vollstaendig geladenes Settingsobjekt zurueck oder null im Fehlerfall
     */
    protected Settings loadSettings() {
        Settings settings = new Settings(this);
        try {
            settings.loadFromPrefs();
            isSoundActivated = settings.isSoundActivated();
            isVideoActivated = settings.isVideohelpActivated();
            isTimelimit = settings.isTimeLimit();
            return settings;
        } catch (NoSuchFieldException e) {
            Toast.makeText(this, getString(R.string.error_could_not_load_settings), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_could_not_load_database), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training, menu);
        return true;
    }

    protected void clearImageviewsForRLayout(RelativeLayout layout) {

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child.getClass() == ImageView.class) {
                ImageView iv = (ImageView) child;
                iv.setImageDrawable(null);
            } else if (child.getClass() == LinearLayout.class) {
                LinearLayout lo = (LinearLayout) child;
                clearImageviewsForLLayout(lo);
            } else if (child.getClass() == RelativeLayout.class) {
                RelativeLayout lo = (RelativeLayout) child;
                clearImageviewsForRLayout(lo);
            }
        }
    }

    protected void clearImageviewsForLLayout(LinearLayout layout) {

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child.getClass() == ImageView.class) {
                ImageView iv = (ImageView) child;
                iv.setImageDrawable(null);
            } else if (child.getClass() == RelativeLayout.class) {
                RelativeLayout lo = (RelativeLayout) child;
                clearImageviewsForRLayout(lo);
            } else if (child.getClass() == LinearLayout.class) {
                LinearLayout lo = (LinearLayout) child;
                clearImageviewsForLLayout(lo);
            }
        }
    }


    // ONLY FOR PLUS VERSION
    private void setAnswerData() throws IOException {
        if (exerciseType.equalsIgnoreCase("exercisesortcharacters")) {
            String answerString = "";
            for (String s : givenAnswerList) {
                answerString += " " + s;
            }
            answerString = answerString.trim();
            currentAnswer.setAnswerTxt(answerString);
            if (TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer().equalsIgnoreCase(answerString)) {
                currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
                currentAnswer.setIsCorrect(isCorrect);
            } else {
                currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
                currentAnswer.setIsCorrect(isCorrect);
            }

        } else if (exerciseType.equalsIgnoreCase("exercisegapwordforimage")) {
            currentAnswer.setAnswerTxt(givenAnswer);
            if (givenAnswer.equalsIgnoreCase(TrainingSingleton.getInstance().getCurrentExercise().getCorrectAnswer())) {
                currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
                currentAnswer.setIsCorrect(isCorrect);
            } else {
                currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
                currentAnswer.setIsCorrect(isCorrect);
            }
        } else if (exerciseType.equalsIgnoreCase("exerciseimageforword")) {
            currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
            currentAnswer.setIsCorrect(isCorrect);
            AnswerDataSource aDS = new AnswerDataSource(this);
            aDS.open();
            currentAnswer.setAnswerID(aDS.getAnswerIdForValueAndExID(answerID, currentAnswer.getExerciseID()));
            aDS.close();

        } else if (exerciseType.equalsIgnoreCase("exercisewordforimage")) {
            currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
            currentAnswer.setIsCorrect(isCorrect);
            AnswerDataSource aDS = new AnswerDataSource(this);
            aDS.open();
            currentAnswer.setAnswerID(aDS.getAnswerIdForValueAndExID(answerID, currentAnswer.getExerciseID()));
            aDS.close();
        } else if (exerciseType.equalsIgnoreCase("exercisesortwords")) {
            currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
            currentAnswer.setIsCorrect(isCorrect);
            currentAnswer.setAnswerTxt(givenAnswer);
        } else if (exerciseType.equalsIgnoreCase("exercisegapsentenceforimage")) {
            currentAnswer.setSolved((isCorrect == null) ? false : isCorrect);
            currentAnswer.setIsCorrect(isCorrect);
            currentAnswer.setAnswerTxt(givenAnswer);
        } else {
            // do nothing - what has happened?
        }


        int durationsek = (int) (System.currentTimeMillis() - startTime) / 1000;
//        if(durationsek>60){
//            int minuten=durationsek/60;
//            int rest = durationsek-minuten*60;
//            if(rest>10){currentAnswer.setDuration(minuten+":"+rest);}
//            else{currentAnswer.setDuration(minuten+":0"+rest);}
//
//        }
//        else if(durationsek>10){
//            currentAnswer.setDuration("0:"+durationsek);
//        }
//        else{
//            currentAnswer.setDuration("0:0"+durationsek);
//        }
        currentAnswer.setDuration(String.valueOf(durationsek));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentAnswer.setAnswerTime(sdf.format(new Date()));
    }

    private void sendAnswer() {

        TaskSendAnswer taskSendAnswer = new TaskSendAnswer(activity, currentAnswer);
        taskSendAnswer.execute();
    }

    /**
     * timer fuer das zeitbasierte Training
     * http://android-developers.blogspot.kr/2007/11/stitch-in-time.html
     */
    protected class TimerTask extends Thread implements Runnable {
        public void run() {
            final long start = mStartTime;
            final long stop = mStopTime;
            String zeit_old = "";
            while (isRunning.get()) { //&& isRunning.get()
                long millis = 0;

                if (isTimelimit) {
                    millis = stop - SystemClock.uptimeMillis();
                    if (millis < 0) {
                        break;
                    }
                } else if (!isTimelimit) {
                    millis = SystemClock.uptimeMillis() - start;
                }


                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                String zeit;
                if (seconds < 10) {
                    zeit = "" + minutes + ":0" + seconds;
                } else {
                    zeit = "" + minutes + ":" + seconds;
                }

                if (!zeit.equalsIgnoreCase(zeit_old)) {
                    Message message = mHandler.obtainMessage();
                    message.obj = zeit;
                    mHandler.sendMessage(message);
                    zeit_old = zeit;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isRunning.get())
                        finishTraining();
                }
            });

        }
    }
}
