package de.salait.speechcare.data;

import java.util.ArrayList;

/**
 * Created by speechcare on 24.04.18.
 */

public class ImageCardExercise {

    public static final int NO_RELATION = 0;
    public static final int RELATION_CORRECT = 1;
    public static final int RELATION_WRONG = 2;

    private ArrayList<ImageCard> cards;
    private int userRelation;  // 0 -> keine Zuordnung ; 1 -> Haken/Correct/Nicht in die Wiedervorlage ; 2 Kreuz/Wrong/in Wiedervorlage speichern


    public ArrayList<ImageCard> getImageCards() {
        if (cards == null) {
            cards = new ArrayList<ImageCard>();
        }
        return cards;
    }

    public void setImageCards(ArrayList<ImageCard> imageCards) {
        this.cards = imageCards;
    }

    public int getUserRelation() {
        return userRelation;
    }

    public void setUserRelation(int userRelation) {
        this.userRelation = userRelation;
    }

}
