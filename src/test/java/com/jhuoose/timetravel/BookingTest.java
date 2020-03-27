package com.jhuoose.timetravel;
import com.jhuoose.timetravel.models.Booking;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
class BookingTest {

    @Test
    void test_getBooking() {

        var booking = new Booking("general",23768828, 1, "Delta", "Air", "BCDPD", "Ronald", "Weasley", new ArrayList<>(
                List.of("Ronald Weasley", "Luna Lovegood")), Boolean.TRUE);

        assertEquals(booking.get_mail_identifier(), 23768828);
        assertEquals(booking.get_segment_index(), 1);
        assertEquals(booking.get_booking_source(), "Delta");
        assertEquals(booking.get_type(), "Air");
        assertEquals(booking.get_confirmation_no(),"BCDPD");
        assertEquals(booking.get_first_name(), "Ronald");
        assertEquals(booking.get_last_name(), "Weasley");
        assertEquals(booking.get_travelers(), List.of("Ronald Weasley", "Luna Lovegood"));
        assertEquals(booking.is_new_booking(), Boolean.TRUE);
    }

}

