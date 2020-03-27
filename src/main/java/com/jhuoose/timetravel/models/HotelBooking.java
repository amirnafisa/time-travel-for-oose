package com.jhuoose.timetravel.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

public class HotelBooking extends Booking {

    private String userLogin, hotel_name, address1, address2, city_name, country, postal_code, lat, lon, checkin_date,
    checkout_date, time_zone_id, created;
    private Integer number_of_rooms;

    public HotelBooking(String userLogin, long id, int segment_index, String source, String type, String confirmation_no,
                        String first_name, String last_name, String hotel_name, String address1, String address2,
                        String city_name, String country, String postal_code, String lat, String lon,
                        String checkin_date, String checkout_date, String time_zone_id, Integer number_of_rooms,
                        String created, List<String> travelers, Boolean is_new_booking) {
        super(userLogin, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers, is_new_booking);
        this.hotel_name = hotel_name;
        this.address1 = address1;
        this.address2 = address2;
        this.city_name = city_name;
        this.country = country;
        this.postal_code = postal_code;
        this.lat = lat;
        this.lon = lon;
        this.checkin_date = checkin_date;
        this.checkout_date = checkout_date;
        this.time_zone_id = time_zone_id;
        this.number_of_rooms = number_of_rooms;
        this.created = created;
    }

    public HotelBooking(String userLogin, int booking_identifier, long id, int segment_index, String source, String type, String confirmation_no,
                        String first_name, String last_name, String hotel_name, String address1, String address2,
                        String city_name, String country, String postal_code, String lat, String lon,
                        String checkin_date, String checkout_date, String time_zone_id, Integer number_of_rooms,
                        String created, List<String> travelers, Boolean is_new_booking) {
        super(userLogin, booking_identifier, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers, is_new_booking);
        this.hotel_name = hotel_name;
        this.address1 = address1;
        this.address2 = address2;
        this.city_name = city_name;
        this.country = country;
        this.postal_code = postal_code;
        this.lat = lat;
        this.lon = lon;
        this.checkin_date = checkin_date;
        this.checkout_date = checkout_date;
        this.time_zone_id = time_zone_id;
        this.number_of_rooms = number_of_rooms;
        this.created = created;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getCheckin_date() {
        return checkin_date;
    }

    public void setCheckin_date(String checkin_date) {
        this.checkin_date = checkin_date;
    }

    public String getCheckout_date() {
        return checkout_date;
    }

    public void setCheckout_date(String checkout_date) {
        this.checkout_date = checkout_date;
    }

    public String getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(String time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Integer getNumber_of_rooms() {
        return number_of_rooms;
    }

    public void setNumber_of_rooms(Integer number_of_rooms) {
        this.number_of_rooms = number_of_rooms;
    }

    public static Comparator<HotelBooking> comparator = new Comparator<HotelBooking>() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public int compare(HotelBooking obj1, HotelBooking obj2) {
            try {
                return dateFormat.parse(obj1.getCheckin_date()).compareTo(dateFormat.parse(obj2.getCheckin_date()));
            } catch (ParseException exception) {
                throw new IllegalArgumentException(exception);
            }
        }
    };
}
