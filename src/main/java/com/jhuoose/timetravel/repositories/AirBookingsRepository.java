package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.AirBooking;
import com.jhuoose.timetravel.models.Booking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class AirBookingsRepository {
    private static List<String> BookingParameters = new ArrayList<>();

    private Connection connection;

    public AirBookingsRepository(Connection connection) throws SQLException {

        this.connection = connection;

        String create_statement = "CREATE TABLE IF NOT EXISTS air_bookings (booking_identifier INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userLogin TEXT, mail_identifier INTEGER, segment_index INTEGER, booking_source TEXT, booking_type TEXT, confirmation_no TEXT, first_name TEXT, " +
                "last_name TEXT, airline TEXT, iata_code TEXT, flight_number TEXT, origin_airport TEXT, origin_city TEXT, " +
                "origin_country TEXT, origin_lat TEXT, origin_lon TEXT, destination_airport TEXT, destination_city TEXT, " +
                "destination_country TEXT, destination_lat TEXT, destination_lon TEXT, departure_datetime TEXT, " +
                "departure_time_zone_id TEXT, arrival_datetime TEXT, arrival_time_zone_id TEXT, ticket_num TEXT, " +
                "travelers TEXT, is_new_booking BIT)";

        String unique_statement = "CREATE UNIQUE INDEX IF NOT EXISTS air_bookings_mail_segment_id ON air_bookings (mail_identifier, segment_index)";

        var statement = connection.createStatement();
        statement.execute(create_statement);
        statement.execute(unique_statement);
        statement.close();
    }

    public AirBooking getOne(int identifier) throws SQLException, BookingNotFoundException {
        String get_statement = "SELECT booking_identifier, userLogin, mail_identifier, segment_index, booking_source, " +
                "booking_type, confirmation_no, first_name, last_name, airline, iata_code, flight_number, " +
                "origin_airport, origin_city, origin_country, origin_lat, origin_lon, destination_airport, " +
                "destination_city, destination_country, destination_lat, destination_lon, departure_datetime, " +
                "departure_time_zone_id, arrival_datetime, arrival_time_zone_id, ticket_num, travelers, " +
                "is_new_booking FROM air_bookings WHERE booking_identifier = ?";


        var statement = connection.prepareStatement(get_statement);
        statement.setInt(1, identifier);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new AirBooking( result.getString("userLogin"),
                        result.getInt("booking_identifier"),
                        result.getLong("mail_identifier"),
                        result.getInt("segment_index"),
                        result.getString("booking_source"),
                        result.getString("booking_type"),
                        result.getString("confirmation_no"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("airline"),
                        result.getString("iata_code"),
                        result.getString("flight_number"),
                        result.getString("origin_airport"),
                        result.getString("origin_city"),
                        result.getString("origin_country"),
                        result.getString("origin_lat"),
                        result.getString("origin_lon"),
                        result.getString("destination_airport"),
                        result.getString("destination_city"),
                        result.getString("destination_country"),
                        result.getString("destination_lat"),
                        result.getString("destination_lon"),
                        result.getString("departure_datetime"),
                        result.getString("departure_time_zone_id"),
                        result.getString("arrival_datetime"),
                        result.getString("arrival_time_zone_id"),
                        result.getString("ticket_num"),
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

    public List<AirBooking> getAll(String curUser) throws SQLException {
        var bookings = new ArrayList<AirBooking>();
        var statement = connection.prepareStatement("SELECT booking_identifier, userLogin, mail_identifier, segment_index, " +
                "booking_source, booking_type, confirmation_no, first_name, last_name, airline, iata_code, " +
                "flight_number, origin_airport, origin_city, origin_country, origin_lat, origin_lon, " +
                "destination_airport, destination_city, destination_country, destination_lat, destination_lon, " +
                "departure_datetime, departure_time_zone_id, arrival_datetime, arrival_time_zone_id, ticket_num, " +
                "travelers, is_new_booking FROM air_bookings WHERE userLogin = ?");
        statement.setString(1,curUser);
        var result = statement.executeQuery();
        try {
            while (result.next()) {
                bookings.add(
                        new AirBooking(result.getString("userLogin"),
                                result.getInt("booking_identifier"),
                                result.getLong("mail_identifier"),
                                result.getInt("segment_index"),
                                result.getString("booking_source"),
                                result.getString("booking_type"),
                                result.getString("confirmation_no"),
                                result.getString("first_name"),
                                result.getString("last_name"),
                                result.getString("airline"),
                                result.getString("iata_code"),
                                result.getString("flight_number"),
                                result.getString("origin_airport"),
                                result.getString("origin_city"),
                                result.getString("origin_country"),
                                result.getString("origin_lat"),
                                result.getString("origin_lon"),
                                result.getString("destination_airport"),
                                result.getString("destination_city"),
                                result.getString("destination_country"),
                                result.getString("destination_lat"),
                                result.getString("destination_lon"),
                                result.getString("departure_datetime"),
                                result.getString("departure_time_zone_id"),
                                result.getString("arrival_datetime"),
                                result.getString("arrival_time_zone_id"),
                                result.getString("ticket_num"),
                                new ArrayList<String>(Arrays.asList(result.getString("travelers").split(" , "))),
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

    public void create(AirBooking booking) throws SQLException, BookingNotFoundException {
        var insert_statement = connection.prepareStatement("INSERT OR IGNORE INTO air_bookings (mail_identifier, " +
                "userLogin, segment_index, booking_source, booking_type, confirmation_no, first_name, " +
                "last_name, airline, iata_code, flight_number, origin_airport, origin_city, " +
                "origin_country, origin_lat, origin_lon, destination_airport, destination_city, " +
                "destination_country, destination_lat, destination_lon, departure_datetime, " +
                "departure_time_zone_id, arrival_datetime, arrival_time_zone_id, ticket_num, " +
                "travelers, is_new_booking) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ", Statement.RETURN_GENERATED_KEYS);
        insert_statement.setLong(1, booking.get_mail_identifier());
        insert_statement.setString(2, booking.getUserLogin());
        insert_statement.setInt(3, booking.get_segment_index());
        insert_statement.setString(4, booking.get_booking_source());
        insert_statement.setString(5, booking.get_type());
        insert_statement.setString(6, booking.get_confirmation_no());
        insert_statement.setString(7, booking.get_first_name());
        insert_statement.setString(8, booking.get_last_name());
        insert_statement.setString(9, booking.get_airline());
        insert_statement.setString(10, booking.get_iata_code());
        insert_statement.setString(11, booking.get_flight_number());
        insert_statement.setString(12, booking.get_origin_airport());
        insert_statement.setString(13, booking.get_origin_city());
        insert_statement.setString(14, booking.get_origin_country());
        insert_statement.setString(15, booking.get_origin_lat());
        insert_statement.setString(16, booking.get_origin_lon());
        insert_statement.setString(17, booking.get_destination_airport());
        insert_statement.setString(18, booking.get_destination_city());
        insert_statement.setString(19, booking.get_destination_country());
        insert_statement.setString(20, booking.get_destination_lat());
        insert_statement.setString(21, booking.get_destination_lon());
        insert_statement.setString(22, booking.get_departure_datetime());
        insert_statement.setString(23, booking.get_departure_time_zone_id());
        insert_statement.setString(24, booking.get_arrival_datetime());
        insert_statement.setString(25, booking.get_arrival_time_zone_id());
        insert_statement.setString(26, booking.get_ticket_number());
        insert_statement.setString(27, String.join(",", booking.get_travelers()));
        insert_statement.setBoolean(28, booking.is_new_booking());
        try {
            if (insert_statement.executeUpdate() == 0) {
                System.out.println("Possibly a duplicate entry... "+booking.get_mail_identifier()+" - "+booking.get_segment_index());
            } else {
                ResultSet generatedKeys = insert_statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    booking.set_booking_identifier(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("ERR: New booking not created.");
                }
            }
        }
        finally {
            insert_statement.close();
        }
    }

    public void update_new_booking_flag (AirBooking booking) throws SQLException, BookingNotFoundException {
        var statement = connection.prepareStatement("UPDATE air_bookings SET is_new_booking = ? WHERE booking_identifier = ?");
        statement.setBoolean(1, booking.is_new_booking());
        statement.setInt(2, booking.get_booking_identifier());
        try {
            if (statement.executeUpdate() == 0) throw new BookingNotFoundException();
        }
        finally {
            statement.close();
        }
    }

    public void delete (int identifier) throws SQLException, BookingNotFoundException {
        var statement = connection.prepareStatement("DELETE FROM air_bookings WHERE booking_identifier = ?");
        statement.setInt(1, identifier);
        try {
            if (statement.executeUpdate() == 0) throw new BookingNotFoundException();
        }
        finally {
            statement.close();
        }
    }

}

