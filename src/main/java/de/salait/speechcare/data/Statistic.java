package de.salait.speechcare.data;

public class Statistic {
    private String ID;
    private String Timestamp;
    private String answerStatus;
    private String givenAnswer;

    public String getID() {
        return ID;
    }
    public void setID(String id) {
        this.ID = id;
    }

    public String getTimestamp() {
        return Timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.Timestamp = timestamp;
    }

    public String getAnswerStatus() {
        return answerStatus;
    }
    public void setAnswerStatus(String answerstatus) {
        this.answerStatus = answerstatus;
    }

    public String getGivenAnswer() {
        return givenAnswer;
    }
    public void setGivenAnswer(String givenanswer) {
        this.givenAnswer = givenanswer;
    }


}
