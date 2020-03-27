package com.jhuoose.timetravel.controllers;

import com.jhuoose.timetravel.models.*;
import com.jhuoose.timetravel.repositories.AirDiscrepancyRepository;
import com.jhuoose.timetravel.repositories.BookingNotFoundException;
import com.jhuoose.timetravel.repositories.HotelDiscrepancyRepository;
import com.jhuoose.timetravel.repositories.ItineraryRepository;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscrepancyController {
    private ItineraryRepository itineraryRepository;
    private AirDiscrepancyRepository airDiscrepancyRepository;
    private HotelDiscrepancyRepository hotelDiscrepancyRepository;
    private UsersController usersController;

    public DiscrepancyController(ItineraryRepository itineraryRepository, AirDiscrepancyRepository airDiscrepancyRepository,
                                 HotelDiscrepancyRepository hotelDiscrepancyRepository, UsersController usersController) {
        this.itineraryRepository = itineraryRepository;
        this.airDiscrepancyRepository = airDiscrepancyRepository;
        this.hotelDiscrepancyRepository = hotelDiscrepancyRepository;
        this.usersController = usersController;
    }

    public void fetchHotelDiscrepancies(Context ctx) throws SQLException, BookingNotFoundException {
        String curUser = this.usersController.currentUser(ctx).getLogin();
        ctx.json(hotelDiscrepancyRepository.getAll(curUser));
    }

    public void fetchAirDiscrepancies(Context ctx) throws SQLException, BookingNotFoundException {
        String curUser = this.usersController.currentUser(ctx).getLogin();

        ctx.json(airDiscrepancyRepository.getAll(curUser));
    }

    public void detectDiscrepancies(String curUser) throws SQLException, BookingNotFoundException, ParseException {
        int airDiscrepancyCount = 0, hotelDiscrepancyCount = 0;
        boolean isConnected = false;
        airDiscrepancyRepository.clear();
        hotelDiscrepancyRepository.clear();
        for (Itinerary itinerary: itineraryRepository.getAll(curUser)) {
            isConnected = false;
            for (Itinerary itinerary1: itineraryRepository.getAll(curUser)) {
                if (!itinerary.equals(itinerary1) && areConnectedTrips(itinerary, itinerary1)) {
                    String fromCity = extractLastBooking(itinerary.getBookings()).get_destination_city();
                    String toCity = extractFirstBooking(itinerary1.getBookings()).get_origin_city();
                    airDiscrepancyCount += 1;
                    airDiscrepancyRepository.insert(new AirDiscrepancy(curUser, airDiscrepancyCount,
                            Arrays.asList(Integer.valueOf(itinerary.getIdentifier()), Integer.valueOf(itinerary1.getIdentifier())),
                            fromCity, toCity));
                    isConnected = true;
                }
            }
            if (hasTravelBookings(itinerary.getBookings())) {
                if ((!isConnected && !isRoundTrip(itinerary.getBookings())) && (!isAddedToDiscrepancies(itinerary.getIdentifier(),curUser))) {
                    String fromCity = extractLastBooking(itinerary.getBookings()).get_destination_city();
                    String toCity = extractFirstBooking(itinerary.getBookings()).get_origin_city();
                    airDiscrepancyCount += 1;
                    airDiscrepancyRepository.insert(new AirDiscrepancy(curUser, airDiscrepancyCount,
                            Arrays.asList(Integer.valueOf(itinerary.getIdentifier())), fromCity, toCity));
                }
                hotelDiscrepancyCount = detectMissingHotelBookings(curUser, itinerary, hotelDiscrepancyCount);
            } else {
                airDiscrepancyCount = detectMissingAirBookings(curUser, itinerary, airDiscrepancyCount);
            }
        }
    }

    private boolean areConnectedTrips(Itinerary itinerary, Itinerary itinerary1) throws ParseException {
        if (!hasTravelBookings(itinerary.getBookings()) || isRoundTrip(itinerary.getBookings())) {
            return false;
        }
        if (!hasTravelBookings(itinerary1.getBookings()) || isRoundTrip(itinerary1.getBookings())) {
            return false;
        }

        AirBooking firstBooking = extractFirstBooking(itinerary.getBookings());
        AirBooking firstBooking1 = extractFirstBooking(itinerary1.getBookings());
        Date firstBookingDate = extractDate(firstBooking.get_departure_datetime().split("T")[0]);
        Date firstBookingDate1 = extractDate(firstBooking1.get_departure_datetime().split("T")[0]);
        if (firstBookingDate.compareTo(firstBookingDate1) <= 0) {
            return detectMissingLinks(itinerary, itinerary1);
        } else {
            return false;
        }
    }

    private boolean detectMissingLinks(Itinerary itinerary, Itinerary itinerary1) throws ParseException {
        AirBooking firstBooking = extractFirstBooking(itinerary.getBookings());
        AirBooking lastBooking = extractLastBooking(itinerary.getBookings());
        AirBooking firstBooking1 = extractFirstBooking(itinerary1.getBookings());
        AirBooking lastBooking1 = extractLastBooking(itinerary1.getBookings());
        if (firstBooking.get_origin_city().equals(lastBooking1.get_destination_city())) {
            if (isValidDates(lastBooking, firstBooking1)) {
                return true;
            }
        }
        return false;
    }

    private int detectMissingHotelBookings(String curUser, Itinerary itinerary, int hotelDiscrepancyCount) throws ParseException, SQLException {
        AirBooking airBooking1 = null, airBooking2 = null;
        List<HotelBooking> hotelBookings = new ArrayList<>();
        for(Booking booking: itinerary.getBookings()) {
            if (booking.get_type().equals("Air")) {
                if (airBooking1 == null) {
                    airBooking1 = (AirBooking) booking;
                } else if (airBooking2 == null) {
                    airBooking2 = (AirBooking) booking;
                    if(!isOvernightStayBooked(airBooking1, airBooking2, hotelBookings)) {
                        hotelDiscrepancyCount += 1;
                        String city = airBooking1.get_destination_city();
                        hotelDiscrepancyRepository.insert(
                                new HotelDiscrepancy(curUser, hotelDiscrepancyCount, Arrays.asList(itinerary.getIdentifier()), city));
                    }
                    airBooking1 = airBooking2;
                    airBooking2 = null;
                    hotelBookings = new ArrayList<>();
                }
            } else {
                hotelBookings.add((HotelBooking) booking);
            }
        }
        return hotelDiscrepancyCount;
    }

    private int detectMissingAirBookings(String curUser, Itinerary itinerary, int airDiscrepancyCount) throws SQLException {
        String city = ((HotelBooking)itinerary.getBookings().get(0)).getCity_name();
        airDiscrepancyCount += 1;
        airDiscrepancyRepository.insert(new AirDiscrepancy(curUser, airDiscrepancyCount, Arrays.asList(itinerary.getIdentifier()), "N/A", city));
        airDiscrepancyCount += 1;
        airDiscrepancyRepository.insert(new AirDiscrepancy(curUser, airDiscrepancyCount, Arrays.asList(itinerary.getIdentifier()), city,"N/A"));
        return airDiscrepancyCount;
    }

    private boolean isOvernightStayBooked(AirBooking airBooking1, AirBooking airBooking2, List<HotelBooking> hotelBookings) throws ParseException {
        Date arrivalDate = extractDate(airBooking1.get_arrival_datetime().split("T")[0]);
        Date departureDate = extractDate(airBooking2.get_departure_datetime().split("T")[0]);
        List<Date> stayDates = extractDateRange(arrivalDate, departureDate);

        List<Date> bookedDates = new ArrayList<>();
        for (HotelBooking hotelBooking: hotelBookings) {
            Date checkinDate = extractDate(hotelBooking.getCheckin_date());
            Date checkoutDate = extractDate(hotelBooking.getCheckout_date());
            bookedDates.addAll(extractDateRange(checkinDate, checkoutDate));
        }
        bookedDates = bookedDates.stream().distinct().collect(Collectors.toList());

        if (stayDates.size() > 1) // This condition checks if overnight stay is required.
        {
            for (Date stayDate: stayDates) {
                if (! bookedDates.contains(stayDate)) // This condition checks if a booking is made for every date that requires overnight stay.
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAddedToDiscrepancies(int itineraryIdentifier, String curUser) throws SQLException {
        List<AirDiscrepancy> airDiscrepancies = airDiscrepancyRepository.getAll(curUser);
        for (AirDiscrepancy airDiscrepancy: airDiscrepancies) {
            if (airDiscrepancy.getItineraryIdentifiers().contains(itineraryIdentifier)) {
                return true;
            }
        }
        return false;
    }

    private List<Date> extractDateRange(Date date1, Date date2) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(date2);
        dates.add(date1);
        while (calendar.before(endCalendar)) {
            calendar.add(Calendar.DATE, 1);
            dates.add(calendar.getTime());
        }
        return dates;
    }

    private boolean isValidDates(AirBooking booking1, AirBooking booking2) throws ParseException {
        Date date1 = extractDate(booking1.get_arrival_datetime().split("T")[0]);
        Date date2 = extractDate(booking2.get_departure_datetime().split("T")[0]);
        long differenceInDays = computeDifferenceInDays(date1, date2);
        return ((date1.compareTo(date2) <= 0) && (differenceInDays <= 10));
    }

    private long computeDifferenceInDays(Date date1, Date date2) {
        long differenceInMillies = Math.abs(date1.getTime() - date2.getTime());
        long differenceInDays = TimeUnit.DAYS.convert(differenceInMillies, TimeUnit.MILLISECONDS);
        return differenceInDays;
    }

    private boolean hasTravelBookings(List<Booking> bookings) {
        return ((extractFirstBooking(bookings)==null) ? false : true);
    }

    private boolean isRoundTrip(List<Booking> bookings) {
        AirBooking firstBooking = extractFirstBooking(bookings);
        AirBooking lastBooking = extractLastBooking(bookings);
        return (firstBooking == null) ? false : (firstBooking.get_origin_city().equals(lastBooking.get_destination_city()));
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

    private AirBooking extractLastBooking(List<Booking> bookings) {
        AirBooking lastBooking = null;
        for (Booking booking: bookings) {
            if (booking.get_type().equals("Air")) {
                lastBooking = (AirBooking) booking;
            }
        }
        return lastBooking;
    }

    private Date extractDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(date);
    }
}
