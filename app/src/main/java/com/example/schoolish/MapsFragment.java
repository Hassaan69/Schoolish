package com.example.schoolish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment {
    boolean doubleBackToExitPressedOnce = false;
    private List<SchoolModel> schoolList;
    private Integer preferenceDistance;
    private String preferenceType;
    private AutoCompleteTextView searchView;
    private List<SearchItem> searchItems;
    private List<Marker> markerList = new ArrayList<>();
    private SchoolSharedViewModel schoolSharedViewModel;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            schoolSharedViewModel.initLocation(getContext());
            schoolSharedViewModel.getSearchPreferencesItemMutableLiveData().observe(getViewLifecycleOwner(), new Observer<SearchPreferencesItem>() {
                @Override
                public void onChanged(SearchPreferencesItem searchPreferencesItem) {
                    preferenceType = searchPreferencesItem.getTypePreference();
                    preferenceDistance = searchPreferencesItem.getDistancePreference();
                    Log.d("CheckPreference", "onChanged: " + preferenceType + preferenceDistance);
                    schoolSharedViewModel.getGeoPointMutableLiveData().observe(getViewLifecycleOwner(), new Observer<GeoPoint>() {
                        @Override
                        public void onChanged(final GeoPoint geoPoint) {
                            Log.d("GG", "onChanged:  on geo point");
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            if (preferenceType.equals("Show All") && preferenceDistance == 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();
                                for (int i = 0; i < schoolList.size(); i++) {
                                    String title = schoolList.get(i).getSchoolName();
                                    searchItems.add(new SearchItem(title));
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithoutDistance(markerData, googleMap);
                                }
                            } else if (preferenceType.equals("Show All") && preferenceDistance > 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();
                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithDistance(markerData, googleMap, geoPoint);
                                }
                            } else if (preferenceType.equals("Co Education") && preferenceDistance == 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();

                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                }
                            } else if (preferenceType.equals("Co Education") && preferenceDistance > 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();
                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                }
                            } else if (preferenceType.equals("For Boys") && preferenceDistance == 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();

                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                }
                            } else if (preferenceType.equals("For Boys") && preferenceDistance > 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();
                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                }
                            } else if (preferenceType.equals("For Girls") && preferenceDistance == 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();

                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                }
                            } else if (preferenceType.equals("For Girls") && preferenceDistance > 0) {
                                Log.d("Check", "onChanged: HERE ");
                                googleMap.clear();
                                searchItems = new ArrayList<>();
                                for (int i = 0; i < schoolList.size(); i++) {
                                    SchoolModel markerData = schoolList.get(i);
                                    setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                }
                            }
                            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            googleMap.setMyLocationEnabled(true);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                            searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String selection = parent.getItemAtPosition(position).toString();
                                    for (Marker marker : markerList) {
                                        if (Objects.equals(marker.getTitle(), selection)) {
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
                                            return;
                                        }
                                    }
                                }
                            });
                            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    if (doubleBackToExitPressedOnce) {
                                        Intent intent = new Intent(getContext(), DetailsActivity.class);
                                        LatLng latLngfrom = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                        LatLng latLngto = marker.getPosition();
                                        double distance = CalculationByDistance(latLngfrom, latLngto);
                                        SchoolModel schoolModel = (SchoolModel) marker.getTag();
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("schoolDetails", schoolModel);
                                        bundle.putDouble("distance", distance);
                                        bundle.putDouble("latitude", latLngto.latitude);
                                        bundle.putDouble("longitude", latLngto.longitude);
                                        intent.putExtra("schoolData", bundle);
                                        startActivity(intent);
                                    } else {
                                        doubleBackToExitPressedOnce = true;
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                doubleBackToExitPressedOnce = false;
                                            }
                                        }, 2000);
                                    }
                                    return false;
                                }
                            });
                        }
                    });

                }
            });

        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        schoolList = new ArrayList<>();
        if (bundle != null) {
            schoolList = bundle.getParcelableArrayList("schoolArray");
        } else {
            Toast.makeText(context, "Something is wrong ", Toast.LENGTH_SHORT).show();
        }
        Log.d("SIZEDEIKH", "onCreateView: " + schoolList.size());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        schoolSharedViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(SchoolSharedViewModel.class);
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        searchView = view.findViewById(R.id.sv_location);
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void getPreferencesValue() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferenceType = prefs.getString("schoolType", "Co Education");
        preferenceDistance = prefs.getInt("distance", 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferencesValue();
        Toast.makeText(getContext(), "Showing Schools of type : " + " ' " + preferenceType + " ' " + " within " + preferenceDistance + " KMs", Toast.LENGTH_SHORT).show();
    }

    private void setMarkersWithoutDistance(SchoolModel markerData, GoogleMap googleMap) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(school);
        markerOptions.title(markerData.getSchoolName());
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
        Marker marker = googleMap.addMarker(markerOptions);
        markerList.add(marker);
        marker.setTag(markerData);
        AutoCompleteTvAdapter autoCompleteTvAdapter = new AutoCompleteTvAdapter(Objects.requireNonNull(getContext()), searchItems);
        searchView.setAdapter(autoCompleteTvAdapter);

    }

    private void setMarkersWithDistance(SchoolModel markerData, GoogleMap googleMap, GeoPoint geoPoint) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        LatLng latLngfrom = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        double distance = CalculationByDistance(latLngfrom, school);
        if (preferenceDistance >= distance) {
            Log.d("CHECLONG", "onChanged: " + markerData.getSchoolName());
            searchItems.add(new SearchItem(markerData.getSchoolName()));
            Log.d("CHECLONG", "onChanged: " + searchItems.size());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(school);
            markerOptions.title(markerData.getSchoolName());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(markerData);
            markerList.add(marker);
            AutoCompleteTvAdapter autoCompleteTvAdapter = new AutoCompleteTvAdapter(Objects.requireNonNull(getContext()), searchItems);
            searchView.setAdapter(autoCompleteTvAdapter);
        }

    }

    private void setMarkersWithTypeandWithoutDistance(SchoolModel markerData, GoogleMap googleMap) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        if (markerData.getType().equals(preferenceType)) {
            Log.d("CHECLONG", "onChanged: " + markerData.getSchoolName());
            SearchItem searchItem = new SearchItem(markerData.getSchoolName());
            searchItems.add(new SearchItem(markerData.getSchoolName()));
            Log.d("CHECLONG", "onChanged: " + searchItems.size());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(school);
            markerOptions.title(markerData.getSchoolName());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(markerData);
            markerList.add(marker);
            AutoCompleteTvAdapter autoCompleteTvAdapter = new AutoCompleteTvAdapter(Objects.requireNonNull(getContext()), searchItems);
            searchView.setAdapter(autoCompleteTvAdapter);

        }
    }

    private void setMarkersWithTypAndDistance(SchoolModel markerData, GoogleMap googleMap, GeoPoint geoPoint) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        LatLng latLngfrom = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        double distance = CalculationByDistance(latLngfrom, school);
        if (markerData.getType().equals(preferenceType)) {
            if (preferenceDistance >= distance) {
                searchItems.add(new SearchItem(markerData.getSchoolName()));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(school);
                markerOptions.title(markerData.getSchoolName());
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
                Marker marker = googleMap.addMarker(markerOptions);
                marker.setTag(markerData);
                markerList.add(marker);
                AutoCompleteTvAdapter autoCompleteTvAdapter = new AutoCompleteTvAdapter(Objects.requireNonNull(getContext()), searchItems);
                searchView.setAdapter(autoCompleteTvAdapter);
            }
        }

    }

}