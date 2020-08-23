package com.example.schoolish;

public class SearchItem {
    private String schoolName;

    public SearchItem() {
    }

    public SearchItem(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    @Override
    public String toString() {
        return schoolName ;
    }
}
