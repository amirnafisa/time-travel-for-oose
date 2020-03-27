package com.jhuoose.timetravel.models;

public class MetaData {
    private int identifier;
    private String email_last_synced_datetime;
    private String booking_last_synced_datetime;
    private String userLogin;

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public MetaData (String userLogin, String email_last_synced_datetime, String booking_last_synced_datetime) {
        this.userLogin = userLogin;
        this.email_last_synced_datetime = email_last_synced_datetime;
        this.booking_last_synced_datetime = booking_last_synced_datetime;
    }

    public MetaData (int identifier, String userLogin, String email_last_synced_datetime, String booking_last_synced_datetime) {
        this.identifier = identifier;
        this.userLogin = userLogin;
        this.email_last_synced_datetime = email_last_synced_datetime;
        this.booking_last_synced_datetime = booking_last_synced_datetime;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public String getEmail_last_synced_datetime() {
        return email_last_synced_datetime;
    }

    public void setEmail_last_synced_datetime(String email_last_synced_datetime) {
        this.email_last_synced_datetime = email_last_synced_datetime;
    }

    public String getBooking_last_synced_datetime() {
        return booking_last_synced_datetime;
    }

    public void setBooking_last_synced_datetime(String booking_last_synced_datetime) {
        this.booking_last_synced_datetime = booking_last_synced_datetime;
    }

}
