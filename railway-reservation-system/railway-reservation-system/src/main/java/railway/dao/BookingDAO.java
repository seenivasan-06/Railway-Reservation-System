package railway.dao;

import railway.model.Booking;
import railway.model.Passenger;
import railway.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access layer for bookings and booking_passengers.
 * Seat-related operations live in TrainDAO since they operate on the
 * shared "seats" table; BookingDAO composes them within a transaction.
 */
public class BookingDAO {

    /**
     * Persists a booking header row plus one row per passenger, and marks
     * the assigned seats as booked, all within a single transaction.
     * The Connection is expected to already have auto-commit disabled by
     * the caller (BookingService), which also commits/rolls back.
     */
    public void saveBooking(Connection conn, Booking booking, List<Integer> seatNumbers) throws SQLException {
        String bookingSql = "INSERT INTO bookings (pnr, user_id, train_number, journey_date, total_fare, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
            ps.setString(1, booking.getPnr());
            ps.setInt(2, booking.getUserId());
            ps.setString(3, booking.getTrainNumber());
            ps.setDate(4, java.sql.Date.valueOf(booking.getJourneyDate()));
            ps.setBigDecimal(5, booking.getTotalFare());
            ps.setString(6, booking.getStatus().name());
            ps.executeUpdate();
        }

        String passengerSql = "INSERT INTO booking_passengers (pnr, name, age, gender, phone, seat_number) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(passengerSql)) {
            List<Passenger> passengers = booking.getPassengers();
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                int seatNumber = seatNumbers.get(i);
                p.setSeatNumber(seatNumber);
                p.setPnr(booking.getPnr());

                ps.setString(1, booking.getPnr());
                ps.setString(2, p.getName());
                ps.setInt(3, p.getAge());
                ps.setString(4, String.valueOf(p.getGender()));
                ps.setString(5, p.getPhone());
                ps.setInt(6, seatNumber);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public boolean pnrExists(Connection conn, String pnr) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE pnr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Booking findByPnr(String pnr) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByPnr(conn, pnr);
        }
    }

    public Booking findByPnr(Connection conn, String pnr) throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.source, t.destination FROM bookings b "
                + "JOIN trains t ON b.train_number = t.train_number WHERE b.pnr = ?";
        Booking booking = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    booking = mapRow(rs);
                }
            }
        }
        if (booking != null) {
            booking.setPassengers(findPassengers(conn, pnr));
        }
        return booking;
    }

    public List<Passenger> findPassengers(Connection conn, String pnr) throws SQLException {
        String sql = "SELECT * FROM booking_passengers WHERE pnr = ? ORDER BY seat_number";
        List<Passenger> passengers = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Passenger p = new Passenger();
                    p.setId(rs.getInt("id"));
                    p.setPnr(rs.getString("pnr"));
                    p.setName(rs.getString("name"));
                    p.setAge(rs.getInt("age"));
                    p.setGender(rs.getString("gender").charAt(0));
                    p.setPhone(rs.getString("phone"));
                    p.setSeatNumber(rs.getInt("seat_number"));
                    passengers.add(p);
                }
            }
        }
        return passengers;
    }

    public void updateStatus(Connection conn, String pnr, Booking.Status status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE pnr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, pnr);
            ps.executeUpdate();
        }
    }

    public List<Booking> findByUserId(int userId) throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.source, t.destination FROM bookings b "
                + "JOIN trains t ON b.train_number = t.train_number WHERE b.user_id = ? ORDER BY b.booking_date DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRow(rs);
                    b.setPassengers(findPassengers(conn, b.getPnr()));
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.source, t.destination FROM bookings b "
                + "JOIN trains t ON b.train_number = t.train_number ORDER BY b.booking_date DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
        }
        return bookings;
    }

    public List<Booking> findByStatus(Booking.Status status) throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.source, t.destination FROM bookings b "
                + "JOIN trains t ON b.train_number = t.train_number WHERE b.status = ? ORDER BY b.booking_date DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    /** train_number -> number of CONFIRMED bookings, for admin statistics. */
    public List<Object[]> trainWiseBookingStats() throws SQLException {
        String sql = "SELECT t.train_number, t.train_name, COUNT(b.pnr) AS total_bookings, "
                + "SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed, "
                + "SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled "
                + "FROM trains t LEFT JOIN bookings b ON t.train_number = b.train_number "
                + "GROUP BY t.train_number, t.train_name ORDER BY t.train_number";
        List<Object[]> stats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.add(new Object[]{
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getInt("total_bookings"),
                        rs.getInt("confirmed"),
                        rs.getInt("cancelled")
                });
            }
        }
        return stats;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setPnr(rs.getString("pnr"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setTrainNumber(rs.getString("train_number"));
        booking.setJourneyDate(rs.getDate("journey_date").toLocalDate());
        booking.setTotalFare(rs.getBigDecimal("total_fare"));
        booking.setStatus(Booking.Status.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("booking_date");
        if (ts != null) {
            booking.setBookingDate(ts.toLocalDateTime());
        }
        booking.setTrainName(rs.getString("train_name"));
        booking.setSource(rs.getString("source"));
        booking.setDestination(rs.getString("destination"));
        return booking;
    }
}
