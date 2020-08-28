package com.example.schoolish;

public class SearchPreferencesItem {

    private String typePreference;
    private int distancePreference;
    private int feePreference;

    public SearchPreferencesItem(String typePreference, int distancePreference) {
        this.typePreference = typePreference;
        this.distancePreference = distancePreference;
    }

    public SearchPreferencesItem() {
    }

    public SearchPreferencesItem(String typePreference, int distancePreference, int feePreference) {
        this.typePreference = typePreference;
        this.distancePreference = distancePreference;
        this.feePreference = feePreference;
    }

    public int getFeePreference() {
        return feePreference;
    }

    public void setFeePreference(int feePreference) {
        this.feePreference = feePreference;
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
