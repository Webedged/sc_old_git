package de.salait.speechcare.utility;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.salait.speechcare.R;
import de.salait.speechcare.dao.ExerciseDataSource;
import de.salait.speechcare.dao.MediaCollectionDataSource;
import de.salait.speechcare.data.Exercise;
import de.salait.speechcare.data.ImageCardExercise;
import de.salait.speechcare.data.Settings;
import de.salait.speechcare.data.TrainingSingleton;

/**
 * Created by speechcare on 26.04.18.
 */

public class DifferentiateCardHandler {

    private static final String releaspackageUrl = "releasepackage/export/";
    private static final String releasepackageURLForCardApp = "service/ExportCollection/";


    public String getApiMethod(Context context) {

        if (context.getResources().getBoolean(R.bool.isCardApp) == true) {
            return releasepackageURLForCardApp;
        }
        return releaspackageUrl;
    }

    public List<String> getExerciseTypes(Context context, String bildart) throws IOException {


        List<String> thisExerciseTypes;

        if (context.getResources().getBoolean(R.bool.isCardApp) == true) {


            MediaCollectionDataSource mcDS = new MediaCollectionDataSource(context);
            mcDS.open();
            thisExerciseTypes = mcDS.getAvailableCardtypes(bildart.toLowerCase());
            mcDS.close();

        } else {
            ExerciseDataSource dataSource = new ExerciseDataSource(context);
            dataSource.open();
            thisExerciseTypes = dataSource.getAvailableExercisetypes();
            dataSource.close();
        }

        return thisExerciseTypes;
    }


    public String getSelectedTypsLblTxt(Context context, Settings settings) {

        try {
            List<String> typeList = getExerciseTypes(context, settings.getBildArt());
            String exTypes = "";
            if (context.getResources().getBoolean(R.bool.isCardApp) == true) {
                ExerciseDataSource exerciseDataSource = new ExerciseDataSource(context);
                exerciseDataSource.open();

                for (int i = 0, size = typeList.size(); i < size; i++) {
                    if (settings.isExerciseTypeEnabled(typeList.get(i)) == true) {

                        if (exTypes.length() != 0) {

                            exTypes = exTypes + ", " + exerciseDataSource.getCardTypeForID(typeList.get(i));
                        } else {

                            exTypes = context.getResources().getString(R.string.label_versionselection) + " " + exerciseDataSource.getCardTypeForID(typeList.get(i));

                        }
                    }
                }
                exerciseDataSource.close();
                return exTypes;
            } else {

                ExerciseDataSource exerciseDataSource = new ExerciseDataSource(context);
                exerciseDataSource.open();
                for (int i = 0, size = typeList.size(); i < size; i++) {
                    if (settings.isExerciseTypeEnabled(typeList.get(i)) == true) {
                        if (exTypes.length() != 0) {
                            exTypes = exTypes + ", " + exerciseDataSource.getExTypeNameForID(typeList.get(i));
                        } else {
                            exTypes = context.getResources().getString(R.string.label_versionselection) + " " + exerciseDataSource.getExTypeNameForID(typeList.get(i));
                        }
                    }
                }
                exerciseDataSource.close();
                return exTypes;
            }


        } catch (IOException e) {

            System.out.println("ERROR " + e.getLocalizedMessage());
            e.printStackTrace();
            return "";
        }
    }


    public String getDifficultyLevelLblTxt(Activity context, Settings sets) {

        if (context.getResources().getBoolean(R.bool.isCardApp) == true) {
            return context.getResources().getString(R.string.label_imageCount) + sets.getCardCountForExercise();
        } else {
            if (sets.getDifficultyLevel().equalsIgnoreCase("easy")) {
                return context.getResources().getString(R.string.label_difficulty_level) + " " + context.getResources().getString(R.string.button_easy);
            }
            if (sets.getDifficultyLevel().equalsIgnoreCase("medium")) {
                return context.getResources().getString(R.string.label_difficulty_level) + " " + context.getResources().getString(R.string.button_medium);

            }
            if (sets.getDifficultyLevel().equalsIgnoreCase("hard")) {
                return context.getResources().getString(R.string.label_difficulty_level) + " " + context.getResources().getString(R.string.button_hard);
            }
        }
        return "";
    }

    public String getWiedervorlageLblCountTxt(Context context, Settings settings) {

        return String.valueOf(getRepeatExercisesCount(context, settings));
    }

