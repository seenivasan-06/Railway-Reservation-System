package railway.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reservation identified by a unique PNR.
 * A booking can contain multiple passengers.
 */
public class Booking {

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    private String pnr;
    private int userId;
    private String trainNumber;
    private LocalDate journeyDate;
    private BigDecimal totalFare;
    private Status status;
    private LocalDateTime bookingDate;
    private List<Passenger> passengers = new ArrayList<>();

    // Fields populated by joined queries (not persisted directly on this table)
    private String trainName;
    private String source;
    private String destination;

    public Booking() {
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("PNR            : ").append(pnr).append("\n");
        sb.append("Train          : ").append(trainNumber).append(" - ").append(trainName).append("\n");
        sb.append("Route          : ").append(source).append(" -> ").append(destination).append("\n");
        sb.append("Journey Date   : ").append(journeyDate).append("\n");
        sb.append("Status         : ").append(status).append("\n");
        sb.append("Total Fare     : Rs.").append(totalFare).append("\n");
        sb.append("Booked On      : ").append(bookingDate).append("\n");
        sb.append("Passengers     :\n");
        for (Passenger p : passengers) {
            sb.append("   - ").append(p).append("\n");
        }
        sb.append("========================================");
        return sb.toString();
    }
}
