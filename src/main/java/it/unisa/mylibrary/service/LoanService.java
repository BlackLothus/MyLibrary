package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Loan;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class LoanService {
    public static final int DEFAULT_LOAN_DAYS = 15;
    public static final int MAX_ACTIVE_LOANS = 3;

    private final Archive archive;
    private final Clock clock;

    public LoanService(Archive archive, Clock clock) {
        this.archive = archive;
        this.clock = clock;
    }

    public LocalDate today() {
        // TODO: la data odierna tramite Clock
        return null;
    }

    public LocalDate defaultDueDate() {
        // TODO: scadenza di default
        return null;
    }

    public boolean isOverdue(Loan loan) {
        // TODO: Verifica se in ritardo
        return false;
    }

    public int availableCopies(String isbn) {
        // TODO: Calcolo copie totali meno prestiti attivi
        return 0;
    }

    public Loan registerLoan(String readerId, String isbn, LocalDate dueDate) throws LibraryException {
        // TODO: controlli bloccanti:
        //      - esistenza lettore e libro
        //      - unicità volume per lettore (RD14)
        //      - disponibilità copie > 0 (RD12)
        //      - limite 3 prestiti attivi (RD13)
        //      - scadenza valida non nel passato (RD16)
        //      se tutto ok crea istanza Loan e aggiunge ad archive.getLoans()
        return null;
    }

    public void returnLoan(String loanId) throws LibraryException {
        // TODO: qui va la chiusura del prestito e storicizzazione
    }

    public List<Loan> getReturnedLoansOfReader(String readerId) {
        // TODO: filtrare per readerId e !isActive(), ordinare per returnDate decrescente
        return null;
    }

    public List<Loan> getActiveLoansSortedByDueDate() {
       // TODO: isActive() e dueDate
        return null;
    }

    public List<Loan> getActiveLoansSortedByStartDate() {
        // TODO: isActive() e startDate
        return null;
    }

    public List<Loan> getActiveLoansOfReader(String readerId) {
        // TODO:readerId e isActive()
        return null;
    }

    public List<Loan> getActiveLoansOfBook(String isbn) {
        // TODO:isbn e isActive()
        return null;
    }

    public List<Loan> getReturnedLoansOfBook(String isbn) {
        return null;
    }
}