    public int getRepeatExercisesCount(Context context, Settings sets) {

        int count = 0;
        ExerciseDataSource datasource = null;

        try {
            datasource = new ExerciseDataSource(context);

            datasource.open();


            if (context.getResources().getBoolean(R.bool.isPlusVersion) == true) {

                sets.loadUserprefs();
                count = datasource.countRepeatExercise(sets.getUserid());
            } else {
                if (context.getResources().getBoolean(R.bool.isCardApp) == true) {
                    count = datasource.countRepeatCardExercise(sets.getUserid());
                } else {
                    count = datasource.countRepeatExercise();
                    count = datasource.countRepeatExercise();
                }
            }

            datasource.close();

        } catch (SQLiteException sqlEx) {
            System.out.println("DB ERROR " + sqlEx.getLocalizedMessage());

        } catch (Exception e) {
            System.out.println("DB ERROR " + e.getLocalizedMessage());
        }
        return count;
    }

    public String getEmailBodyTxt(Activity context, boolean isrepeatModus) {

        String bodytxt = "";
        if (context.getResources().getBoolean(R.bool.isCardApp) == true) {
            if (isrepeatModus) {

                String newRepeatcount = "";
                if (TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT) == TrainingSingleton.getInstance().cardList.size()) {
                    newRepeatcount = "Es befinden sich keine weiteren Aufgaben in ihrer Wiedervolage.";
                } else if ((TrainingSingleton.getInstance().cardList.size() - TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT)) == 1) {
                    newRepeatcount = "Es befindet sich noch 1 Aufgabe in ihrer Wiedervolage.";
                } else {
                    newRepeatcount = "Es befinden sich noch " +
                            (TrainingSingleton.getInstance().cardList.size() - TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT)) +
                            " Aufgaben in ihrer Wiedervolage.";
                }
                bodytxt = "Ihre Aufgaben aus der Wiedervorlage \n\n" + "Sie haben " + String.valueOf(TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT))
                        + " von insgesamt " + String.valueOf(TrainingSingleton.getInstance().cardList.size()) + " Aufgaben gelöst.\nTrainingsdauer: " + TrainingSingleton.getInstance().getElapsedTime() +
                        " Minuten\n\n" + newRepeatcount;
            } else {
                bodytxt = "Hier sind Ihre Trainingsergebnisse:\n\n" + "Trainingsdauer: " + TrainingSingleton.getInstance().getElapsedTime() +
                        " Minuten\n" + context.getResources().getString(R.string.geloest) + ": " + String.valueOf(TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_CORRECT)) + "\n" +
                        context.getResources().getString(R.string.ungeloest) + ": " + String.valueOf(TrainingSingleton.getInstance().getCardCountUserRelation(ImageCardExercise.RELATION_WRONG)) + "\n" + "\n\n" + context.getResources().getString(R.string.training_eval_settings)
                        + "\nBilderanzahl: " + String.valueOf(TrainingSingleton.getInstance().getCardCount()) + "\n" + getSettingKindOfSettingAndValue(context) + "\n" + getSelectedImageCardtypes(context);
            }

        } else {

            bodytxt = "Hier sind Ihre Trainingsergebnisse:\n\n" + "Trainingsdauer: " + TrainingSingleton.getInstance().getElapsedTime() +
                    " Minuten\n" + context.getResources().getString(R.string.geloest) + ":" + String.valueOf(TrainingSingleton.getInstance().getCorrectAnswers().size()) + "\n" +
                    context.getResources().getString(R.string.ungeloest) + ":" + String.valueOf(TrainingSingleton.getInstance().getWrongAnswers().size()) + "\n" + getTypeAnalyse(context) + "\n\n" + context.getResources().getString(R.string.training_eval_settings)
                    + "\nSchwierigkeitsgrad: " + getSettingDifficult(context) + "\n" + getSettingKindOfSettingAndValue(context) + "\n" + getPlayedExTypes(context);
        }
        return bodytxt;
    }


    //  Email Bodytext Values
    private String getTypeAnalyse(Context context) {

        String end = "";

        ArrayList<String> types = new ArrayList<String>();
        ExerciseDataSource datasource = null;
        try {
            datasource = new ExerciseDataSource(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Exercise ex : TrainingSingleton.getInstance().exerciseList) {
            if (!types.contains(ex.getExerciseTypeID())) {
                types.add(ex.getExerciseTypeID());
            }
        }

        for (String typeID : types) {
            int solvedRight = 0;
            int unsolved = 0;

            datasource.open();
            String title = datasource.getExTypeNameForID(typeID);
            datasource.close();

            for (Exercise ex : TrainingSingleton.getInstance().exerciseList) {
                if (ex.getExerciseTypeID().equals(typeID)) {
                    if (ex.answerstatus == 1) {
                        solvedRight++;
                    } else {
                        unsolved++;
                    }
                }
            }
            end = end + "\n" + title + ": Richtig:" + solvedRight + " - " + "Falsch/" + context.getResources().getString(R.string.ungeloest) + ":" + unsolved;
        }

        return end;
    }


    private String getSettingDifficult(Activity activity) {

        String diff = "";
        Settings settings = new Settings(activity);
        try {
            settings.loadFromPrefs();
            if (settings.getDifficultyLevel().equalsIgnoreCase("easy")) {
                diff = "einfach";
            } else if (settings.getDifficultyLevel().equalsIgnoreCase("medium")) {
                diff = "mittel";
            } else if (settings.getDifficultyLevel().equalsIgnoreCase("hard")) {
                diff = "schwierig";
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diff;
    }

    private String getSettingKindOfSettingAndValue(Activity context) {
        String kind = "";
        Settings settings = new Settings(context);
        try {
            settings.loadFromPrefs();
            if (settings.isExerciseLimit() == true) {
                kind = "Aufgabenlimit: " + String.valueOf(settings.getExerciseLimit());
            } else {
                kind = "Zeitlimit: " + String.valueOf(settings.getTimelimit());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return kind;
    }

    private String getPlayedExTypes(Activity context) {
        String types = "";
        Settings settings = new Settings(context);
        try {
            settings.loadFromPrefs();
            if (settings.isRandomTypeActivated() == false) {
                ArrayList<String> typeList = new ArrayList<String>();
                String tmp = "Zusammenstellung: ";
                for (int i = 0; i < TrainingSingleton.getInstance().exerciseList.size(); i++) {
                    Exercise e = TrainingSingleton.getInstance().exerciseList.get(i);
                    if (!typeList.contains(e.getTitle())) {
                        typeList.add(e.getTitle());
                        tmp = tmp + e.getTitle() + ", ";
                    }

                }

                types = tmp.substring(0, tmp.length() - 2);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return types;
    }


    private String getSelectedImageCardtypes(Context context) {

        String types = "Zusammenstellung: ";
        Settings settings = new Settings(context);
        try {
            settings.loadFromPrefs();
            if (settings.isRandomTypeActivated() == true) {
                types = types + "Zufällige Typenauswahl";
            } else {
                MediaCollectionDataSource mcDS = new MediaCollectionDataSource(context);
                mcDS.open();
                ArrayList<String> typeList = mcDS.getAvailableCardtypes(settings.getBildArt().toLowerCase());
                mcDS.close();
                ExerciseDataSource exerciseDataSource = new ExerciseDataSource(context);
                exerciseDataSource.open();
                for (int i = 0, size = typeList.size(); i < size; i++) {

                    types = types + exerciseDataSource.getCardTypeForID(typeList.get(i)) + ", ";

                }


                exerciseDataSource.close();
                types = types.substring(0, types.length() - 2);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return types;

    }


    public void truncateRepateExercises(Context context) {

        try {
            ExerciseDataSource exerciseDataSource = new ExerciseDataSource(context);
            exerciseDataSource.open();
            if (context.getResources().getBoolean(R.bool.isCardApp) == true) {

                exerciseDataSource.truncateRepetitionCardsTable();
            } else {
                exerciseDataSource.truncateRepetitionTable();
            }
            exerciseDataSource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getMaxExercisesValue(Context context, Settings settings, List<String> typeList) throws IOException {

        ExerciseDataSource exerciseDataSource = new ExerciseDataSource(context);
        exerciseDataSource.open();
        int maxExercises = 0;

        for (int i = 0; i < typeList.size(); i++) {

            if (settings.isExerciseTypeEnabled(typeList.get(i))) {

                if (!context.getResources().getBoolean(R.bool.isCardApp)) {
                    maxExercises = maxExercises + exerciseDataSource.countExerciseTypeOfDifficulty(settings.getDifficultyLevel(), typeList.get(i));
                } else {
                    maxExercises = maxExercises + exerciseDataSource.countCardsForID(typeList.get(i));
                }
            }
        }
        exerciseDataSource.close();


        return getMaxValue(context, maxExercises, settings.getCardCountForExercise());
    }

    public int getMaxValue(Context context, int maxValue, int cardCountForExercise) {
        if (context.getResources().getBoolean(R.bool.isCardApp) == true) {

            maxValue = maxValue / cardCountForExercise;
        }
        if (maxValue == 0) {
            maxValue = 1;
        }
        return maxValue;
    }

    public String getTypeNameForID(Context context, String typeid) {
        String name = "";
        try {
            ExerciseDataSource dataSource = new ExerciseDataSource(context);
            dataSource.open();

            if (context.getResources().getBoolean(R.bool.isCardApp)) {
                name = dataSource.getCardTypeForID(typeid);
            } else {
                name = dataSource.getExTypeNameForID(typeid);
            }
            dataSource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;

    }

    public int getCountForExTypeWithID(Context context, String typeid, String difficultyValue) {
        int count = 0;
        try {
            ExerciseDataSource dataSource = new ExerciseDataSource(context);
            dataSource.open();

            if (context.getResources().getBoolean(R.bool.isCardApp)) {
                count = dataSource.countCardsForID(typeid);
            } else {
                count = dataSource.countExerciseTypeOfDifficulty(difficultyValue, typeid);
            }
            dataSource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;

    }


}
