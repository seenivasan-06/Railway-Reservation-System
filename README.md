# 🚆 Railway Reservation System

A **Java-based Railway Reservation System** designed to simplify train search, passenger management, seat reservation, booking cancellation, and PNR generation through a structured and modular application architecture.

---

## 📌 Project Overview

The Railway Reservation System provides a digital platform for managing railway reservations.

The system allows users to:

* Register and log in
* Search available trains
* View train details
* Check seat availability
* Book tickets
* Add passenger details
* Generate PNR numbers
* View booking details
* Cancel bookings

Administrators can manage trains, users, passengers, and bookings.

The project follows a layered architecture using **Model, DAO, Service, Utility, and Main** packages to keep the application maintainable and scalable.

---

## 🎯 Objectives

* Automate railway ticket reservation.
* Reduce manual booking operations.
* Provide real-time-style seat availability.
* Maintain passenger and booking information.
* Generate unique PNR numbers.
* Provide secure user authentication.
* Prevent invalid or duplicate bookings.
* Maintain organized database records.

---

## 🛠️ Technologies Used

| Technology | Purpose                           |
| ---------- | --------------------------------- |
| Java       | Core application development      |
| JDBC       | Database connectivity             |
| MySQL      | Data storage                      |
| Maven      | Dependency and project management |
| SQL        | Database operations               |
| Git/GitHub | Version control                   |

---

## 📂 Project Structure

```text
railway-reservation-system/
│
├── pom.xml
├── README.md
│
├── sql/
│   └── schema.sql
│
└── src/
    └── main/
        └── java/
            └── railway/
                │
                ├── model/
                │   ├── User.java
                │   ├── Admin.java
                │   ├── Train.java
                │   ├── Passenger.java
                │   ├── Booking.java
                │   └── Seat.java
                │
                ├── dao/
                │   ├── UserDAO.java
                │   ├── AdminDAO.java
                │   ├── TrainDAO.java
                │   ├── BookingDAO.java
                │   └── PassengerDAO.java
                │
                ├── service/
                │   ├── UserService.java
                │   ├── TrainService.java
                │   ├── BookingService.java
                │   └── ServiceException.java
                │
                ├── util/
                │   ├── DBConnection.java
                │   ├── PNRGenerator.java
                │   ├── InputValidator.java
                │   └── PasswordUtil.java
                │
                └── main/
                    └── RailwayReservationSystem.java
```

---

# ✨ Features

## 👤 User Management

Users can:

* Create an account
* Log in securely
* Update profile information
* View booking history
* View current bookings

---

## 🚆 Train Management

The system provides train-related functionality such as:

* Add train
* Update train information
* Remove train
* Search trains
* View train details
* Check available seats

---

## 🎫 Ticket Booking

Users can:

1. Select source station.
2. Select destination station.
3. Search available trains.
4. Select a train.
5. Select journey date.
6. Enter passenger details.
7. Select available seats.
8. Confirm booking.
9. Generate a unique PNR.

---

## 🧑 Passenger Management

Passenger information includes:

* Passenger ID
* Name
* Age
* Gender
* Contact information
* Seat number
* Booking reference

Multiple passengers can be associated with a single booking.

---

## 💺 Seat Management

The system manages:

* Available seats
* Reserved seats
* Seat numbers
* Seat status
* Seat allocation

Example:

```text
Seat No.    Status
----------------------
S01         AVAILABLE
S02         BOOKED
S03         AVAILABLE
S04         BOOKED
```

---

## 🔖 PNR Generation

Every successful booking receives a unique **PNR (Passenger Name Record)**.

Example:

```text
PNR: RLY826451
```

The PNR can be used to:

* View booking details
* Check reservation status
* Cancel tickets
* Identify a booking

---

## ❌ Booking Cancellation

Users can cancel existing bookings using their PNR.

When a booking is cancelled:

* Booking status changes to `CANCELLED`
* Reserved seats become available
* Booking history is updated

---

# 🔐 Security

The application includes basic security mechanisms such as:

* Password hashing
* Input validation
* User authentication
* Admin authentication
* Prepared SQL statements
* Exception handling

Passwords should **never be stored as plain text**.

---

# 🗄️ Database

The project uses **MySQL** as the relational database.

Main database entities include:

```text
User
Admin
Train
Passenger
Booking
Seat
```

