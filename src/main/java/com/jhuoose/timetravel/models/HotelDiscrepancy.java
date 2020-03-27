package com.jhuoose.timetravel.models;

import java.util.List;

public class HotelDiscrepancy extends Discrepancy {
    private String city;

    public HotelDiscrepancy(String userLogin, int identifier, List<Integer> itineraryIdentifiers, String city) {
        super(userLogin, identifier, itineraryIdentifiers);
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
