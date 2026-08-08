package railway.main;

import railway.model.Admin;
import railway.model.Booking;
import railway.model.Passenger;
import railway.model.Train;
import railway.model.User;
import railway.service.BookingService;
import railway.service.ServiceException;
import railway.service.TrainService;
import railway.service.UserService;
import railway.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console entry point for the Railway Reservation System.
 * Wires together the service layer and drives the user/admin menus.
 */
public class RailwayReservationSystem {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final UserService userService = new UserService();
    private static final TrainService trainService = new TrainService();
    private static final BookingService bookingService = new BookingService();

    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     WELCOME TO RAILWAY RESERVATION");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showMainMenu();
            } else {
                showLoggedInMenu();
            }
        }

        System.out.println("Thank you for using Railway Reservation System. Goodbye!");
        SCANNER.close();
    }

    // ------------------------------------------------------------
    // Top-level menu (not logged in)
    // ------------------------------------------------------------

    private static boolean showMainMenu() {
        printMenu();
        String choice = readLine("Enter your choice: ");
        switch (choice) {
            case "1":
                handleRegister();
                return true;
            case "2":
                handleLogin();
                return true;
            case "3":
                handleSearchTrain();
                return true;
            case "0":
                return false;
            case "admin":
                handleAdminLogin();
                return true;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       RAILWAY RESERVATION SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Search Train");
        System.out.println("0. Exit");
        System.out.println("(type 'admin' to open the admin login)");
        System.out.println("========================================");
    }

    // ------------------------------------------------------------
    // Logged-in user menu
    // ------------------------------------------------------------

    private static void showLoggedInMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       RAILWAY RESERVATION SYSTEM");
        System.out.println("========================================");
        System.out.println("Logged in as: " + currentUser.getUsername());
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Search Train");
        System.out.println("4. Book Ticket");
        System.out.println("5. View Ticket");
        System.out.println("6. Cancel Ticket");
        System.out.println("7. View My Bookings");
        System.out.println("8. Logout");
        System.out.println("9. Exit");
        System.out.println("========================================");

        String choice = readLine("Enter your choice: ");
        switch (choice) {
            case "1":
                System.out.println("You are already logged in. Please logout first.");
                break;
            case "2":
                System.out.println("You are already logged in.");
                break;
            case "3":
                handleSearchTrain();
                break;
            case "4":
                handleBookTicket();
                break;
            case "5":
                handleViewTicket();
                break;
            case "6":
                handleCancelTicket();
                break;
            case "7":
                handleViewMyBookings();
                break;
            case "8":
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            case "9":
                currentUser = null;
                exitProgram();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void exitProgram() {
        System.out.println("Thank you for using Railway Reservation System. Goodbye!");
        SCANNER.close();
        System.exit(0);
    }

    // ------------------------------------------------------------
    // User handlers
    // ------------------------------------------------------------

    private static void handleRegister() {
        System.out.println("\n--- Register New Account ---");
        String username = readLine("Username: ");
        String email = readLine("Email: ");
        String password = readLine("Password (min 6 chars): ");
        String fullName = readLine("Full Name: ");
        String phone = readLine("Phone (10 digits): ");

        try {
            User user = userService.register(username, email, password, fullName, phone);
            System.out.println("Registration successful! You can now login as '" + user.getUsername() + "'.");
        } catch (ServiceException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- Login ---");
        String usernameOrEmail = readLine("Username or Email: ");
        String password = readLine("Password: ");
        try {
            currentUser = userService.login(usernameOrEmail, password);
            System.out.println("Login successful. Welcome, " + currentUser.getFullName() + "!");
        } catch (ServiceException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void handleSearchTrain() {
        System.out.println("\n--- Search Train ---");
        String source = readLine("Source: ");
        String destination = readLine("Destination: ");
        String dateStr = readLine("Journey Date (yyyy-MM-dd): ");
        LocalDate date = InputValidator.parseDate(dateStr);
        if (date == null) {
            System.out.println("Invalid date format.");
            return;
        }

        try {
            List<Train> trains = trainService.searchTrains(source, destination);
            if (trains.isEmpty()) {
                System.out.println("No trains found for this route.");
                return;
            }
            System.out.println("\nMatching trains:");
            for (Train t : trains) {
                int available = trainService.getAvailableSeats(t.getTrainNumber(), date);
                System.out.println(t + " | Available seats on " + date + ": " + available);
            }
        } catch (ServiceException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
    }

    private static void handleBookTicket() {
        System.out.println("\n--- Book Ticket ---");
        String trainNumber = readLine("Train Number: ");
        String dateStr = readLine("Journey Date (yyyy-MM-dd): ");
        LocalDate date = InputValidator.parseDate(dateStr);
        if (date == null) {
            System.out.println("Invalid date format.");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(readLine("Number of passengers: ").trim());
            if (count <= 0) {
                System.out.println("Number of passengers must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        List<Passenger> passengers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            System.out.println("Passenger " + i + ":");
            String name = readLine("  Name: ");
            int age;
            try {
                age = Integer.parseInt(readLine("  Age: ").trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid age. Aborting booking.");
                return;
            }
            String gender = readLine("  Gender (M/F/O): ").trim().toUpperCase();
            String phone = readLine("  Phone (10 digits): ");
            char g = gender.isEmpty() ? ' ' : gender.charAt(0);
            passengers.add(new Passenger(name, age, g, phone));
        }

        try {
            Booking booking = bookingService.bookTicket(currentUser.getId(), trainNumber, date, passengers);
            System.out.println("\nBooking confirmed!");
            System.out.println(booking);
        } catch (ServiceException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private static void handleViewTicket() {
        System.out.println("\n--- View Ticket ---");
        String pnr = readLine("Enter PNR: ");
        try {
            Booking booking = bookingService.getBookingDetails(pnr);
            System.out.println(booking);
        } catch (ServiceException e) {
            System.out.println("Lookup failed: " + e.getMessage());
        }
    }

    private static void handleCancelTicket() {
        System.out.println("\n--- Cancel Ticket ---");
        String pnr = readLine("Enter PNR to cancel: ");
        String confirm = readLine("Are you sure you want to cancel " + pnr + "? (yes/no): ");
        if (!confirm.trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }
        try {
            BigDecimal refund = bookingService.cancelTicket(pnr);
            System.out.println("Ticket cancelled successfully. Refund amount: Rs." + refund);
        } catch (ServiceException e) {
            System.out.println("Cancellation failed: " + e.getMessage());
        }
    }

    private static void handleViewMyBookings() {
        System.out.println("\n--- My Bookings ---");
        try {
            List<Booking> bookings = bookingService.getBookingsForUser(currentUser.getId());
            if (bookings.isEmpty()) {
                System.out.println("You have no bookings yet.");
                return;
            }
            for (Booking b : bookings) {
                System.out.println(b);
            }
        } catch (ServiceException e) {
            System.out.println("Could not fetch bookings: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------

    private static void handleAdminLogin() {
        System.out.println("\n--- Admin Login ---");
        String username = readLine("Admin Username: ");
        String password = readLine("Admin Password: ");
        try {
            Admin admin = userService.loginAdmin(username, password);
            System.out.println("Admin login successful. Welcome, " + admin.getUsername() + "!");
            runAdminPanel();
        } catch (ServiceException e) {
            System.out.println("Admin login failed: " + e.getMessage());
        }
    }

    private static void runAdminPanel() {
        boolean inAdminPanel = true;
        while (inAdminPanel) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("             ADMIN PANEL");
            System.out.println("========================================");
            System.out.println("1. Add Train");
            System.out.println("2. Update Train");
            System.out.println("3. Delete Train");
            System.out.println("4. View All Trains");
            System.out.println("5. View All Bookings");
            System.out.println("6. View Users");
            System.out.println("7. Booking Statistics");
            System.out.println("8. Logout");
            System.out.println("========================================");

            String choice = readLine("Enter your choice: ");
            switch (choice) {
                case "1":
                    handleAddTrain();
                    break;
                case "2":
                    handleUpdateTrain();
                    break;
                case "3":
                    handleDeleteTrain();
                    break;
                case "4":
                    handleViewAllTrains();
                    break;
                case "5":
                    handleViewAllBookings();
                    break;
                case "6":
                    handleViewUsers();
                    break;
                case "7":
                    handleBookingStatistics();
                    break;
                case "8":
                    inAdminPanel = false;
                    System.out.println("Admin logged out.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void handleAddTrain() {
        System.out.println("\n--- Add Train ---");
        String number = readLine("Train Number: ");
        String name = readLine("Train Name: ");
        String source = readLine("Source: ");
        String destination = readLine("Destination: ");
        String departure = readLine("Departure Time (HH:mm): ");
        String arrival = readLine("Arrival Time (HH:mm): ");
        int totalSeats = readInt("Total Seats: ");
        BigDecimal fare = readBigDecimal("Fare: ");

        try {
            trainService.addTrain(number, name, source, destination, departure, arrival, totalSeats, fare);
            System.out.println("Train added successfully.");
        } catch (ServiceException e) {
            System.out.println("Failed to add train: " + e.getMessage());
        }
    }

    private static void handleUpdateTrain() {
        System.out.println("\n--- Update Train ---");
        String number = readLine("Train Number to update: ");
        String name = readLine("New Train Name: ");
        String source = readLine("New Source: ");
        String destination = readLine("New Destination: ");
        String departure = readLine("New Departure Time (HH:mm): ");
        String arrival = readLine("New Arrival Time (HH:mm): ");
        int totalSeats = readInt("New Total Seats: ");
        BigDecimal fare = readBigDecimal("New Fare: ");

        try {
            trainService.updateTrain(number, name, source, destination, departure, arrival, totalSeats, fare);
            System.out.println("Train updated successfully.");
        } catch (ServiceException e) {
            System.out.println("Failed to update train: " + e.getMessage());
        }
    }

    private static void handleDeleteTrain() {
        System.out.println("\n--- Delete Train ---");
        String number = readLine("Train Number to delete: ");
        try {
            trainService.deleteTrain(number);
            System.out.println("Train deleted successfully.");
        } catch (ServiceException e) {
            System.out.println("Failed to delete train: " + e.getMessage());
        }
    }

    private static void handleViewAllTrains() {
        System.out.println("\n--- All Trains ---");
        List<Train> trains = trainService.getAllTrains();
        if (trains.isEmpty()) {
            System.out.println("No trains found.");
            return;
        }
        for (Train t : trains) {
            System.out.println(t);
        }
    }

    private static void handleViewAllBookings() {
        System.out.println("\n--- All Bookings ---");
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking b : bookings) {
            System.out.println(b);
        }
    }

    private static void handleViewUsers() {
        System.out.println("\n--- All Users ---");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        for (User u : users) {
            System.out.println(u);
        }
    }

    private static void handleBookingStatistics() {
        System.out.println("\n--- Train-wise Booking Statistics ---");
        List<Object[]> stats = bookingService.getTrainWiseStatistics();
        if (stats.isEmpty()) {
            System.out.println("No data available.");
            return;
        }
        System.out.printf("%-10s %-25s %-10s %-10s %-10s%n", "Train#", "Name", "Total", "Confirmed", "Cancelled");
        for (Object[] row : stats) {
            System.out.printf("%-10s %-25s %-10s %-10s %-10s%n", row[0], row[1], row[2], row[3], row[4]);
        }
    }

    // ------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                return new BigDecimal(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount (e.g. 450.00).");
            }
        }
    }
}
