package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.Booking;
import com.jhuoose.timetravel.models.Itinerary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItineraryRepository {
    private Connection connection;
    private BookingsRepository bookingsRepository;

    public ItineraryRepository(Connection connection, BookingsRepository bookingsRepository) throws SQLException {
        this.connection = connection;
        this.bookingsRepository = bookingsRepository;
        var statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS itinerary (identifier INTEGER PRIMARY KEY AUTOINCREMENT, userLogin TEXT, bookings VARCHAR)");
        statement.close();
    }
    public void insert(Itinerary itinerary) throws SQLException {
        int identifier = itinerary.getIdentifier();
        String bookingIds = extractBookingIds(itinerary);
        String curUser = itinerary.getUserLogin();
        var statement = connection.prepareStatement("INSERT INTO itinerary (identifier, userLogin, bookings) VALUES (?, ?, ?)");
        statement.setInt(1, identifier);
        statement.setString(2, curUser);
        statement.setString(3, bookingIds);
        statement.execute();
        statement.close();
    }

    public void update(Itinerary itinerary) throws SQLException, ItineraryNotFoundException {
        int identifier = itinerary.getIdentifier();
        String bookingIds = extractBookingIds(itinerary);
        update(identifier, bookingIds);
    }

    public void update(int identifier, String bookingIds) throws SQLException, ItineraryNotFoundException {
        var statement = connection.prepareStatement("UPDATE itinerary SET bookings = ? WHERE identifier = ?");
        statement.setInt(2, identifier);
        statement.setString(1, bookingIds);
        try {
            if (statement.executeUpdate() == 0) throw new ItineraryNotFoundException();
        } finally {
            statement.close();
        }
    }

    public List<Itinerary> getAll(String curUser) throws SQLException, BookingNotFoundException {
        var itineraries = new ArrayList<Itinerary>();
        var statement = connection.prepareStatement("SELECT identifier, userLogin, bookings FROM itinerary WHERE userLogin = ?");
        statement.setString(1,curUser);
        var result = statement.executeQuery();
        try {
            while (result.next()) {
                itineraries.add(
                        new Itinerary(result.getString("userLogin"),
                                result.getInt("identifier"),
                                extractBookings(result.getString("bookings"))
                        )
                );
            }
        } finally {
            statement.close();
            result.close();
        }
        return itineraries;
    }

    public Itinerary getOne(int identifier) throws SQLException, BookingNotFoundException, ItineraryNotFoundException {
        var statement = connection.prepareStatement("SELECT identifier, userLogin, bookings FROM itinerary WHERE identifier = ?");
        statement.setInt(1, identifier);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new Itinerary(result.getString("userLogin"),
                        result.getInt("identifier"),
                        extractBookings(result.getString("bookings"))
                );
            } else {
                throw new ItineraryNotFoundException();
            }
        }
        finally {
            statement.close();
            result.close();
        }
    }

    public void clear() throws SQLException {
        var statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM itinerary");
        statement.close();
    }

    public void delete(Itinerary itinerary) throws SQLException, ItineraryNotFoundException {
        delete(itinerary.getIdentifier());
    }

    public void delete(int identifier) throws SQLException, ItineraryNotFoundException {
        var statement = connection.prepareStatement("DELETE FROM itinerary WHERE identifier = ?");
        statement.setInt(1, identifier);
        try {
            if (statement.executeUpdate() == 0) throw new ItineraryNotFoundException();
        }
        finally {
            statement.close();
        }
    }

    public void deleteBookingEntry(int itineraryToBeUpdated, String bookingToBeDeleted) throws SQLException, ItineraryNotFoundException, BookingNotFoundException {
        //get the earlier itinerary entry and corresponding bookings
        var statement = connection.prepareStatement("SELECT bookings FROM itinerary WHERE identifier = ?");
        statement.setInt(1, itineraryToBeUpdated);
        var result = statement.executeQuery();
        String bookingIds;

        if (result.next()) {
            bookingIds = result.getString("bookings");
        } else {
            throw new ItineraryNotFoundException();
        }

        //If the given booking id is present in the itinerary, delete the booking from bookings Repository and save new BookingIds String
        List<String> updatedBookingIds = new ArrayList<>();
        for(String bookingId: bookingIds.split(",")) {
            if(bookingId.compareTo(bookingToBeDeleted) == 0) {
                String booking_conf[] = bookingId.split("-");
                if(booking_conf[0].compareTo("Air") == 0)
                    bookingsRepository.delete_air_booking(Integer.parseInt(booking_conf[1]));
                else
                    bookingsRepository.delete_hotel_booking(Integer.parseInt(booking_conf[1]));
            } else {
                updatedBookingIds.add(bookingId);
            }
        }

        //If no bookings left in the itinerary delete it, otherwise update itinerary with new BookingIds String
        if (updatedBookingIds.size() == 0) {
            delete(itineraryToBeUpdated);
        } else {
            update(itineraryToBeUpdated, String.join(",",updatedBookingIds));
        }
    }

    private String extractBookingIds(Itinerary itinerary) {
        String bookings = "";
        boolean isFirst = true;
        for (Booking booking : itinerary.getBookings()) {
            if (isFirst) {
                bookings += booking.get_type() + "-" + Integer.toString(booking.get_booking_identifier());
                isFirst = false;
            } else {
                bookings += "," + booking.get_type() + "-" + Integer.toString(booking.get_booking_identifier());
            }
        }
        return bookings;
    }

    private List<Booking> extractBookings(String bookingIds) throws SQLException, BookingNotFoundException{
        List<Booking> bookings = new ArrayList<>();
        for(String bookingId: bookingIds.split(",")) {
            String booking_conf[] = bookingId.split("-");
            if (booking_conf[0].compareTo("Air") == 0)
                bookings.add(bookingsRepository.getOneAir(Integer.parseInt(booking_conf[1])));
            else
                bookings.add(bookingsRepository.getOneHotel(Integer.parseInt(booking_conf[1])));
        }
        return bookings;
    }
}