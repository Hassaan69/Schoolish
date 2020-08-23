package com.example.schoolish;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.schoolish.Constants.ERROR_DIALOG_REQUEST;
import static com.example.schoolish.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.schoolish.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.schoolish.Constants.selectedButton;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SchoolSharedViewModel schoolSharedViewModel;
    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private ConstraintLayout content;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private List<SchoolModel> listOfSchools;
    private Toolbar toolbar;
    private HashMap<String, HashMap<String, String>> uploadData = new HashMap<>();
    private boolean mLocationPermissionGranted = false;
    MapsFragment mapsFragment = new MapsFragment();
    FirebaseUser user;
    private ToggleButton mainMapButton;
    private ToggleButton mainFilterButton;
    private ImageView userImage ;
    private TextView userName;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainMapButton = findViewById(R.id.mainMapButton);
        mainFilterButton = findViewById(R.id.mainFilterButton);
        content = findViewById(R.id.content);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        drawerLayout = findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = (drawerView.getWidth() - 200) * slideOffset;
                content.setTranslationX(slideX);
                float scaleFactoryX = 999f;
                float scaleFactoryY = 50f;
                content.setScaleX(1 - (slideOffset / scaleFactoryX));
                content.setScaleY(1 - (slideOffset / scaleFactoryY));
            }
        };
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        userImage = headerView.findViewById(R.id.ivDrawerProfile);
        userName = headerView.findViewById(R.id.tvDrawerName);


        uploadToFiresStore();
    }

    public void setMainMapButton(View view) {
        if (selectedButton.equals(Constants.Map)) {
            mainMapButton.setChecked(true);
            return;
        }

        mainFilterButton.setChecked(false);
        selectedButton = Constants.Map;
        initializeViewMoel();
    }

    public void setMainFilterButton(View view) {
        if (selectedButton.equals(Constants.Filter)) {
            mainFilterButton.setChecked(true);
            return;
        }
        mainMapButton.setChecked(false);
        selectedButton = Constants.Filter;
        startActivity(new Intent(MainActivity.this, FilterActivity.class));
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            // DO SOMETHING
//            getLocation();
            uploadToFiresStore();
            initializeViewMoel();
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, mapsFragment).commit();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    //DO SOMETHING
//                    getLocation();
                    uploadToFiresStore();
                    initializeViewMoel();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, mapsFragment).commit();


                } else {
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadToFiresStore() {
        FirebaseFirestore.getInstance().collection("SchoolsData").document("xhHBIaM7VUaJF39b2Pb2")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> map = documentSnapshot.getData();
                    assert map != null;
                    if (!map.isEmpty()) {
                        Log.d("UploadData", " Data already present ");
                    } else {
                        firestoreData();
                    }
                } else {
                    firestoreData();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void firestoreData() {
        Toast.makeText(MainActivity.this, "Inserting Data to FireStore", Toast.LENGTH_SHORT).show();
        try {
            listOfSchools = readCSV();
        } catch (IOException | CsvException e) {
            Log.d("CSVERROR", Objects.requireNonNull(e.getMessage()));
        }
        for (int i = 0; i < listOfSchools.size(); i++) {
            HashMap<String, String> schoolData = new HashMap<>();
            SchoolModel schoolModel = listOfSchools.get(i);
            schoolData.put("fees", schoolModel.getFees());
            schoolData.put("location", schoolModel.getLocation());
            schoolData.put("organization", schoolModel.getOrganization());
            schoolData.put("principalName", schoolModel.getPrincipalName());
            schoolData.put("schoolName", schoolModel.getSchoolName());
            schoolData.put("type", schoolModel.getType());
            String formated = String.format("%02d", i);
            String mapCounter = "SO" + formated;
            uploadData.put(mapCounter, schoolData);
        }
        FirebaseFirestore.getInstance().collection("SchoolsData").document("xhHBIaM7VUaJF39b2Pb2")
                .update("Schools", uploadData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Upload", "Data uploaded To FireStore");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Upload", Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    private List<SchoolModel> readCSV() throws IOException, CsvException {
        List<SchoolModel> schoolModelList = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(R.raw.school_data);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        CSVReader reader = new CSVReader(bufferedReader);
        reader.skip(1);
        String[] line;
        while ((line = reader.readNext()) != null) {
            SchoolModel schoolModel = getOneSchool(line);
            schoolModelList.add(schoolModel);
        }
        return schoolModelList;
    }

    private SchoolModel getOneSchool(String[] attributes) {
        String schoolname = attributes[0];
        String organization = attributes[1];
        String type = attributes[2];
        String fees = attributes[3];
        String principal = attributes[4];
        String location = attributes[5];

        SchoolModel schoolModel = new SchoolModel();
        schoolModel.setSchoolName(schoolname);
        schoolModel.setLocation(location);
        schoolModel.setOrganization(organization);
        schoolModel.setType(type);
        schoolModel.setPrincipalName(principal);
        schoolModel.setFees(fees);

        return schoolModel;
    }

    private void initializeViewMoel() {
        schoolSharedViewModel = new ViewModelProvider(this).get(SchoolSharedViewModel.class);
        schoolSharedViewModel.init();
        schoolSharedViewModel.getSchoolList().observe(this, new Observer<List<SchoolModel>>() {
            @Override
            public void onChanged(List<SchoolModel> schoolModels) {
                ArrayList<SchoolModel> list = new ArrayList<>(schoolModels.size());
                list.addAll(schoolModels);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("schoolArray", list);
                mapsFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, mapsFragment).commit();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                // DO SOMETHING
//                getLocation();
                uploadToFiresStore();
                initializeViewMoel();
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, mapsFragment).commit();

            } else {
                getLocationPermission();
            }
        }
        mainMapButton.setChecked(true);
        mainFilterButton.setChecked(false);
        selectedButton = Constants.Map;
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
         user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, SignInActivity.class));
        }
        else {
            userName.setText(user.getDisplayName());
            Picasso.get().load(user.getPhotoUrl()).into(userImage);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutDrawer:
                FirebaseAuth.getInstance().signOut();
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }


}