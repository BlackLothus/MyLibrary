package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresenta un prestito che collega un lettore e un libro tramite i loro ID (ED05a).
 * Mantiene i dati anche dopo la restituzione per conservare lo storico (RD15).
 */
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String readerId;
    private final String isbn;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; //non deve essere final perchè non la sappiamo fino al ritorno

    /**
     * Crea un nuovo prestito attivo generando un ID univoco.
     *
     * @param readerId  matricola del lettore
     * @param isbn      ISBN del libro prestato
     * @param startDate data di erogazione
     * @param dueDate   data di scadenza prevista
     */
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

    /**
     * Indica se il libro è ancora in carico al lettore.
     *
     * @return {@code true} se il prestito non è stato ancora restituito
     */
    public boolean isActive() {
        return returnDate == null;
    }

    /**
     * Verifica se il prestito è attivo e ha superato la data di scadenza.
     *
     * @param today data di riferimento per il controllo
     * @return {@code true} se è attivo e {@code today} è successiva a {@code dueDate}
     */
    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(dueDate); //vedo se non restituito e in ritardo ritardo
    }

    /**
     * Conclude il prestito registrando la data di restituzione effettiva (RF23, RD15).
     *
     * @param returnDate data di rientro del volume
     */
    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
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

    @Override
    public String toString(){
        return "Loan id= " + id + ",readerId=" + readerId + ", isbn=" + isbn;
    }
}