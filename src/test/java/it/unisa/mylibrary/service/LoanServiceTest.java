package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {

    private Archive archive;
    private Clock fixedClock;
    private LoanService service;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        // Fisso il tempo per test deterministici sulle date di scadenza
        fixedClock = Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneId.of("UTC"));
        service = new LoanService(archive, fixedClock);

        archive.getReaders().put("R1", new Reader("Mario", "Rossi", "R1", "m@unisa.it"));
        archive.getReaders().put("R2", new Reader("Luigi", "Verdi", "R2", "l@unisa.it"));

        archive.getBooks().put("9780000000001", new Book("9780000000001", "Libro 1", List.of("A"), 2020, 2));
        archive.getBooks().put("9780000000002", new Book("9780000000002", "Libro 2", List.of("A"), 2020, 1));
        archive.getBooks().put("9780000000003", new Book("9780000000003", "Libro 3", List.of("A"), 2020, 1));
        archive.getBooks().put("9780000000004", new Book("9780000000004", "Libro 4", List.of("A"), 2020, 1));
    }

    @Test
    void testCostruttore() {
        assertThrows(LibraryException.class, () -> new LoanService(null, fixedClock));
        assertThrows(LibraryException.class, () -> new LoanService(archive, null));
    }

    @Test
    void testCalcoloDateTramiteClock() {
        assertEquals(LocalDate.of(2026, 10, 9), service.today());
        assertEquals(LocalDate.of(2026, 10, 24), service.defaultDueDate());
    }

    @Test
    void testCalcoloCopieDisponibili() {
        assertEquals(0, service.availableCopies("UNKNOWN_ISBN"));
        assertEquals(2, service.availableCopies("9780000000001"));

        Loan prestito = service.registerLoan("R1", "9780000000001", null);
        assertEquals(1, service.availableCopies("9780000000001"));

        service.returnLoan(prestito.getId());
        assertEquals(2, service.availableCopies("9780000000001"));
    }

    @Test
    void testRegistrazionePrestitoValido() {
        // RD16: Se dueDate è null, si applica la durata di default
        Loan loan = service.registerLoan("R1", "9780000000001", null);

        assertEquals(LocalDate.of(2026, 10, 9), loan.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 24), loan.getDueDate());
        assertEquals(1, service.availableCopies("9780000000001"));
    }

    @Test
    void testRegistrazionePrestitoVincoliBase() {
        assertThrows(LibraryException.class, () -> service.registerLoan("FAKE", "9780000000001", null));
        assertThrows(LibraryException.class, () -> service.registerLoan("R1", "9780000000000", null));

        // Impedisce scadenze nel passato
        LocalDate dataPassata = LocalDate.of(2026, 10, 8);
        assertThrows(LibraryException.class, () -> service.registerLoan("R1", "9780000000001", dataPassata));

        // Verifica assenza di alterazioni in caso di fallimento
        assertTrue(archive.getLoans().isEmpty());
    }

    // RD12: Blocco prestiti per copie esaurite
    @Test
    void testRegistrazionePrestitoCopieEsaurite() {
        service.registerLoan("R1", "9780000000002", null);

        LibraryException ex = assertThrows(LibraryException.class, () -> service.registerLoan("R2", "9780000000002", null));
        assertEquals("Copie disponibili esaurite.", ex.getMessage());
    }

    // RD14: Unicità volume in carico allo stesso lettore
    @Test
    void testRegistrazionePrestitoStessoVolume() {
        service.registerLoan("R1", "9780000000001", null);

        LibraryException ex = assertThrows(LibraryException.class, () -> service.registerLoan("R1", "9780000000001", null));
        assertTrue(ex.getMessage().contains("Il lettore ha già in carico una copia"));
    }

    // RD13: Limite globale di prestiti per lettore
    @Test
    void testRegistrazionePrestitoRaggiungimentoLimite() {
        Loan l1 = service.registerLoan("R1", "9780000000001", null);
        service.registerLoan("R1", "9780000000002", null);
        service.registerLoan("R1", "9780000000003", null);

        LibraryException ex = assertThrows(LibraryException.class, () -> service.registerLoan("R1", "9780000000004", null));
        assertTrue(ex.getMessage().contains("Il lettore ha già 3 prestiti attivi"));

        service.returnLoan(l1.getId());
        assertDoesNotThrow(() -> service.registerLoan("R1", "9780000000004", null));
    }

    @Test
    void testRestituzionePrestito() {
        Loan loan = service.registerLoan("R1", "9780000000001", null);

        assertThrows(LibraryException.class, () -> service.returnLoan("FAKE"));

        service.returnLoan(loan.getId());
        assertEquals(LocalDate.of(2026, 10, 9), loan.getReturnDate());

        assertThrows(LibraryException.class, () -> service.returnLoan(loan.getId()));
    }

    @Test
    void testFiltriPrestitiAttivi() {
        Loan l1 = service.registerLoan("R1", "9780000000001", LocalDate.of(2026, 10, 20));
        Loan l2 = service.registerLoan("R1", "9780000000002", LocalDate.of(2026, 10, 15));
        service.registerLoan("R2", "9780000000003", null);

        List<Loan> attiviR1 = service.getActiveLoansOfReader("R1");
        assertEquals(2, attiviR1.size());

        List<Loan> byDue = service.getActiveLoansSortedByDueDate();
        assertEquals("9780000000002", byDue.get(0).getIsbn());

        service.returnLoan(l1.getId());
        assertEquals(1, service.getActiveLoansOfReader("R1").size());
        assertEquals(0, service.getActiveLoansOfBook("9780000000001").size());
    }

    @Test
    void testStoricoPrestitiConclusi() {
        Loan l1 = service.registerLoan("R1", "9780000000001", null);
        service.registerLoan("R1", "9780000000002", null);

        service.returnLoan(l1.getId());

        List<Loan> storicoLettore = service.getReturnedLoansOfReader("R1");
        assertEquals(1, storicoLettore.size());
        assertEquals(l1.getId(), storicoLettore.get(0).getId());

        List<Loan> storicoLibro = service.getReturnedLoansOfBook("9780000000001");
        assertEquals(1, storicoLibro.size());
    }

    @Test
    void testStatoRitardo() {
        Loan prestitoInRitardo = new Loan("R1", "9780000000001", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 8));
        Loan prestitoRegolare = new Loan("R1", "9780000000002", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15));

        assertTrue(service.isOverdue(prestitoInRitardo));
        assertFalse(service.isOverdue(prestitoRegolare));
        assertFalse(service.isOverdue(null));
    }
}