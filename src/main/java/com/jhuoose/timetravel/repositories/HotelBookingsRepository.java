package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.HotelBooking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class HotelBookingsRepository {
    private static List<String> BookingParameters = new ArrayList<>();

    private Connection connection;

    public HotelBookingsRepository(Connection connection) throws SQLException {

        this.connection = connection;

        String create_statement = "CREATE TABLE IF NOT EXISTS hotel_bookings (booking_identifier INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userLogin TEXT, mail_identifier INTEGER, segment_index INTEGER, booking_source TEXT, booking_type TEXT, confirmation_no TEXT, first_name TEXT, " +
                "last_name TEXT, hotel_name TEXT, address1 TEXT, address2 TEXT, city_name TEXT, country TEXT, " +
                "postal_code TEXT, lat TEXT, lon TEXT, checkin_date TEXT, checkout_date TEXT, " +
                "time_zone_id TEXT, number_of_rooms INTEGER , created TEXT, " +
                "travelers TEXT, is_new_booking BIT)";

        String unique_statement = "CREATE UNIQUE INDEX IF NOT EXISTS hotel_bookings_mail_segment_id ON hotel_bookings (mail_identifier, segment_index)";

        var statement = connection.createStatement();
        statement.execute(create_statement);
        statement.execute(unique_statement);
        statement.close();
    }

    public HotelBooking getOne(int identifier) throws SQLException, BookingNotFoundException {
        String get_statement = "SELECT booking_identifier, userLogin, mail_identifier, segment_index, booking_source, " +
                "booking_type, confirmation_no, first_name, last_name, hotel_name, address1, address2, city_name , " +
                "country, postal_code, lat, lon, checkin_date, checkout_date, time_zone_id, number_of_rooms, created, " +
                "travelers, is_new_booking FROM hotel_bookings WHERE booking_identifier = ?";


        var statement = connection.prepareStatement(get_statement);
        statement.setInt(1, identifier);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new HotelBooking(result.getString("userLogin"),
                        result.getInt("booking_identifier"),
                        result.getLong("mail_identifier"),
                        result.getInt("segment_index"),
                        result.getString("booking_source"),
                        result.getString("booking_type"),
                        result.getString("confirmation_no"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("hotel_name"),
                        result.getString("address1"),
                        result.getString("address2"),
                        result.getString("city_name"),
                        result.getString("country"),
                        result.getString("postal_code"),
                        result.getString("lat"),
                        result.getString("lon"),
                        result.getString("checkin_date"),
                        result.getString("checkout_date"),
                        result.getString("time_zone_id"),
                        result.getInt("number_of_rooms"),
                        result.getString("created"),
                        new ArrayList<>(Arrays.asList(result.getString("travelers").split(" , "))),
                        result.getBoolean("is_new_booking")
                );
            } else {
                throw new BookingNotFoundException();
            }
        } finally {
            statement.close();
            result.close();
        }
    }

    public List<HotelBooking> getAll(String curUser) throws SQLException {
        var bookings = new ArrayList<HotelBooking>();
        var statement = connection.prepareStatement("SELECT booking_identifier, userLogin, mail_identifier, segment_index, " +
                "booking_source, booking_type, confirmation_no, first_name, last_name, hotel_name, address1, address2, " +
                "city_name , country, postal_code, lat, lon, checkin_date, checkout_date, time_zone_id, number_of_rooms, " +
                "created, travelers, is_new_booking FROM hotel_bookings WHERE userLogin = ?");
        statement.setString(1,curUser);
        var result = statement.executeQuery();
        try {
            while (result.next()) {
                bookings.add(
                        new HotelBooking(result.getString("userLogin"),
                                result.getInt("booking_identifier"),
                                result.getLong("mail_identifier"),
                                result.getInt("segment_index"),
                                result.getString("booking_source"),
                                result.getString("booking_type"),
                                result.getString("confirmation_no"),
                                result.getString("first_name"),
                                result.getString("last_name"),
                                result.getString("hotel_name"),
                                result.getString("address1"),
                                result.getString("address2"),
                                result.getString("city_name"),
                                result.getString("country"),
                                result.getString("postal_code"),
                                result.getString("lat"),
                                result.getString("lon"),
                                result.getString("checkin_date"),
                                result.getString("checkout_date"),
                                result.getString("time_zone_id"),
                                result.getInt("number_of_rooms"),
                                result.getString("created"),
                                new ArrayList<>(Arrays.asList(result.getString("travelers").split(" , "))),
                                result.getBoolean("is_new_booking")
                        )
                );
            }
        } finally {
            statement.close();
            result.close();
        }
        return bookings;
    }

    public void create(HotelBooking booking) throws SQLException, BookingNotFoundException {
        var insert_statement = connection.prepareStatement("INSERT OR IGNORE INTO hotel_bookings (mail_identifier, " +
                "userLogin, segment_index, booking_source, booking_type, confirmation_no, first_name, last_name, hotel_name, " +
                "address1, address2, city_name , country, postal_code, lat, lon, checkin_date, checkout_date, " +
                "time_zone_id, number_of_rooms, created, travelers, is_new_booking) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?) ", Statement.RETURN_GENERATED_KEYS);
        try (insert_statement) {
            insert_statement.setLong(1, booking.get_mail_identifier());
            insert_statement.setString(2, booking.getUserLogin());
            insert_statement.setInt(3, booking.get_segment_index());
            insert_statement.setString(4, booking.get_booking_source());
            insert_statement.setString(5, booking.get_type());
            insert_statement.setString(6, booking.get_confirmation_no());
            insert_statement.setString(7, booking.get_first_name());
            insert_statement.setString(8, booking.get_last_name());
            insert_statement.setString(9, booking.getHotel_name());
            insert_statement.setString(10, booking.getAddress1());
            insert_statement.setString(11, booking.getAddress2());
            insert_statement.setString(12, booking.getCity_name());
            insert_statement.setString(13, booking.getCountry());
            insert_statement.setString(14, booking.getPostal_code());
            insert_statement.setString(15, booking.getLat());
            insert_statement.setString(16, booking.getLon());
            insert_statement.setString(17, booking.getCheckin_date());
            insert_statement.setString(18, booking.getCheckout_date());
            insert_statement.setString(19, booking.getTime_zone_id());
            insert_statement.setInt(20, booking.getNumber_of_rooms());
            insert_statement.setString(21, booking.getCreated());
            insert_statement.setString(22, String.join(",", booking.get_travelers()));
            insert_statement.setBoolean(23, booking.is_new_booking());
            if (insert_statement.executeUpdate() == 0) {
                System.out.println("Possibly a duplicate entry... " + booking.get_mail_identifier() + " - " + booking.get_segment_index());
            } else {
                ResultSet generatedKeys = insert_statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    booking.set_booking_identifier(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("ERR: New booking not created.");
                }
            }
        }
    }

    public void update_new_booking_flag (HotelBooking booking) throws SQLException, BookingNotFoundException {
        var statement = connection.prepareStatement("UPDATE hotel_bookings SET is_new_booking = ? WHERE booking_identifier = ?");
        try (statement) {
            statement.setBoolean(1, booking.is_new_booking());
            statement.setInt(2, booking.get_booking_identifier());
            if (statement.executeUpdate() == 0) throw new BookingNotFoundException();
        }
    }

    public void delete (int identifier) throws SQLException, BookingNotFoundException {
        var statement = connection.prepareStatement("DELETE FROM hotel_bookings WHERE booking_identifier = ?");
        try (statement) {
            statement.setInt(1, identifier);
            if (statement.executeUpdate() == 0) throw new BookingNotFoundException();
        }
    }

}

