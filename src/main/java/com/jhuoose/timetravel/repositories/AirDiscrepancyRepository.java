package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.AirDiscrepancy;
import com.jhuoose.timetravel.models.Itinerary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirDiscrepancyRepository {
    private Connection connection;

    public AirDiscrepancyRepository(Connection connection) throws SQLException {
        this.connection = connection;
        var statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS air_discrepancy (identifier INTEGER PRIMARY KEY AUTOINCREMENT, itinerary_identifiers VARCHAR, " +
                "userLogin TEXT, from_city VARCHAR, to_city VARCHAR)");
        statement.close();
    }

    public void insert(AirDiscrepancy airDiscrepancy) throws SQLException {
        int identifier = airDiscrepancy.getIdentifier();
        String itineraryIdentifiers = airDiscrepancy.getItineraryIdentifiers().stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        String curUser = airDiscrepancy.getUserLogin();
        String fromCity = airDiscrepancy.getFromCity();
        String toCity = airDiscrepancy.getToCity();
        var statement = connection.prepareStatement("INSERT INTO air_discrepancy (identifier, itinerary_identifiers, userLogin, from_city, to_city) VALUES (?, ?, ?, ?, ?)");
        statement.setInt(1, identifier);
        statement.setString(2, itineraryIdentifiers);
        statement.setString(3, curUser);
        statement.setString(4, fromCity);
        statement.setString(5, toCity);
        statement.execute();
        statement.close();
    }

    public List<AirDiscrepancy> getAll(String curUser) throws SQLException {
        var discrepancies = new ArrayList<AirDiscrepancy>();
        var statement = connection.prepareStatement("SELECT identifier, itinerary_identifiers, userLogin, from_city, to_city FROM air_discrepancy WHERE userLogin = ?");
        statement.setString(1,curUser);
        var result = statement.executeQuery();
        try {
            while (result.next()) {
                discrepancies.add(
                        new AirDiscrepancy(result.getString("userLogin"),
                                result.getInt("identifier"),
                                extractItineraryIds(result.getString("itinerary_identifiers")),
                                result.getString("from_city"),
                                result.getString("to_city")
                        )
                );
            }
        }finally {
            result.close();
            statement.close();
        }
        return discrepancies;
    }

    public AirDiscrepancy getOne(int identifier) throws SQLException, DiscrepancyNotFoundException {
        var statement = connection.prepareStatement("SELECT identifier, itinerary_identifiers, userLogin, from_city, to_city FROM air_discrepancy WHERE identifier = ?");
        statement.setInt(1, identifier);
        var result = statement.executeQuery();
        try {
            if (result.next()) {
                return new AirDiscrepancy(result.getString("userLogin"),
                        result.getInt("identifier"),
                        extractItineraryIds(result.getString("itinerary_identifiers")),
                        result.getString("from_city"),
                        result.getString("to_city")
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
        statement.executeUpdate("DELETE FROM air_discrepancy");
        statement.close();
    }

    private List<Integer> extractItineraryIds(String itineraryIdentifiers) {
        return Stream.of(itineraryIdentifiers.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
