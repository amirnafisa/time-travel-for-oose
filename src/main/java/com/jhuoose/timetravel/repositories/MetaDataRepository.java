package com.jhuoose.timetravel.repositories;

import com.jhuoose.timetravel.models.MetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetaDataRepository {

    private Connection connection;

    public MetaDataRepository(Connection connection) throws SQLException {

        this.connection = connection;

        String create_statement = "CREATE TABLE IF NOT EXISTS meta_data_table (identifier INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userLogin TEXT, mail_sync_stamp TEXT, booking_sync_stamp TEXT)";

        var statement = connection.createStatement();
        statement.execute(create_statement);
        statement.close();

    }

    private boolean isTableEmpty() throws SQLException {
        var get_statement = "SELECT COUNT(*) FROM meta_data_table";
        var statement = connection.prepareStatement(get_statement);
        try (statement; var result = statement.executeQuery()) {
            if (result.next()) {
                return (result.getInt("COUNT(*)") == 0);
            } else {
                return true;
            }
        }
    }

    public String getLastEmailSyncTime(String curUser) throws SQLException {
        var get_statement = "SELECT * FROM meta_data_table WHERE userLogin = ? ORDER BY identifier DESC LIMIT 1";
        var statement = connection.prepareStatement(get_statement);
        statement.setString(1, curUser);
        var result = statement.executeQuery();
        String time_stamp;
        try {
            if (result.next()) {
                time_stamp = result.getString("mail_sync_stamp");
            } else {
                var date = new Date();
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
                df.setTimeZone(tz);
                time_stamp = df.format(date);
                System.out.println("[INFO] No previous email time stamp found... returning current time, " + time_stamp);
            }
        } finally {
            statement.close();
            result.close();
        }
        return time_stamp;
    }

    public String getLastTraxoSyncTime(String curUser) throws SQLException {
        var get_statement = "SELECT * FROM meta_data_table WHERE userLogin = ? ORDER BY identifier DESC LIMIT 1";
        var statement = connection.prepareStatement(get_statement);
        statement.setString(1, curUser);
        var result = statement.executeQuery();
        String time_stamp;
        try {
            if (result.next()) {
                time_stamp = result.getString("booking_sync_stamp");
            } else {
                var date = new Date();
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);
                time_stamp = df.format(date);

                System.out.println("[INFO] No previous traxo time stamp found... returning current time, " + time_stamp);
            }
        } finally {
            statement.close();
            result.close();
        }
        return time_stamp;
    }

    public void update_sync_time_stamps(MetaData metaData) throws SQLException {
        var insert_statement = connection.prepareStatement("INSERT INTO meta_data_table (userLogin, mail_sync_stamp, " +
                "booking_sync_stamp) VALUES (?,?,?) ", Statement.RETURN_GENERATED_KEYS);
        try (insert_statement) {
            insert_statement.setString(1, metaData.getUserLogin());
            insert_statement.setString(2, metaData.getEmail_last_synced_datetime());
            insert_statement.setString(3, metaData.getBooking_last_synced_datetime());
            if (insert_statement.executeUpdate() == 0) {
                System.out.println("[ERR] Could not insert the entry with email stamp " + metaData.getEmail_last_synced_datetime()
                        + ", booking stamp " + metaData.getBooking_last_synced_datetime() + " for user " + metaData.getUserLogin());
            } else {
                ResultSet generatedKeys = insert_statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    metaData.setIdentifier(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("[ERR] New stamp entry not created.");
                }
            }
        }
    }
}
