# Railway Reservation System (Java + JDBC + MySQL)

A console-based Railway Reservation System built with plain Java (OOP), JDBC
and MySQL, structured into `model` / `dao` / `service` / `util` / `main`
packages. Suitable for a college-level Java project submission.

## 1. Project Structure

```
railway-reservation-system/
├── pom.xml
├── README.md
├── sql/
│   └── schema.sql
└── src/main/java/railway/
    ├── model/
    │   ├── User.java
    │   ├── Admin.java
    │   ├── Train.java
    │   ├── Passenger.java
    │   ├── Booking.java
    │   └── Seat.java
    ├── dao/
    │   ├── UserDAO.java
    │   ├── AdminDAO.java
    │   ├── TrainDAO.java
    │   ├── BookingDAO.java
    │   └── PassengerDAO.java
    ├── service/
    │   ├── UserService.java
    │   ├── TrainService.java
    │   ├── BookingService.java
    │   └── ServiceException.java
    ├── util/
    │   ├── DBConnection.java
    │   ├── PNRGenerator.java
    │   ├── InputValidator.java
    │   └── PasswordUtil.java
    └── main/
        └── RailwayReservationSystem.java
```

## 2. Requirements

- Java 17 or later (JDK, not just JRE)
- Maven 3.6+
- MySQL 8.x server running locally (or reachable over the network)

## 3. Database Setup

1. Start your MySQL server.
2. Run the schema script, which drops/creates the `railway_reservation`
   database, all tables, and seeds sample trains plus a default admin:

   ```bash
   mysql -u root -p < sql/schema.sql
   ```

3. The default admin credentials seeded by the script are:
   - **username:** `admin`
   - **password:** `admin123`

## 4. Configure the Database Connection

`railway.util.DBConnection` reads its connection settings from (in order of
precedence) JVM system properties, then environment variables, then a
built-in default:

| Setting  | System property | Env variable | Default                                                              |
|----------|------------------|--------------|-----------------------------------------------------------------------|
| URL      | `db.url`         | `DB_URL`     | `jdbc:mysql://localhost:3306/railway_reservation?useSSL=false&serverTimezone=UTC` |
| Username | `db.user`        | `DB_USER`    | `root`                                                                 |
| Password | `db.password`    | `DB_PASSWORD`| `root`                                                                 |

Easiest options:

- **Edit the defaults** directly in `DBConnection.java`, or
- **Pass system properties** when running:
  ```bash
  mvn exec:java -Ddb.user=root -Ddb.password=yourpassword
  ```

## 5. Build & Run

### Using Maven (recommended)

```bash
# Compile
mvn clean compile

# Run directly (development)
mvn exec:java

# OR build a runnable "fat jar" (MySQL driver bundled in)
mvn clean package
java -jar target/railway-reservation-system.jar
```

### Using an IDE (IntelliJ IDEA / Eclipse / VS Code)

1. Open the `railway-reservation-system` folder as a Maven project — the
   IDE will detect `pom.xml` and download the MySQL connector automatically.
2. Let Maven finish resolving dependencies.
3. Run `railway.main.RailwayReservationSystem` (the class with the `main`
   method) directly from the IDE.
4. Make sure MySQL is running and `DBConnection` points at it before running.

**VS Code specifically:** install the "Extension Pack for Java" and the
"Maven for Java" extension, open the folder, then right-click
`RailwayReservationSystem.java` → *Run Java*.

## 6. Using the Application

### Main menu (not logged in)
```
1. Register        - create a new account
2. Login            - log in as a passenger
3. Search Train      - search trains by source/destination (no login required)
0. Exit
(type 'admin' to open the admin login)
```

### Logged-in passenger menu
```
1. Register
2. Login
3. Search Train
4. Book Ticket
5. View Ticket
6. Cancel Ticket
7. View My Bookings
8. Logout
9. Exit
```

### Admin panel (after `admin` login)
```
1. Add Train
2. Update Train
3. Delete Train
4. View All Trains
5. View All Bookings
6. View Users
7. Booking Statistics
8. Logout
```

## 7. Example Console Session

