package de.salait.speechcare.data;

/**
 * Created by Christian.Ramthun on 12.09.13.
 */
public class ExerciseType {

    private String id;
    private String title;
    private String exerciseTitle;
    private String icon;
    private String exerciseModel;
    private String grammarLayer;
    private boolean isDeactivated;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExerciseTitle() {
        return exerciseTitle;
    }

    public void setExerciseTitle(String exerciseTitle) {
        this.exerciseTitle = exerciseTitle;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getExerciseModel() {
        return exerciseModel;
    }

    public void setExerciseModel(String exerciseModel) {
        this.exerciseModel = exerciseModel;
    }

    public String getGrammarLayer() {
        return grammarLayer;
    }

    public void setGrammarLayer(String grammarLayer) {
        this.grammarLayer = grammarLayer;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void setDeactivated(boolean deactivated) {
        isDeactivated = deactivated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ExerciseType{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", exerciseTitle='" + exerciseTitle + '\'' +
                ", icon='" + icon + '\'' +
                ", exerciseModel='" + exerciseModel + '\'' +
                ", grammarLayer='" + grammarLayer + '\'' +
                ", isDeactivated=" + isDeactivated +
                ", description='" + description + '\'' +
                '}';
    }
}
