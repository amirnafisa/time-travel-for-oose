package com.jhuoose.timetravel;

import com.jhuoose.timetravel.controllers.*;

import com.jhuoose.timetravel.repositories.*;
import io.javalin.Javalin;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.DriverManager;
import java.sql.SQLException;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {
    public static void main(String[] args) throws SQLException, GeneralSecurityException, IOException {
        System.out.println(System.getenv("GMAIL_CLIENT_ID"));
        var connection = DriverManager.getConnection("jdbc:sqlite:timetravel.db");
        var usersRepository = new UsersRepository(connection);
        var bookingsRepository = new BookingsRepository(connection);
        var itineraryRepository = new ItineraryRepository(connection, bookingsRepository);
        var airDiscrepancyRepository = new AirDiscrepancyRepository(connection);
        var hotelDiscrepancyRepository = new HotelDiscrepancyRepository(connection);
        var meta_dataRepository = new MetaDataRepository(connection);

        var meta_data_controller = MetaDataController.getInstance(meta_dataRepository);
        var usersController = new UsersController(usersRepository);
        var traxo_controller = BookingController.getInstance(bookingsRepository, usersController, meta_data_controller);
        var discrepancyController = new DiscrepancyController(itineraryRepository, airDiscrepancyRepository, hotelDiscrepancyRepository, usersController);
        var itineraryController = new ItineraryController(itineraryRepository, bookingsRepository, discrepancyController, usersController);

        var gmail_controller = EmailController.getInstance(usersController, meta_data_controller);

        var app = Javalin.create(config -> config.addStaticFiles("/public"));
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        app.events(event -> event.serverStopped(connection::close))
        .routes(() -> {
            path("emails", () -> get(gmail_controller::loadCredentials));
            path("Callback", () -> get(gmail_controller::Callback));
            path("bookings", () -> {
                get(traxo_controller::sync_booking);
                path("add", () -> put(traxo_controller::addBooking));

            });
            path("itineraries", () -> {
                get(itineraryController::syncItineraries);
                path("delete/:itinerary_id", () -> path(":booking_id", () ->
                    put(itineraryController::deleteBooking)
                ));
            });
            path("temp", () -> get(itineraryController::fetchItineraries));
            path("hoteldiscrepancies", () -> get(discrepancyController::fetchHotelDiscrepancies));
            path("airdiscrepancies", () -> get(discrepancyController::fetchAirDiscrepancies));
            path("users", () -> {
                post(usersController::signup);
                path("login", () -> {
                    post(usersController::login);
                    get(usersController::is_login);
                });
            });

        })
        .exception(BookingNotFoundException.class, (e, ctx) -> ctx.status(404))
        .start(System.getenv("PORT") == null ? 7000 : Integer.parseInt(System.getenv("PORT")));
    }
}
