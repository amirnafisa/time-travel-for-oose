package com.jhuoose.timetravel.controllers;

import com.jhuoose.timetravel.models.*;
import com.jhuoose.timetravel.repositories.BookingNotFoundException;
import com.jhuoose.timetravel.repositories.BookingsRepository;
import com.jhuoose.timetravel.repositories.ItineraryNotFoundException;
import com.jhuoose.timetravel.repositories.ItineraryRepository;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ItineraryController {
    private ItineraryRepository itineraryRepository;
    private BookingsRepository bookingsRepository;
    private DiscrepancyController discrepancyController;
    private UsersController usersController;

    public ItineraryController(ItineraryRepository itineraryRepository, BookingsRepository bookingsRepository,
                               DiscrepancyController discrepancyController, UsersController usersController) {
        this.itineraryRepository = itineraryRepository;
        this.bookingsRepository = bookingsRepository;
        this.discrepancyController = discrepancyController;
        this.usersController = usersController;
    }

    public void syncItineraries(Context ctx) throws ParseException, BookingNotFoundException, SQLException {
        String curUser = this.usersController.currentUser(ctx).getLogin();
        List<AirBooking> airBookings = bookingsRepository.getAllAir(curUser);
        itineraryRepository.clear();
        airBookings.sort(AirBooking.comparator);
        int itinerary_count = 0;
        boolean isNewItinerary = true;
        List<Booking> currentBookings = new ArrayList<>();
        List<Integer> addedHotelBookings = new ArrayList<>();
        AirBooking previousBooking = null, firstBooking = null;

        for(AirBooking airBooking: airBookings) {

            // Beginning of a new itinerary
            if (isNewItinerary) {
                itinerary_count += 1;
                currentBookings.add(airBooking);
                previousBooking = firstBooking = airBooking;
                isNewItinerary = false;
            }
            else {
                // The current booking and previous booking are related to each other
                if (isSameTrip(previousBooking, airBooking)) {
                    // Adding hotel bookings, if any, in between the two flight bookings
                    addAccomodation(curUser, currentBookings, addedHotelBookings, previousBooking, airBooking);
                    currentBookings.add(airBooking);
                    previousBooking = airBooking;
                    // If a round trip is completed with the current booking, then add it to the itinerary database
                    if (airBooking.get_destination_city().equals(firstBooking.get_origin_city())) {
                        itineraryRepository.insert(
                                new Itinerary(curUser, itinerary_count, currentBookings)
                        );
                        isNewItinerary = true;
                        currentBookings = new ArrayList<>();
                    }
                }
                else {
                    // If the previous booking is not related to the current booking, add the itinerary till the previous booking into the itinerary database
                    addAccomodation(curUser, currentBookings, addedHotelBookings, previousBooking, null);
                    itineraryRepository.insert(
                            new Itinerary(curUser, itinerary_count, currentBookings)
                    );
                    // Refresh the currentBookings[] list to proceed with the subsequent bookings and form other itineraries from them
                    itinerary_count += 1;
                    currentBookings = new ArrayList<>();
                    currentBookings.add(airBooking);
                    previousBooking = firstBooking = airBooking;
                }
            }
        }
        if (! currentBookings.isEmpty()) {
            addAccomodation(curUser, currentBookings, addedHotelBookings, previousBooking, null);
            itineraryRepository.insert(new Itinerary(curUser, itinerary_count, currentBookings));
        }
        addAllAccomodations(curUser, addedHotelBookings, itinerary_count);

        discrepancyController.detectDiscrepancies(curUser);
        ctx.status(201);
    }

    public void fetchItineraries(Context ctx) throws SQLException, BookingNotFoundException, ParseException {
        String curUser = this.usersController.currentUser(ctx).getLogin();
        List<Itinerary> itineraries = itineraryRepository.getAll(curUser);
        ctx.json(orderItinerariesInDisplayFormat(itineraries));
    }

    public void deleteBooking(Context ctx) throws SQLException, BookingNotFoundException, ItineraryNotFoundException {
        int itineraryIdToBeUpdated = ctx.pathParam("itinerary_id", Integer.class).get();
        String bookingIdToBeDeleted = ctx.pathParam("booking_id", String.class).get();

        itineraryRepository.deleteBookingEntry(itineraryIdToBeUpdated, bookingIdToBeDeleted);
        ctx.status(201);
    }

    private void addAccomodation(String curUser, List<Booking> currentBookings, List<Integer> addedHotelBookings, AirBooking arrivalBooking, AirBooking departureBooking)
            throws SQLException, ParseException {
        List<HotelBooking> hotelBookings = bookingsRepository.getAllHotel(curUser);
        for (HotelBooking hotelBooking: hotelBookings) {
            if (isValidBooking(hotelBooking, arrivalBooking, departureBooking)) {
                currentBookings.add(hotelBooking);
                addedHotelBookings.add(hotelBooking.get_booking_identifier());
            }
        }
    }

    private List<Itinerary> orderItinerariesInDisplayFormat(List<Itinerary> itineraries) throws ParseException {
        List<Itinerary> orderedItineraries = new ArrayList<>();
        int i=0;
        for(; (i < itineraries.size()) && hasTravelBookings(itineraries.get(i).getBookings()); i+=1);
        List<Integer> addedHotelItineraries = new ArrayList<>();
        for(int j=0; j<i; j+=1) {
            addSingleHotelItineraries(orderedItineraries, itineraries, i, j, addedHotelItineraries);
            orderedItineraries.add(itineraries.get(j));
        }
        addLastHotelBookings(orderedItineraries, itineraries, i, addedHotelItineraries);
        Collections.reverse(orderedItineraries);
        return orderedItineraries;
    }

    private void addSingleHotelItineraries(List<Itinerary> orderedItineraries, List<Itinerary> itineraries, int i, int j, List<Integer> addedHotelItineraries) throws ParseException {
        Booking firstBooking = itineraries.get(j).getBookings().get(0);
        Date firstDate, hotelDate = null;
        if (firstBooking.get_type().equals("Air")) {
            firstDate = extractDate(((AirBooking)firstBooking).get_departure_datetime());
        } else {
            firstDate = extractDate(((HotelBooking)firstBooking).getCheckin_date());
        }
        while (i < itineraries.size()) {
            Booking hotelBooking = itineraries.get(i).getBookings().get(0);
            hotelDate = extractDate(((HotelBooking)hotelBooking).getCheckout_date());
            if ((!addedHotelItineraries.contains(i)) && (hotelDate.compareTo(firstDate) <= 0)) {
                orderedItineraries.add(itineraries.get(i));
                addedHotelItineraries.add(i);
            }
            i+=1;
        }
    }

    private void addLastHotelBookings(List<Itinerary> orderedItineraries, List<Itinerary> itineraries, int i, List<Integer> addedHotelItineraries) throws ParseException {
        if (i > 0) {
            List<Booking> bookings = itineraries.get(i-1).getBookings();
            Booking lastBooking = bookings.get(bookings.size()-1);
            Date lastDate, hotelDate = null;
            if (lastBooking.get_type().equals("Air")) {
                lastDate = extractDate(((AirBooking)lastBooking).get_arrival_datetime());
            } else {
                lastDate = extractDate(((HotelBooking)lastBooking).getCheckout_date());
            }
            while (i < itineraries.size()) {
                Booking hotelBooking = itineraries.get(i).getBookings().get(0);
                hotelDate = extractDate(((HotelBooking)hotelBooking).getCheckin_date());
                if ((!addedHotelItineraries.contains(i)) && (hotelDate.compareTo(lastDate) >= 0)) {
                    orderedItineraries.add(itineraries.get(i));
                    addedHotelItineraries.add(i);
                }
                i+=1;
            }
        }
    }

    private AirBooking extractFirstBooking(List<Booking> bookings) {
        AirBooking firstBooking = null;
        for (Booking booking: bookings) {
            if (booking.get_type().equals("Air")) {
                firstBooking = (AirBooking) booking;
                break;
            }
        }
        return firstBooking;
    }

    private boolean hasTravelBookings(List<Booking> bookings) {
        return ((extractFirstBooking(bookings)==null) ? false : true);
    }

    private boolean isValidBooking(HotelBooking hotelBooking, AirBooking arrivalBooking, AirBooking departureBooking)
            throws ParseException {
        // Compute validity of booked location
        boolean isSameCity = isSameCity(arrivalBooking.get_destination_city(), hotelBooking.getCity_name());
        boolean isNeighborLocation = isNeighborLocation(arrivalBooking.get_destination_lat(),
                arrivalBooking.get_destination_lon(), hotelBooking.getLat(), hotelBooking.getLon());

        // Compute validity of booked dates
        Date checkInDate = extractDate(hotelBooking.getCheckin_date());
        Date checkOutDate = extractDate(hotelBooking.getCheckout_date());
        Date arrivalDate = extractDate(arrivalBooking.get_arrival_datetime().split("T")[0]);
        Date departureDate = null;
        long differenceInDays = 0;
        if (departureBooking!=null) {
            departureDate = extractDate(departureBooking.get_departure_datetime().split("T")[0]);
        } else {
            long differenceInMillies = Math.abs(checkInDate.getTime() - arrivalDate.getTime());
            differenceInDays = TimeUnit.DAYS.convert(differenceInMillies, TimeUnit.MILLISECONDS);
        }
        boolean isValidDates = (checkInDate.compareTo(arrivalDate) >= 0) && (departureDate==null ? differenceInDays<=1 : checkOutDate.compareTo(departureDate) <= 0);

        return ((isSameCity || isNeighborLocation) && isValidDates);
    }

    private boolean isNeighborLocation(String latitude1, String longitude1, String latitude2, String longitude2) {
        float latitudeDistance = computeDistance(latitude1, latitude2);
        float longitudeDistance = computeDistance(longitude1, longitude2);
        return ((latitudeDistance < 1) && (longitudeDistance < 1));
    }

    private boolean isSameCity(String city1, String city2) {
        return (city1.equals(city2));
    }

    private float computeDistance(String location1, String location2) {
        return Math.abs(Float.parseFloat(location1) - Float.parseFloat(location2));
    }

    private boolean isSameTrip(AirBooking airBooking1, AirBooking airBooking2) throws ParseException {
        Date date1 = extractDate(airBooking1.get_arrival_datetime().split("T")[0]);
        Date date2 = extractDate(airBooking2.get_departure_datetime().split("T")[0]);
        long differenceInMillies = Math.abs(date1.getTime() - date2.getTime());
        long differenceInDays = TimeUnit.DAYS.convert(differenceInMillies, TimeUnit.MILLISECONDS);
        boolean isSameCity = isSameCity(airBooking1.get_destination_city(), airBooking2.get_origin_city());
        return ((isSameCity) && (differenceInDays <= 10));
    }

    private Date extractDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(date);
    }

    private void addAllAccomodations(String cur_User, List<Integer> addedHotelBookings, int itinerary_count) throws SQLException, ParseException {
        List<HotelBooking> hotelBookings = bookingsRepository.getAllHotel(cur_User);
        hotelBookings.sort(HotelBooking.comparator);
        List<Booking> currentBookings = new ArrayList<>();
        HotelBooking previousBooking = null;

        for (HotelBooking hotelBooking : hotelBookings) {
            if (!addedHotelBookings.contains(hotelBooking.get_booking_identifier())) {
                if (currentBookings.isEmpty()) {
                    currentBookings.add(hotelBooking);
                    addedHotelBookings.add(hotelBooking.get_booking_identifier());
                    previousBooking = hotelBooking;
                } else {
                    // Compute validity of booked locations
                    boolean isSameCity = isSameCity(previousBooking.getCity_name(), hotelBooking.getCity_name());
                    boolean isNeighborLocation = isNeighborLocation(previousBooking.getLat(), previousBooking.getLon(),
                            hotelBooking.getLat(), hotelBooking.getLon());

                    // Compute validity of booked dates
                    Date previousCheckoutDate = extractDate(previousBooking.getCheckout_date());
                    Date currentCheckinDate = extractDate(hotelBooking.getCheckin_date());
                    long differenceInMillies = Math.abs(previousCheckoutDate.getTime() - currentCheckinDate.getTime());
                    long differenceInDays = TimeUnit.DAYS.convert(differenceInMillies, TimeUnit.MILLISECONDS);
                    boolean isValidDate = ((previousCheckoutDate.compareTo(currentCheckinDate) <= 0) && (differenceInDays <= 1));

                    if ((isSameCity || isNeighborLocation) && isValidDate) {
                        currentBookings.add(hotelBooking);
                        addedHotelBookings.add(hotelBooking.get_booking_identifier());
                        previousBooking = hotelBooking;
                    } else {
                        itinerary_count += 1;
                        itineraryRepository.insert(new Itinerary(cur_User, itinerary_count, currentBookings));
                        currentBookings = new ArrayList<>();
                        currentBookings.add(hotelBooking);
                        addedHotelBookings.add(hotelBooking.get_booking_identifier());
                        previousBooking = hotelBooking;
                    }
                }
            }
        }
        if (! currentBookings.isEmpty()) {
            itinerary_count += 1;
            itineraryRepository.insert(new Itinerary(cur_User, itinerary_count, currentBookings));
        }
    }
}