```
========================================
     WELCOME TO RAILWAY RESERVATION
========================================

========================================
       RAILWAY RESERVATION SYSTEM
========================================
1. Register
2. Login
3. Search Train
0. Exit
(type 'admin' to open the admin login)
========================================
Enter your choice: 1

--- Register New Account ---
Username: arjun
Email: arjun@example.com
Password (min 6 chars): secret123
Full Name: Arjun Kumar
Phone (10 digits): 9876543210
Registration successful! You can now login as 'arjun'.

Enter your choice: 2

--- Login ---
Username or Email: arjun
Password: secret123
Login successful. Welcome, Arjun Kumar!

Logged in as: arjun
...
Enter your choice: 4

--- Book Ticket ---
Train Number: 12001
Journey Date (yyyy-MM-dd): 2026-09-15
Number of passengers: 1
Passenger 1:
  Name: Arjun Kumar
  Age: 29
  Gender (M/F/O): M
  Phone (10 digits): 9876543210

Booking confirmed!
========================================
PNR            : PNR2608084821
Train          : 12001 - Shatabdi Express
Route          : Chennai -> Bangalore
Journey Date   : 2026-09-15
Status         : CONFIRMED
Total Fare     : Rs.750.00
Booked On      : 2026-08-08T10:15:32
Passengers     :
   - Seat 1    Arjun Kumar          Age:29  Gender:M  Phone:9876543210
========================================
```

## 8. How the Core Logic Works

### Seat allocation
Seats are tracked in the `seats` table on a **(train, journey_date,
seat_number)** basis rather than being fixed per train forever — this lets
the same physical train run (and be booked) on many different dates.
The first time a train/date combination is searched or booked,
`TrainDAO.ensureSeatsExist` lazily inserts one row per physical seat
(`1..total_seats`), all initially unbooked. Because seat rows are only
created on demand, dates nobody ever books never bloat the table.

### PNR generation
`PNRGenerator` builds a PNR as `PNR` + `yyMMdd` + a random 4-digit suffix
(e.g. `PNR2608084821`). This keeps PNRs roughly time-sortable while making
collisions unlikely. `BookingService.generateUniquePnr` still explicitly
checks the database and regenerates on the rare collision, so uniqueness is
guaranteed rather than assumed.

### Booking (transactional)
`BookingService.bookTicket`:
1. Validates all passenger fields and the journey date.
2. Opens a JDBC transaction (`autoCommit = false`).
3. Ensures the seat inventory for that train/date exists.
4. Runs `SELECT ... FOR UPDATE` to lock and fetch the next N free seats —
   this prevents two simultaneous bookings from grabbing the same seat.
5. If fewer seats are available than requested, it rolls back and reports
   how many are actually free.
6. Otherwise it marks those seats booked, inserts the `bookings` row and
   one `booking_passengers` row per passenger, then commits everything
   together. If any step throws, the whole transaction rolls back and no
   partial booking is left behind.

### Cancellation (transactional)
`BookingService.cancelTicket`:
1. Looks up the booking by PNR and rejects unknown PNRs.
2. Rejects PNRs that are already `CANCELLED` (no double-cancellation).
3. Inside a transaction: releases the booking's seats back to the pool
   (`is_booked = FALSE`) and flips the booking's status to `CANCELLED`.
4. Returns the refund amount (in this simplified model, the full fare —
   a real system would apply slab-based cancellation charges).

### Security notes
- All SQL access goes through `PreparedStatement` — no string-concatenated
  queries anywhere — to prevent SQL injection.
- Passwords are stored as SHA-256 hashes (`PasswordUtil`), never in plain
  text. For a production system, a slow salted hash such as BCrypt or
  Argon2 should be used instead.

## 9. Notes / Possible Extensions

- This is a console app per the assignment brief; the `service` layer is
  UI-agnostic, so a Swing, JavaFX, or Spring Boot REST front end could be
  layered on top later without touching `dao`/`service` code.
- Cancellation currently refunds the full fare; adding slab-based
  cancellation charges would only require a small change inside
  `BookingService.cancelTicket`.
