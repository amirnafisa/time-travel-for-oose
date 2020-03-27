package com.jhuoose.timetravel.models;

import java.util.List;

public class Discrepancy {
    private int identifier;
    private List<Integer> itineraryIdentifiers;

    private String userLogin;

    public Discrepancy(String userLogin, int identifier, List<Integer> itineraryIdentifiers) {
        this.identifier = identifier;
        this.itineraryIdentifiers = itineraryIdentifiers;
        this.userLogin = userLogin;
    }

    public String getUserLogin() { return userLogin; }

    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public List<Integer> getItineraryIdentifiers() {
        return itineraryIdentifiers;
    }

    public void setItineraryIdentifiers(List<Integer> itineraryIdentifiers) {
        this.itineraryIdentifiers = itineraryIdentifiers;
    }
}
