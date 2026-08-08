package railway.model;

/**
 * Represents an administrator account.
 */
public class Admin {

    private int id;
    private String username;
    private String passwordHash;

    public Admin() {
    }

    public Admin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return String.format("Admin{id=%d, username='%s'}", id, username);
    }
}
