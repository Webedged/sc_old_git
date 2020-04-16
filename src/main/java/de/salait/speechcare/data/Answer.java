package de.salait.speechcare.data;

/**
 * Created by sandra.stuck on 01.11.13.
 */
public class Answer {
    private String answerID;
    private String answerTime;
    private String collectionID;
    private Boolean correct;
    private String duration;
    private String exerciseID;
    private String givenAnswer;
    private Boolean solved;

    public String getAnswerID() {
        return answerID;
    }

    public void setAnswerID(String answerid) {
        this.answerID = answerid;
    }

    public String getAnswerTxt() {
        return givenAnswer;
    }

    public void setAnswerTxt(String answertxt) {
        this.givenAnswer = answertxt;
    }

    public String getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(String answertime) {
        this.answerTime = answertime;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionid) {
        this.collectionID = collectionid;
    }

    public Boolean getIsCorrect() {
        return correct;
    }

    public void setIsCorrect(Boolean iscorrect) {
        this.correct = iscorrect;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getExerciseID() {
        return exerciseID;
    }

    public void setExerciseID(String exerciseid) {
        this.exerciseID = exerciseid;
    }

    public String getGivenAnswer() {
        return givenAnswer;
    }

    public void setGivenAnswer(String givenanswer) {
        this.givenAnswer = givenanswer;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solve) {
        this.solved = solve;
    }
}
