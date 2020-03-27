package com.jhuoose.timetravel.models;

import java.util.List;

public class AirDiscrepancy extends Discrepancy {
    private String fromCity;
    private String toCity;

    public AirDiscrepancy(String userLogin, int identifier, List<Integer> itineraryIdentifiers, String fromCity, String toCity) {
        super(userLogin, identifier, itineraryIdentifiers);
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }
}