### Example Relationships

```text
USER
  │
  └── BOOKING
          │
          ├── PASSENGER
          │
          ├── TRAIN
          │
          └── SEAT
```

---

# ⚙️ Requirements

Before running the project, install:

* Java JDK 17 or later
* Maven
* MySQL Server
* MySQL Workbench (optional)
* Git (optional)
* IDE such as IntelliJ IDEA, Eclipse, or VS Code

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

# 🚀 Installation

## 1. Clone the Repository

```bash
git clone <your-repository-url>
```

Move into the project directory:

```bash
cd railway-reservation-system
```

---

## 2. Create the Database

Open MySQL and create the database:

```sql
CREATE DATABASE railway_reservation;
```

---

## 3. Execute the SQL Schema

Run:

```text
sql/schema.sql
```

This creates the required tables and constraints.

---

## 4. Configure Database Connection

Update the database configuration in:

```text
DBConnection.java
```

Example:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/railway_reservation";

private static final String USER = "root";

private static final String PASSWORD = "your_password";
```

Replace:

```text
your_password
```

with your MySQL password.

---

# 📦 Build the Project

Run:

```bash
mvn clean install
```

If the build is successful, Maven will generate the compiled application.

---

# ▶️ Run the Application

Run the main class:

```text
railway.main.RailwayReservationSystem
```

Or, if configured as a Maven application:

```bash
mvn exec:java
```

---

# 🖥️ Application Flow

```text
                    ┌──────────────┐
                    │    START     │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ Login /      │
                    │ Registration │
                    └──────┬───────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
             ▼                           ▼
       ┌───────────┐              ┌───────────┐
       │   USER    │              │   ADMIN   │
       └─────┬─────┘              └─────┬─────┘
             │                          │
             ▼                          ▼
      Search Trains              Manage Trains
             │                   Manage Users
             ▼                   Manage Bookings
      Select Train
             │
             ▼
      Enter Passenger
          Details
             │
             ▼
       Check Seats
             │
             ▼
        Book Ticket
             │
             ▼
       Generate PNR
             │
             ▼
      View / Cancel
         Booking
```

---

# 🧩 Architecture

The project follows a layered architecture.

### Model Layer

Contains Java classes representing database entities.

```text
User
Admin
Train
Passenger
Booking
Seat
```

### DAO Layer

Responsible for database operations.

```text
UserDAO
TrainDAO
BookingDAO
PassengerDAO
AdminDAO
```

### Service Layer

Contains application/business logic.

```text
UserService
TrainService
BookingService
```

### Utility Layer

Contains reusable utilities.

```text
DBConnection
PNRGenerator
InputValidator
PasswordUtil
```

### Main Layer

Contains the application entry point:

```text
RailwayReservationSystem.java
```

---

# 🧪 Example Booking

```text
---------------------------------------
       RAILWAY RESERVATION
---------------------------------------

Passenger Name : Seenivasan
Train          : Chennai Express
From           : Chennai
To             : Bengaluru
Journey Date   : 15-08-2026
Seat Number    : S24

Booking Status : CONFIRMED
PNR            : RLY826451

---------------------------------------
```

---

# 🔮 Future Enhancements

The system can be extended with:

* Spring Boot REST APIs
* React/Angular frontend
* Mobile application
* Online payment integration
* Email/SMS ticket confirmation
* QR-code tickets
* Live train tracking
* Waitlist management
* RAC support
* Dynamic seat allocation
* Automatic fare calculation
* Multi-language support
* Admin dashboard
* JWT authentication
* Cloud deployment

---

# 📈 Advantages

* Easy ticket reservation
* Reduced manual work
* Centralized passenger information
* Organized database management
* Secure authentication
* Automatic PNR generation
* Modular architecture
* Easy to maintain and extend
* Suitable for academic and portfolio projects

---

# 👨‍💻 Project Type

**Academic / Java Backend Project**

### Domain

**Railway Transportation & Reservation**

### Architecture

**Layered Architecture – Model + DAO + Service + Utility**

### Database

**MySQL**

### Build Tool

**Maven**

---

# 📄 License

This project is developed for **educational and academic purposes**.

You are free to modify and extend the project for learning and demonstration purposes.

---

## ⭐ If you find this project useful

Consider giving the repository a ⭐ on GitHub.
