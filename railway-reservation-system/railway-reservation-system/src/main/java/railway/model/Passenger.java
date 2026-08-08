package railway.model;

/**
 * Represents a single passenger travelling under a booking (PNR).
 */
public class Passenger {

    private int id;
    private String pnr;
    private String name;
    private int age;
    private char gender; // 'M', 'F', 'O'
    private String phone;
    private int seatNumber;

    public Passenger() {
    }

    public Passenger(String name, int age, char gender, String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public String toString() {
        return String.format("Seat %-4d %-20s Age:%-3d Gender:%-2c Phone:%s",
                seatNumber, name, age, gender, phone);
    }
}
