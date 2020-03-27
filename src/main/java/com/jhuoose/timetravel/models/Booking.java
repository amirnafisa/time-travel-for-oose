package com.jhuoose.timetravel.models;

import java.util.List;

public class Booking {
    private int booking_identifier = 0;
    private long mail_identifier;
    private int segment_index;
    private String userLogin, source, booking_type, confirmation_no, first_name, last_name;


    private List<String> travelers;
    private Boolean is_new_booking;

    private static int booking_counts = 0;

    public Booking(String userLogin, long id, int segment_index, String source, String type, String confirmation_no, String first_name, String last_name, List<String> travelers,
                   Boolean is_new_booking) {
        init_other_params(userLogin, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers,
                is_new_booking);
    }

    public Booking(String userLogin, int booking_id, long id, int segment_index, String source, String type, String confirmation_no, String first_name, String last_name, List<String> travelers,
                   Boolean is_new_booking) {
        this.booking_identifier = booking_id;
        init_other_params(userLogin, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers,
                is_new_booking);
    }

    private void init_other_params (String userLogin, long id, int segment_index, String source, String type, String confirmation_no, String first_name, String last_name, List<String> travelers,
                                    Boolean is_new_booking) {
        this.userLogin = userLogin;
        this.mail_identifier = id;
        this.segment_index = segment_index;
        this.source = source;
        this.booking_type = type;
        this.confirmation_no = confirmation_no;
        this.first_name = first_name;
        this.last_name = last_name;
        this.travelers = travelers;
        this.is_new_booking = is_new_booking;
    }

    public String getUserLogin () { return this.userLogin;}

    public void setUserLogin (String userLogin) {this.userLogin = userLogin;}

    public void set_booking_identifier (int booking_identifier) { this.booking_identifier = booking_identifier;}

    public int get_booking_identifier () {
        return this.booking_identifier;
    }

    public long get_mail_identifier () {
        return this.mail_identifier;
    }

    public int get_segment_index() {
        return this.segment_index;
    }

    public void set_segment_index(int segment_index) {
        this.segment_index = segment_index;
    }

    public String get_booking_source () {
        return this.source;
    }

    public String get_type () {
        return this.booking_type;
    }

    public String get_confirmation_no () {
        return this.confirmation_no;
    }

    public String get_first_name () {
        return this.first_name;
    }

    public String get_last_name () {
        return this.last_name;
    }

    public List<String> get_travelers () {
        return this.travelers;
    }

    public Boolean is_new_booking() {
        return is_new_booking;
    }
    public void update_is_new_booking(Boolean is_new_booking) {
        this.is_new_booking = is_new_booking;
    }
}
