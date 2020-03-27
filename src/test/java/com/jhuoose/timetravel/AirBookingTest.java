package com.jhuoose.timetravel;

import com.jhuoose.timetravel.models.AirBooking;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AirBookingTest {

    @Test
    void test_getAirBooking() {

        var booking = new AirBooking("general",23768828, 1, "Delta", "Air", "BCDPD",
                "Ronald", "Weasley", "Delta Airlines", "DL",
                "245", "Chicago Airport", "Chicago", "US",
                "123.23", "42.1", "New York Airport", "New York",
                "US", "221.2", "33.3",
                "2019-09-22T11:22:11+0000", "UTC",
                "2019-09-23T13:22:11+0000","UTC","24456",
                new ArrayList<>(List.of("Ronald Weasley", "Luna Lovegood")), Boolean.TRUE);

        assertEquals(booking.get_mail_identifier(), 23768828);
        assertEquals(booking.get_segment_index(), 1);
        assertEquals(booking.get_booking_source(), "Delta");
        assertEquals(booking.get_type(), "Air");
        assertEquals(booking.get_confirmation_no(),"BCDPD");
        assertEquals(booking.get_first_name(), "Ronald");
        assertEquals(booking.get_last_name(), "Weasley");
        assertEquals(booking.get_airline(), "Delta Airlines");
        assertEquals(booking.get_iata_code(), "DL");
        assertEquals(booking.get_flight_number(), "245");
        assertEquals(booking.get_origin_airport(), "Chicago Airport");
        assertEquals(booking.get_origin_city(), "Chicago");
        assertEquals(booking.get_origin_country(), "US");
        assertEquals(booking.get_origin_lat(), "123.23");
        assertEquals(booking.get_origin_lon(), "42.1");
        assertEquals(booking.get_destination_airport(), "New York Airport");
        assertEquals(booking.get_destination_city(), "New York");
        assertEquals(booking.get_destination_country(), "US");
        assertEquals(booking.get_destination_lat(), "221.2");
        assertEquals(booking.get_destination_lon(), "33.3");
        assertEquals(booking.get_departure_datetime(), "2019-09-22T11:22:11+0000");
        assertEquals(booking.get_departure_time_zone_id(), "UTC");
        assertEquals(booking.get_arrival_datetime(), "2019-09-23T13:22:11+0000");
        assertEquals(booking.get_arrival_time_zone_id(), "UTC");
        assertEquals(booking.get_ticket_number(), "24456");
        assertEquals(booking.get_travelers(), List.of("Ronald Weasley", "Luna Lovegood"));
        assertEquals(booking.is_new_booking(), Boolean.TRUE);
    }

}
