package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.AirDiscrepancy;
import com.jhuoose.timetravel.models.HotelDiscrepancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HotelDiscrepancyRepository {
    private Connection connection;

    public HotelDiscrepancyRepository(Connection connection) throws SQLException {
        this.connection = connection;
        var statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS hotel_discrepancy (identifier INTEGER PRIMARY KEY AUTOINCREMENT, itinerary_identifiers VARCHAR, " +
                "userLogin TEXT, city VARCHAR)");
        statement.close();
    }

    public void insert(HotelDiscrepancy hotelDiscrepancy) throws SQLException {
        int identifier = hotelDiscrepancy.getIdentifier();
        String itineraryIdentifiers = hotelDiscrepancy.getItineraryIdentifiers().stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        String city = hotelDiscrepancy.getCity();
        String curUser = hotelDiscrepancy.getUserLogin();
        var statement = connection.prepareStatement("INSERT INTO hotel_discrepancy (identifier, itinerary_identifiers, userLogin, city) VALUES (?, ?, ?, ?)");
        statement.setInt(1, identifier);
        statement.setString(2, itineraryIdentifiers);
        statement.setString(3, curUser);
        statement.setString(4, city);
        statement.execute();
        statement.close();
    }

    public List<HotelDiscrepancy> getAll(String curUser) throws SQLException {
        var discrepancies = new ArrayList<HotelDiscrepancy>();
        var statement = connection.prepareStatement("SELECT identifier, itinerary_identifiers, userLogin, city FROM hotel_discrepancy WHERE userLogin = ?");
        statement.setString(1, curUser);
        var result = statement.executeQuery();
        try {
            while (result.next()) {
                discrepancies.add(
                        new HotelDiscrepancy(result.getString("userLogin"),
                                result.getInt("identifier"),
                                extractItineraryIds(result.getString("itinerary_identifiers")),
                                result.getString("city")
                        )
                );
            }
        } finally {
            result.close();
            statement.close();
        }
        return discrepancies;

    }


    public HotelDiscrepancy getOne(int identifier) throws SQLException, DiscrepancyNotFoundException {
        var statement = connection.prepareStatement("SELECT identifier, itinerary_identifiers, userLogin, city FROM hotel_discrepancy WHERE identifier = ?");
        statement.setInt(1, identifier);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new HotelDiscrepancy(result.getString("userLogin"),
                        result.getInt("identifier"),
                        extractItineraryIds(result.getString("itinerary_identifiers")),
                        result.getString("city")
                );
            } else {
                throw new DiscrepancyNotFoundException();
            }
        }
        finally {
            statement.close();
            result.close();
        }
    }

    public void clear() throws SQLException {
        var statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM hotel_discrepancy");
        statement.close();
    }

    private List<Integer> extractItineraryIds(String itineraryIdentifiers) {
        return Stream.of(itineraryIdentifiers.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
