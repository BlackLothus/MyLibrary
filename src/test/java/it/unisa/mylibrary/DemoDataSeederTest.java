package it.unisa.mylibrary;

import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DemoDataSeederTest {

    private Archive archive;
    private LoanService loanService;
    private BookService bookService;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        loanService = new LoanService(archive, Clock.systemDefaultZone());
        bookService = new BookService(archive, loanService);
        readerService = new ReaderService(archive, loanService);
    }

    @Test
    void testEsecuzioniMultiple() {
        DemoDataSeeder.seedIfEmpty(archive, bookService, readerService);

        assertFalse(archive.getBooks().isEmpty());
        assertFalse(archive.getReaders().isEmpty());
        assertEquals(5, archive.getLoans().size());

        int initialBooks = archive.getBooks().size();

        //Una seconda chiamata accidentale non deve raddoppiare i dati
        assertDoesNotThrow(() -> DemoDataSeeder.seedIfEmpty(archive, bookService, readerService));
        assertEquals(initialBooks, archive.getBooks().size());
    }

    @Test
    void testIgnoraArchivioGiaPopolato() {
        // Se l'archivio ha già dati utente, il seeder si deve disattivare per non sporcarlo
        bookService.addBook(new Book("9780000000111", "Libro Utente", List.of("Autore"), 2024, 1));
        DemoDataSeeder.seedIfEmpty(archive, bookService, readerService);

        assertEquals(1, archive.getBooks().size());
        assertTrue(archive.getReaders().isEmpty());
    }
}