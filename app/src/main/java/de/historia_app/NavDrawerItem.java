package de.historia_app;


import android.support.annotation.NonNull;

public class NavDrawerItem {

    public enum ID {
        SELECT_AREA,
        EXPLORE_AREA,
        SELECT_TOUR,
        EXPLORE_DATA,
        LOAD_TOUR,
        ABOUT,
        IMPRESSUM,
        PRIVACY_POLICY
    }

    private final ID id;

    private final String title;

    private final boolean isSubItem;

    private boolean displayed = true;

    private Object relatedObject;

    public NavDrawerItem(@NonNull ID id, @NonNull String title, boolean isSubItem) {
        this.id = id;
        this.title = title;
        this.isSubItem = isSubItem;
    }

    public @NonNull ID getId() {
        return id;
    }

    public @NonNull String getTitle() {
        return title;
    }

    public boolean isSubItem() {
        return isSubItem;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public Object getRelatedObject() {
        return relatedObject;
    }

    public void setRelatedObject(Object relatedObject) {
        this.relatedObject = relatedObject;
    }
}
