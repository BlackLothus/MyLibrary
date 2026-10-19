package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gestisce il catalogo libri applicando validazioni formali e regole di dominio (RF01-RF06, RF08).
 */
public class BookService {
    //[ED02]: Controllo su ISBN-10 (9 cifre + decima cifra o 'X') e ISBN-13 (prefisso 978/979 + 10 cifre)
    private static final Pattern FORMATO_ISBN = Pattern.compile("^(?:\\d{9}[\\dX]|97[89]\\d{10})$");
    private final Archive archive;
    private final LoanService loanService;

    /**
     * Inizializza il servizio associandolo all'archivio e al servizio prestiti.
     *
     * @param archive     stato globale della biblioteca
     * @param loanService servizio prestiti per verificare i vincoli sulle copie
     * @throws LibraryException se {@code archive} o {@code loanService} sono nulli
     */
    public BookService(Archive archive, LoanService loanService) {
        if (archive == null) {
            throw new LibraryException("L'archivio non può essere nullo.");
        }
        if (loanService == null) {
            throw new LibraryException("il LoanService non può essere nullo.");
        }
        this.archive = archive;
        this.loanService = loanService;
    }

    /**
     * Inserisce un nuovo libro nel catalogo (RF03).
     *
     * @param book il libro da aggiungere
     * @throws LibraryException se i dati obbligatori mancano (ED01a), l'ISBN non è valido (ED02),
     *         le copie sono negative o l'ISBN è già presente in archivio (RD04a)
     */
    public void addBook(Book book) {
        validate(book);
        if (archive.getBooks().containsKey(book.getIsbn())) {
            throw new LibraryException("Esiste già un libro con ISBN " + book.getIsbn());
        }
        archive.getBooks().put(book.getIsbn(), book);
    }

    /**
     * Aggiorna i dati di un libro esistente (RF05).
     * È consentito impostare le copie totali a zero (RD01), purché non scendano sotto i prestiti attivi.
     *
     * @param book il libro con i dati aggiornati
     * @throws LibraryException se i dati non sono validi (ED01a, ED02), il libro non esiste
     *         o le copie totali sono inferiori ai prestiti attualmente attivi (RD03)
     */
    public void updateBook(Book book) {
        validate(book);
        if (!archive.getBooks().containsKey(book.getIsbn())) {
            throw new LibraryException("Nessun libro trovato con ISBN " + book.getIsbn() + ".");
        }

        //RD03
        int prestitiAttivi = loanService.getActiveLoansOfBook(book.getIsbn()).size();
        if (book.getTotalCopies() < prestitiAttivi) {
            throw new LibraryException("Le copie totali (" + book.getTotalCopies()
                    + ") non possono essere inferiori ai prestiti attualmente attivi (" + prestitiAttivi + ").");
        }
        archive.getBooks().put(book.getIsbn(),book);
    }

    /**
     * Rimuove un libro dal catalogo (RF06).
     *
     * @param isbn codice ISBN del libro da eliminare
     * @throws LibraryException se il libro non esiste o se ha prestiti attivi o conclusi nello storico (RD02)
     */
    public void removeBook(String isbn) {
        if (isbn == null) {
            throw new LibraryException("L'ISBN non può essere vuoto.");
        }

        isbn = Book.normalizeIsbn(isbn);
        if (!archive.getBooks().containsKey(isbn)) {
            throw new LibraryException("Nessun libro trovato con ISBN " + isbn + ".");
        }

        //RD02
        long prestitiAttivi = loanService.getActiveLoansOfBook(isbn).size();
        if (prestitiAttivi > 0) {
            throw new LibraryException("Impossibile eliminare: ha " + prestitiAttivi + " prestiti attivi.");
        }
        int storicoPrestiti = loanService.getReturnedLoansOfBook(isbn).size();
        if (storicoPrestiti > 0) {
            throw new LibraryException("Impossibile eliminare: ha uno storico di " + storicoPrestiti
                    + " prestiti conclusi conservati.");
        }

        archive.getBooks().remove(isbn);
    }

