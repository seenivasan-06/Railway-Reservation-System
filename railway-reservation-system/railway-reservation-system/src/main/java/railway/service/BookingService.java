package railway.service;

import railway.dao.BookingDAO;
import railway.dao.TrainDAO;
import railway.model.Booking;
import railway.model.Passenger;
import railway.model.Train;
import railway.util.DBConnection;
import railway.util.InputValidator;
import railway.util.PNRGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for ticket reservation and cancellation.
 * <p>
 * Booking and cancellation are the two operations that touch multiple
 * tables (bookings, booking_passengers, seats) and must be atomic, so
 * both are wrapped in explicit JDBC transactions here.
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final TrainDAO trainDAO = new TrainDAO();

    /**
     * Books a ticket for one or more passengers on a given train/date.
     * <p>
     * Seat allocation works like this:
     * 1. The seat inventory for (train, journeyDate) is lazily created the
     *    first time it's needed (one row per physical seat).
     * 2. Inside a transaction, the required number of free seats are
     *    selected with SELECT ... FOR UPDATE, which locks those rows so a
     *    concurrent booking cannot grab the same seats.
     * 3. If enough seats are available, they are marked booked and the
     *    booking + passenger rows are inserted; the whole thing commits
     *    together. If anything fails, everything rolls back and no seats
     *    are held.
     */
    public Booking bookTicket(int userId, String trainNumber, LocalDate journeyDate, List<Passenger> passengers) {
        if (passengers == null || passengers.isEmpty()) {
            throw new ServiceException("At least one passenger is required.");
        }
        for (Passenger p : passengers) {
            if (InputValidator.isNullOrBlank(p.getName())) {
                throw new ServiceException("Passenger name cannot be empty.");
            }
            if (!InputValidator.isValidAge(p.getAge())) {
                throw new ServiceException("Passenger age must be between 1 and 129.");
            }
            if (!InputValidator.isValidGender(String.valueOf(p.getGender()))) {
                throw new ServiceException("Passenger gender must be M, F or O.");
            }
            if (!InputValidator.isValidPhone(p.getPhone())) {
                throw new ServiceException("Passenger phone number must be exactly 10 digits.");
            }
        }
        if (!InputValidator.isFutureOrTodayDate(journeyDate)) {
            throw new ServiceException("Journey date cannot be in the past.");
        }

        Train train;
        try {
            train = trainDAO.findByNumber(trainNumber.trim());
        } catch (SQLException e) {
            throw new ServiceException("Database error while looking up train: " + e.getMessage(), e);
        }
        if (train == null) {
            throw new ServiceException("No train found with number " + trainNumber);
        }

        int seatsNeeded = passengers.size();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            trainDAO.ensureSeatsExist(conn, train.getTrainNumber(), journeyDate, train.getTotalSeats());

            List<Integer> seatNumbers = trainDAO.lockAvailableSeats(conn, train.getTrainNumber(), journeyDate, seatsNeeded);
            if (seatNumbers.size() < seatsNeeded) {
                conn.rollback();
                throw new ServiceException(String.format(
                        "Only %d seat(s) available on train %s for %s; %d requested.",
                        seatNumbers.size(), train.getTrainNumber(), journeyDate, seatsNeeded));
            }

            String pnr = generateUniquePnr(conn);
            BigDecimal totalFare = train.getFare().multiply(BigDecimal.valueOf(seatsNeeded));

            Booking booking = new Booking();
            booking.setPnr(pnr);
            booking.setUserId(userId);
            booking.setTrainNumber(train.getTrainNumber());
            booking.setJourneyDate(journeyDate);
            booking.setTotalFare(totalFare);
            booking.setStatus(Booking.Status.CONFIRMED);
            booking.setPassengers(passengers);

            trainDAO.markSeatsBooked(conn, train.getTrainNumber(), journeyDate, seatNumbers, pnr);
            bookingDAO.saveBooking(conn, booking, seatNumbers);

            conn.commit();

            booking.setTrainName(train.getTrainName());
            booking.setSource(train.getSource());
            booking.setDestination(train.getDestination());
            return booking;

        } catch (ServiceException se) {
            rollbackQuietly(conn);
            throw se;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new ServiceException("Database error while booking ticket: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Cancels a booking: releases its seats and marks it CANCELLED.
     * Refund is calculated as the full fare paid (simple policy — no
     * cancellation charge — suitable for a college project; a real
     * system would apply slab-based cancellation charges).
     */
    public BigDecimal cancelTicket(String pnr) {
        if (InputValidator.isNullOrBlank(pnr)) {
            throw new ServiceException("PNR is required.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Booking booking = bookingDAO.findByPnr(conn, pnr.trim());
            if (booking == null) {
                conn.rollback();
                throw new ServiceException("No booking found with PNR " + pnr);
            }
            if (booking.getStatus() == Booking.Status.CANCELLED) {
                conn.rollback();
                throw new ServiceException("This ticket (PNR " + pnr + ") is already cancelled.");
            }

            trainDAO.releaseSeats(conn, booking.getPnr());
            bookingDAO.updateStatus(conn, booking.getPnr(), Booking.Status.CANCELLED);

            conn.commit();
            return booking.getTotalFare();

        } catch (ServiceException se) {
            rollbackQuietly(conn);
            throw se;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new ServiceException("Database error while cancelling ticket: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    public Booking getBookingDetails(String pnr) {
        if (InputValidator.isNullOrBlank(pnr)) {
            throw new ServiceException("PNR is required.");
        }
        try {
            Booking booking = bookingDAO.findByPnr(pnr.trim());
            if (booking == null) {
                throw new ServiceException("No booking found with PNR " + pnr);
            }
            return booking;
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching booking: " + e.getMessage(), e);
        }
    }

    public List<Booking> getBookingsForUser(int userId) {
        try {
            return bookingDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching bookings: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAllBookings() {
        try {
            return bookingDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching bookings: " + e.getMessage(), e);
        }
    }

    public List<Booking> getCancelledBookings() {
        try {
            return bookingDAO.findByStatus(Booking.Status.CANCELLED);
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching cancelled bookings: " + e.getMessage(), e);
        }
    }

    public List<Object[]> getTrainWiseStatistics() {
        try {
            return bookingDAO.trainWiseBookingStats();
        } catch (SQLException e) {
            throw new ServiceException("Database error while computing statistics: " + e.getMessage(), e);
        }
    }

    private String generateUniquePnr(Connection conn) throws SQLException {
        String pnr;
        int attempts = 0;
        do {
            pnr = PNRGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new ServiceException("Could not generate a unique PNR, please try again.");
            }
        } while (bookingDAO.pnrExists(conn, pnr));
        return pnr;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // best effort
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignored) {
                // best effort
            }
        }
    }
}
