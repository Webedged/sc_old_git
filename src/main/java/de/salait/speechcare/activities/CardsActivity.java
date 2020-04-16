package de.salait.speechcare.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.data.Answer;
import de.salait.speechcare.data.ImageCard;
import de.salait.speechcare.data.ImageCardExercise;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.TrainingSingleton;
import de.salait.speechcare.tasks.TaskSendAnswer;
import de.salait.speechcare.utility.ButtonHighlighterOnTouchListener;
import de.salait.speechcare.utility.Utils;


public class CardsActivity extends Activity {


    protected final static long SECOND = 60;
    protected final static long MILLISECOND = 1000;
    protected final Activity activity = this;
    protected File directory;
    protected boolean isSoundActivated;
    protected ProgressDialog dialog;
    protected ExerciseDataSource datasource;
    protected List<ImageCard> imageCardList;
    protected ImageView cardiv1_1;
    protected ImageView cardiv2_1;
    protected ImageView cardiv2_2;
    protected ImageView cardiv4_1;
    protected ImageView cardiv4_2;
    protected ImageView cardiv4_3;
    protected ImageView cardiv4_4;
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

    protected RelativeLayout rl_trainingField;

    protected Typeface tfLTElight;
    protected ImageButton btn_nextExercise_R;
    protected ImageButton btn_nextExercise_F;

    protected ImageButton btn_previousExercise;

    protected LinearLayout progressView;
    protected boolean isTimelimit;
    protected Point displaySize;
    protected float density;
    /**
     * gibt an, ob Wiedervorlagenlauf oder neues Training
     */
    protected boolean isRepetition;

    /**
     * Liste aller Antwortfelder
     */
    /**
     * vom Benutzer gegebene Antwort
     */
    protected Boolean isCorrect;
    protected String answerID;


    // PLUS VERSION VALUES

