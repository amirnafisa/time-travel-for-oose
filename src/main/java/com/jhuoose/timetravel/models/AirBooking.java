package com.jhuoose.timetravel.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

public class AirBooking extends Booking {

    private String userLogin, airline, iata_code, flight_number, origin_airport, origin_city, origin_country, origin_lat, origin_lon,
            destination_airport, destination_city, destination_country, destination_lat, destination_lon,
            departure_datetime, departure_time_zone_id, arrival_datetime, arrival_time_zone_id, ticket_num;

    public AirBooking(String userLogin, long id, int segment_index, String source, String type, String confirmation_no, String first_name, String last_name, String airline,
                      String iata_code, String flight_number, String origin_airport, String origin_city, String origin_country, String origin_lat, String origin_lon,
                      String destination_airport, String destination_city, String destination_country, String destination_lat, String destination_lon,
                      String departure_datetime, String departure_time_zone_id, String arrival_datetime, String arrival_time_zone_id, String ticket_num, List<String> travelers,
                      Boolean is_new_booking) {
        super(userLogin, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers, is_new_booking);
        this.airline = airline;
        this.iata_code = iata_code;
        this.flight_number = flight_number;
        this.origin_airport = origin_airport;
        this.origin_city = origin_city;
        this.origin_country = origin_country;
        this.origin_lat = origin_lat;
        this.origin_lon = origin_lon;
        this.destination_airport = destination_airport;
        this.destination_city = destination_city;
        this.destination_country = destination_country;
        this.destination_lat = destination_lat;
        this.destination_lon = destination_lon;
        this.departure_datetime = departure_datetime;
        this.departure_time_zone_id = departure_time_zone_id;
        this.arrival_datetime = arrival_datetime;
        this.arrival_time_zone_id = arrival_time_zone_id;
        this.ticket_num = ticket_num;
    }
    public AirBooking(String userLogin, int booking_identifier, long id, int segment_index, String source, String type, String confirmation_no, String first_name, String last_name, String airline,
                           String iata_code, String flight_number, String origin_airport, String origin_city, String origin_country, String origin_lat, String origin_lon,
                           String destination_airport, String destination_city, String destination_country, String destination_lat, String destination_lon,
                           String departure_datetime, String departure_time_zone_id, String arrival_datetime, String arrival_time_zone_id, String ticket_num, List<String> travelers,
                           Boolean is_new_booking) {
        super(userLogin, booking_identifier, id, segment_index, source, type, confirmation_no, first_name, last_name, travelers, is_new_booking);
        this.airline = airline;
        this.iata_code = iata_code;
        this.flight_number = flight_number;
        this.origin_airport = origin_airport;
        this.origin_city = origin_city;
        this.origin_country = origin_country;
        this.origin_lat = origin_lat;
        this.origin_lon = origin_lon;
        this.destination_airport = destination_airport;
        this.destination_city = destination_city;
        this.destination_country = destination_country;
        this.destination_lat = destination_lat;
        this.destination_lon = destination_lon;
        this.departure_datetime = departure_datetime;
        this.departure_time_zone_id = departure_time_zone_id;
        this.arrival_datetime = arrival_datetime;
        this.arrival_time_zone_id = arrival_time_zone_id;
        this.ticket_num = ticket_num;
    }

    public String get_airline () {
        return this.airline;
    }

    public String get_iata_code () {
        return this.iata_code;
    }

    public String get_flight_number () {
        return this.flight_number;
    }

    public String get_origin_airport () {
        return this.origin_airport;
    }

    public String get_origin_city (){
        return this.origin_city;
    }

    public String get_origin_country () {
        return this.origin_country;
    }

    public String get_origin_lat () {
        return this.origin_lat;
    }

    public String get_origin_lon () {
        return this.origin_lon;
    }

    public String get_destination_airport () {
        return this.destination_airport;
    }

    public String get_destination_city () {
        return this.destination_city;
    }

    public String get_destination_country () {
        return this.destination_country;
    }

    public String get_destination_lat () {
        return this.destination_lat;
    }

    public String get_destination_lon () {
        return this.destination_lon;
    }

    public String get_departure_datetime () {
        return this.departure_datetime;
    }

    public String get_departure_time_zone_id () {
        return this.departure_time_zone_id;
    }

    public String get_arrival_datetime () {
        return this.arrival_datetime;
    }

    public String get_arrival_time_zone_id () {
        return this.arrival_time_zone_id;
    }

    public String get_ticket_number () {
        return this.ticket_num;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setIata_code(String iata_code) {
        this.iata_code = iata_code;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public void setOrigin_airport(String origin_airport) {
        this.origin_airport = origin_airport;
    }

    public void setOrigin_city(String origin_city) {
        this.origin_city = origin_city;
    }

    public void setOrigin_country(String origin_country) {
        this.origin_country = origin_country;
    }

    public void setOrigin_lat(String origin_lat) {
        this.origin_lat = origin_lat;
    }

    public void setOrigin_lon(String origin_lon) {
        this.origin_lon = origin_lon;
    }

    public void setDestination_airport(String destination_airport) {
        this.destination_airport = destination_airport;
    }

    public void setDestination_city(String destination_city) {
        this.destination_city = destination_city;
    }

    public void setDestination_country(String destination_country) {
        this.destination_country = destination_country;
    }

    public void setDestination_lat(String destination_lat) {
        this.destination_lat = destination_lat;
    }

    public void setDestination_lon(String destination_lon) {
        this.destination_lon = destination_lon;
    }

    public void setDeparture_datetime(String departure_datetime) {
        this.departure_datetime = departure_datetime;
    }

    public void setDeparture_time_zone_id(String departure_time_zone_id) {
        this.departure_time_zone_id = departure_time_zone_id;
    }

    public void setArrival_datetime(String arrival_datetime) {
        this.arrival_datetime = arrival_datetime;
    }

    public void setArrival_time_zone_id(String arrival_time_zone_id) {
        this.arrival_time_zone_id = arrival_time_zone_id;
    }

    public void setTicket_num(String ticket_num) {
        this.ticket_num = ticket_num;
    }

    public static Comparator<AirBooking> comparator = new Comparator<AirBooking>() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        public int compare(AirBooking obj1, AirBooking obj2) {
            try {
                return dateFormat.parse(obj1.get_departure_datetime()).compareTo(dateFormat.parse(obj2.get_departure_datetime()));
            } catch (ParseException exception) {
                throw new IllegalArgumentException(exception);
            }
        }
    };


}
