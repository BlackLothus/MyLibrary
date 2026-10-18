package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private Archive archive;
    private BookService service;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        LoanService loanService = new LoanService(archive, Clock.systemDefaultZone());
        service = new BookService(archive, loanService);
    }

    private static Book creaLibro(String isbn, String title, int year, int copies, String... authors) {
        return new Book(isbn, title, Arrays.asList(authors), year, copies);
    }

    private static List<String> estraiTitoli(List<Book> books) {
        return books.stream().map(Book::getTitle).collect(Collectors.toList());
    }

    private void popolaArchivioDati() {
        // Inserimenti misti per testare ordinamento (maiuscole/minuscole) e ricerche cross-field
        service.addBook(creaLibro("9780000000111", "le Lettere a Lucilio", 65, 3, "Lucio Anneo Seneca", "Carmelo Morabito"));
        service.addBook(creaLibro("9780000000222", "1984", 1949, 2, "George Orwell"));
        service.addBook(creaLibro("9780000000333", "La fattoria degli animali", 1945, 1, "george orwell"));
        service.addBook(creaLibro("9780000000444", "il Signore degli Anelli", 1954, 5, "J.R.R. Tolkien"));
    }

    @Test
    void testAggiuntaLibroValidoConVariantiIsbn() {
        // [RD04a] Controllo validità formato e immagazzinamento corretto
        assertDoesNotThrow(() -> service.addBook(creaLibro("8804668237", "1984 Mondadori", 2016, 1, "George Orwell"))); // ISBN-10
        assertDoesNotThrow(() -> service.addBook(creaLibro("884529261x", "Il Signore degli Anelli Bompiani", 2003, 1, "J.R.R. Tolkien"))); // X minuscola
        assertDoesNotThrow(() -> service.addBook(creaLibro("978-88-04-66823-8", "1984 Nuova Edizione", 2021, 1, "George Orwell"))); // ISBN-13

        assertEquals(3, archive.getBooks().size());
        assertTrue(archive.getBooks().containsKey("9788804668238"));
    }

    @Test
    void testValidazioneDatiObbligatoriLibro() {
        // [ED01a] Tutti i campi base devono essere validi
        assertThrows(LibraryException.class, () -> service.addBook(null));
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("", "Lo Hobbit", 1937, 1, "J.R.R. Tolkien"))); // ISBN Vuoto
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("123", "Lo Hobbit", 1937, 1, "J.R.R. Tolkien"))); // ISBN Invalido
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("9780000000111", "", 1937, 1, "J.R.R. Tolkien"))); // Titolo Vuoto
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("9780000000111", "Lo Hobbit", 1937, 1))); // Autore Vuoto
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("9780000000111", "Lo Hobbit", 0, 1, "J.R.R. Tolkien"))); // Anno invalido
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("9780000000111", "Lo Hobbit", 1937, -1, "J.R.R. Tolkien"))); // Copie negative

        // Verifica che nessuno dei tentativi falliti abbia alterato l'archivio
        assertTrue(archive.getBooks().isEmpty());
    }

    @Test
    void testUnicitaIsbnNelCatalogo() {
        // [RD04a] Unicità del codice ISBN
        service.addBook(creaLibro("9788804668238", "1984", 1949, 1, "George Orwell"));
        assertThrows(LibraryException.class, () -> service.addBook(creaLibro("978-88-04-66823-8", "La fattoria degli animali", 1945, 2, "George Orwell")));
    }

    @Test
    void testAggiornamentoGiacenzaLibro() {
        // [RD03] Limite minimo giacenza rispetto ai prestiti in corso
        service.addBook(creaLibro("9780000000111", "Il Silmarillion", 1977, 3, "J.R.R. Tolkien"));
        archive.getLoans().add(new Loan("R1", "9780000000111", LocalDate.now(), LocalDate.now().plusDays(15)));
        archive.getLoans().add(new Loan("R2", "9780000000111", LocalDate.now(), LocalDate.now().plusDays(15)));

        Book aggiornamentoErrato = creaLibro("9780000000111", "Il Silmarillion", 1977, 1, "J.R.R. Tolkien");
        assertThrows(LibraryException.class, () -> service.updateBook(aggiornamentoErrato));

        Book aggiornamentoValido = creaLibro("9780000000111", "Il Silmarillion", 1977, 2, "J.R.R. Tolkien");
        assertDoesNotThrow(() -> service.updateBook(aggiornamentoValido));
    }

    @Test
    void testRimozioneLibroInBaseAStoricoPrestiti() {
        // [RD02] Blocco eliminazione in presenza di prestiti e conservazione storico
        service.addBook(creaLibro("9780000000111", "Super Mario Bros: La Storia", 2015, 2, "Mario Auditore"));
        Loan prestito = new Loan("R1", "9780000000111", LocalDate.now(), LocalDate.now().plusDays(10));
        archive.getLoans().add(prestito);

        assertThrows(LibraryException.class, () -> service.removeBook("9780000000111"));

        prestito.markReturned(LocalDate.now());
        assertThrows(LibraryException.class, () -> service.removeBook("9780000000111")); // Blocco per storico
    }

    @Test
    void testOrdinamentoCatalogo() {
        popolaArchivioDati();

        // Ordine alfabetico ignorando le maiuscole: 1984 -> il Signore -> La fattoria -> le Lettere
        assertEquals(Arrays.asList("1984", "il Signore degli Anelli", "La fattoria degli animali", "le Lettere a Lucilio"),
                estraiTitoli(service.getAllSortedByTitle()));

        // Ordine per autore: George Orwell (1984, La fattoria) -> J.R.R. Tolkien -> Lucio Anneo Seneca
        assertEquals(Arrays.asList("1984", "La fattoria degli animali", "il Signore degli Anelli", "le Lettere a Lucilio"),
                estraiTitoli(service.getAllSortedByAuthor()));
    }

    @Test
    void testRicercaCrossFieldConFiltro() {
        popolaArchivioDati();

        assertEquals(Arrays.asList("1984", "La fattoria degli animali"), estraiTitoli(service.search("ORWELL")));
        assertEquals(Arrays.asList("le Lettere a Lucilio"), estraiTitoli(service.search("carmelo")));
        assertEquals(Arrays.asList("il Signore degli Anelli"), estraiTitoli(service.search("444")));
        assertEquals(Arrays.asList("il Signore degli Anelli"), estraiTitoli(service.search("signor")));
    }
}