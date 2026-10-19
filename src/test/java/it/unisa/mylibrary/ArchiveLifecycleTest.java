package it.unisa.mylibrary;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.persistence.ArchiveStorage;
import it.unisa.mylibrary.service.ArchiveManager;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveLifecycleTest {

    private Archive archive;
    private LoanService loanService;
    private BookService bookService;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        // Fisso il tempo per testare i prestiti in modo deterministico
        Clock clockFisso = Clock.fixed(Instant.parse("2026-10-10T10:00:00Z"), ZoneId.of("UTC"));
        loanService = new LoanService(archive, clockFisso);
        bookService = new BookService(archive, loanService);
        readerService = new ReaderService(archive, loanService);
    }

    @Test
    void testSalvataggio(@TempDir Path tempDir) {
        Path fileBackup = tempDir.resolve("backup.dat");
        ArchiveManager manager = new ArchiveManager(archive, new ArchiveStorage(), fileBackup);

        // inizio con setup dati
        bookService.addBook(new Book("9780000000111", "Il Signore degli Anelli", List.of("Tolkien"), 1954, 3));
        readerService.addReader(new Reader("Mario", "Rossi", "0612701", "m.rossi@studenti.unisa.it"));
        Loan prestito = loanService.registerLoan("0612701", "9780000000111", null);

        // simulo spegnimento (salvataggio e pulizia RAM senza violare il BeforeEach)
        manager.saveToFile(fileBackup);
        archive.replaceContentWith(new Archive());

        //riavvio
        manager.loadFromFile(fileBackup);

        //Verifica
        assertEquals(2, loanService.availableCopies("9780000000111"));
        assertEquals(1, loanService.getActiveLoansOfReader("0612701").size());
        loanService.returnLoan(prestito.getId());
        assertEquals(3, loanService.availableCopies("9780000000111"));
    }

    @Test
    void testAutosave(@TempDir Path tempDir) {
        Path autosavePath = tempDir.resolve("autosave.dat");
        ArchiveManager manager = new ArchiveManager(archive, new ArchiveStorage(), autosavePath);

        // Al primo avvio non trova l'autosave e genera i dati demo
        assertFalse(manager.loadAutosave());
        DemoDataSeeder.seedIfEmpty(archive, bookService, readerService);

        int libriIniziali = archive.getBooks().size();

        // Simulo la chiusura dell'app che innesca l'autosave
        manager.executeAutosave();

        // Svuoto e ricarico
        archive.replaceContentWith(new Archive());
        assertTrue(manager.loadAutosave());

        assertEquals(libriIniziali, archive.getBooks().size());
    }

    @Test
    void testCancellazioneDatiBloccata() {
        DemoDataSeeder.seedIfEmpty(archive, bookService, readerService);

        // sono i dati che dovrebbero bloccare l'eliminazione perchè hanno uno storico
        assertThrows(LibraryException.class, () -> bookService.removeBook("9788845268342"));
        assertThrows(LibraryException.class, () -> readerService.removeReader("0612700021"));
    }
}