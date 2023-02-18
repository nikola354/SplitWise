package bg.sofia.uni.fmi.mjt.splitwise.server.logic.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class UserPersonal implements Serializable {
    @Serial
    private static final long serialVersionUID = -2338626292552177485L;

    private String username;
    private String firstName;
    private String lastName;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public UserPersonal(String username, String firstName, String lastName, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    public boolean login(String username, String password) {
        return username.equals(this.username) && password.equals(this.password);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " [" + username + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPersonal that)) return false;

        if (getUsername() != null ? !getUsername().equals(that.getUsername()) : that.getUsername() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(that.getFirstName()) : that.getFirstName() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(that.getLastName()) : that.getLastName() != null)
            return false;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = getUsername() != null ? getUsername().hashCode() : 0;
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
