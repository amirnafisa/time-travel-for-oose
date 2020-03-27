package com.jhuoose.timetravel.controllers;

import com.jhuoose.timetravel.models.MetaData;
import com.jhuoose.timetravel.repositories.MetaDataRepository;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MetaDataController {
    private MetaDataRepository metaDataRepository;
    private static MetaDataController meta_data_controllerInstance = null;

    private String last_email_stamp, last_booking_stamp;

    private MetaDataController (MetaDataRepository metaDataRepository) throws SQLException {
        this.metaDataRepository = metaDataRepository;
    }

    public static MetaDataController getInstance(MetaDataRepository metaDataRepository) throws SQLException {
        if (meta_data_controllerInstance == null) {
            meta_data_controllerInstance = new MetaDataController(metaDataRepository);

        }
        return meta_data_controllerInstance;
    }

    public void setLastEmailSyncTime(String user) throws SQLException {
        var date = new Date();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
        df.setTimeZone(tz);
        this.last_email_stamp = df.format(date);

        this.metaDataRepository.update_sync_time_stamps(new MetaData(user, this.last_email_stamp, this.metaDataRepository.getLastTraxoSyncTime(user)));
    }

    public void setLastTraxoSyncTime(String user) throws SQLException {
        var date = new Date();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        this.last_booking_stamp = df.format(date);

        this.metaDataRepository.update_sync_time_stamps(new MetaData(user, this.metaDataRepository.getLastEmailSyncTime(user),  this.last_booking_stamp));
    }

    public String getLastEmailSyncTime(String user) throws SQLException {
        return this.metaDataRepository.getLastEmailSyncTime(user);
    }

    public String getLastTraxoSyncTime(String user) throws SQLException {
        return this.metaDataRepository.getLastTraxoSyncTime(user);
    }
}
