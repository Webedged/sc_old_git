package de.salait.speechcare.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Erstellt und beinhaltet alle Daten f�r einen Trainingsdurchlauf
 * Created by Christian.Ramthun on 16.09.13.
 */
public class TrainingSingleton {

    private static TrainingSingleton instance = new TrainingSingleton();
    public List<Exercise> exerciseList;
    public List<ImageCardExercise> cardList;
    private String elapsedTime;
    private int timeLimit;
    private int exerciseIndex;
    private int cardForExerciseCount;
    private TrainingSingleton() {
    }

    public static TrainingSingleton getInstance() {
        return instance;
    }

    /**
     * getter & setter
     **/
    public String getElapsedTime() {
        return elapsedTime;
    }

    public int getExerciseLimit() {
        return exerciseList.size();
    }

    public int getCardExerciseLimit() {
        return cardList.size();
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }


    public List<String> getCorrectAnswers() {
        ArrayList<String> correctAnswers = new ArrayList<String>();

        for (Exercise ex : exerciseList) {
            if (ex.answerstatus == 1) {
                correctAnswers.add(ex.getId());
            }
        }
        return correctAnswers;
    }


    public int getCardCountUserRelation(int userRelation) {

        int count = 0;
        for (ImageCardExercise cardex : cardList) {
            if (cardex.getUserRelation() == userRelation) {
                count++;
            }
        }
        return count;
    }


    public List<String> getWrongAnswers() {
        ArrayList<String> wrongAnswers = new ArrayList<String>();

        for (Exercise ex : exerciseList) {
            if (ex.answerstatus == 0) {
                wrongAnswers.add(ex.getId());
            }
        }

        return wrongAnswers;
    }


    public int getExerciseIndex() {
        return exerciseIndex;
    }

    public int getCardCount() {
        return cardForExerciseCount;
    }

    public void createTraining(Settings settings, List<Exercise> allExerciseList) {
        exerciseList = new ArrayList<Exercise>();
        exerciseIndex = 0;
        this.setTimeLimit(settings.getTimelimit());
        if (settings.isTimeLimit()) {                                    // Zeitbasiertes Training, es gibt keine Begrenzung f�r die Anzahl der �bungen
            exerciseList = allExerciseList;
        } else {                                                         // kein zeitbasiertes Training, eingestellte Anzahl an �bungen herauspicken
            pickRandomExercises(settings.getExerciseLimit(), allExerciseList);
        }
    }


    public void createImageCardTraining(Settings settings, List<ImageCard> allCardsList) {
        cardList = new ArrayList<ImageCardExercise>();
        exerciseIndex = 0;
        cardForExerciseCount = settings.getCardCountForExercise();
        this.setTimeLimit(settings.getTimelimit());
        if (settings.isTimeLimit()) {                                    // Zeitbasiertes Training, es gibt keine Begrenzung f�r die Anzahl der �bungen
            generateCardExercisesFromAllCards(allCardsList);
        } else {                                                         // kein zeitbasiertes Training, eingestellte Anzahl an �bungen herauspicken
            pickRandomCards(settings.getExerciseLimit() * cardForExerciseCount, allCardsList);
        }

    }

    public void createImageCardTrainingForRepeatModus(Settings settings, List<ImageCardExercise> cardExercises) {
        cardList = cardExercises;
        exerciseIndex = 0;
        this.setTimeLimit(settings.getTimelimit());
    }

    public void generateCardExercisesFromAllCards(List<ImageCard> allCardsList) {

        System.out.println("<<< AllCardSize before " + allCardsList.size());
        System.out.println("<<< cardForExerciseCount " + cardForExerciseCount);
        int delCount = allCardsList.size() % cardForExerciseCount;

        int d = 0;
        while (d < delCount) {
            allCardsList.remove(0);
            d++;
        }
        System.out.println("<<< AllCardSize after " + allCardsList.size());
        int cardCount = 0;

        while (cardCount < allCardsList.size()) {
            int i = 0;
            ImageCardExercise cardExercise = new ImageCardExercise();
            cardExercise.setUserRelation(ImageCardExercise.NO_RELATION);
            while (i < cardForExerciseCount) {
                ImageCard card = allCardsList.get(cardCount + i);
                cardExercise.getImageCards().add(card);
                i++;

            }
            cardList.add(cardExercise);
            cardCount = cardCount + cardForExerciseCount;

        }

        System.out.println("<<< cardList -> Exercises   size " + cardList.size());

    }

    /**
     * begrenzt die Liste der Uebungen fuer dieses Training, den Settings entsprechend, auf zufaellig ausgewaehlte Uebungen
     *
     * @param exerciseLimit
     * @param allExerciseList
     */
    private void pickRandomExercises(int exerciseLimit, List<Exercise> allExerciseList) {
        if (exerciseLimit == allExerciseList.size()) {
            exerciseList = allExerciseList;
            return;
        }
        for (int i = 0; i < exerciseLimit && i < allExerciseList.size(); i++) {
            Random r = new Random();                                                                // zufaellige Uebung bestimmen
            int exerciseOrder = r.nextInt(allExerciseList.size());
            Exercise exercise = allExerciseList.get(exerciseOrder);
            if (isExerciseInList(exercise)) {                                                      // pruefen, ob Uebung zurvor der Liste hinzugefuegt wurde
                i--;
                continue;
            }
            exerciseList.add(exercise);
        }
    }

