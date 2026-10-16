package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String readerId;
    private final String isbn;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; //non deve essere final perchè non la sappiamo fino al ritorno

    public Loan(String readerId, String isbn, LocalDate startDate, LocalDate dueDate) {
        this.id = UUID.randomUUID().toString();
        this.readerId = readerId;
        this.isbn = isbn;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    public String getId() {
        return id;
    }
    public String getReaderId() {
        return readerId;
    }
    public String getIsbn() {
        return isbn;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }//quella concordata
    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isActive() {
        // TODO:check sullo stato del prestito
        return false;
    }

    public boolean isOverdue(LocalDate today) {
        // TODO:i prestiti in ritardo
        return false;
    }

    public void markReturned(LocalDate returnDate) {
        // TODO: restituzione
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Loan)) {
            return false;
        }
        Loan prestito = (Loan) o;
        if (this.id == null) {
            return prestito.id == null;
        }
        return this.id.equals(prestito.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}