    private Answer currentAnswer;
    private String userid;
    private long startTime;
    private String exerciseType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardtraining);

        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(displaySize);
        } else {
            display.getSize(displaySize);
        }


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


        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        density = getResources().getDisplayMetrics().density;
        //System.out.println(">>> density "+ String.valueOf(density));

        tfLTElight = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        LinearLayout backG = (LinearLayout) findViewById(R.id.cardBack);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);

        ContextWrapper c = new ContextWrapper(this);
        directory = c.getFilesDir();
        isRepetition = getIntent().getBooleanExtra("repetition", false);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setTypeface(tfLTElight);
        tvTitle.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG);
        Button btnHome = (Button) findViewById(R.id.btn_home);
        btnHome.setOnTouchListener(new ButtonHighlighterOnTouchListener(btnHome));

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHomePressed();
            }
        });

        tv_timer = (TextView) findViewById(R.id.tv_timer);
        rl_exerciseField = (RelativeLayout) findViewById(R.id.rl_exercise_field);

        rl_trainingField = (RelativeLayout) findViewById(R.id.ll_training);

        progressView = (LinearLayout) findViewById(R.id.trainingProgressView);

        btn_nextExercise_R = (ImageButton) findViewById(R.id.btn_next_right);
        btn_nextExercise_R.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_nextExercise_R));
        btn_nextExercise_R.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadNextExercise(true);
            }
        });
        btn_nextExercise_F = (ImageButton) findViewById(R.id.btn_next_false);
        btn_nextExercise_F.setOnTouchListener(new ButtonHighlighterOnTouchListener(btn_nextExercise_F));
        btn_nextExercise_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundPageFlip();
                loadNextExercise(false);
            }
        });

        btn_previousExercise = (ImageButton) findViewById(R.id.btn_previous_exercise);
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


        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) rl_trainingField.getLayoutParams();
        p.height = displaySize.y - 90 - 30;
        p.width = displaySize.x;
        rl_trainingField.setLayoutParams(p);

        RelativeLayout.LayoutParams e = (RelativeLayout.LayoutParams) rl_exerciseField.getLayoutParams();

        rl_exerciseField.setLayoutParams(e);


        HorizontalScrollView progressScrollView = (HorizontalScrollView) findViewById(R.id.progressScrollView);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) progressScrollView.getLayoutParams();

        progressScrollView.setLayoutParams(lp);


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

    /**
     * verkleinert oder vergroessert das Video
     */

    /************************************************/
    /** Allgemeine Methoden fuer den Trainingslauf **/
    /************************************************/

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
     */

    protected void updateTrainingProgressBar(ImageCardExercise ex) {

        HorizontalScrollView progressScrollView = (HorizontalScrollView) findViewById(R.id.progressScrollView);

        int index = TrainingSingleton.getInstance().cardList.indexOf(ex);

        if (progressView.getChildCount() == 0) return;

        LinearLayout ll = (LinearLayout) progressView.getChildAt(index);

        ImageView aI = (ImageView) ll.getChildAt(0);

        if (ex.getUserRelation() == ImageCardExercise.RELATION_WRONG) {            // falsche Antwort
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.ico_wrong, (int) (19 * density), (int) (19 * density));
            aI.setImageBitmap(bm);
            if (bm != null) {
                // bm.recycle();
                bm = null;
            }
            ll.setBackgroundColor(Color.DKGRAY);
        } else if (ex.getUserRelation() == ImageCardExercise.RELATION_CORRECT) {     // korrekte Antwort
            Bitmap bm = Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.ico_right, (int) (19 * density), (int) (19 * density));
            aI.setImageBitmap(bm);
            if (bm != null) {
                //  bm.recycle();
                bm = null;
            }
            ll.setBackgroundColor(Color.DKGRAY);
        } else if (ex.getUserRelation() == ImageCardExercise.NO_RELATION) {          // noch keine Antwort
            aI.setImageBitmap(null);
            if (index == TrainingSingleton.getInstance().getExerciseIndex()) {
                ll.setBackgroundColor(Color.BLUE);
            } else {
                ll.setBackgroundColor(Color.DKGRAY);
            }
        }

        if (index != TrainingSingleton.getInstance().cardList.size() - 1) {
            LinearLayout ll2 = (LinearLayout) progressView.getChildAt(index + 1);
            ll2.setBackgroundColor(Color.DKGRAY);
        }

        if (index > 3) {
            progressScrollView.scrollTo((index - 3) * 20, 0);
        }
    }

    @SuppressLint("ResourceAsColor")
    protected void createTrainingProgressBar() {

        HorizontalScrollView progressScrollView = (HorizontalScrollView) findViewById(R.id.progressScrollView);

        if (progressView.getChildCount() > 0)
            progressView.removeAllViews();

        for (int i = 0; i < TrainingSingleton.getInstance().cardList.size(); i++) {


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

    protected void initCardImageViews() {


        cardiv1_1 = (ImageView) findViewById(R.id.iv_card1);
        cardiv2_1 = (ImageView) findViewById(R.id.iv_card2_1);
        cardiv2_2 = (ImageView) findViewById(R.id.iv_card2_2);
        cardiv4_1 = (ImageView) findViewById(R.id.iv_card4_1);
        cardiv4_2 = (ImageView) findViewById(R.id.iv_card4_2);
        cardiv4_3 = (ImageView) findViewById(R.id.iv_card4_3);
        cardiv4_4 = (ImageView) findViewById(R.id.iv_card4_4);

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


        isRunning.set(false);
        startEvaluation();
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


           /* if(getResources().getBoolean(R.bool.isPlusVersion)==true){

                exerciseList = datasource.getAllRepeatExercises(userid);
            }
            else {
                exerciseList = datasource.getAllRepeatExercises();
            }*/

            TrainingSingleton.getInstance().createImageCardTrainingForRepeatModus(settings, datasource.getImageCardExercisesForRepeatModus(settings.getUserid()));
            initCardImageViews();
            loadExercise(TrainingSingleton.getInstance().getFirstCardExercise());
            createTrainingProgressBar();


        } else {

            String cardTpys = "(";
            for (String cardstring : settings.allowedExerciseTypes) {
                cardTpys += cardstring + ",";
            }

            cardTpys = cardTpys.substring(0, cardTpys.length() - 1) + ")";

            if (getResources().getBoolean(R.bool.isPlusVersion) == true) {

                // exerciseList = datasource.getAllExercisesOfDifficultyForUser(settings.getDifficultyLevel(), exercisetypes,userid);
            } else {

                imageCardList = datasource.getAllCardsForBildart(settings.getBildArt().toLowerCase(), cardTpys);
            }
            if (imageCardList.size() >= settings.getCardCountForExercise()) {
                // Trainingsverlauf initiieren
                TrainingSingleton.getInstance().createImageCardTraining(settings, imageCardList);
                initCardImageViews();
                loadExercise(TrainingSingleton.getInstance().getFirstCardExercise());
                createTrainingProgressBar();
            } else {
                finish();
            }

        }

    }

    /**
     * springt zur naechsten uebung
     */
    protected void loadNextExercise(Boolean isright) {

       /*if(getResources().getBoolean(R.bool.isPlusVersion)==true){
            try {
                setAnswerData();
                sendAnswer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        if (!isright) {
            TrainingSingleton.getInstance().getCurrentCardExercise().setUserRelation(ImageCardExercise.RELATION_WRONG);
            datasource.saveRepeatMediaCards(TrainingSingleton.getInstance().getCurrentCardExercise().getImageCards(), userid);

        } else {
            if (isRepetition) {
                datasource.deleteRepeatMediaCards(TrainingSingleton.getInstance().getCurrentCardExercise().getImageCards(), userid);
            }
            TrainingSingleton.getInstance().getCurrentCardExercise().setUserRelation(ImageCardExercise.RELATION_CORRECT);
        }
        updateTrainingProgressBar(TrainingSingleton.getInstance().getCurrentCardExercise());
        updateEvaluationbar();
        loadExercise(TrainingSingleton.getInstance().getNextCards());
    }

    /**
     * springt zur vorhergehenden uebung
     */
    protected void loadPreviousExercise() {


        TrainingSingleton.getInstance().getCurrentCardExercise().setUserRelation(ImageCardExercise.NO_RELATION);

        updateTrainingProgressBar(TrainingSingleton.getInstance().getCurrentCardExercise());
        updateEvaluationbar();
        ImageCardExercise prevCardExercise = TrainingSingleton.getInstance().getPreviousCardExercise();


        if (prevCardExercise == null) return;
        loadExercise(prevCardExercise);
    }

    protected void loadExercise(ImageCardExercise cards) {

        if (cards == null) {
            System.out.println(" <<< cards == null ");
            finishTraining();
            return;
        }

        cardiv1_1.setVisibility(View.GONE);
        cardiv2_1.setVisibility(View.GONE);
        cardiv2_2.setVisibility(View.GONE);
        cardiv4_1.setVisibility(View.GONE);
        cardiv4_2.setVisibility(View.GONE);
        cardiv4_3.setVisibility(View.GONE);
        cardiv4_4.setVisibility(View.GONE);

        System.out.println(" <<< FirstExercise CardCount " + cards.getImageCards().size());


        switch (cards.getImageCards().size()) {
            case 1:
                File c1 = new File(directory + "/" + cards.getImageCards().get(0).getMediapath());
                if (c1.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(c1.getAbsolutePath());
                    cardiv1_1.setImageBitmap(myBitmap);
                    cardiv1_1.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                File c2_1 = new File(directory + "/" + cards.getImageCards().get(0).getMediapath());
                if (c2_1.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(c2_1.getAbsolutePath());
                    cardiv2_1.setImageBitmap(myBitmap);
                    cardiv2_1.setVisibility(View.VISIBLE);
                }
                File c2_2 = new File(directory + "/" + cards.getImageCards().get(1).getMediapath());
                if (c2_2.exists()) {
                    Bitmap myBitmap2 = BitmapFactory.decodeFile(c2_2.getAbsolutePath());
                    cardiv2_2.setImageBitmap(myBitmap2);
                    cardiv2_2.setVisibility(View.VISIBLE);
                }
                break;
            case 4:
                File c4_1 = new File(directory + "/" + cards.getImageCards().get(0).getMediapath());
                if (c4_1.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(c4_1.getAbsolutePath());
                    cardiv4_1.setImageBitmap(myBitmap);
                    cardiv4_1.setVisibility(View.VISIBLE);
                }
                File c4_2 = new File(directory + "/" + cards.getImageCards().get(1).getMediapath());
                if (c4_2.exists()) {
                    Bitmap myBitmap2 = BitmapFactory.decodeFile(c4_2.getAbsolutePath());
                    cardiv4_2.setImageBitmap(myBitmap2);
                    cardiv4_2.setVisibility(View.VISIBLE);
                }
                File c4_3 = new File(directory + "/" + cards.getImageCards().get(2).getMediapath());
                if (c4_3.exists()) {
                    Bitmap myBitmap3 = BitmapFactory.decodeFile(c4_3.getAbsolutePath());
                    cardiv4_3.setImageBitmap(myBitmap3);
                    cardiv4_3.setVisibility(View.VISIBLE);
                }
                File c4_4 = new File(directory + "/" + cards.getImageCards().get(3).getMediapath());
                if (c4_4.exists()) {
                    Bitmap myBitmap4 = BitmapFactory.decodeFile(c4_4.getAbsolutePath());
                    cardiv4_4.setImageBitmap(myBitmap4);
                    cardiv4_4.setVisibility(View.VISIBLE);
                }
                break;
            default:
                System.out.println("<<< loadExercise CardCount Error cardcaount = " + cards.getImageCards().size());
                break;
        }


        updateEvaluationbar();
        updateTrainingProgressBar(cards);

        btn_previousExercise.bringToFront();
        btn_nextExercise_R.bringToFront();
        btn_nextExercise_F.bringToFront();
        // Zurueck-Button ausblenden, wenn erste Uebung
        if (TrainingSingleton.getInstance().getExerciseIndex() == 0) {
            btn_previousExercise.setVisibility(View.INVISIBLE);
        } else {
            btn_previousExercise.setVisibility(View.VISIBLE);
        }
    }

    /**
     * aktualisiert die Auswertungsanzeige im Footer
     */
    protected void updateEvaluationbar() {

        TextView tv_wrongAnswers = (TextView) findViewById(R.id.tv_exercises_wrong);
        int wrong = TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_WRONG);
        tv_wrongAnswers.setText(String.valueOf(wrong));

        TextView tv_correctAnswers = (TextView) findViewById(R.id.tv_exercises_correct);
        int correct = TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT);
        tv_correctAnswers.setText(String.valueOf(correct));

        TextView tv_exercisesStatus = (TextView) findViewById(R.id.tv_exercise_status);
        String exerciseStatus = String.valueOf(correct + wrong);
        if (!isTimelimit)
            exerciseStatus += "/" + TrainingSingleton.getInstance().getCardExerciseLimit();
        tv_exercisesStatus.setText(exerciseStatus); //  +"/"+TrainingSingleton.getInstance().getExerciseLimit()
    }


    /**     allgemeine Methoden für Übungen **/

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

    // ONLY FOR PLUS VERSION
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
                    message.obj = String.valueOf(zeit);
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
