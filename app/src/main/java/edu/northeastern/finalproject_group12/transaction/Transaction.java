package edu.northeastern.finalproject_group12.transaction;

import android.os.Parcel;
import android.os.Parcelable;

public class Transaction implements Parcelable {
    private int transactionId;
    private String category;
    private double amount;
    private String date;
    private double latitude;
    private double longitude;
    private String notes;


    public Transaction(int transactionId, String category, double amount, String date, double latitude, double longitude, String notes) {
        this.transactionId = transactionId;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.notes = notes;
    }


    public Transaction(int transactionId, String category, double amount, String date) {
        this.transactionId = transactionId;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
        this.notes = ""; // Default empty notes
    }


    protected Transaction(Parcel in) {
        transactionId = in.readInt();
        category = in.readString();
        amount = in.readDouble();
        date = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        notes = in.readString();
    }


    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(transactionId);
        dest.writeString(category);
        dest.writeDouble(amount);
        dest.writeString(date);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(notes);
    }


    public int getTransactionId() {
        return transactionId;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNotes() {
        return notes;
    }
}
