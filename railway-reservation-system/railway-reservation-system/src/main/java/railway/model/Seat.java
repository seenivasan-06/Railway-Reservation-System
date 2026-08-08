package railway.model;

import java.time.LocalDate;

/**
 * Represents a single seat on a train for a specific journey date.
 */
public class Seat {

    private int id;
    private String trainNumber;
    private LocalDate journeyDate;
    private int seatNumber;
    private boolean booked;
    private String pnr;

    public Seat() {
    }

    public Seat(String trainNumber, LocalDate journeyDate, int seatNumber) {
        this.trainNumber = trainNumber;
        this.journeyDate = journeyDate;
        this.seatNumber = seatNumber;
        this.booked = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }
}
