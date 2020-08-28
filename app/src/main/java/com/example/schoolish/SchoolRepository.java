package com.example.schoolish;


import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchoolRepository {
    private static SchoolRepository instance;
    public MutableLiveData<List<SchoolModel>> schoolList = new MutableLiveData<>();
    private List<SchoolModel> dummyList = new ArrayList<>();
    public MutableLiveData<GeoPoint> geoPointMutableLiveData = new MutableLiveData<>();
//    public MutableLiveData<String> searchPreference = new MutableLiveData<>();
    public MutableLiveData<SearchPreferencesItem> preferencesItemMutableLiveData = new MutableLiveData<>();

//  public ListenerRegistration listenerRegistration;

    public static SchoolRepository getInstance() {
        if (instance == null) {
            instance = new SchoolRepository();
        }
        return instance;
    }

    public void fetchSchoolData() {
        FirebaseFirestore.getInstance().collection("SchoolsData")
                .document("xhHBIaM7VUaJF39b2Pb2")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.d("ListenerFirestore", "Failed", error);
                            return;
                        }
                        //if we are here we have data
                        if (value != null) {
                            parseData(value);
                        }
                    }
                });
    }

    private void parseData(DocumentSnapshot value) {
        HashMap<String, HashMap<String, String>> firestoreData = (HashMap<String, HashMap<String, String>>) value.get("Schools");
        assert firestoreData != null;
        List<HashMap<String, String>> hashMapArrayList = new ArrayList<>();
        for (int i = 0; i < firestoreData.size(); i++) {
            String formated = String.format("%02d", i);
            String mapCounter = "SO" + formated;
            hashMapArrayList.add(firestoreData.get(mapCounter));
        }
        for (int i = 0; i < hashMapArrayList.size(); i++) {
            SchoolModel schoolModel = new SchoolModel();
            schoolModel.setSchoolName(hashMapArrayList.get(i).get("schoolName"));
            schoolModel.setFees(hashMapArrayList.get(i).get("fees"));
            schoolModel.setOrganization(hashMapArrayList.get(i).get("organization"));
            schoolModel.setPrincipalName(hashMapArrayList.get(i).get("principalName"));
            schoolModel.setType(hashMapArrayList.get(i).get("type"));
            schoolModel.setLocation(hashMapArrayList.get(i).get("location"));
            Log.d("LISTWALA", "dummy: " + dummyList.size());
            dummyList.add(schoolModel);
        }
        schoolList.postValue(dummyList);
    }

    public void getLocation(final Context context)
    {
        FusedLocationProviderClient mFusedLocationPoint = LocationServices.getFusedLocationProviderClient(context);;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationPoint.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location!=null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        geoPointMutableLiveData.postValue(geoPoint);
                    }
                }
            }
        });
    }

    public MutableLiveData<GeoPoint> getGeoPointMutableLiveData() {
        return geoPointMutableLiveData;
    }
    public void getSearchPreferences(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String preferenceType = prefs.getString("schoolType", "Show All");
        int preferenceDistance = prefs.getInt("distance", 0);
        SharedPreferences freeprefs = context.getSharedPreferences("searchItems", context.MODE_PRIVATE );;
        int preferenceFee = freeprefs.getInt("fees",0);
        preferencesItemMutableLiveData.postValue(new SearchPreferencesItem(preferenceType,preferenceDistance,preferenceFee));
//        searchPreference.postValue(preferenceType);
        Log.d("CheckPref", "getPreferencesValue: " + preferenceType + preferenceDistance + preferenceFee ) ;
    }
}
