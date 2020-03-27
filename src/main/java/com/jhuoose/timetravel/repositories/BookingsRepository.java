package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.AirBooking;
import com.jhuoose.timetravel.models.Booking;
import com.jhuoose.timetravel.models.HotelBooking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class BookingsRepository {
    private static List<String> BookingParameters = new ArrayList<>();

    private Connection connection;

    private AirBookingsRepository air_bookings_repository;
    private HotelBookingsRepository hotel_bookings_repository;

    public BookingsRepository(Connection connection) throws SQLException {
        this.connection = connection;
        this.air_bookings_repository = new AirBookingsRepository(connection);
        this.hotel_bookings_repository = new HotelBookingsRepository(connection);
    }

    public Booking getOne(int identifier) throws SQLException, BookingNotFoundException {
        try {
            return getOneAir(identifier);
        } catch(BookingNotFoundException exception) {
            return getOneHotel(identifier);
        }
    }

    public Booking getOneAir(int identifier) throws SQLException, BookingNotFoundException {

        return this.air_bookings_repository.getOne(identifier);
    }

    public HotelBooking getOneHotel(int identifier) throws SQLException, BookingNotFoundException {
        return this.hotel_bookings_repository.getOne(identifier);
    }

    public List<Booking> getAll(String curUser) throws SQLException {

        var bookings = new ArrayList<Booking>();

        bookings.addAll(this.air_bookings_repository.getAll(curUser));
        bookings.addAll(this.hotel_bookings_repository.getAll(curUser));

        return bookings;
    }

    public List<AirBooking> getAllAir(String curUser) throws SQLException {
        return this.air_bookings_repository.getAll(curUser);
    }

    public List<HotelBooking> getAllHotel(String curUser) throws SQLException {
        return this.hotel_bookings_repository.getAll(curUser);
    }

    public void create(AirBooking booking) throws SQLException, BookingNotFoundException {

        this.air_bookings_repository.create(booking);

    }


    public void create(HotelBooking booking) throws SQLException, BookingNotFoundException {

        this.hotel_bookings_repository.create(booking);

    }

    public void update_new_booking_flagAir (AirBooking booking) throws SQLException, BookingNotFoundException {
        this.air_bookings_repository.update_new_booking_flag(booking);
    }

    public void update_new_booking_flagHotel (HotelBooking booking) throws SQLException, BookingNotFoundException {
        this.hotel_bookings_repository.update_new_booking_flag(booking);
    }

    void delete_air_booking(int identifier) throws SQLException, BookingNotFoundException {
        this.air_bookings_repository.delete(identifier);
    }

    void delete_hotel_booking(int identifier) throws SQLException, BookingNotFoundException {
        this.hotel_bookings_repository.delete(identifier);
    }



}

