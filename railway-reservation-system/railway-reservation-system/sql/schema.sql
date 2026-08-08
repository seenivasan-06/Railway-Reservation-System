-- ============================================================
-- Railway Reservation System - Database Schema
-- ============================================================

DROP DATABASE IF EXISTS railway_reservation;
CREATE DATABASE railway_reservation;
USE railway_reservation;

-- ------------------------------------------------------------
-- Table: users
-- ------------------------------------------------------------
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: admins
-- ------------------------------------------------------------
CREATE TABLE admins (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: trains
-- ------------------------------------------------------------
CREATE TABLE trains (
    train_number    VARCHAR(10)  PRIMARY KEY,
    train_name      VARCHAR(100) NOT NULL,
    source          VARCHAR(50)  NOT NULL,
    destination     VARCHAR(50)  NOT NULL,
    departure_time  TIME         NOT NULL,
    arrival_time    TIME         NOT NULL,
    total_seats     INT          NOT NULL CHECK (total_seats > 0),
    fare            DECIMAL(10,2) NOT NULL CHECK (fare >= 0),
    INDEX idx_source_dest (source, destination)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: bookings
-- One row per reservation (a PNR can cover multiple passengers)
-- ------------------------------------------------------------
CREATE TABLE bookings (
    pnr           VARCHAR(15) PRIMARY KEY,
    user_id       INT NOT NULL,
    train_number  VARCHAR(10) NOT NULL,
    journey_date  DATE NOT NULL,
    total_fare    DECIMAL(10,2) NOT NULL,
    status        ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booking_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_train FOREIGN KEY (train_number) REFERENCES trains(train_number),
    INDEX idx_train_date (train_number, journey_date),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: booking_passengers
-- Passenger + seat detail for a booking (many rows per PNR)
-- ------------------------------------------------------------
CREATE TABLE booking_passengers (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    pnr          VARCHAR(15) NOT NULL,
    name         VARCHAR(100) NOT NULL,
    age          INT NOT NULL CHECK (age > 0 AND age < 130),
    gender       ENUM('M', 'F', 'O') NOT NULL,
    phone        VARCHAR(15) NOT NULL,
    seat_number  INT NOT NULL,
    CONSTRAINT fk_bp_booking FOREIGN KEY (pnr) REFERENCES bookings(pnr) ON DELETE CASCADE,
    INDEX idx_pnr (pnr)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Table: seats
-- Tracks per-train, per-journey-date seat occupancy explicitly.
-- Populated on demand (see TrainDAO.ensureSeatsExist) the first
-- time a train is queried/booked for a given journey date, so we
-- do not have to pre-generate seats for dates nobody ever books.
-- ------------------------------------------------------------
CREATE TABLE seats (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    train_number  VARCHAR(10) NOT NULL,
    journey_date  DATE NOT NULL,
    seat_number   INT NOT NULL,
    is_booked     BOOLEAN NOT NULL DEFAULT FALSE,
    pnr           VARCHAR(15) NULL,
    CONSTRAINT fk_seat_train FOREIGN KEY (train_number) REFERENCES trains(train_number),
    CONSTRAINT uq_seat UNIQUE (train_number, journey_date, seat_number),
    INDEX idx_seat_lookup (train_number, journey_date, is_booked)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Sample data
-- ------------------------------------------------------------

-- Default admin: username = admin, password = admin123
INSERT INTO admins (username, password_hash) VALUES
('admin', SHA2('admin123', 256));

-- Sample trains
INSERT INTO trains (train_number, train_name, source, destination, departure_time, arrival_time, total_seats, fare) VALUES
('12001', 'Shatabdi Express',   'Chennai',   'Bangalore', '06:00:00', '11:00:00', 60, 750.00),
('12002', 'Coromandel Express', 'Chennai',   'Kolkata',   '08:30:00', '20:00:00', 72, 1450.00),
('12003', 'Grand Trunk Express','Chennai',   'Delhi',     '19:15:00', '05:45:00', 80, 2100.00),
('12004', 'Mysore Express',     'Bangalore', 'Mysore',    '07:00:00', '09:30:00', 50,  250.00),
('12005', 'Howrah Mail',        'Kolkata',   'Delhi',     '22:00:00', '10:00:00', 90, 1800.00),
('12006', 'Deccan Queen',       'Mumbai',    'Pune',      '17:10:00', '20:25:00', 65,  350.00),
('12007', 'Rajdhani Express',   'Delhi',     'Mumbai',    '16:00:00', '08:15:00', 75, 2400.00),
('12008', 'Chennai Express',    'Mumbai',    'Chennai',   '11:40:00', '13:00:00', 70, 1900.00);
