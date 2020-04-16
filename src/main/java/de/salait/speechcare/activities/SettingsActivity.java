package de.salait.speechcare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.AnswerDataSource;
import de.salait.speechcare.dao.AppDataSource;
import de.salait.speechcare.dao.CollectionDataSource;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.dao.MediaCollectionDataSource;
import de.salait.speechcare.dao.MediaDataSource;
import de.salait.speechcare.dao.PackageDataSource;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.utility.DifferentiateCardHandler;

public class SettingsActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final Activity activity = this;

    protected LinearLayout videoHelpLayout;
    protected LinearLayout bildartLayout;

    protected EditText etEmail;

    protected Button btnEasy;
    protected Button btnMedium;
    protected Button btnHard;

    protected Button btnKeine;
    protected Button btnMitWv;
    protected Button btnOhneWv;

    protected Button btnWiedervorlage;
    protected Button btnReset;
    protected Button btnUebersicht;
    protected Button btnHome;
    protected Button btnFoto;
    protected Button btnIllustration;

    protected Switch switchAppsounds;
    protected Switch switchVideohilfe;
    protected Switch switchZeitlimit;
    protected Switch switchAufgabenlimit;
    protected Switch switchRandomType;

    protected SeekBar seekBarZeitlimit;
    protected SeekBar seekBarAufgabenlimit;

    protected int maxExercises;

    protected TextView tvCountWiedervorlage;
    protected TextView textViewzahlZeitlimit;
    protected TextView textViewZahlAufgabenlimit;
    protected TextView tvEinstellungen;
    protected TextView tvEmail;
    protected TextView tvAufgabenwv;
    protected TextView tvAppsounds;
    protected TextView tvVideohilfe;
    protected TextView tvSchwierigkeitsgrad;
    protected TextView tvArtDerWiederholung;
    protected TextView tvTeitlimit;
    protected TextView tvAufgabenlimit;
    protected TextView tvZufalltypenauswahl;
    protected TextView tvReset;
    protected TextView tvbildart;

    protected List<String> typeList;
    protected ArrayList<TextView> countLblArray;
    protected Settings settings;
    protected Typeface font;
    protected boolean allowedToChange;
    DifferentiateCardHandler differentiateCardHandler;
    private ArrayList<Switch> switchArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                allowedToChange = true;
            } else {
                allowedToChange = extras.getBoolean("allowedToChange");
            }
        } else {
            allowedToChange = true;
        }

        differentiateCardHandler = new DifferentiateCardHandler();

        settings = loadSettings();

        font = Typeface.createFromAsset(getAssets(), "ltelight.ttf");

        Point displaySize;
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LinearLayout backG = (LinearLayout) findViewById(R.id.settingsBack);
        // Drawable d = new BitmapDrawable(getResources(), Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.hintergrund_blank, displaySize.x, displaySize.y));
        // backG.setBackground(d);
        backG.setBackgroundResource(R.drawable.hintergrund_blank);


        videoHelpLayout = (LinearLayout) findViewById(R.id.videoHelpLaypot);
        bildartLayout = (LinearLayout) findViewById(R.id.bildArtLaypot);

        tvEinstellungen = (TextView) findViewById(R.id.textVieweinstellungen);
        tvEmail = (TextView) findViewById(R.id.textViewemail);
        tvAufgabenwv = (TextView) findViewById(R.id.textViewaufgabenwv);
        tvAppsounds = (TextView) findViewById(R.id.textViewappsounds);
        tvVideohilfe = (TextView) findViewById(R.id.textViewvideohilfe);
        tvSchwierigkeitsgrad = (TextView) findViewById(R.id.textViewschwierigkeitsgrad);
        tvArtDerWiederholung = (TextView) findViewById(R.id.textViewwiederholung);
        tvTeitlimit = (TextView) findViewById(R.id.textViewzeitlimit);
        tvAufgabenlimit = (TextView) findViewById(R.id.textViewaufgabenlimit);
        tvZufalltypenauswahl = (TextView) findViewById(R.id.textViewzufalltypenauswahl);
        tvReset = (TextView) findViewById(R.id.textViewreset);
        tvCountWiedervorlage = (TextView) findViewById(R.id.textViewzahlaufgabenwiedervorlage);
        tvbildart = (TextView) findViewById(R.id.textViewbildart);

        btnHome = (Button) findViewById(R.id.button_home);
        btnWiedervorlage = (Button) findViewById(R.id.buttonwiedervorlage);
        btnEasy = (Button) findViewById(R.id.buttoneinfach);
        btnMedium = (Button) findViewById(R.id.buttonmittel);
        btnHard = (Button) findViewById(R.id.buttonschwierig);
        btnKeine = (Button) findViewById(R.id.button_kein);
        btnMitWv = (Button) findViewById(R.id.button_mitWiedervorlage);
        btnOhneWv = (Button) findViewById(R.id.button_ohneWiedervorlage);
        btnReset = (Button) findViewById(R.id.buttonreset);
        btnUebersicht = (Button) findViewById(R.id.buttonuebersicht);
        btnFoto = (Button) findViewById(R.id.btnFoto);
        btnIllustration = (Button) findViewById(R.id.btnIllustration);

        etEmail = (EditText) findViewById(R.id.editTextemail);
        etEmail.setText(settings.getEmailadressForResults());

        switchAppsounds = (Switch) findViewById(R.id.switch_appsounds);
        switchVideohilfe = (Switch) findViewById(R.id.switch_videohilfe);

        tvEinstellungen.setTypeface(font);
        tvEmail.setTypeface(font);
        tvAufgabenwv.setTypeface(font);
        tvAppsounds.setTypeface(font);
        tvVideohilfe.setTypeface(font);
        tvSchwierigkeitsgrad.setTypeface(font);
        tvArtDerWiederholung.setTypeface(font);
        tvTeitlimit.setTypeface(font);
        tvAufgabenlimit.setTypeface(font);
        tvZufalltypenauswahl.setTypeface(font);
        tvReset.setTypeface(font);
        tvbildart.setTypeface(font);
        btnWiedervorlage.setTypeface(font);
        btnReset.setTypeface(font);
        btnEasy.setTypeface(font);
        btnMedium.setTypeface(font);
        btnHard.setTypeface(font);
        btnKeine.setTypeface(font);
        btnOhneWv.setTypeface(font);
        btnMitWv.setTypeface(font);
        btnUebersicht.setTypeface(font);
        btnFoto.setTypeface(font);
        btnIllustration.setTypeface(font);

        tvEinstellungen.setPaintFlags(tvEinstellungen.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvEmail.setPaintFlags(tvEmail.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvAufgabenwv.setPaintFlags(tvAufgabenwv.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvAppsounds.setPaintFlags(tvAppsounds.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvVideohilfe.setPaintFlags(tvVideohilfe.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvSchwierigkeitsgrad.setPaintFlags(tvSchwierigkeitsgrad.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvArtDerWiederholung.setPaintFlags(tvArtDerWiederholung.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvTeitlimit.setPaintFlags(tvTeitlimit.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvAufgabenlimit.setPaintFlags(tvAufgabenlimit.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvZufalltypenauswahl.setPaintFlags(tvZufalltypenauswahl.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvReset.setPaintFlags(tvReset.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        tvbildart.setPaintFlags(tvbildart.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnWiedervorlage.setPaintFlags(btnWiedervorlage.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnReset.setPaintFlags(btnReset.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnEasy.setPaintFlags(btnEasy.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnMedium.setPaintFlags(btnMedium.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnHard.setPaintFlags(btnHard.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnKeine.setPaintFlags(btnKeine.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnMitWv.setPaintFlags(btnMitWv.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnOhneWv.setPaintFlags(btnOhneWv.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnUebersicht.setPaintFlags(btnUebersicht.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnFoto.setPaintFlags(btnFoto.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        btnIllustration.setPaintFlags(btnIllustration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        //ZeitLimit
        switchZeitlimit = (Switch) findViewById(R.id.switch_zeitlimit);
        textViewzahlZeitlimit = (TextView) findViewById(R.id.textViewzahlzeitlimit);
        seekBarZeitlimit = (SeekBar) findViewById(R.id.seekBarzeitlimit);
        //AufgabenLimit
        switchAufgabenlimit = (Switch) findViewById(R.id.switch_aufgabenlimit);
        textViewZahlAufgabenlimit = (TextView) findViewById(R.id.textViewzahlaufgabenlimit);
        seekBarAufgabenlimit = (SeekBar) findViewById(R.id.seekBaraufgabenlimit);
        seekBarAufgabenlimit.setEnabled(false);
        //AufgabenSwitche
        switchRandomType = (Switch) findViewById(R.id.switch_typenauswahl);

        seekBarZeitlimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            //SeekBarZeitLimit Methode
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress = 1;
                textViewzahlZeitlimit.setText(String.valueOf(progress));
                settings.setTimelimit(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarAufgabenlimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //SeekBarAufgabenLimit Methoden
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress = 1;
                textViewZahlAufgabenlimit.setText(String.valueOf(progress));
                settings.setExerciseLimit(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnHome.setOnClickListener(this);
        btnWiedervorlage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                truncateRepeatExercise();
                setRepeatTextView();
            }
        });
        btnUebersicht.setOnClickListener(this);
        btnReset.setOnClickListener(this);


        if (allowedToChange == true) {
            btnEasy.setOnClickListener(this);
            btnMedium.setOnClickListener(this);
            btnHard.setOnClickListener(this);

            btnKeine.setOnClickListener(this);
            btnMitWv.setOnClickListener(this);
            btnOhneWv.setOnClickListener(this);

            switchAppsounds.setOnCheckedChangeListener(this);
            switchVideohilfe.setOnCheckedChangeListener(this);
            switchZeitlimit.setOnCheckedChangeListener(this);
            switchAufgabenlimit.setOnCheckedChangeListener(this);
            switchRandomType.setOnCheckedChangeListener(this);
        } else {
            switchAppsounds.setEnabled(false);
            switchVideohilfe.setEnabled(false);
            switchZeitlimit.setEnabled(false);
            switchAufgabenlimit.setEnabled(false);
            switchRandomType.setEnabled(false);
            seekBarAufgabenlimit.setEnabled(false);
            seekBarZeitlimit.setEnabled(false);
        }


        typeList = new ArrayList<String>();
        countLblArray = new ArrayList<TextView>();
        switchArray = new ArrayList<Switch>();

        if (getResources().getBoolean(R.bool.isCardApp) == true) {

            videoHelpLayout.setVisibility(View.GONE);
            tvSchwierigkeitsgrad.setText("Bilderanzahl");

            btnEasy.setText("1");
            btnMedium.setText("2");
            btnHard.setText("4");
            setBildArt(settings.getBildArt());
            btnFoto.setOnClickListener(this);
            btnIllustration.setOnClickListener(this);

        } else {
            bildartLayout.setVisibility(View.GONE);
        }

        if (settings.getDifficultyLevel().equalsIgnoreCase("easy")) {
            btnEasy.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
            btnMedium.setBackgroundColor(getResources().getColor(R.color.grey));
            btnHard.setBackgroundColor(getResources().getColor(R.color.grey));
        } else if (settings.getDifficultyLevel().equalsIgnoreCase("medium")) {
            btnEasy.setBackgroundColor(getResources().getColor(R.color.grey));
            btnMedium.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
            btnHard.setBackgroundColor(getResources().getColor(R.color.grey));
        } else if (settings.getDifficultyLevel().equalsIgnoreCase("hard")) {
            btnEasy.setBackgroundColor(getResources().getColor(R.color.grey));
            btnMedium.setBackgroundColor(getResources().getColor(R.color.grey));
            btnHard.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
        }
        switch (settings.getKindOfRepeat()) {
            case 1:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.grey));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            case 2:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.grey));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            case 3:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.grey));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.grey));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                break;
            default:
                break;
        }

        try {

            typeList = differentiateCardHandler.getExerciseTypes(this, settings.getBildArt());

        } catch (IOException e) {
            e.printStackTrace();
        }


        seekBarZeitlimit.setProgress(settings.getTimelimit());
        seekBarAufgabenlimit.setProgress(settings.getExerciseLimit());

        switchAppsounds.setChecked(settings.isSoundActivated());
        switchVideohilfe.setChecked(settings.isVideohelpActivated());
        switchZeitlimit.setChecked(settings.isTimeLimit());
        switchAufgabenlimit.setChecked(settings.isExerciseLimit());


        setRepeatTextView();
        createTypeSwitchLayout();

        switchRandomType.setChecked(settings.isRandomTypeActivated());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settings == null) {
            settings = loadSettings();
        }
        if (differentiateCardHandler == null) {
            differentiateCardHandler = new DifferentiateCardHandler();
        }

        etEmail = (EditText) findViewById(R.id.editTextemail);
        etEmail.setText(settings.getEmailadressForResults());
        setAufgabenLimitMax();

        if (allowedToChange == false) {
            etEmail.setInputType(InputType.TYPE_NULL);
        } else {
            etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }

    /**
     * erstellt das Layout für die zu ladenden AufgabenIDs
     */
    private void createTypeSwitchLayout() {


        LinearLayout layout = (LinearLayout) findViewById(R.id.exercisetype_mainlayout);
        if (layout.getChildCount() > 0) {

            layout.removeAllViews();
        }

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);

        LayoutParams extypTVParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        extypTVParam.setMargins(5, 0, 0, 0);
        extypTVParam.weight = 5;

        LayoutParams countTVParam = new LayoutParams(60, LayoutParams.WRAP_CONTENT);

        LayoutParams switchParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


        maxExercises = 0;
        for (int i = 0, size = typeList.size(); i < size; i++) {
            String type = differentiateCardHandler.getTypeNameForID(this, typeList.get(i));
            int count = differentiateCardHandler.getCountForExTypeWithID(this, typeList.get(i), settings.getDifficultyLevel());

            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            l.setLayoutParams(params);

            TextView extypTV = new TextView(this);
            extypTV.setText(type);
            extypTV.setTextSize(20); // getResources().getDimension(R.dimen.fontsize_20)
            extypTV.setTypeface(font);
            extypTV.setGravity(Gravity.CENTER_VERTICAL);
            extypTV.setPaintFlags(extypTV.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

            TextView extypCount = new TextView(this);

            extypCount.setText(String.valueOf(count));
            extypCount.setTag(typeList.get(i));
            extypCount.setTextSize(20); // getResources().getDimension(R.dimen.fontsize_20)
            extypCount.setTypeface(font);
            extypCount.setGravity(Gravity.CENTER_VERTICAL);
            extypCount.setPaintFlags(extypCount.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            maxExercises = maxExercises + count;

            countLblArray.add(extypCount);


            Switch enabledSwitch = new Switch(this);
            enabledSwitch.setTag(typeList.get(i));
            enabledSwitch.setChecked(settings.isExerciseTypeEnabled(typeList.get(i)));
            enabledSwitch.setOnCheckedChangeListener(this);
            if (settings.isRandomTypeActivated()) {
                enabledSwitch.setEnabled(false);
            }

            switchArray.add(enabledSwitch);

            if (allowedToChange == false) {
                enabledSwitch.setEnabled(false);
            }

            layout.addView(l, params);
            l.addView(extypTV, extypTVParam);
            l.addView(extypCount, countTVParam);
            l.addView(enabledSwitch, switchParams);

        }

        maxExercises = differentiateCardHandler.getMaxValue(this, maxExercises, settings.getCardCountForExercise());

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttoneinfach:
                try {
                    setDifficultylevelEasy();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonmittel:
                try {
                    setDifficultylevelMedium();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonschwierig:
                try {
                    setDifficultylevelHard();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonuebersicht:
                onHomePressed();
                break;
            case R.id.button_home:
                onHomePressed();
                break;
            case R.id.buttonreset:
                showDeleteTrainingDataDialog();
                break;
            case R.id.btnFoto:
                settings.setPrefsBildart("Foto");
                setBildArt("Foto");
                break;
            case R.id.btnIllustration:
                settings.setPrefsBildart("Illustration");
                setBildArt("Illustration");
                break;
            case R.id.button_kein:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.grey));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.grey));
                settings.setKindOfRepeat(1);
                break;
            case R.id.button_ohneWiedervorlage:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.grey));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.grey));
                settings.setKindOfRepeat(2);
                break;
            case R.id.button_mitWiedervorlage:
                btnKeine.setBackgroundColor(getResources().getColor(R.color.grey));
                btnOhneWv.setBackgroundColor(getResources().getColor(R.color.grey));
                btnMitWv.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
                settings.setKindOfRepeat(3);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        settings.setEmailadressForResults(String.valueOf(etEmail.getText()));
        settings.saveToPrefs();
      /*  Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
        finish();
    }

    private void onHomePressed() {
        onBackPressed();
    }


    //Button clicks easy/medium/hard methode
    private void setDifficultylevelEasy() throws NoSuchFieldException {

        btnEasy.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
        btnMedium.setBackgroundColor(getResources().getColor(R.color.grey));
        btnHard.setBackgroundColor(getResources().getColor(R.color.grey));
        settings.setDifficultyLevel("easy");
        setTypeCountLbl();
        setAufgabenLimitMax();
    }

    private void setDifficultylevelMedium() throws NoSuchFieldException {
        btnEasy.setBackgroundColor(getResources().getColor(R.color.grey));
        btnMedium.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
        btnHard.setBackgroundColor(getResources().getColor(R.color.grey));
        settings.setDifficultyLevel("medium");
        setTypeCountLbl();
        setAufgabenLimitMax();
    }

    private void setDifficultylevelHard() throws NoSuchFieldException {
        btnEasy.setBackgroundColor(getResources().getColor(R.color.grey));
        btnMedium.setBackgroundColor(getResources().getColor(R.color.grey));
        btnHard.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
        settings.setDifficultyLevel("hard");
        setTypeCountLbl();
        setAufgabenLimitMax();
    }

    //Button clicks BildArt
    private void setBildArt(String bildArt) {
        if (bildArt.equalsIgnoreCase("Foto")) {
            btnFoto.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
            btnIllustration.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            btnFoto.setBackgroundColor(getResources().getColor(R.color.grey));
            btnIllustration.setBackgroundColor(getResources().getColor(R.color.fontcolor_lightblue));
        }

        countLblArray.clear();


        MediaCollectionDataSource mcDS = null;
        try {
            mcDS = new MediaCollectionDataSource(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mcDS.open();
        typeList = mcDS.getAvailableCardtypes(settings.getBildArt().toLowerCase());
        mcDS.close();
        createTypeSwitchLayout();

    }

    //SwitchMethode
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.switch_zeitlimit:
                switchStateTimelimit(isChecked);
                break;
            case R.id.switch_aufgabenlimit:
                switchStateExerciselimit(isChecked);
                break;
            case R.id.switch_typenauswahl:
                setTypenauswahl(isChecked);
                break;
            case R.id.switch_appsounds:
                settings.setSoundActivated(isChecked);
                break;
            case R.id.switch_videohilfe:
                settings.setVideohelpActivated(isChecked);
                break;
            default:
                if (compoundButton.getTag() != null) {
                    settings.setExerciseTypeEnabled(String.valueOf(compoundButton.getTag()), isChecked);
                    if (!isChecked) {                    // button wurde ausgestellt, also checken wir mal, ob alle anderen auch aus sind
                        switchAllExerciseSwitchesOn(compoundButton);
                    }
                    setAufgabenLimitMax();
                }
                break;
        }
    }

    private void switchAllExerciseSwitchesOn(CompoundButton compoundButton) {
        boolean isOneButtonChecked = false;
        for (Switch aSwitch : switchArray) {
            isOneButtonChecked |= aSwitch.isChecked();
        }
        if (isOneButtonChecked) return;
        // alle anderen sind aus, also muessen wir alle anstellen
        for (Switch aSwitch : switchArray) {
            aSwitch.setChecked(true);
            settings.setExerciseTypeEnabled(String.valueOf(compoundButton.getTag()), true);
        }
        switchRandomType.setChecked(true);
        compoundButton.setChecked(true);
    }


    private void switchStateTimelimit(boolean isChecked) {
        settings.setTimeLimit(isChecked);
        if (isChecked) {
            switchAufgabenlimit.setChecked(false);
            seekBarAufgabenlimit.setEnabled(false);
            textViewZahlAufgabenlimit.setEnabled(false);
            textViewzahlZeitlimit.setEnabled(true);
            seekBarZeitlimit.setEnabled(true);
        } else {
            switchAufgabenlimit.setChecked(true);
        }
    }

    private void switchStateExerciselimit(boolean isChecked) {
        settings.setIsExerciseLimit(isChecked);
        if (isChecked) {
            switchZeitlimit.setChecked(false);
            seekBarZeitlimit.setEnabled(false);
            textViewzahlZeitlimit.setEnabled(false);
            seekBarAufgabenlimit.setEnabled(true);
            textViewZahlAufgabenlimit.setEnabled(true);
            setAufgabenLimitMax();
        } else {
            switchZeitlimit.setChecked(true);
        }
    }


    private void setTypenauswahl(boolean isChecked) {
        settings.setRandomTypeActivated(isChecked);
        enabledTypesSwitches(!isChecked);
    }

    private void enabledTypesSwitches(boolean enabled) {
        for (Switch s : switchArray) {
            s.setEnabled(enabled);
        }
    }


    private void setTypeCountLbl() {
        if (!getResources().getBoolean(R.bool.isCardApp)) {
            ExerciseDataSource exerciseDataSource = null;
            try {
                exerciseDataSource = new ExerciseDataSource(this);
                exerciseDataSource.open();

                for (int i = 0; i < countLblArray.size(); i++) {
                    TextView tv = countLblArray.get(i);
                    tv.setText(String.valueOf(exerciseDataSource.countExerciseTypeOfDifficulty(settings.getDifficultyLevel(), String.valueOf(tv.getTag()))));
                }
                exerciseDataSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void setAufgabenLimitMax() {

        try {
            maxExercises = differentiateCardHandler.getMaxExercisesValue(this, settings, typeList);

            seekBarAufgabenlimit.setMax(maxExercises);
            seekBarAufgabenlimit.setProgress(settings.getExerciseLimit());
            textViewZahlAufgabenlimit.setText(String.valueOf(seekBarAufgabenlimit.getProgress()));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private Settings loadSettings() {
        settings = new Settings(this);
        try {
            settings.loadFromPrefs();
            return settings;
        } catch (NoSuchFieldException e) {
            Toast.makeText(this, getString(R.string.error_could_not_load_settings) + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_could_not_load_database) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }


    /**
     * zeigt den Loesch-Dialog
     */
    private void showDeleteTrainingDataDialog() {


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        showClearAllAppDatasDialog();
                        truncateAllTables();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        showClearAllAppDatasDialog();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.dialog_delete_all_training_data).setPositiveButton("Ja", dialogClickListener)
                .setNegativeButton("Nein", dialogClickListener).show();
    }
    //TODO: Abfrage ob Trainingsdaten und oder Medien gelöscht werden sollen, sandra fragen wie ich medien lösche, bzw wie sie in zusammenhang damit stehen

    void showClearAllAppDatasDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        settings.setFirstStart(true);
                        settings.saveToPrefs();
                        Settings.clearPrefs(activity);
                        onBackPressed();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        onBackPressed();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.dialog_clear_all_appdata).setPositiveButton("Ja", dialogClickListener)
                .setNegativeButton("Nein", dialogClickListener).show();
    }

    /**
     * loescht alle lokalen Tabellen in der SQLITE-DB
     */
    void truncateAllTables() {

        try {
            AppDataSource appDataSource = new AppDataSource(this);
            appDataSource.open();
            appDataSource.truncateTable();
            appDataSource.close();

            AnswerDataSource answerDataSource = new AnswerDataSource(this);
            answerDataSource.open();
            answerDataSource.truncateTable();
            answerDataSource.close();

            ExerciseDataSource exerciseDataSource = new ExerciseDataSource(this);
            exerciseDataSource.open();
            exerciseDataSource.truncateTable();
            exerciseDataSource.close();

            MediaDataSource mediaDataSource = new MediaDataSource(this);
            mediaDataSource.open();
            mediaDataSource.truncateTable();
            mediaDataSource.close();

            PackageDataSource packageDataSource = new PackageDataSource(this);
            packageDataSource.open();
            //packageDataSource.truncateTable();
            packageDataSource.close();

            CollectionDataSource collectionDataSource = new CollectionDataSource(this);
            collectionDataSource.open();
            collectionDataSource.truncateTable();
            collectionDataSource.close();
            MediaCollectionDataSource mediacollectionDS = new MediaCollectionDataSource(this);
            mediacollectionDS.open();
            mediacollectionDS.truncateTable();
            mediacollectionDS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void truncateRepeatExercise() {

        if (getResources().getBoolean(R.bool.isPlusVersion)) {

            try {

                Settings sets = new Settings(this);
                sets.loadFromPrefs();
                sets.loadUserprefs();

                ExerciseDataSource ds = new ExerciseDataSource(this);
                ds.open();
                ds.deleteRepeatExerciseforUser(sets.getUserid());
                ds.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            differentiateCardHandler.truncateRepateExercises(this);
        }
    }

    protected void setRepeatTextView() {

        int repeatExerciseCount = differentiateCardHandler.getRepeatExercisesCount(this, settings);
        tvCountWiedervorlage.setText(String.valueOf(repeatExerciseCount));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }


    /*
    private void setTypesSwitch(){
        if(!settings.isRandomTypeActivated()){
             Boolean isTypeChecked=false;
            for (int i = 0, size = switchArray.size(); i < size; i++){
                Switch s = switchArray.get(i);
                if(settings.isExerciseTypeEnabled(String.valueOf(s.getTag()))==true){
                    isTypeChecked=true;
                }
            }
            if(isTypeChecked==false){
                // alle  Types aktivieren
                for (int i = 0, size = switchArray.size(); i < size; i++){
                 Switch s = switchArray.get(i);
                 s.setChecked(true);
                    settings.setExerciseTypeEnabled(String.valueOf(s.getTag()),true);
                }
            }
        }
    }*/
}

