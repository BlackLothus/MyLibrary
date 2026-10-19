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

public class BookService {
    //[ED02]: Controllo su ISBN-10 (9 cifre + decima cifra o 'X') e ISBN-13 (prefisso 978/979 + 10 cifre)
    private static final Pattern FORMATO_ISBN = Pattern.compile("^(?:\\d{9}[\\dX]|97[89]\\d{10})$");
    private final Archive archive;
    private final LoanService loanService;

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

    public void addBook(Book book) {
        validate(book);
        if (archive.getBooks().containsKey(book.getIsbn())) {
            throw new LibraryException("Esiste già un libro con ISBN " + book.getIsbn());
        }
        archive.getBooks().put(book.getIsbn(), book);
    }

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

    public void removeBook(String isbn) {
        if (isbn == null || !archive.getBooks().containsKey(isbn)) {
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
                    + "prestiti conclusi conservati.");
        }

        archive.getBooks().remove(isbn);
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        //normalizzo per la ricerca anche con trattini e spazi
        String isbnNormalizzato = Book.normalizeIsbn(isbn);
        return Optional.ofNullable(archive.getBooks().get(isbnNormalizzato));
    }

    public List<Book> getAllSortedByTitle() {
        List<Book> result = new ArrayList<>(archive.getBooks().values());
        result.sort(byTitle());
        return result;
    }

    public List<Book> getAllSortedByAuthor() {
        List<Book> result = new ArrayList<>(archive.getBooks().values());
        result.sort(byAuthor());
        return result;
    }
    //RF08
    public List<Book> search(String o) {
        if (o == null || o.isBlank()) {
            return getAllSortedByTitle();
        }
        return filterBooks(o).sorted(byTitle()).collect(Collectors.toList());
    }

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