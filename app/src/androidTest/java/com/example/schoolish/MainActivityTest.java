package com.example.schoolish;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.exceptions.CsvException;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test
    public void onStart() throws IOException, CsvException {

//        List<SchoolModel> list = ReadCSV();
//        SchoolModel schoolModel = list.get(0);
//        FirebaseFirestore.getInstance().collection("SchoolsData").document("xhHBIaM7VUaJF39b2Pb2")
//                .update("Schools",schoolModel);

    }
    private List<SchoolModel> ReadCSV() throws IOException, CsvException {

        List<SchoolModel> schoolModelList = new ArrayList<>();
        InputStream inputStream = getInstrumentation().getContext().getResources().openRawResource(R.raw.school_data);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.readLine(); // for skipping the firstline
        String row = bufferedReader.readLine();
        while (row != null) {
            String[] attributes = row.split(",");
            SchoolModel schoolModel = getOneSchool(attributes);
            schoolModelList.add(schoolModel);
            row = bufferedReader.readLine();
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
        return new SchoolModel(schoolname, type, fees, location, organization, principal);
    }

}