package com.example.schoolish;

import android.os.Parcel;
import android.os.Parcelable;

public class SchoolModel implements Parcelable {

    private String fees;
    private String location;
    private String organization;
    private String principalName;
    private String schoolName;
    private String  type;

    public SchoolModel() {
    }

    public SchoolModel(String fees, String location, String organization, String principalName, String schoolName, String type) {
        this.fees = fees;
        this.location = location;
        this.organization = organization;
        this.principalName = principalName;
        this.schoolName = schoolName;
        this.type = type;
    }

    protected SchoolModel(Parcel in) {
        fees = in.readString();
        location = in.readString();
        organization = in.readString();
        principalName = in.readString();
        schoolName = in.readString();
        type = in.readString();
    }

    public static final Creator<SchoolModel> CREATOR = new Creator<SchoolModel>() {
        @Override
        public SchoolModel createFromParcel(Parcel in) {
            return new SchoolModel(in);
        }

        @Override
        public SchoolModel[] newArray(int size) {
            return new SchoolModel[size];
        }
    };

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fees);
        dest.writeString(location);
        dest.writeString(organization);
        dest.writeString(principalName);
        dest.writeString(schoolName);
        dest.writeString(type);
    }
}
