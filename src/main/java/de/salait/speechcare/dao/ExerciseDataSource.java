package de.salait.speechcare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.salait.speechcare.data.Exercise;
import de.salait.speechcare.data.ExerciseType;
import de.salait.speechcare.data.ImageCard;
import de.salait.speechcare.data.ImageCardExercise;
import de.salait.speechcare.data.SpeechcareSQLITEHelper;


/**
 * Created by Christian.Ramthun on 12.09.13.
 */
public class ExerciseDataSource {
    protected SQLiteDatabase database;
    protected SpeechcareSQLITEHelper dbHelper;
    protected List<ExerciseType> exerciseTypeList;


    public ExerciseDataSource(Context context) throws IOException {
        dbHelper = new SpeechcareSQLITEHelper(context, null, null, 0);
    }

    /**
     * closes the database connection
     */
    public void close() {
        if (dbHelper != null) dbHelper.close();
    }


    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }


    public void insertExerciseData(String id, String title, String question, String question_media_id,
                                   String helpVideo, String helpVideo_media_id, String exercisetype_id,
                                   String difficulty) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, Integer.valueOf(id));
        values.put(SpeechcareSQLITEHelper.COLUMN_TITLE, title);
        values.put(SpeechcareSQLITEHelper.COLUMN_QUESTION, question);
        values.put(SpeechcareSQLITEHelper.COLUMN_QUESTION_MEDIA_ID, Integer.valueOf(question_media_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_HELPVIDEO, helpVideo);
        values.put(SpeechcareSQLITEHelper.COLUMN_HELPVIDIO_MEDIA_ID, Integer.valueOf(helpVideo_media_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISETYPE_ID, Integer.valueOf(exercisetype_id));
        values.put(SpeechcareSQLITEHelper.COLUMN_DIFFICULTY, difficulty);


        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_EXERCISE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public boolean exerciseDataExits() {
        open();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_EXERCISE, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        if (count > 0) {
            return true;
        }
        return false;
    }


    public void insertExerciseTYPEData(String id, String number, String title, String exercisetitle,
                                       String description, String exercisemodel, String deactivated,
                                       String grammarlayer) {

        ContentValues values = new ContentValues();
        values.put(SpeechcareSQLITEHelper.COLUMN_ID, id);
        values.put(SpeechcareSQLITEHelper.COLUMN_NUMBER, number);
        values.put(SpeechcareSQLITEHelper.COLUMN_TITLE, title);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_TITLE, exercisetitle);
        values.put(SpeechcareSQLITEHelper.COLUMN_DESCRIPTION, description);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_MODEL, exercisemodel);
        values.put(SpeechcareSQLITEHelper.COLUMN_DEACTIVATED, deactivated);
        values.put(SpeechcareSQLITEHelper.COLUMN_GRAMMARLAYER, grammarlayer);

        database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    public void insertExerciseModelTable(JSONObject modeldata, String tablename) {

        ContentValues values = new ContentValues();
        Iterator<?> keys = modeldata.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                values.put(key, modeldata.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (values.size() > 0) {
            database.insertWithOnConflict(tablename, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }


    /**
     * inserts the id into repeatExercise
     *
     * @param exerciseID
     * @return row of newly inserted row, or -1 on error
     */
    public long insertIntoRepeatExercise(String exerciseID) {
        boolean idFound = false;
        long id = 0;

        Cursor selCursor = database.query(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_REPEATEXERCISE, null, null, null, null, null); // TODO id als where-Bedingung in die Abfrage nehmen
        selCursor.moveToFirst();

        while (!selCursor.isAfterLast()) {
            if (selCursor.getString(0).equalsIgnoreCase(exerciseID)) {
                idFound = true;
                break;
            }
            selCursor.moveToNext();
        }
        selCursor.close();
        if (idFound) return id;

        ContentValues values = new ContentValues(1);
        values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, Integer.valueOf(exerciseID));

        try {
            id = database.insert(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, null, values);
        } catch (SQLiteConstraintException e) {
            // nichts tun, wir wollten nur sicher gehen, dass die Uebung erfasst wurde
        } catch (Exception e) {

        }
        return id;
    }


    public void insertIntoRepeatExercise(List<String> exerciseIDList) {
        for (String s : exerciseIDList) {
            Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE + " WHERE exercise_id = " + s, null);
            cursor.moveToFirst();
            int count = 0;
            while (!cursor.isAfterLast()) {
                count = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
            if (count > 0) continue;

            ContentValues values = new ContentValues(1);
            values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, Integer.valueOf(s));
            try {
                database.insert(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, null, values);
            } catch (SQLiteConstraintException e) {
                // nichts tun, wir wollten nur sicher gehen, dass die Uebung erfasst wurde
            } catch (Exception e) {

            }
        }
    }


    /**
     * deletes the exercise from repeatExercise
     *
     * @param exersiceID
     * @return number of rows deleted
     */
    public long deleteFromRepeatExercise(String exersiceID) {
        long id = 0;
        try {
            database.delete(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID + " = " + exersiceID, null);
        } catch (Exception e) {

        }
        return id;
    }

    public int countRepeatExercise() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }

    public int countRepeatCardExercise(String userid) {

        if (userid == null) {
            userid = "0";
        }

        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_REPEATCARDEXERCISE + " WHERE userid = '" + userid + "'", null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }

    /**
     * fetches all exerciseIDs from repeatExercise
     *
     * @return
     */
    public List<Exercise> getAllRepeatExercises() {
        exerciseTypeList = getExerciseTypes();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_REPEATEXERCISE, null, null, null, null, null);
        cursor.moveToFirst();
        String ids = "";
        while (!cursor.isAfterLast()) {
            ids += cursor.getString(0) + ",";
            cursor.moveToNext();
        }
        cursor.close();
        ids = ids.substring(0, ids.length() - 1);


        List<Exercise> exerciseList = new ArrayList<Exercise>();
        Cursor cur = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISE, "id in (  " + ids + ")", null, null, null, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            Exercise exercise = cursorToExercise(cur);

            for (ExerciseType exerciseType : exerciseTypeList) {
                if (exerciseType.getId().equalsIgnoreCase(exercise.getExerciseTypeID())) {
                    exercise.setExerciseModell(exerciseType.getExerciseModel());
                    exercise.setGrammarLayer(exerciseType.getGrammarLayer());
                    break;
                }
            }
            exerciseList.add(exercise);
            cur.moveToNext();
        }
        cur.close();

        //exerciseList = fillupExercises(exerciseList);
        Collections.shuffle(exerciseList);
        return exerciseList;
    }

    /**
     * fetches all exerciseIDs from repeatExercise
     *
     * @return
     */
    public List<String> getAllRepeatExerciseIds() {
        ArrayList<String> repeatExerciseList = new ArrayList<String>();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_REPEATEXERCISE, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            repeatExerciseList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return repeatExerciseList;
    }


    /**
     * ruft alle Uebungen zu der App ab und gibt sie in einer ArrayList zurueck
     *
     * @return
     */

    public List<ImageCard> getAllCardsForBildart(String bildart, String cartdtypIDs) {

        database = dbHelper.getWritableDatabase();
        List<ImageCard> cardList = new ArrayList<ImageCard>();

        //Cursor cursor =database.rawQuery("SELECT mediacollection_media.media_id,  FROM mediacollection_media INNER JOIN  mediacollection ON mediacollection_media.mediacollection_id = mediacollection.id WHERE mediacollection.bildart = '"+bildart+"' AND mediacollection.id IN "+cartdtypIDs,null);
        Cursor cursor = database.rawQuery(" SELECT m.id, m.title, m.filename FROM media m INNER JOIN mediacollection_media mc ON m.id = mc.media_id WHERE mc.mediacollection_id IN " + cartdtypIDs, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ImageCard card = new ImageCard();

            card.setImagemdediaID(cursor.getString(0));
            card.setTitle(cursor.getString(1));
            String filename = cursor.getString(2);
            card.setMediapath(filename.substring(filename.lastIndexOf("/") + 1));
            cardList.add(card);
            cursor.moveToNext();
        }
        cursor.close();
        Collections.shuffle(cardList);
        return cardList;

    }

    public List<ImageCardExercise> getImageCardExercisesForRepeatModus(String userid) {

        if (userid == null) {

            userid = "0";
        }
        List<ImageCardExercise> repetCardExs = new ArrayList<>();
        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT image1,image2,image3,image4 FROM repeatimagecards WHERE userid ='" + userid + "'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ImageCardExercise exercise = new ImageCardExercise();
            exercise.setUserRelation(ImageCardExercise.NO_RELATION);
            ArrayList<ImageCard> cards = new ArrayList<ImageCard>();
            ImageCard c1 = new ImageCard();
            c1.setImagemdediaID(cursor.getString(0));
            cards.add(c1);
            if (cursor.getString(1) != null) {
                ImageCard c2 = new ImageCard();
                c2.setImagemdediaID(cursor.getString(1));
                cards.add(c2);

            }
            if (cursor.getString(2) != null) {
                ImageCard c3 = new ImageCard();
                c3.setImagemdediaID(cursor.getString(2));
                cards.add(c3);

            }
            if (cursor.getString(3) != null) {
                ImageCard c4 = new ImageCard();
                c4.setImagemdediaID(cursor.getString(3));
                cards.add(c4);

            }
            exercise.setImageCards(cards);
            repetCardExs.add(exercise);
            cursor.moveToNext();
        }
        cursor.close();
        Collections.shuffle(repetCardExs);


        for (ImageCardExercise cardEx : repetCardExs) {

            for (ImageCard card : cardEx.getImageCards()) {

                cursor = database.rawQuery("SELECT filename FROM media WHERE id = '" + card.getImagemdediaID() + "'", null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String filename = cursor.getString(0);
                    card.setMediapath(filename.substring(filename.lastIndexOf("/") + 1));
                    cursor.moveToNext();
                }
                cursor.close();

            }

        }

        return repetCardExs;
    }


    public List<Exercise> getAllExercisesOfDifficulty(String difficulty, String exercisetypeID) {
        exerciseTypeList = getExerciseTypes();

        List<Exercise> exerciseList = new ArrayList<Exercise>();
        //Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISE, "difficulty = '"+ "medium"+"'" + " AND exercisetype_id in ( 16, 25)", null, null, null, null); //6, 21,24,  8, 18, 19
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISE, "difficulty = '" + difficulty + "'" + " " + exercisetypeID, null, null, null, null);
        //Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISE, "difficulty = '"+difficulty+"'" , null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Exercise exercise = cursorToExercise(cursor);
            for (ExerciseType exerciseType : exerciseTypeList) {
                if (exerciseType.getId().equalsIgnoreCase(exercise.getExerciseTypeID())) {
                    exercise.setExerciseModell(exerciseType.getExerciseModel());
                    exercise.setGrammarLayer(exerciseType.getGrammarLayer());
                    break;
                }
            }
            exerciseList.add(exercise);
            cursor.moveToNext();
        }

        cursor.close();
        //exerciseList = fillupExercises(exerciseList);
        Collections.shuffle(exerciseList);
        return exerciseList;
    }

    public int countExerciseTypeOfDifficulty(String difficulty, String exercisetypeID) {

        Cursor mCount = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_EXERCISE + " WHERE difficulty = '" + difficulty + "' AND exercisetype_id ='" + exercisetypeID + "'", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }


    public int countCardsForID(String cardid) {

        Cursor mCount = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION_MEDIA + " WHERE " + SpeechcareSQLITEHelper.COLUMN_MEDIACOLLECTION_ID + " = '" + cardid + "'", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    protected List<Exercise> fillupExercises(List<Exercise> exerciseList) {
        // Uebungen je nach bestimmtem Typ auffuellen
        exerciseList = getExerciseWordForImage(exerciseList);
        exerciseList = getExerciseImageForWord(exerciseList);
        exerciseList = getExerciseSortCharacters(exerciseList);
        exerciseList = getExerciseSortWords(exerciseList);
        exerciseList = getExerciseGapWordForImage(exerciseList);
        exerciseList = getExerciseGapSentenceForImage(exerciseList);
        return exerciseList;
    }

    protected Exercise cursorToExercise(Cursor cursor) {
        Exercise exercise = new Exercise(cursor.getString(0));
        exercise.setTitle(cursor.getString(1));
        exercise.setQuestion(cursor.getString(2));
        exercise.setHelpVideo(cursor.getString(3));
        exercise.setQuestionMediaID(cursor.getString(4));
        exercise.setHelpVideoMediaID(cursor.getString(5));
        exercise.setExerciseTypeID(cursor.getString(6));
        exercise.setDifficultyLevel(cursor.getString(7));
        return exercise;
    }

    /**
     * holt alle Uebungstypen aus der Datenbank
     *
     * @return ArrayList mit Uebungstypen
     */
    public List<ExerciseType> getExerciseTypes() {
        exerciseTypeList = new ArrayList<ExerciseType>();

        if (database == null) database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISETYPE, null, null, null, null, null);
        ExerciseType exerciseType;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            exerciseType = new ExerciseType();
            exerciseType.setId(cursor.getString(0));
            exerciseType.setTitle(cursor.getString(1));
            exerciseType.setExerciseTitle(cursor.getString(2));
            exerciseType.setDescription(cursor.getString(3));
            exerciseType.setExerciseModel(cursor.getString(4));
            exerciseType.setDeactivated((cursor.getShort(5) > 0));
            exerciseType.setGrammarLayer(cursor.getString(6));

            exerciseTypeList.add(exerciseType);
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseTypeList;
    }

    public ArrayList<String> getAvailableExercisetypes() {
        ArrayList<String> typeList = new ArrayList<String>();
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, new String[]{"id"},
                null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            typeList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return typeList;
    }

    public String getExTypeNameForID(String typid) {

        String s = "";
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, new String[]{"title"},
                "id = '" + typid + "'", null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            s = (cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return s;

    }

    public String getCardTypeForID(String typid) {

        String s = "";
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_MEDIACOLLECTION, new String[]{SpeechcareSQLITEHelper.COLUMN_TITLE},
                SpeechcareSQLITEHelper.COLUMN_ID + " = '" + typid + "'", null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            s = (cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return s;
    }


    /**
     * fuellt alle ExerciseGapSentenceForImage-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseGapSentenceForImage(List<Exercise> exerciseGapSentenceForImageList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPSENTENCEFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEGAPSENTENCEFORIMAGE, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseGapSentenceForImageList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0))) continue;
                exercise.setCorrectAnswer(cursor.getString(1));
                exercise.setGaps(cursor.getString(2));
                exercise.setAdditionalWords(cursor.getString(3));

                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseGapSentenceForImageList;
    }

    public Exercise getExerciseGapSentenceForImage(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPSENTENCEFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEGAPSENTENCEFORIMAGE, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            exercise.setCorrectAnswer(cursor.getString(1));
            exercise.setGaps(cursor.getString(2));
            exercise.setAdditionalWords(cursor.getString(3));

            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }


    /**
     * fuellt alle ExerciseImageForVideo-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    public Exercise getExerciseImageForVideo(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEIMAGEFORVIDEO, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEIMAGEFORVIDEO, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            exercise.setCorrectAnswer(cursor.getString(1));

            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();

        Cursor cursor2 = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_SEVANSWER, "exercise_id = " + exercise.getId(), null, null, null, null);
        cursor2.moveToFirst();

        ArrayList<HashMap> answers = new ArrayList<HashMap>();
        while (!cursor2.isAfterLast()) {

            HashMap a = new HashMap();
            a.put("id", cursor2.getString(0));
            a.put("exercise_id", cursor2.getString(1));
            a.put("sort", cursor2.getString(2));
            a.put("media_id", cursor2.getString(3));
            a.put("value", cursor2.getString(4));
            a.put("help_media_id", cursor2.getString(5));

            if (cursor2.isNull(3) == false && cursor2.getString(3).length() > 0)
                a.put("media", getMediaString(cursor2.getString(3)));
            Log.i("exerciseid", exercise.getId());
            if (cursor2.isNull(5) == false && cursor2.getString(5).length() > 0) {

                System.out.println("<<< helpmedia from db " + cursor2.getString(5));
                a.put("help_media", getHelpvideoString(cursor2.getString(5)));
            }

            answers.add(a);
            cursor2.moveToNext();
        }
        cursor2.close();
        Collections.shuffle(answers);
        exercise.setifvAnswers(answers);

        return exercise;
    }

    /**
     * fuellt alle ExerciseGapWordForVideo-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    public Exercise getExerciseWordForVideo(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEWORDFORVIDEO, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEWORDFORVIDEO, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            exercise.setCorrectAnswer(cursor.getString(1));

            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();

        Cursor cursor2 = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_SEVANSWER, "exercise_id = " + exercise.getId(), null, null, null, null);
        cursor2.moveToFirst();

        ArrayList<HashMap> answers = new ArrayList<HashMap>();
        while (!cursor2.isAfterLast()) {

            HashMap a = new HashMap();
            a.put("id", cursor2.getString(0));
            a.put("exercise_id", cursor2.getString(1));
            a.put("sort", cursor2.getString(2));
            a.put("media_id", cursor2.getString(3));
            a.put("value", cursor2.getString(4));
            a.put("help_media_id", cursor2.getString(5));

//            if (cursor2.isNull(3) == false && cursor2.getString(3).length() > 0 )
//                a.put("media",getMediaString(cursor2.getString(3)));

            if (cursor2.isNull(5) == false && cursor2.getString(5).length() > 0)
                a.put("help_media", getHelpvideoString(cursor2.getString(5)));

            answers.add(a);
            cursor2.moveToNext();
        }
        cursor2.close();
        //Collections.shuffle(answers);
        exercise.setifvAnswers(answers);

        return exercise;
    }

    /**
     * fuellt alle ExerciseGapWordForImage-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseGapWordForImage(List<Exercise> exerciseGapWordForImageList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPWORDFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEGAPWORDFORIMAGE, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseGapWordForImageList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0))) continue;
                exercise.setCorrectAnswer(cursor.getString(1));
                exercise.setGaps(cursor.getString(2));
                exercise.setAdditionalChars(cursor.getString(3));

                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseGapWordForImageList;
    }

    public Exercise getExerciseGapWordForImage(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPWORDFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEGAPWORDFORIMAGE, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            exercise.setCorrectAnswer(cursor.getString(1));
            exercise.setGaps(cursor.getString(2));
            exercise.setAdditionalChars(cursor.getString(3));

            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }

    /**
     * fuellt alle ExerciseImageForWord-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseImageForWord(List<Exercise> exerciseImageForWordList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEIMAGEFORWORD, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEIMAGEFORWORD, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseImageForWordList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0))) continue;
                // answers holen
                String correctAnswerID = cursor.getString(1);
                Cursor answerCursor = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_ANSWER, "exercise_id = '" + exercise.getId() + "'", null, null, null, null);
                answerCursor.moveToFirst();
                while (!answerCursor.isAfterLast()) {
                    if (answerCursor.getString(0).equalsIgnoreCase(correctAnswerID)) {          // pruefen, ob die ID der richtigen Antwort entspricht
                        exercise.setQuestionMediaID(answerCursor.getString(3));
                    } else {
                        exercise.addWrongAnswers(answerCursor.getString(3));
                    }
                    answerCursor.moveToNext();
                }

                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                ArrayList<String> wrongAnswers = exercise.getWrongAnswers();
                for (int i = 0; i < wrongAnswers.size(); i++) {
                    String s = wrongAnswers.get(i);
                    s = getMediaString(s);
                    wrongAnswers.add(i, s);
                    wrongAnswers.remove(i + 1);
                }


                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseImageForWordList;
    }


    public Exercise getExerciseImageForWord(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEIMAGEFORWORD, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEIMAGEFORWORD, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            // answers holen
            String correctAnswerID = cursor.getString(1);
            Cursor answerCursor = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_ANSWER, "exercise_id = '" + exercise.getId() + "'", null, null, null, null);
            answerCursor.moveToFirst();
            if (exercise.getWrongAnswers().size() == 0) {
                while (!answerCursor.isAfterLast()) {
                    if (answerCursor.getString(0).equalsIgnoreCase(correctAnswerID)) {          // pruefen, ob die ID der richtigen Antwort entspricht
                        exercise.setQuestionMediaID(answerCursor.getString(3));
                    } else {
                        exercise.addWrongAnswers(answerCursor.getString(3));
                    }
                    answerCursor.moveToNext();
                }

                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                ArrayList<String> wrongAnswers = exercise.getWrongAnswers();
                for (int i = 0; i < wrongAnswers.size(); i++) {
                    String s = wrongAnswers.get(i);
                    s = getMediaString(s);
                    wrongAnswers.add(i, s);
                    wrongAnswers.remove(i + 1);
                }

                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }

    /**
     * fuellt alle ExerciseSortCharacters-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseSortCharacters(List<Exercise> exerciseSortCharactersList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISESORTCHARACTERS, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISESORTCHARACTERS, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseSortCharactersList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0))) continue;
                exercise.setCorrectAnswer(cursor.getString(1));
                exercise.setAdditionalChars(cursor.getString(2));
                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseSortCharactersList;
    }

    public Exercise getExerciseSortCharacters(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISESORTCHARACTERS, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISESORTCHARACTERS, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            exercise.setCorrectAnswer(cursor.getString(1));
            exercise.setAdditionalChars(cursor.getString(2));
            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }

    /**
     * fuellt alle ExerciseSortWords-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseSortWords(List<Exercise> exerciseSortWordsList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISESORTWORDS, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISESORTWORDS, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseSortWordsList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0))) continue;
                exercise.setCorrectAnswer(cursor.getString(1));
                exercise.setAdditionalWords(cursor.getString(2));

                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseSortWordsList;
    }

    public Exercise getExerciseSortWords(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISESORTWORDS, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISESORTWORDS, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            exercise.setCorrectAnswer(cursor.getString(1));
            exercise.setAdditionalWords(cursor.getString(2));

            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }


    /**
     * fuellt alle ExerciseWordForImage-Typen in der Liste auf
     *
     * @return vervollstaendigte Liste
     */
    protected List<Exercise> getExerciseWordForImage(List<Exercise> exerciseWordForImageList) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEWORDFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEWORDFORIMAGE, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Exercise exercise : exerciseWordForImageList) {
                if (!exercise.getId().equalsIgnoreCase(cursor.getString(0)))
                    continue;         // wir wollen nur die aktuelle Uebung in der Liste finden, also weiter

                // answers holen
                String correctAnswerID = cursor.getString(1);
                Cursor answerCursor = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_ANSWER, "exercise_id = '" + exercise.getId() + "'", null, null, null, null);
                answerCursor.moveToFirst();
                while (!answerCursor.isAfterLast()) {
                    if (answerCursor.getString(0).equalsIgnoreCase(correctAnswerID)) {          // pruefen, ob die ID der richtigen Antwort entspricht
                        exercise.setCorrectAnswer(answerCursor.getString(4));
                    } else {
                        exercise.addWrongAnswers(answerCursor.getString(4));
                    }
                    answerCursor.moveToNext();
                }

                // media holen
                exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
                if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                    exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseWordForImageList;
    }

    public Exercise getExerciseWordForImage(Exercise exercise) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISEWORDFORIMAGE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISEWORDFORIMAGE, "id = " + exercise.getId(), null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // answers holen
            String correctAnswerID = cursor.getString(1);
            Cursor answerCursor = database.query(SpeechcareSQLITEHelper.TABLE_ANSWER, SpeechcareSQLITEHelper.COLUMN_TABLE_ANSWER, "exercise_id = '" + exercise.getId() + "'", null, null, null, null);
            answerCursor.moveToFirst();
            if (exercise.getWrongAnswers().size() == 0) {
                while (!answerCursor.isAfterLast()) {
                    if (answerCursor.getString(0).equalsIgnoreCase(correctAnswerID)) {          // pruefen, ob die ID der richtigen Antwort entspricht
                        exercise.setCorrectAnswer(answerCursor.getString(4));
                    } else {
                        exercise.addWrongAnswers(answerCursor.getString(4));
                    }
                    answerCursor.moveToNext();
                }
            }

            // media holen
            exercise.setQuestionMedia(getMediaString(exercise.getQuestionMediaID()));
            if (!exercise.getHelpVideoMediaID().equalsIgnoreCase("0")) {
                exercise.setHelpVideo(getHelpvideoString(exercise.getHelpVideoMediaID()));
            }

            cursor.moveToNext();
        }
        cursor.close();
        return exercise;
    }


    protected String getMediaString(String mediaID) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_MEDIA, new String[]{"filename"}, "id = '" + mediaID + "'", null, null, null, null);
        cursor.moveToFirst();
        String filename = null;
        while (!cursor.isAfterLast()) {
            filename = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return filename.substring(filename.lastIndexOf("/") + 1);
    }

    protected String getHelpvideoString(String helpvideoID) {
        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_MEDIA, new String[]{"filename"}, "id = '" + helpvideoID + "'", null, null, null, null);
        cursor.moveToFirst();
        String filename = null;
        while (!cursor.isAfterLast()) {
            filename = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        System.out.println("<<< getHelpvideoString from db " + filename);
        if (filename == null) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("/") + 1);
    }


    public void truncateTable() {
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISE + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPSENTENCEFORIMAGE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISEGAPSENTENCEFORIMAGE + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISEGAPWORDFORIMAGE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISEGAPWORDFORIMAGE + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISESORTCHARACTERS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISESORTCHARACTERS + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISESORTWORDS, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISESORTWORDS + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISEWORDFORIMAGE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISEWORDFORIMAGE + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISEMIRRORWITHAUDIO, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISEMIRRORWITHAUDIO + "'", null);
        database.delete(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_EXERCISETYPE + "'", null);
        this.truncateRepetitionTable();
    }

    public void truncateRepetitionTable() {

        database.delete(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE + "'", null);
    }

    public void truncateRepetitionCardsTable() {

        database.delete(SpeechcareSQLITEHelper.TABLE_REPEATCARDEXERCISE, null, null);
        database.rawQuery("delete from sqlite_sequence where name='" + SpeechcareSQLITEHelper.TABLE_REPEATCARDEXERCISE + "'", null);
    }

    public int countTable(String tableName) {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }


    // METHODS FOR PLUS APPS


    public void insertIntoRepeatExercise(List<String> exerciseIDList, String userID) {
        System.out.println("<< START INSERT FOR REPEAT FOR PLUSVERSION");

        if (database == null) database = dbHelper.getWritableDatabase();
        for (String exerciseID : exerciseIDList) {
            // CollectionId zur Uebung finden
            String collectionID = null;
            Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_COLLECTION_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_COLLECTION_EXERCISE, "exercise_id = " + exerciseID, null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                collectionID = cursor.getString(0);
                cursor.moveToNext();
            }
            cursor.close();
            System.out.println("<< INSERT FOR REPEAT collectionID " + collectionID);
            if (collectionID == null) return;

            // pruefen ob exercise schon in repeatexercise vorhanden
            //Cursor cursor = database.rawQuery("SELECT * FROM "+ SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE + " WHERE exercise_id = "+s + " AND user_id = " + userID + " AND collection_id = " + collectionID, null);
            Cursor cursor2 = database.query(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_REPEATEXERCISE_PraxisV, "exercise_id = " + exerciseID + " AND user_id = " + userID + " AND collection_id = " + collectionID, null, null, null, null);
            cursor2.moveToFirst();
            int count = 0;
            while (!cursor.isAfterLast()) {
                count = cursor2.getInt(0);
                cursor2.moveToNext();
            }
            cursor2.close();
            System.out.println("<< INSERT FOR REPEAT count " + String.valueOf(count));
            if (count > 0) continue;


            // Uebung in repeatexercise eintragen
            ContentValues values = new ContentValues(1);
            values.put(SpeechcareSQLITEHelper.COLUMN_EXERCISE_ID, Integer.valueOf(exerciseID));
            values.put(SpeechcareSQLITEHelper.COLUMN_USER_ID, Integer.valueOf(userID));
            values.put(SpeechcareSQLITEHelper.COLUMN_COLLECTION_ID, Integer.valueOf(collectionID));

            try {
                database.insertWithOnConflict(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                Log.i("repeatexerciseLOG", "erfolgreich eingetragen, ID: " + exerciseID);

            } catch (SQLiteConstraintException e) {
                System.out.println("<< INSERT FOR REPEAT SQLiteConstraintException " + e.getLocalizedMessage());
                // nichts tun, wir wollten nur sicher gehen, dass die Uebung erfasst wurde
            } catch (Exception e) {
                System.out.println("<< INSERT FOR REPEAT Exception " + e.getLocalizedMessage());
            }
        }
    }


    public void saveRepeatMediaCards(List<ImageCard> mediaIDList, String userid) {

        if (userid == null) {

            userid = "0";
        }


        String imageColumn = "";
        String imageIDS = "";
        String checkImages = "";
        int i = 0;

        for (ImageCard card : mediaIDList) {
            i++;
            imageColumn = imageColumn + "image" + i + ",";
            imageIDS = imageIDS + card.getImagemdediaID() + ",";
            checkImages = checkImages + " AND image" + i + " = " + card.getImagemdediaID();

        }

        if (imageColumn.length() > 0) {
            imageColumn = imageColumn.substring(0, imageColumn.length() - 1);
        }
        if (imageIDS.length() > 0) {
            imageIDS = imageIDS.substring(0, imageIDS.length() - 1);
        }

        // CHECK if same exercise for repeat exits

        int foundCount = 0;
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM repeatimagecards WHERE userid ='" + userid + "'" + checkImages, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            foundCount = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();

        if (foundCount == 0) {
            // INSERT
            database.execSQL("INSERT INTO repeatimagecards (" + imageColumn + ",userid) VALUES (" + imageIDS + "," + userid + ")");

        }


    }

    public void deleteRepeatMediaCards(List<ImageCard> mediaIDList, String userid) {

        if (userid == null) {

            userid = "0";
        }

        String whereColumns = "";
        int i = 0;

        for (ImageCard card : mediaIDList) {
            i++;
            whereColumns = whereColumns + " AND image" + i + " = " + card.getImagemdediaID();
        }

        database.execSQL("DELETE FROM repeatimagecards WHERE userid ='" + userid + "'" + whereColumns);
    }

    public boolean userHasLicense(String userID) {
        database = dbHelper.getWritableDatabase();

        boolean result = false;

        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM collection WHERE user_id ='" + userID + "'", null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();

        if (count > 0) return true;

        return result;
    }

    public List<Exercise> getAllRepeatExercises(String userId) {
        exerciseTypeList = getExerciseTypes();

        database = dbHelper.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT exercise_id  FROM " + SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE + " WHERE user_id ='" + userId + "'", null);
        cursor.moveToFirst();
        String ids = "";
        while (!cursor.isAfterLast()) {
            ids += cursor.getString(0) + ",";
            cursor.moveToNext();
        }
        cursor.close();

        if (ids.length() > 0) ids = ids.substring(0, ids.length() - 1);

        List<Exercise> exerciseList = new ArrayList<Exercise>();
        Cursor cur = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISE, "id in (  " + ids + ")", null, null, null, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            Exercise exercise = cursorToExercise(cur);

            for (ExerciseType exerciseType : exerciseTypeList) {
                if (exerciseType.getId().equalsIgnoreCase(exercise.getExerciseTypeID())) {
                    exercise.setExerciseModell(exerciseType.getExerciseModel());
                    exercise.setGrammarLayer(exerciseType.getGrammarLayer());
                    break;
                }
            }
            exerciseList.add(exercise);
            cur.moveToNext();
        }
        cur.close();

        //exerciseList = fillupExercises(exerciseList);
        Collections.shuffle(exerciseList);
        return exerciseList;
    }

    public List<ExerciseType> getExerciseTypesPraxis() {
        exerciseTypeList = new ArrayList<ExerciseType>();

        Cursor cursor = database.query(SpeechcareSQLITEHelper.TABLE_EXERCISETYPE, SpeechcareSQLITEHelper.COLUMN_TABLE_EXERCISETYPE, null, null, null, null, null);
        ExerciseType exerciseType;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            exerciseType = new ExerciseType();
            exerciseType.setId(cursor.getString(0));
            exerciseType.setTitle(cursor.getString(1));
            exerciseType.setExerciseTitle(cursor.getString(2));
            exerciseType.setDescription(cursor.getString(3));
            exerciseType.setExerciseModel(cursor.getString(4));
            exerciseType.setDeactivated((cursor.getShort(5) > 0));
            exerciseType.setGrammarLayer(cursor.getString(6));

            exerciseTypeList.add(exerciseType);
            cursor.moveToNext();
        }
        cursor.close();
        return exerciseTypeList;
    }

    public List<Exercise> getAllExercisesOfDifficultyForUser(String difficulty, String exercisetypeID, String userID) {

        if (database == null) database = dbHelper.getWritableDatabase();

        exerciseTypeList = getExerciseTypesPraxis();

        List<Exercise> exerciseList = new ArrayList<Exercise>();
        String q = "SELECT * FROM exercise INNER JOIN  collection_exercise ON exercise.id = collection_exercise.exercise_id INNER JOIN collection ON collection_exercise.collection_id = collection.id WHERE collection.user_id ='" + userID + "' AND difficulty='" + difficulty + "' " + exercisetypeID;
        Cursor cursor = database.rawQuery(q, null);


        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Exercise exercise = cursorToExercise(cursor);
            for (ExerciseType exerciseType : exerciseTypeList) {
                if (exerciseType.getId().equalsIgnoreCase(exercise.getExerciseTypeID())) {
                    exercise.setExerciseModell(exerciseType.getExerciseModel());
                    exercise.setGrammarLayer(exerciseType.getGrammarLayer());
                    break;
                }
            }
            exerciseList.add(exercise);
            cursor.moveToNext();
        }
        cursor.close();

        //exerciseList = fillupExercises(exerciseList);
        Collections.shuffle(exerciseList);

        return exerciseList;
    }

    public int countRepeatExercise(String userID) {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE + " WHERE user_id ='" + userID + "'", null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return count;
    }

    public void deleteRepeatExerciseforUser(String userId) {
        if (database == null) database = dbHelper.getWritableDatabase();
        database.delete(SpeechcareSQLITEHelper.TABLE_REPEATEXERCISE, "user_id='" + userId + "'", null);
    }


    public String getCollectionIDForExercise(String exerciseID, String userID) {
        String q = "SELECT collection_id FROM exercise INNER JOIN  collection_exercise ON exercise.id = collection_exercise.exercise_id INNER JOIN collection ON collection_exercise.collection_id = collection.id WHERE collection.user_id ='" + userID + "' AND exercise.id = '" + exerciseID + "'";
        Cursor cursor = database.rawQuery(q, null);
        cursor.moveToFirst();
        String collectionid = "";
        while (!cursor.isAfterLast()) {
            collectionid = cursor.getString(0);

            cursor.moveToNext();
        }
        cursor.close();
        return collectionid;
    }
}
