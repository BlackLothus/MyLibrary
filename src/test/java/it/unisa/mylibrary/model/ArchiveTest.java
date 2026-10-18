package it.unisa.mylibrary.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveTest {

    // Helper per istanziare rapidamente oggetti fittizi per i test
    private static Book creaLibro(String isbn) {
        return new Book(isbn, "Titolo " + isbn, List.of("Autore Test"), 2020, 3);
    }

    private static Reader creaLettore(String matricola) {
        return new Reader("Nome", "Cognome", matricola, matricola + "@studenti.unisa.it");
    }

    @Test
    void testSostituzioneContenutoArchivio() {
        Archive archivioSorgente = new Archive();
        Book libro = creaLibro("1234");
        Reader lettore = creaLettore("0612700001");
        Loan prestito = new Loan(lettore.getStudentId(), libro.getIsbn(), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15));

        archivioSorgente.getBooks().put(libro.getIsbn(), libro);
        archivioSorgente.getReaders().put(lettore.getStudentId(), lettore);
        archivioSorgente.getLoans().add(prestito);

        Archive archivioDestinazione = new Archive();

        // Simulo la presenza di vecchi dati per assicurarmi che vengano sovrascritti
        archivioDestinazione.getBooks().put("old", creaLibro("old"));

        archivioDestinazione.replaceContentWith(archivioSorgente);

        // Verifica che i vecchi dati siano spariti e quelli nuovi siano stati copiati
        assertFalse(archivioDestinazione.getBooks().containsKey("old"));
        assertEquals(1, archivioDestinazione.getBooks().size());
        assertSame(libro, archivioDestinazione.getBooks().get("1234"));

        assertEquals(1, archivioDestinazione.getReaders().size());
        assertSame(lettore, archivioDestinazione.getReaders().get("0612700001"));

        assertEquals(1, archivioDestinazione.getLoans().size());
        assertSame(prestito, archivioDestinazione.getLoans().get(0));
    }

    @Test
    void testReplaceContentWithPreservaIstanzeCollezioni() {
        // Questo test è cruciale: garantisce che svuotiamo e riempiamo le mappe,
        // senza riassegnare l'oggetto Map o List stesso, per non rompere i reference nei Service
        Archive target = new Archive();
        Map<String, Book> riferimentoLibri = target.getBooks();
        Map<String, Reader> riferimentoLettori = target.getReaders();
        List<Loan> riferimentoPrestiti = target.getLoans();

        target.replaceContentWith(new Archive());

        assertSame(riferimentoLibri, target.getBooks());
        assertSame(riferimentoLettori, target.getReaders());
        assertSame(riferimentoPrestiti, target.getLoans());
    }

    @Test
    void testSostituzioneArchivioVuotoOStessaIstanza() {
        Archive archivio = new Archive();
        archivio.getBooks().put("1", creaLibro("1"));

        // Rimpiazzare l'archivio con se stesso non deve svuotarlo o causare errori
        archivio.replaceContentWith(archivio);
        assertEquals(1, archivio.getBooks().size());

        // Rimpiazzarlo con un archivio nuovo (vuoto) deve pulire i dati
        archivio.replaceContentWith(new Archive());
        assertTrue(archivio.getBooks().isEmpty());
    }

    @Test
    void testIsolamentoArchivioSorgente() {
        // Verifica che modifiche successive al nuovo archivio non si riflettano su quello vecchio
        Archive sorgente = new Archive();
        sorgente.getBooks().put("1", creaLibro("1"));

        Archive destinazione = new Archive();
        destinazione.replaceContentWith(sorgente);

        destinazione.getBooks().clear();

        assertEquals(1, sorgente.getBooks().size());
    }
}