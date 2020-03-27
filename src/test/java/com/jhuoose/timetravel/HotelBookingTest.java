package com.jhuoose.timetravel;

import com.jhuoose.timetravel.models.HotelBooking;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotelBookingTest {

    @Test
    void test_getHotelBooking () {
        var booking = new HotelBooking("general",2376882, 1, "AirBnB", "Hotel",
                "BCDPD", "Ronald", "Weasley", "AirBnB",
                "Address Line 1", "Address Line 2", "New York", "US",
                "25512","221.2", "33.3", "2019-09-22", "2019-09-23",
                "UTC",2,"2019-08-11T11:11:11+0000",
                new ArrayList<>(List.of("Ronald Weasley", "Luna Lovegood")),Boolean.TRUE);

        assertEquals(booking.get_mail_identifier(), 2376882);
        assertEquals(booking.get_segment_index(), 1);
        assertEquals(booking.get_booking_source(), "AirBnB");
        assertEquals(booking.get_type(), "Hotel");
        assertEquals(booking.get_confirmation_no(),"BCDPD");
        assertEquals(booking.get_first_name(), "Ronald");
        assertEquals(booking.get_last_name(), "Weasley");
        assertEquals(booking.getHotel_name(), "AirBnB");
        assertEquals(booking.getAddress1(), "Address Line 1");
        assertEquals(booking.getAddress2(), "Address Line 2");
        assertEquals(booking.getCity_name(), "New York");
        assertEquals(booking.getCountry(), "US");
        assertEquals(booking.getPostal_code(), "25512");
        assertEquals(booking.getLat(), "221.2");
        assertEquals(booking.getLon(), "33.3");
        assertEquals(booking.getCheckin_date(), "2019-09-22");
        assertEquals(booking.getCheckout_date(), "2019-09-23");
        assertEquals(booking.getTime_zone_id(), "UTC");
        assertEquals(booking.getNumber_of_rooms(), 2);
        assertEquals(booking.getCreated(), "2019-08-11T11:11:11+0000");
        assertEquals(booking.get_travelers(), List.of("Ronald Weasley", "Luna Lovegood"));
        assertEquals(booking.is_new_booking(), Boolean.TRUE);

    }
}
