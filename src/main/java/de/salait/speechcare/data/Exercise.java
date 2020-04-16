package de.salait.speechcare.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Christian.Ramthun on 12.09.13.
 */
public class Exercise {
    public static final String DIFFICULTY_LEVEL_EASY = "easy";
    public static final String DIFFICULTY_LEVEL_MEDIUM = "medium";
    public static final String DIFFICULTY_LEVEL_HARD = "hard";

    public static final int WRONG_ANSWER_GIVEN = 0;
    public static final int CORRECT_ANSWER_GIVEN = 1;
    public static final int NO_ANSWER_GIVEN = 2;
    /**
     * 0 = falsch
     * 1 = richtig
     * 2 = unbeantwortet
     **/
    public int answerstatus = 2;
    private String id;
    private String exerciseTypeID;
    private ExerciseType exerciseType;
    private String exerciseModell;
    private String grammarLayer;
    private String difficultyLevel;
    private String title;
    private String question;
    private String correctAnswer;
    private String gaps;
    private ArrayList<String> wrongAnswers;
    private String helpVideoMediaID;
    private String questionMediaID;
    private String questionMedia;
    private String answerSort;
    private String helpVideo;
    private String additionalChars;
    private String additionalWords;
    private ArrayList<HashMap> ifvAnswers;


    public Exercise() {
    }

    public Exercise(String id) {
        this.id = id;
        wrongAnswers = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExerciseTypeID() {
        return exerciseTypeID;
    }

    public void setExerciseTypeID(String exerciseTypeID) {
        this.exerciseTypeID = exerciseTypeID;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }


    public String getExerciseModell() {
        return exerciseModell;
    }

    public void setExerciseModell(String exerciseModell) {
        this.exerciseModell = exerciseModell;
    }

    public String getGrammarLayer() {
        return grammarLayer;
    }

    public void setGrammarLayer(String grammarLayer) {
        this.grammarLayer = grammarLayer;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getGaps() {
        return gaps;
    }

    public void setGaps(String gaps) {
        this.gaps = gaps;
    }

    public ArrayList<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public void addWrongAnswers(String wrongAnswer) {
        this.wrongAnswers.add(wrongAnswer);
    }

    public String getHelpVideoMediaID() {
        return helpVideoMediaID;
    }

    public void setHelpVideoMediaID(String helpVideoMediaID) {
        this.helpVideoMediaID = helpVideoMediaID;
    }

    public String getQuestionMediaID() {
        return questionMediaID;
    }

    public void setQuestionMediaID(String questionMediaID) {
        this.questionMediaID = questionMediaID;
    }

    public String getQuestionMedia() {
        return questionMedia;
    }

    public void setQuestionMedia(String questionMedia) {
        this.questionMedia = questionMedia;
    }

    public String getAnswerSort() {
        return answerSort;
    }

    public void setAnswerSort(String answerSort) {
        this.answerSort = answerSort;
    }

    public String getHelpVideo() {
        return helpVideo;
    }

    public void setHelpVideo(String helpVideo) {
        this.helpVideo = helpVideo;
    }

    public String getAdditionalChars() {
        return additionalChars;
    }

    public void setAdditionalChars(String additionalChars) {
        this.additionalChars = additionalChars;
    }

    public String getAdditionalWords() {
        return additionalWords;
    }

    public void setAdditionalWords(String additionalWords) {
        this.additionalWords = additionalWords;
    }

    public ArrayList<HashMap> getifvAnswers() {
        return ifvAnswers;
    }

    public void setifvAnswers(ArrayList<HashMap> answers) {
        this.ifvAnswers = answers;
    }

}