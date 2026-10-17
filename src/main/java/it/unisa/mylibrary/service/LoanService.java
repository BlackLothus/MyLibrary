package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Book;

//qui metto quelle non mie
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LoanService {
    public static final int DEFAULT_LOAN_DAYS = 15;
    public static final int MAX_ACTIVE_LOANS = 3;

    private final Archive archive;
    private final Clock clock;

    public LoanService(Archive archive, Clock clock) {
        if (archive == null) {
            throw new LibraryException("L'archivio non può essere nullo.");
        }
        if (clock == null) {
            throw new LibraryException("Il clock non può essere nullo.");
        }
        this.archive = archive;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public LocalDate defaultDueDate() {
        return today().plusDays(DEFAULT_LOAN_DAYS);
    }

    public boolean isOverdue(Loan loan) {
        return loan != null && loan.isOverdue(today());
    }

    public int availableCopies(String isbn) {
        Book book = archive.getBooks().get(isbn); //prendo solo l'isbn che corrisponde
        if (book == null) {
            return 0;
        }
        return book.getTotalCopies() - getActiveLoansOfBook(isbn).size();
    }
 //tolgo il throws che è un unchecked
    public Loan registerLoan(String readerId, String isbn, LocalDate dueDate) {
        //ho notato che lo chiamo due volte, lo semplifico qui  con "prestitiAttivi":
        List<Loan> prestitiAttivi = getActiveLoansOfReader(readerId);

        LocalDate today = today();
        LocalDate finalDueDate = (dueDate == null)
                ? defaultDueDate()  //metto 15 se non specificato
                : dueDate;

        if (!archive.getReaders().containsKey(readerId)) { //se non contiene il reader, quello sotto per isbn
            throw new LibraryException("Lettore inesistente.");
        }
        if (!archive.getBooks().containsKey(isbn)) {
            throw new LibraryException("Libro inesistente.");
        }
        for (Loan prestito : prestitiAttivi) { //scorro i prestiti, se ne trovo uno con stesso isbn lancio ex
            if (prestito.getIsbn().equals(isbn)) {
                throw new LibraryException("Il lettore ha già in carico una copia di questo libro");
            }
        }
        if (finalDueDate.isBefore(today)) {
            throw new LibraryException("Data di scadenza non può essere precedente a oggi");
        }
        if (availableCopies(isbn) <= 0) {
            throw new LibraryException("Copie disponibili esaurite.");
        }
        if (prestitiAttivi.size() >= MAX_ACTIVE_LOANS) {
            throw new LibraryException("Il lettore ha già " + MAX_ACTIVE_LOANS + " prestiti attivi.");
        }

        Loan loan = new Loan(readerId, isbn, today, finalDueDate); //creo il prestito
        archive.getLoans().add(loan); //carico il prestito
        return loan;
    }
 //di seguito userò stream API, visto che userò loan di archive che è una lista posso farlo in modo natuale senza dover prendere pirma chiave/vaore
    public void returnLoan(String loanId) {
        Loan loan = archive.getLoans().stream()
                .filter(l -> l.getId().equals(loanId)) //filter(Predicate<? super T> predicate) va
                .findFirst()
                .orElseThrow(() -> new LibraryException("Prestito inesistente."));

        if (!loan.isActive()) {
            throw new LibraryException("Prestito già restituito.");
        }
        loan.markReturned(today());
    }

    public List<Loan> getReturnedLoansOfReader(String readerId) {
        return archive.getLoans().stream()
                .filter(l -> !l.isActive())
                .filter(l -> l.getReaderId().equals(readerId))
                .sorted(Comparator.comparing(Loan::getReturnDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansSortedByDueDate() {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .sorted(Comparator.comparing(Loan::getDueDate).thenComparing(Loan::getStartDate))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansSortedByStartDate() {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .sorted(Comparator.comparing(Loan::getStartDate).thenComparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansOfReader(String readerId) {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .filter(l -> l.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansOfBook(String isbn) {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .filter(l -> l.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }

    public List<Loan> getReturnedLoansOfBook(String isbn) {
        return archive.getLoans().stream()
                .filter(l -> !l.isActive())
                .filter(l -> l.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }
}