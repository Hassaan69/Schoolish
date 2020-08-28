package com.example.schoolish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.example.schoolish.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MapsFragment extends Fragment {

    private int strokeColor = 0xff167AD3; //blue outline
    private int shadeColor = 0x20167AD3; //opaque blue fill
    boolean doubleBackToExitPressedOnce = false;
    private boolean mLocationPermissionGranted = false;
    private List<SchoolModel> schoolList;
    private Integer preferenceDistance;
    private String preferenceType;
    private Integer preferennceFee;
    private AutoCompleteTextView searchView;
    private ImageButton myLocationButton;
    private List<SearchItem> searchItems;
    private List<Marker> markerList = new ArrayList<>();
    private SchoolSharedViewModel schoolSharedViewModel;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            //Checking if the location is granted
            if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.183270, 66.996452), 12));
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(20 * 1000);
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        for (Location templocation : locationResult.getLocations()) {
                            if (templocation != null) {
                                latitude = templocation.getLatitude();
                                longitude = templocation.getLongitude();
                            }

                        }
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
                        //WHEN LOCATION AND PREFERENCE CHANGE
                        schoolSharedViewModel.initLocation(getContext());
                        schoolSharedViewModel.getSearchPreferencesItemMutableLiveData()
                                .observe(getViewLifecycleOwner(), searchPreferencesItem -> {
                                    preferenceType = searchPreferencesItem.getTypePreference();
                                    preferenceDistance = searchPreferencesItem.getDistancePreference();
                                    preferennceFee = searchPreferencesItem.getFeePreference();
                                    Log.d("CheckPreference", "onChanged: " + preferenceType + preferenceDistance + preferennceFee);
                                    if (preferennceFee == 0) {
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
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithDistance(markerData, googleMap, geoPoint);
                                            }

                                        } else if (preferenceType.equals("Co Education") && preferenceDistance == 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();

                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                            }
                                        } else if (preferenceType.equals("Co Education") && preferenceDistance > 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                            }
                                        } else if (preferenceType.equals("For Boys") && preferenceDistance == 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();

                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                            }
                                        } else if (preferenceType.equals("For Boys") && preferenceDistance > 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                            }
                                        } else if (preferenceType.equals("For Girls") && preferenceDistance == 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();

                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypeandWithoutDistance(markerData, googleMap);
                                            }
                                        } else if (preferenceType.equals("For Girls") && preferenceDistance > 0) {
//                                    Log.d("Check", "onChanged: HERE ");
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypAndDistance(markerData, googleMap, geoPoint);
                                            }
                                        }
                                    } else if (preferennceFee > 0) {

                                        if (preferenceDistance > 0 && !preferenceType.equals("Show All")) {
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithType_Distance_Fee(markerData, googleMap, geoPoint);
                                            }
                                        } else if (preferenceDistance == 0 && !preferenceType.equals("Show All")) {
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithTypeAndFee(markerData, googleMap);
                                            }
                                        } else if (preferenceType.equals("Show All") && preferenceDistance == 0) {
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                String title = schoolList.get(i).getSchoolName();
                                                searchItems.add(new SearchItem(title));
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithoutDistanceAndWithFees(markerData, googleMap);
                                            }
                                        } else if (preferenceType.equals("Show All") && preferenceDistance > 0) {
                                            googleMap.clear();
                                            searchItems = new ArrayList<>();
                                            for (int i = 0; i < schoolList.size(); i++) {
                                                SchoolModel markerData = schoolList.get(i);
                                                setMarkersWithDistanceAndWithFees(markerData, googleMap, geoPoint);
                                            }
                                        }
                                    }
                                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                    googleMap.addCircle(new CircleOptions()
                                            .center(latLng)
                                            .radius(preferenceDistance * 1000)
                                            .fillColor(shadeColor)
                                            .strokeColor(strokeColor)
                                            .strokeWidth(8f));

                                    searchView.setOnItemClickListener((parent, view, position, id) -> {
                                        String selection = parent.getItemAtPosition(position).toString();
                                        for (Marker marker : markerList) {
                                            if (Objects.equals(marker.getTitle(), selection)) {
                                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
                                                searchView.setText("");
                                                return;
                                            }
                                        }
                                    });

                                    myLocationButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                                                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                                ;
                                            }
                                            googleMap.setMyLocationEnabled(true);
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.5f));
                                        }
                                    });
                                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                        @Override
                                        public void onInfoWindowClick(Marker marker) {
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
                                        }
                                    });

                                });
                    }
                };
//
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            }
            // if permission not granted
            else {

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.199, 67.00971), 12));
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle bundle = new Bundle(getArguments());
        schoolList = new ArrayList<>(Objects.requireNonNull(bundle.getParcelableArrayList("schoolArray")));
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
        myLocationButton = Objects.requireNonNull(getActivity()).findViewById(R.id.myLocationButton);
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
        int kmInDec = Integer.parseInt(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }


    private void setMarkersWithoutDistanceAndWithFees(SchoolModel markerData, GoogleMap googleMap) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        int fee = Integer.parseInt(markerData.getFees());
        if (fee <= preferennceFee) {
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
    }

    private void setMarkersWithDistanceAndWithFees(SchoolModel markerData, GoogleMap googleMap, GeoPoint geoPoint) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        LatLng latLngfrom = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        double distance = CalculationByDistance(latLngfrom, school);
        int fee = Integer.parseInt(markerData.getFees());
        if (preferenceDistance >= distance) {
            if (fee <= preferennceFee) {
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

    private void setMarkersWithType_Distance_Fee(SchoolModel markerData, GoogleMap googleMap, GeoPoint geoPoint) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        LatLng latLngfrom = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        double distance = CalculationByDistance(latLngfrom, school);
        int fee = Integer.parseInt(markerData.getFees());
        if (markerData.getType().equals(preferenceType)) {
            if (preferenceDistance >= distance) {
                if (fee <= preferennceFee) {
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

    private void setMarkersWithTypeAndFee(SchoolModel markerData, GoogleMap googleMap) {
        String location = markerData.getLocation();
        location = location.substring(location.indexOf('@'));
        location = location.substring(1, location.indexOf('/'));
        String[] latLongValues = location.split(",");
        double latitude = Double.parseDouble(latLongValues[0]);
        double longitude = Double.parseDouble(latLongValues[1]);
        LatLng school = new LatLng(latitude, longitude);
        int fee = Integer.parseInt(markerData.getFees());
        if (markerData.getType().equals(preferenceType)) {
            if (fee <= preferennceFee) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        String type = prefs.getString("schoolType", "Show All");
        int distance = prefs.getInt("distance", 0);
        int fee = prefs.getInt("fee", 0);
        fee = fee * 500;
        Toast.makeText(getContext(), "Showing Schools of type : " + " ' " + type + " ' " + " within " + distance + " KMs" + " and " + "fee in range "
                + fee, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        if (fusedLocationProviderClient != null) {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//        }
//    }
}