package com.example.schoolish;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import java.util.Objects;

public class FilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Quick Filter");
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final Preference schooltype = findPreference("schoolType");
            final Preference schoolDistance = findPreference("distance");
            final SearchPreferencesItem searchPreferencesItem = new SearchPreferencesItem();
            schoolDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SchoolRepository schoolRepository = SchoolRepository.getInstance();
                    searchPreferencesItem.setDistancePreference((Integer) newValue);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    searchPreferencesItem.setTypePreference(prefs.getString("schoolType", "Co Education"));
                    schoolRepository.preferencesItemMutableLiveData.postValue(searchPreferencesItem);
                    return true;
                }
            });
            assert schooltype != null;
            schooltype.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    int distance = prefs.getInt("distance", 0);
                    SchoolRepository schoolRepository = SchoolRepository.getInstance();
                    schoolRepository.searchPreference.postValue(newValue.toString());
                    searchPreferencesItem.setTypePreference(newValue.toString());
                    searchPreferencesItem.setDistancePreference(distance);
                    schoolRepository.preferencesItemMutableLiveData.postValue(searchPreferencesItem);
                    return true;
                }
            });

        }

    }
}