package com.example.schoolish;

public class SearchPreferencesItem {

    private String typePreference;
    private int distancePreference;

    public SearchPreferencesItem(String typePreference, int distancePreference) {
        this.typePreference = typePreference;
        this.distancePreference = distancePreference;
    }

    public SearchPreferencesItem() {
    }

    public String getTypePreference() {
        return typePreference;
    }

    public void setTypePreference(String typePreference) {
        this.typePreference = typePreference;
    }

    public int getDistancePreference() {
        return distancePreference;
    }

    public void setDistancePreference(int distancePreference) {
        this.distancePreference = distancePreference;
    }
}
