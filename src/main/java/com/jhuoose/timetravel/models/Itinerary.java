package com.jhuoose.timetravel.models;

import java.util.List;

public class Itinerary {
    private String userLogin;
    private int identifier;
    private List<Booking> bookings;

    public Itinerary(String userLogin, int identifier, List<Booking> bookings) {
        this.userLogin = userLogin;
        this.identifier = identifier;
        this.bookings = bookings;
    }

    public String getUserLogin() { return this.userLogin;}

    public void setUserLogin(String userLogin) {this.userLogin = userLogin;}

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public boolean equals(Object obj) {
        Itinerary itinerary = (Itinerary) obj;
        return (this.getIdentifier() == itinerary.getIdentifier());
    }
}