package com.example.schoolish;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {
    private SchoolModel schoolDetails;
    private TextView schoolName , schoolAddress;
    private Chip chipOrganization, chipSchoolType, chipDistance , chipFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        schoolName = findViewById(R.id.schoolName);
        schoolAddress = findViewById(R.id.schoolAddress);
        chipOrganization = findViewById(R.id.chipOrganization);
        chipSchoolType = findViewById(R.id.chipSchoolType);
        chipDistance = findViewById(R.id.chipDistance);
        chipFee = findViewById(R.id.chipFee) ;

        Bundle bundle = getIntent().getBundleExtra("schoolData");
        assert bundle != null;
        double latitude = bundle.getDouble("latitude");
        double longitude = bundle.getDouble("longitude");
        double distanceinkms = bundle.getDouble("distance");
        SchoolModel schoolModel = (SchoolModel) bundle.get("schoolDetails");

        distanceinkms = Math.round(distanceinkms * 100.0) / 100.0;
        try {
            schoolAddress.setText(getLocationFromLatLong(latitude,longitude));
        } catch (IOException e) {
            e.printStackTrace();
        }

        schoolDetails = (SchoolModel) bundle.get("schoolDetails");
        assert schoolDetails != null;

        schoolName.setText(schoolDetails.getSchoolName());
        chipOrganization.setText(schoolDetails.getOrganization());
        chipSchoolType.setText(schoolDetails.getType());
        chipDistance.setText("Distance - " + distanceinkms +" KM");
        assert schoolModel != null;
        chipFee.setText("Monthly Fees - " + schoolModel.getFees() + " RS");


    }

    private String getLocationFromLatLong(double latitude , double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(latitude,longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); //
        return address ;
    }
}