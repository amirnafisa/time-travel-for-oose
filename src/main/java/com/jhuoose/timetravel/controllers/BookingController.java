package com.jhuoose.timetravel.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhuoose.timetravel.models.AirBooking;
import com.jhuoose.timetravel.models.Booking;
import com.jhuoose.timetravel.models.HotelBooking;
import com.jhuoose.timetravel.models.User;
import com.jhuoose.timetravel.repositories.BookingsRepository;
import com.jhuoose.timetravel.repositories.BookingNotFoundException;
import io.javalin.http.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class BookingController {
    private BookingsRepository bookingsRepository;
    private UsersController usersController;
    private MetaDataController metaDataController;
    private static BookingController traxoControllerInstance = null;
    private HttpClient httpClient;
    private String last_synced_time;
    private List<String> id_parameters;
    private String client_id;
    private String client_secret;
    private final static String traxo_uri = "https://api.traxo.com/v2/emails";

    private static final String CREDENTIALS_FILE_PATH = "/client_secret_traxo.json";

    private BookingController(BookingsRepository bookingsRepository, UsersController usersController, MetaDataController metaDataController) throws IOException {
        this.bookingsRepository = bookingsRepository;
        this.usersController = usersController;
        this.metaDataController = metaDataController;
        this.id_parameters = new ArrayList<>();
        this.id_parameters.add("status=Processed");
        this.id_parameters.add("limit=25");
        obtainCredentials();
    }

    public static BookingController getInstance(BookingsRepository bookingsRepository, UsersController usersController, MetaDataController metaDataController) throws IOException {
        if (traxoControllerInstance == null) {
            traxoControllerInstance = new BookingController(bookingsRepository, usersController, metaDataController);
        }
        return traxoControllerInstance;
    }

    private void obtainCredentials () throws IOException {
        InputStream in = BookingController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            System.out.println("[ERR] Resource not found: " + CREDENTIALS_FILE_PATH);
            this.client_id = System.getenv("TRAXO_CLIENT_ID");
            this.client_secret = System.getenv("TRAXO_CLIENT_SECRET");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode obj = objectMapper.readValue(in, JsonNode.class);
            this.client_id = obj.get("client_id").textValue();
            this.client_secret = obj.get("client_secret").textValue();
        }

    }

    public void sync_booking(Context ctx) throws InterruptedException, SQLException, BookingNotFoundException, IOException {
        String curUser = this.usersController.currentUser(ctx).getLogin();
        this.last_synced_time = this.metaDataController.getLastTraxoSyncTime(curUser);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        List<String> new_booking_ids = retrieve_new_booking_ids();

        this.metaDataController.setLastTraxoSyncTime(curUser);

        for (String booking_id : new_booking_ids) {
            String booking = retrieve_booking(booking_id);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode Obj = mapper.readValue(booking, JsonNode.class);
            if (!curUser.equals(Obj.get("user_address").asText())) {
                continue;
            }
            int index = 0;
            for (JsonNode segment : Obj.get("includes").get("segments")) {
                index++;
                List<String> travelers = new ArrayList<>();
                for (JsonNode traveler : segment.get("travelers")) {
                    travelers.add(traveler.get("name").textValue());
                }

                Booking new_booking;
                switch (segment.get("type").textValue()) {
                    case "Air":
                        new_booking = new AirBooking(curUser,
                                Obj.get("id").asLong(),
                                index,
                                segment.get("source").textValue(),
                                segment.get("type").textValue(),
                                segment.get("confirmation_no").textValue(),
                                segment.get("first_name").textValue(),
                                segment.get("last_name").textValue(),
                                segment.get("airline").textValue(),
                                segment.get("iata_code").textValue(),
                                segment.get("flight_number").textValue(),
                                segment.get("origin_name").textValue(),
                                segment.get("origin_city_name").textValue(),
                                segment.get("origin_country").textValue(),
                                segment.get("origin_lat").textValue(),
                                segment.get("origin_lon").textValue(),
                                segment.get("destination_name").textValue(),
                                segment.get("destination_city_name").textValue(),
                                segment.get("destination_country").textValue(),
                                segment.get("destination_lat").textValue(),
                                segment.get("destination_lon").textValue(),
                                segment.get("departure_datetime").textValue(),
                                segment.get("departure_time_zone_id").textValue(),
                                segment.get("arrival_datetime").textValue(),
                                segment.get("arrival_time_zone_id").textValue(),
                                segment.get("ticket_number").textValue(),
                                travelers, Boolean.TRUE
                        );
                        bookingsRepository.create((AirBooking) new_booking);
                        break;

                    case "Hotel":
                        new_booking = new HotelBooking(curUser,
                                Obj.get("id").asLong(),
                                index,
                                segment.get("source").textValue(),
                                segment.get("type").textValue(),
                                segment.get("confirmation_no").textValue(),
                                segment.get("first_name").textValue(),
                                segment.get("last_name").textValue(),
                                segment.get("hotel_name").textValue(),
                                segment.get("address1").textValue(),
                                segment.get("address2").textValue(),
                                segment.get("city_name").textValue(),
                                segment.get("country").textValue(),
                                segment.get("postal_code").textValue(),
                                segment.get("lat").textValue(),
                                segment.get("lon").textValue(),
                                segment.get("checkin_date").textValue(),
                                segment.get("checkout_date").textValue(),
                                segment.get("time_zone_id").textValue(),
                                segment.get("number_of_rooms").asInt(),
                                segment.get("created").textValue(),
                                travelers, Boolean.TRUE
                        );
                        bookingsRepository.create((HotelBooking) new_booking);
                        break;
                    default:
                        System.out.println("[INFO] Booking type "+segment.get("type")+" not supported. {Valid types are Air and Hotel}");

                }
            }
        }
        ctx.status(201);
    }

    private String get_booking_ids_uri() {
        StringBuilder uri = new StringBuilder(traxo_uri + "?");

        for (String param : this.id_parameters) {
            uri.append(param).append("&");
        }
        uri.append("since=").append(this.last_synced_time.replaceAll(":", "%3A"));
        return uri.toString();
    }

    private String get_booking_uri(String booking_id) {

        return traxo_uri + "/" + booking_id + "?include=results";
    }

    private List<String> retrieve_new_booking_ids() throws IOException, InterruptedException {

        List<String> booking_ids = new ArrayList<>();

        //Call traxo to get list of new emails
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(get_booking_ids_uri()))
                .header("Accept", "application/json")
                .header("client_id", client_id)
                .header("client_secret", client_secret)
                .GET()
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("[INFO]: No new booking found since "+this.last_synced_time+" ... Try again later\n");
        } else {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode[] allObjs = mapper.readValue(response.body(), JsonNode[].class);

            for (JsonNode node : allObjs) {
                booking_ids.add(node.get("id").textValue());
            }
        }

        return booking_ids;
    }

    private String retrieve_booking(String booking_id) throws IOException, InterruptedException {

        //Call traxo to get booking
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(get_booking_uri(booking_id)))
                .header("Accept", "application/json")
                .header("client_id", client_id)
                .header("client_secret", client_secret)
                .GET()
                .build();
        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode Obj = mapper.readValue(response.body(), JsonNode.class);

        return response.body();
    }

    public void fetch_booking(Context ctx) throws SQLException {
        String curUser = this.usersController.currentUser(ctx).getLogin();
        List<Booking> fetched_bookings = bookingsRepository.getAll(curUser);
        ctx.json(fetched_bookings);
    }

    public void fetch_the_airbooking(Context ctx) throws SQLException, BookingNotFoundException {
        Booking fetched_booking = bookingsRepository.getOneAir(ctx.pathParam("identifier", Integer.class).get());
        ctx.json(fetched_booking);
    }

    public void fetch_the_hotelbooking(Context ctx) throws SQLException, BookingNotFoundException {
        Booking fetched_booking = bookingsRepository.getOneHotel(ctx.pathParam("identifier", Integer.class).get());
        ctx.json(fetched_booking);
    }

    private Long generateUniqueAirMailId() throws SQLException {
        List<User> users =  this.usersController.getAll();
        List<AirBooking> existing_bookings = new ArrayList<>();
        for (User user: users) {
            existing_bookings.addAll(bookingsRepository.getAllAir(user.getLogin()));
        }
        Set<Long> MailIds = new HashSet<>();
        for(AirBooking booking: existing_bookings) {
            MailIds.add(booking.get_mail_identifier());
        }
        Set<Long> universalSet = LongStream.rangeClosed(1,10000).boxed().collect(Collectors.toSet());
        universalSet.removeAll(MailIds);
        return universalSet.stream().findFirst().orElse((long) -1);

    }

    private Long generateUniqueHotelMailId() throws SQLException {
        List<User> users =  this.usersController.getAll();
        List<HotelBooking> existing_bookings = new ArrayList<>();
        for (User user: users) {
            existing_bookings.addAll(bookingsRepository.getAllHotel(user.getLogin()));
        }
        Set<Long> MailIds = new HashSet<>();
        for(HotelBooking booking: existing_bookings) {
            MailIds.add(booking.get_mail_identifier());
        }
        Set<Long> universalSet = LongStream.rangeClosed(1,10000).boxed().collect(Collectors.toSet());
        universalSet.removeAll(MailIds);
        return universalSet.stream().findFirst().orElse((long) -1);

    }

    public void addBooking(Context ctx) throws BookingNotFoundException, SQLException  {

        String curUser = this.usersController.currentUser(ctx).getLogin();
        String type = ctx.formParam("type", String.class).get();
        switch (type) {
            case "Air":
                AirBooking new_air_booking = new AirBooking(curUser,
                        generateUniqueAirMailId(),
                        1,
                        ctx.formParam("source",""),
                        type,
                        ctx.formParam("confirmation_no",""),
                        ctx.formParam("first_name",""),
                        ctx.formParam("last_name",""),
                        ctx.formParam("airline",""),
                        ctx.formParam("iata_code",""),
                        ctx.formParam("flight_number",""),
                        ctx.formParam("origin_name",""),
                        ctx.formParam("origin_city_name",""),
                        ctx.formParam("origin_country",""),
                        ctx.formParam("origin_lat",""),
                        ctx.formParam("origin_lon",""),
                        ctx.formParam("destination_name",""),
                        ctx.formParam("destination_city_name",""),
                        ctx.formParam("destination_country",""),
                        ctx.formParam("destination_lat",""),
                        ctx.formParam("destination_lon",""),
                        ctx.formParam("departure_datetime",""),
                        ctx.formParam("departure_time_zone_id",""),
                        ctx.formParam("arrival_datetime",""),
                        ctx.formParam("arrival_time_zone_id",""),
                        ctx.formParam("ticket_number",""),
                        ctx.formParams("travelers"),
                        Boolean.TRUE);
                bookingsRepository.create(new_air_booking);

                break;
            case "Hotel":
                HotelBooking new_hotel_booking = new HotelBooking(curUser,
                        generateUniqueHotelMailId(),
                        1,
                        ctx.formParam("source",""),
                        type,
                        ctx.formParam("confirmation_no",""),
                        ctx.formParam("first_name",""),
                        ctx.formParam("last_name",""),
                        ctx.formParam("hotel_name",""),
                        ctx.formParam("address1",""),
                        ctx.formParam("address2",""),
                        ctx.formParam("city_name",""),
                        ctx.formParam("country",""),
                        ctx.formParam("postal_code",""),
                        ctx.formParam("lat",""),
                        ctx.formParam("lon",""),
                        ctx.formParam("checkin_date",""),
                        ctx.formParam("checkout_date",""),
                        ctx.formParam("time_zone_id",""),
                        ctx.formParam("number_of_rooms",Integer.class).get(),
                        ctx.formParam("created",""),
                        ctx.formParams("travelers"),
                        Boolean.TRUE);
                bookingsRepository.create(new_hotel_booking);
                break;
            default:
                System.out.println("[INFO] Booking type "+type+" not supported.");
        }
        ctx.status(201);
    }

}
