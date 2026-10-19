package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Rappresenta l'anagrafica di un lettore iscritto alla biblioteca (ED04).
 * L'identità è determinata univocamente dalla matricola (RD04b).
 */
public class Reader implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private final String studentId; //uso questo come identificativo
    private String email; //non chekko sul solo istituzionale per future implementazioni
    /**
     * Crea un nuovo lettore con i dati anagrafici obbligatori.
     *
     * @param firstName nome del lettore
     * @param lastName  cognome del lettore
     * @param studentId matricola identificativa
     * @param email     indirizzo e-mail
     */

    public Reader(String firstName, String lastName, String studentId, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getStudentId() {
        return studentId;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) { //vedere book sul perchè scritto così
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reader)) {
            return false;
        }
        Reader lettore = (Reader) o;
        if (this.studentId == null){
            return lettore.studentId ==null;
        }
        return this.studentId.equals(lettore.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
    @Override
    public String toString() {
        return "Reader studentId=" + studentId + ", name=" + firstName + " " + lastName;
    }
}