    /**
     * Cerca un libro tramite ISBN, normalizzando l'input prima della ricerca (RF04).
     *
     * @param isbn ISBN da cercare
     * @return {@link Optional} col libro trovato, o vuoto se non esiste o l'input è nullo
     */
    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        //normalizzo per la ricerca anche con trattini e spazi
        String isbnNormalizzato = Book.normalizeIsbn(isbn);
        return Optional.ofNullable(archive.getBooks().get(isbnNormalizzato));
    }

    /**
     * Restituisce tutti i libri ordinati alfabeticamente per titolo, ignorando maiuscole/minuscole (RF01, RF02).
     *
     * @return lista dei libri ordinata per titolo
     */
    public List<Book> getAllSortedByTitle() {
        List<Book> result = new ArrayList<>(archive.getBooks().values());
        result.sort(byTitle());
        return result;
    }

    /**
     * Restituisce tutti i libri ordinati per primo autore e, a parità, per titolo (RF01, RF02).
     *
     * @return lista dei libri ordinata per autore
     */
    public List<Book> getAllSortedByAuthor() {
        List<Book> result = new ArrayList<>(archive.getBooks().values());
        result.sort(byAuthor());
        return result;
    }

    /**
     * Cerca libri per corrispondenza parziale e case-insensitive su titolo, autori o ISBN,
     * restituendo i risultati ordinati per titolo (RF08, RF02).
     *
     * @param o testo da cercare; se nullo o vuoto restituisce l'intero catalogo
     * @return lista dei libri corrispondenti
     */
    //RF08
    public List<Book> search(String o) {
        if (o == null || o.isBlank()) {
            return getAllSortedByTitle();
        }
        return filterBooks(o).sorted(byTitle()).collect(Collectors.toList());
    }

    /**
     * Esegue la stessa ricerca di {@link #search(String)} ma ordina i risultati per autore (RF08, RF02).
     *
     * @param o testo da cercare; se nullo o vuoto restituisce l'intero catalogo
     * @return lista dei libri corrispondenti ordinata per autore
     */
    //RF08
    public List<Book> searchSortedByAuthor(String o) {
        if (o == null || o.isBlank()) {
            return getAllSortedByAuthor();
        }
        return filterBooks(o).sorted(byAuthor()).collect(Collectors.toList());
    }

    //helper per centralizzare il filtro di ricerca su titolo, autore e isbn da usare in diverse varianti di ordinamento
    private Stream<Book> filterBooks(String o){
        String filtroTesto = o.trim().toLowerCase(Locale.ROOT);
        String filtroIsbn = Book.normalizeIsbn(filtroTesto);
        return archive.getBooks().values().stream().filter(b->matches(b, filtroTesto, filtroIsbn));
    }
    private static boolean matches(Book b, String testo, String isbnTarget) {
        if (contains(b.getTitle(), testo)) {
            return true;
        }
        //verifico corrispondenza parziale sull'ISBN normalizzato
        if (b.getIsbn() != null && !isbnTarget.isEmpty() && b.getIsbn().contains(isbnTarget)) {
            return true;
        }
        if (b.getAuthors() != null) {
            for (String autore : b.getAuthors()) {
                if (contains(autore, testo)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean contains(String value, String searchedText) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(searchedText); //non nullo e testo cercato
    }

    private static Comparator<Book> byTitle() {
        return Comparator.comparing((Book b) -> b.getTitle().toLowerCase(Locale.ROOT));
    }

    private static Comparator<Book> byAuthor() {
        return Comparator.comparing(BookService::firstAuthorKey).thenComparing(byTitle());
    }

    private static String firstAuthorKey(Book b) {
        List<String> authors = b.getAuthors();
        if (authors == null || authors.isEmpty() || authors.get(0) == null) {
            return "";
        }
        return authors.get(0).toLowerCase(Locale.ROOT);
    }

    private static void validate(Book book) {
        if (book == null) {
            throw new LibraryException("Il libro non può essere nullo");
        }
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new LibraryException("L'ISBN non può essere vuoto");
        }
        if (!FORMATO_ISBN.matcher(book.getIsbn()).matches()) {
            throw new LibraryException("L'ISBN inserito non è valido");
        }
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new LibraryException("Il titolo non può essere vuoto");
        }
        List<String> authors = book.getAuthors();
        if (authors == null || authors.isEmpty()) {
            throw new LibraryException("Il libro deve avere almeno un autore");
        }
        if (book.getYear() <= 0) {
            throw new LibraryException("L'anno di pubblicazione deve essere maggiore di zero");
        }
        if (book.getTotalCopies() < 0) {
            throw new LibraryException("Il numero di copie totali non può essere negativo");
        }
    }
}