package railway.dao;

import railway.model.Train;
import railway.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access layer for the trains table and per-train seat inventory.
 */
public class TrainDAO {

    public Train create(Train train) throws SQLException {
        String sql = "INSERT INTO trains (train_number, train_name, source, destination, "
                + "departure_time, arrival_time, total_seats, fare) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindTrain(ps, train);
            ps.executeUpdate();
            return train;
        }
    }

    public boolean update(Train train) throws SQLException {
        String sql = "UPDATE trains SET train_name = ?, source = ?, destination = ?, "
                + "departure_time = ?, arrival_time = ?, total_seats = ?, fare = ? WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, train.getTrainName());
            ps.setString(2, train.getSource());
            ps.setString(3, train.getDestination());
            ps.setTime(4, Time.valueOf(train.getDepartureTime()));
            ps.setTime(5, Time.valueOf(train.getArrivalTime()));
            ps.setInt(6, train.getTotalSeats());
            ps.setBigDecimal(7, train.getFare());
            ps.setString(8, train.getTrainNumber());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String trainNumber) throws SQLException {
        String sql = "DELETE FROM trains WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            return ps.executeUpdate() > 0;
        }
    }

    public Train findByNumber(String trainNumber) throws SQLException {
        String sql = "SELECT * FROM trains WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Train> findAll() throws SQLException {
        String sql = "SELECT * FROM trains ORDER BY train_number";
        List<Train> trains = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trains.add(mapRow(rs));
            }
        }
        return trains;
    }

    public List<Train> searchByRoute(String source, String destination) throws SQLException {
        String sql = "SELECT * FROM trains WHERE LOWER(source) = LOWER(?) AND LOWER(destination) = LOWER(?) "
                + "ORDER BY departure_time";
        List<Train> trains = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, destination);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trains.add(mapRow(rs));
                }
            }
        }
        return trains;
    }

    public boolean exists(String trainNumber) throws SQLException {
        return findByNumber(trainNumber) != null;
    }

    // ----------------------------------------------------------------
    // Seat inventory management (per train, per journey date)
    // ----------------------------------------------------------------

    /**
     * Lazily creates the seat rows for a train on a given journey date if
     * they don't already exist. Must be called within the same connection
     * / transaction as the booking that follows, to avoid races.
     */
    public void ensureSeatsExist(Connection conn, String trainNumber, LocalDate journeyDate, int totalSeats)
            throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM seats WHERE train_number = ? AND journey_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, trainNumber);
            ps.setDate(2, java.sql.Date.valueOf(journeyDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return; // already populated
                }
            }
        }

        String insertSql = "INSERT INTO seats (train_number, journey_date, seat_number, is_booked) VALUES (?, ?, ?, FALSE)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (int seatNo = 1; seatNo <= totalSeats; seatNo++) {
                ps.setString(1, trainNumber);
                ps.setDate(2, java.sql.Date.valueOf(journeyDate));
                ps.setInt(3, seatNo);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int countAvailableSeats(Connection conn, String trainNumber, LocalDate journeyDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seats WHERE train_number = ? AND journey_date = ? AND is_booked = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setDate(2, java.sql.Date.valueOf(journeyDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countBookedSeats(Connection conn, String trainNumber, LocalDate journeyDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seats WHERE train_number = ? AND journey_date = ? AND is_booked = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setDate(2, java.sql.Date.valueOf(journeyDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Locks and returns the next N available seat numbers using SELECT ... FOR UPDATE
     * so concurrent bookings cannot double-allocate the same seat.
     */
    public List<Integer> lockAvailableSeats(Connection conn, String trainNumber, LocalDate journeyDate, int count)
            throws SQLException {
        String sql = "SELECT seat_number FROM seats WHERE train_number = ? AND journey_date = ? "
                + "AND is_booked = FALSE ORDER BY seat_number LIMIT ? FOR UPDATE";
        List<Integer> seatNumbers = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setDate(2, java.sql.Date.valueOf(journeyDate));
            ps.setInt(3, count);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seatNumbers.add(rs.getInt("seat_number"));
                }
            }
        }
        return seatNumbers;
    }

    public void markSeatsBooked(Connection conn, String trainNumber, LocalDate journeyDate,
                                 List<Integer> seatNumbers, String pnr) throws SQLException {
        String sql = "UPDATE seats SET is_booked = TRUE, pnr = ? WHERE train_number = ? AND journey_date = ? AND seat_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int seatNo : seatNumbers) {
                ps.setString(1, pnr);
                ps.setString(2, trainNumber);
                ps.setDate(3, java.sql.Date.valueOf(journeyDate));
                ps.setInt(4, seatNo);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void releaseSeats(Connection conn, String pnr) throws SQLException {
        String sql = "UPDATE seats SET is_booked = FALSE, pnr = NULL WHERE pnr = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------

    private void bindTrain(PreparedStatement ps, Train train) throws SQLException {
        ps.setString(1, train.getTrainNumber());
        ps.setString(2, train.getTrainName());
        ps.setString(3, train.getSource());
        ps.setString(4, train.getDestination());
        ps.setTime(5, Time.valueOf(train.getDepartureTime()));
        ps.setTime(6, Time.valueOf(train.getArrivalTime()));
        ps.setInt(7, train.getTotalSeats());
        ps.setBigDecimal(8, train.getFare());
    }

    private Train mapRow(ResultSet rs) throws SQLException {
        Train train = new Train();
        train.setTrainNumber(rs.getString("train_number"));
        train.setTrainName(rs.getString("train_name"));
        train.setSource(rs.getString("source"));
        train.setDestination(rs.getString("destination"));
        train.setDepartureTime(rs.getTime("departure_time").toLocalTime());
        train.setArrivalTime(rs.getTime("arrival_time").toLocalTime());
        train.setTotalSeats(rs.getInt("total_seats"));
        train.setFare(rs.getBigDecimal("fare"));
        return train;
    }
}
