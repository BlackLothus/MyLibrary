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

/**
 * Gestisce l'erogazione, la restituzione e le interrogazioni sui prestiti (RF17, RF18, RF21, RF23),
 * oltre al calcolo dinamico delle copie disponibili (RD06).
 */
public class LoanService {
    public static final int DEFAULT_LOAN_DAYS = 15;
    public static final int MAX_ACTIVE_LOANS = 3;

    private final Archive archive;
    private final Clock clock;

    /**
     * Inizializza il servizio con l'archivio e un {@link Clock} (utile per fissare la data nei test).
     *
     * @param archive stato globale della biblioteca
     * @param clock   orologio di riferimento per calcolare la data odierna
     * @throws LibraryException se {@code archive} o {@code clock} sono nulli
     */
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

    /**
     * Restituisce la data odierna calcolata tramite il clock configurato.
     *
     * @return data corrente
     */
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /**
     * Calcola la scadenza predefinita aggiungendo {@value #DEFAULT_LOAN_DAYS} giorni a oggi (RD16).
     *
     * @return data di scadenza di default
     */
    public LocalDate defaultDueDate() {
        return today().plusDays(DEFAULT_LOAN_DAYS);
    }

    /**
     * Verifica se un prestito è attualmente in ritardo rispetto alla data odierna (IU18).
     *
     * @param loan il prestito da controllare
     * @return {@code true} se non è nullo ed è scaduto
     */
    public boolean isOverdue(Loan loan) {
        return loan != null && loan.isOverdue(today());
    }

    /**
     * Calcola dinamicamente le copie disponibili come differenza tra copie totali e prestiti attivi (RD06).
     *
     * @param isbn ISBN (già normalizzato) del libro
     * @return numero di copie disponibili, oppure 0 se il libro non è in archivio
     */
    public int availableCopies(String isbn) {
        Book book = archive.getBooks().get(isbn); //prendo solo l'isbn che corrisponde
        if (book == null) {
            return 0;
        }
        return book.getTotalCopies() - getActiveLoansOfBook(isbn).size();
    }

    /**
     * Registra un nuovo prestito applicando le regole di dominio (RF21, RNF06).
     *
     * @param readerId matricola del lettore
     * @param isbn     ISBN (già normalizzato) del libro
     * @param dueDate  scadenza concordata; se {@code null} si usa quella di default (RD16)
     * @return il prestito registrato
     * @throws LibraryException se lettore o libro non esistono, il lettore ha già una copia (RD14),
     *         la scadenza è passata (RD16), le copie sono esaurite (RD12) o il lettore ha già
     *         {@value #MAX_ACTIVE_LOANS} prestiti attivi (RD13)
     */
    public Loan registerLoan(String readerId, String isbn, LocalDate dueDate) {
        //ho notato che lo chiamo due volte, lo semplifico qui  con "prestitiAttivi":
        List<Loan> prestitiAttivi = getActiveLoansOfReader(readerId);

        LocalDate today = today();
        LocalDate finalDueDate = (dueDate == null)
                ? defaultDueDate()  //metto 15 se non specificato RD16
                : dueDate;

        if (!archive.getReaders().containsKey(readerId)) {
            throw new LibraryException("Lettore inesistente.");
        }
        if (!archive.getBooks().containsKey(isbn)) {
            throw new LibraryException("Libro inesistente.");
        }
        for (Loan prestito : prestitiAttivi) { //RD14
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

    /**
     * Registra la restituzione di un prestito attivo impostando la data odierna,
     * mantenendo il record in archivio per lo storico (RF23, RD15).
     *
     * @param loanId identificativo univoco del prestito
     * @throws LibraryException se il prestito non esiste o risulta già restituito
     */
    //stream API
    public void returnLoan(String loanId) {
        Loan loan = archive.getLoans().stream()
                .filter(l -> l.getId().equals(loanId)) //filter(Predicate<? super T> predicate)
                .findFirst()
                .orElseThrow(() -> new LibraryException("Prestito inesistente."));

        if (!loan.isActive()) {
            throw new LibraryException("Prestito già restituito.");
        }
        loan.markReturned(today());
    }

    /**
     * Restituisce lo storico dei prestiti conclusi di un lettore, dal più recente al più vecchio (RD15, IU10b).
     *
     * @param readerId matricola del lettore
     * @return lista dei prestiti restituiti dal lettore
     */
    public List<Loan> getReturnedLoansOfReader(String readerId) {
        return archive.getLoans().stream()
                .filter(l -> !l.isActive()) // solo prestiti conclusi
                .filter(l -> l.getReaderId().equals(readerId)) //prendo solo quelli del lettore specifico
                .sorted(Comparator.comparing(Loan::getReturnDate).reversed()) //li metto in ordine
                .collect(Collectors.toList()); //li metto in una lista
    }

    /**
     * Restituisce tutti i prestiti attivi ordinati per data di scadenza prevista (RF17, RF18).
     *
     * @return lista dei prestiti attivi ordinata per scadenza
     */
    public List<Loan> getActiveLoansSortedByDueDate() {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .sorted(Comparator.comparing(Loan::getDueDate).thenComparing(Loan::getStartDate))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i prestiti attivi ordinati per data di inizio prestito (RF17, RF18).
     *
     * @return lista dei prestiti attivi ordinata per data di erogazione
     */
    public List<Loan> getActiveLoansSortedByStartDate() {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .sorted(Comparator.comparing(Loan::getStartDate).thenComparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i prestiti attualmente in carico a uno specifico lettore (RF13).
     *
     * @param readerId matricola del lettore
     * @return lista dei prestiti attivi del lettore
     */
    public List<Loan> getActiveLoansOfReader(String readerId) {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .filter(l -> l.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i prestiti attualmente attivi associati a un determinato libro (IU13b).
     *
     * @param isbn ISBN del libro
     * @return lista dei prestiti attivi per quel volume
     */
    public List<Loan> getActiveLoansOfBook(String isbn) {
        return archive.getLoans().stream()
                .filter(Loan::isActive)
                .filter(l -> l.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i prestiti conclusi relativi a uno specifico libro (utile per il controllo RD02).
     *
     * @param isbn ISBN del libro
     * @return lista dei prestiti restituiti per quel volume
     */
    public List<Loan> getReturnedLoansOfBook(String isbn) {
        return archive.getLoans().stream()
                .filter(l -> !l.isActive())
                .filter(l -> l.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }
}