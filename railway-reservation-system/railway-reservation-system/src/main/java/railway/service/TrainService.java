package railway.service;

import railway.dao.TrainDAO;
import railway.model.Train;
import railway.util.DBConnection;
import railway.util.InputValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for train management and seat-availability lookups.
 */
public class TrainService {

    private final TrainDAO trainDAO = new TrainDAO();

    public Train addTrain(String trainNumber, String trainName, String source, String destination,
                           String departureTime, String arrivalTime, int totalSeats, BigDecimal fare) {
        validateTrainFields(trainNumber, trainName, source, destination, departureTime, arrivalTime, totalSeats, fare);
        try {
            if (trainDAO.exists(trainNumber.trim())) {
                throw new ServiceException("A train with number " + trainNumber + " already exists.");
            }
            Train train = new Train(trainNumber.trim(), trainName.trim(), source.trim(), destination.trim(),
                    LocalTime.parse(departureTime.trim(), InputValidator.TIME_FORMAT),
                    LocalTime.parse(arrivalTime.trim(), InputValidator.TIME_FORMAT),
                    totalSeats, fare);
            return trainDAO.create(train);
        } catch (SQLException e) {
            throw new ServiceException("Database error while adding train: " + e.getMessage(), e);
        }
    }

    public void updateTrain(String trainNumber, String trainName, String source, String destination,
                             String departureTime, String arrivalTime, int totalSeats, BigDecimal fare) {
        validateTrainFields(trainNumber, trainName, source, destination, departureTime, arrivalTime, totalSeats, fare);
        try {
            if (!trainDAO.exists(trainNumber.trim())) {
                throw new ServiceException("No train found with number " + trainNumber);
            }
            Train train = new Train(trainNumber.trim(), trainName.trim(), source.trim(), destination.trim(),
                    LocalTime.parse(departureTime.trim(), InputValidator.TIME_FORMAT),
                    LocalTime.parse(arrivalTime.trim(), InputValidator.TIME_FORMAT),
                    totalSeats, fare);
            trainDAO.update(train);
        } catch (SQLException e) {
            throw new ServiceException("Database error while updating train: " + e.getMessage(), e);
        }
    }

    public void deleteTrain(String trainNumber) {
        if (InputValidator.isNullOrBlank(trainNumber)) {
            throw new ServiceException("Train number is required.");
        }
        try {
            if (!trainDAO.delete(trainNumber.trim())) {
                throw new ServiceException("No train found with number " + trainNumber);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error while deleting train "
                    + "(it may have existing bookings): " + e.getMessage(), e);
        }
    }

    public List<Train> getAllTrains() {
        try {
            return trainDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching trains: " + e.getMessage(), e);
        }
    }

    public List<Train> searchTrains(String source, String destination) {
        if (InputValidator.isNullOrBlank(source) || InputValidator.isNullOrBlank(destination)) {
            throw new ServiceException("Source and destination are required.");
        }
        try {
            return trainDAO.searchByRoute(source.trim(), destination.trim());
        } catch (SQLException e) {
            throw new ServiceException("Database error while searching trains: " + e.getMessage(), e);
        }
    }

    public Train getTrain(String trainNumber) {
        try {
            Train train = trainDAO.findByNumber(trainNumber.trim());
            if (train == null) {
                throw new ServiceException("No train found with number " + trainNumber);
            }
            return train;
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching train: " + e.getMessage(), e);
        }
    }

    /** Available seats for a train on a specific journey date (creates seat inventory on first lookup). */
    public int getAvailableSeats(String trainNumber, LocalDate journeyDate) {
        try {
            Train train = getTrain(trainNumber);
            try (Connection conn = DBConnection.getConnection()) {
                trainDAO.ensureSeatsExist(conn, train.getTrainNumber(), journeyDate, train.getTotalSeats());
                return trainDAO.countAvailableSeats(conn, train.getTrainNumber(), journeyDate);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error while checking seat availability: " + e.getMessage(), e);
        }
    }

    public int getBookedSeats(String trainNumber, LocalDate journeyDate) {
        try {
            Train train = getTrain(trainNumber);
            try (Connection conn = DBConnection.getConnection()) {
                trainDAO.ensureSeatsExist(conn, train.getTrainNumber(), journeyDate, train.getTotalSeats());
                return trainDAO.countBookedSeats(conn, train.getTrainNumber(), journeyDate);
            }
        } catch (SQLException e) {
            throw new ServiceException("Database error while checking booked seats: " + e.getMessage(), e);
        }
    }

    private void validateTrainFields(String trainNumber, String trainName, String source, String destination,
                                      String departureTime, String arrivalTime, int totalSeats, BigDecimal fare) {
        if (InputValidator.isNullOrBlank(trainNumber)) {
            throw new ServiceException("Train number is required.");
        }
        if (InputValidator.isNullOrBlank(trainName)) {
            throw new ServiceException("Train name is required.");
        }
        if (InputValidator.isNullOrBlank(source) || InputValidator.isNullOrBlank(destination)) {
            throw new ServiceException("Source and destination are required.");
        }
        if (source.trim().equalsIgnoreCase(destination.trim())) {
            throw new ServiceException("Source and destination cannot be the same.");
        }
        try {
            LocalTime.parse(departureTime.trim(), InputValidator.TIME_FORMAT);
            LocalTime.parse(arrivalTime.trim(), InputValidator.TIME_FORMAT);
        } catch (Exception e) {
            throw new ServiceException("Departure/arrival time must be in HH:mm format (e.g. 14:30).");
        }
        if (totalSeats <= 0) {
            throw new ServiceException("Total seats must be a positive number.");
        }
        if (fare == null || fare.signum() < 0) {
            throw new ServiceException("Fare must be zero or a positive amount.");
        }
    }
}
