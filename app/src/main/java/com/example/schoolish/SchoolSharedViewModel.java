package com.example.schoolish;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Objects;

public class SchoolSharedViewModel extends ViewModel {
    private MutableLiveData<List<SchoolModel>> schoolList;
    private MutableLiveData<GeoPoint> geoPointMutableLiveData ;
    private MutableLiveData<String> searchPreference;
    private MutableLiveData<SearchPreferencesItem> searchPreferencesItemMutableLiveData;




    private SchoolRepository schoolRepository;

    public void init() {
        schoolRepository = SchoolRepository.getInstance();
        schoolRepository.fetchSchoolData();
        schoolList = schoolRepository.schoolList;
    }

    public void initLocation(Context context)
    {
        schoolRepository = SchoolRepository.getInstance();
        schoolRepository.getLocation(context);
        geoPointMutableLiveData = schoolRepository.getGeoPointMutableLiveData();
        schoolRepository.getSearchPreferences(context);
        searchPreference = schoolRepository.searchPreference;
        schoolRepository.getSearchPreferences(context);
        searchPreferencesItemMutableLiveData = schoolRepository.preferencesItemMutableLiveData;
    }

    public void initDistance()
    {
        schoolRepository = SchoolRepository.getInstance();
        searchPreferencesItemMutableLiveData = schoolRepository.preferencesItemMutableLiveData;

    }

//    public void RemoveListener() {
//        schoolRepository.listenerRegistration.remove();
//    }


    public MutableLiveData<List<SchoolModel>> getSchoolList() {
        return schoolList;
    }
    public MutableLiveData<GeoPoint> getGeoPointMutableLiveData() {
        return geoPointMutableLiveData;
    }

    public MutableLiveData<String> getSearchPreference() {
        return searchPreference;
    }

    public MutableLiveData<SearchPreferencesItem> getSearchPreferencesItemMutableLiveData() {
        return searchPreferencesItemMutableLiveData;
    }
}
