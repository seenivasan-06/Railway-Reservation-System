package railway.dao;

import railway.model.Passenger;
import railway.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access layer dedicated to standalone passenger lookups
 * (booking-scoped passenger persistence is handled transactionally
 * inside BookingDAO.saveBooking, which shares the booking's connection).
 */
public class PassengerDAO {

    public List<Passenger> findByPnr(String pnr) throws SQLException {
        String sql = "SELECT * FROM booking_passengers WHERE pnr = ? ORDER BY seat_number";
        List<Passenger> passengers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public List<Passenger> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM booking_passengers WHERE phone = ? ORDER BY id DESC";
        List<Passenger> passengers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
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
}