    private void pickRandomCards(int exerciseLimit, List<ImageCard> allCardsList) {
        if (exerciseLimit >= allCardsList.size()) {
            generateCardExercisesFromAllCards(allCardsList);
            return;
        }
        List<ImageCard> tmpcardList = new ArrayList<ImageCard>();

        for (int i = 0; i < exerciseLimit && i < allCardsList.size(); i++) {
            Random r = new Random();                                                                // zufaellige Uebung bestimmen
            int exerciseOrder = r.nextInt(allCardsList.size());
            ImageCard card = allCardsList.get(exerciseOrder);
            if (isImageCardInList(card, tmpcardList)) {                                                      // pruefen, ob Uebung zurvor der Liste hinzugefuegt wurde
                i--;
                continue;
            }
            tmpcardList.add(card);
        }

        generateCardExercisesFromAllCards(tmpcardList);
    }

    /**
     * prueft, ob die Uebung bereits der aktuellen Trainingsliste hinzugefuegt wurde
     *
     * @param exercise
     * @return
     */
    private boolean isExerciseInList(Exercise exercise) {
        for (Exercise exercise1 : exerciseList) {
            if (exercise1.getId().equals(exercise.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isImageCardInList(ImageCard imagecard, List<ImageCard> compareArray) {
        for (ImageCard card : compareArray) {
            if (card.getImagemdediaID().equals(imagecard.getImagemdediaID())) {
                return true;
            }
        }
        return false;
    }


    /**
     * bricht einen Trainingslauf ab und verwirft alle zugehoerigen Daten
     */
    public void abortTraining() {
        instance = new TrainingSingleton();
    }


    /**
     * pr�ft, ob die �bung einen erlaubten �bungstypen hat
     * @param exerciseTypeID
     * @param allowedExerciseTypes
     * @return
     */
    /*private boolean hasCorrectType(String exerciseTypeID, ArrayList<String> allowedExerciseTypes) {
        for (String allowedExerciseType : allowedExerciseTypes) {
            if (allowedExerciseType.equalsIgnoreCase(exerciseTypeID)) return true;
        }
        return false;
    }*/

    /**
     * gibt die das folgende Exercise-Objekt zurueck
     *
     * @return
     */
    public Exercise getFirstExercise() throws IndexOutOfBoundsException {
        if (getExerciseIndex() + 1 > exerciseList.size()) {
            return null;
        }
        return getCurrentExercise();
    }

    public ImageCardExercise getFirstCardExercise() throws IndexOutOfBoundsException {

        if (getExerciseIndex() > cardList.size()) {
            return null;
        }
        return getCurrentCardExercise();
    }

    /**
     * gibt die das folgende Exercise-Objekt zurueck
     *
     * @return
     */
    public Exercise getNextExercise() throws IndexOutOfBoundsException {
        if (getExerciseIndex() + 1 >= exerciseList.size()) {
            return null;
        }
        exerciseIndex++;
        return getCurrentExercise();
    }

    public ImageCardExercise getNextCards() throws IndexOutOfBoundsException {
        if (getExerciseIndex() + 1 >= cardList.size()) {
            return null;
        }
        exerciseIndex++;
        return getCurrentCardExercise();

    }

    /**
     * gibt das vorige Exercise-Objekt zurueck
     *
     * @return
     */
    public Exercise getPreviousExercise() {
        if (getExerciseIndex() - 1 < 0) {
            return null;
        }
        exerciseIndex--;
        return getCurrentExercise();
    }

    public ImageCardExercise getPreviousCardExercise() {
        if (getExerciseIndex() - 1 < 0) {
            return null;
        }
        exerciseIndex--;
        ImageCardExercise currentCard = getCurrentCardExercise();
        currentCard.setUserRelation(ImageCardExercise.NO_RELATION);
        return currentCard;
    }

    /**
     * gibt das aktuelle Uebungs-Objekt zurueck
     *
     * @return
     */
    public Exercise getCurrentExercise() {
        if ((getExerciseIndex() < 0) || (getExerciseIndex() >= exerciseList.size())) {
            throw new IndexOutOfBoundsException("ExerciseIndex out of bound.");
        }
        return exerciseList.get(getExerciseIndex());
    }


    public ImageCardExercise getCurrentCardExercise() {
        if ((getExerciseIndex() < 0) || (getExerciseIndex() >= cardList.size())) {
            throw new IndexOutOfBoundsException("CARDExerciseIndex out of bound.");
        }
        return cardList.get(getExerciseIndex());
    }

    /* public List<ImageCard> getCurrentCardList(){

        ArrayList<ImageCard> currentCardList = new ArrayList<ImageCard>();
        if ((getExerciseIndex() < 0) || (getExerciseIndex()*cardForExerciseCount >= cardList.size()) ){
            throw new IndexOutOfBoundsException("ExerciseIndex out of bound.");
        }

        currentCardList.clear();

        int i = 0;

        while (i<cardForExerciseCount){

            ImageCard card = cardList.get(getExerciseIndex()+i);
            currentCardList.add(card);
            currentCardmediaIds.add(card.getImagemdediaID());
            i++;
        }

        System.out.println("<<<<< getCurrentCardList currentCardList.size "+currentCardList.size());

        return currentCardList;
    }*/

    /**
     * beendet das aktuelle Training
     */
    public void finishTraining(String elapsedTime) {
        this.elapsedTime = elapsedTime;
        if (exerciseList == null || exerciseList.size() == 0) return;
        // alle unbeantworteten Fragen als falsch markieren und in die Wiedervorlage schieben
        //int unanswered = exerciseList.size() - (getWrongAnswers().size() + getCorrectAnswers().size());
    }
}
