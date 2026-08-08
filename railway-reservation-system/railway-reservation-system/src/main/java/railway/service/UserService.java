package railway.service;

import railway.dao.AdminDAO;
import railway.dao.UserDAO;
import railway.model.Admin;
import railway.model.User;
import railway.util.InputValidator;
import railway.util.PasswordUtil;

import java.sql.SQLException;

/**
 * Business logic for user and admin registration/authentication.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    public User register(String username, String email, String password, String fullName, String phone) {
        if (!InputValidator.isValidUsername(username)) {
            throw new ServiceException("Username must be between 3 and 50 characters.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new ServiceException("Invalid email address.");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new ServiceException("Password must be at least 6 characters long.");
        }
        if (InputValidator.isNullOrBlank(fullName)) {
            throw new ServiceException("Full name cannot be empty.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new ServiceException("Phone number must be exactly 10 digits.");
        }

        try {
            if (userDAO.existsByUsernameOrEmail(username, email)) {
                throw new ServiceException("Username or email is already registered.");
            }
            User user = new User(username.trim(), email.trim(), PasswordUtil.hash(password), fullName.trim(), phone.trim());
            return userDAO.create(user);
        } catch (SQLException e) {
            throw new ServiceException("Database error during registration: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates using either username or email plus password.
     * @return the authenticated User
     * @throws ServiceException if credentials are invalid
     */
    public User login(String usernameOrEmail, String password) {
        if (InputValidator.isNullOrBlank(usernameOrEmail) || InputValidator.isNullOrBlank(password)) {
            throw new ServiceException("Username/email and password are required.");
        }
        try {
            User user = userDAO.findByUsername(usernameOrEmail.trim());
            if (user == null) {
                user = userDAO.findByEmail(usernameOrEmail.trim());
            }
            if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
                throw new ServiceException("Invalid username/email or password.");
            }
            return user;
        } catch (SQLException e) {
            throw new ServiceException("Database error during login: " + e.getMessage(), e);
        }
    }

    public Admin loginAdmin(String username, String password) {
        if (InputValidator.isNullOrBlank(username) || InputValidator.isNullOrBlank(password)) {
            throw new ServiceException("Username and password are required.");
        }
        try {
            Admin admin = adminDAO.findByUsername(username.trim());
            if (admin == null || !PasswordUtil.matches(password, admin.getPasswordHash())) {
                throw new ServiceException("Invalid admin username or password.");
            }
            return admin;
        } catch (SQLException e) {
            throw new ServiceException("Database error during admin login: " + e.getMessage(), e);
        }
    }

    public java.util.List<User> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Database error while fetching users: " + e.getMessage(), e);
        }
    }
}
