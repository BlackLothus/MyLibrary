package it.unisa.mylibrary.persistence;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.time.LocalDate;
import java.util.List;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveStorageTest {

    private ArchiveStorage storage;
    private Archive sorgente;
    private Archive destinazione;

    @BeforeEach
    void setUp() {
        storage = new ArchiveStorage();
        sorgente = new Archive();
        destinazione = new Archive();
    }

    @Test
    void testSalvataggioECaricamentoArchivioValido(@TempDir Path tempDir) throws Exception {
        Path fileSalvataggio = tempDir.resolve("archivio_test.dat");

        // Preparo un po' di dati di prova
        Book libro = new Book("1234", "Java", List.of("De Sio Cesari"), 2020, 5);
        Reader lettore = new Reader("Yoshisaur", "Munchakoopas", "0612700001", "yoshi@unisa.it");
        Loan prestito = new Loan("0612700001", "1234", LocalDate.now().minusDays(2), LocalDate.now().plusDays(13));

        sorgente.getBooks().put(libro.getIsbn(), libro);
        sorgente.getReaders().put(lettore.getStudentId(), lettore);
        sorgente.getLoans().add(prestito);

        // Verifico l'esistenza del file prima e dopo il salvataggio
        assertFalse(storage.exists(fileSalvataggio));
        storage.save(sorgente, fileSalvataggio);
        assertTrue(storage.exists(fileSalvataggio));

        // Ricarico i dati in un nuovo archivio
        Archive caricato = storage.load(fileSalvataggio);
        destinazione.replaceContentWith(caricato);

        // Sfrutto i metodi equals() già testati nelle classi model per verificare l'integrità
        assertEquals(1, destinazione.getBooks().size());
        assertEquals(libro, destinazione.getBooks().get("1234"));

        assertEquals(1, destinazione.getReaders().size());
        assertEquals(lettore, destinazione.getReaders().get("0612700001"));

        assertEquals(1, destinazione.getLoans().size());
        assertEquals(prestito, destinazione.getLoans().get(0));
    }

    @Test
    void testSovrascritturaFileEsistente(@TempDir Path tempDir) throws Exception {
        Path fileSalvataggio = tempDir.resolve("overwrite_test.dat");

        // Creo un file vuoto iniziale
        storage.save(new Archive(), fileSalvataggio);

        Archive nuovoArchivio = new Archive();
        nuovoArchivio.getBooks().put("123", new Book("123", "Java", List.of("De Sio  Cesari"), 2020, 1));

        // Sovrascrivo e verifico che i nuovi dati siano presenti
        storage.save(nuovoArchivio, fileSalvataggio);
        Archive caricato = storage.load(fileSalvataggio);

        assertTrue(caricato.getBooks().containsKey("123"));
    }

    @Test
    void testErroriDiLetturaFileFisico(@TempDir Path tempDir) throws Exception {
        Path fileInesistente = tempDir.resolve("inesistente.dat");
        Path fileCorrotto = tempDir.resolve("corrotto.dat");
        Path fileVuoto = tempDir.resolve("vuoto.dat");

        Files.writeString(fileCorrotto, "dati_spazzatura_non_serializzati");
        Files.createFile(fileVuoto);

        // Accorpo i test sulle varie eccezioni IO
        LibraryException exMissing = assertThrows(LibraryException.class, () -> storage.load(fileInesistente));
        assertTrue(exMissing.getMessage().contains("non è stato trovato"));

        LibraryException exCorrupt = assertThrows(LibraryException.class, () -> storage.load(fileCorrotto));
        assertTrue(exCorrupt.getMessage().contains("formato del file non è valido"));

        LibraryException exEmpty = assertThrows(LibraryException.class, () -> storage.load(fileVuoto));
        assertTrue(exEmpty.getMessage().contains("vuoto o incompleto"));
    }

    // Simulo un tentativo di iniezione di una classe malevola
    static class PayloadMalevolo implements Serializable {
        private static final long serialVersionUID = 1L;
        String dati = "hack";
    }

    @Test
    void testFiltroSicurezzaEClassiErrate(@TempDir Path tempDir) throws Exception {
        Path fileStringa = tempDir.resolve("stringa.dat");
        Path fileMalevolo = tempDir.resolve("payload.dat");

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(fileStringa))) {
            oos.writeObject("Questa è una banale stringa, non un oggetto Archive");
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(fileMalevolo))) {
            oos.writeObject(new PayloadMalevolo());
        }

        LibraryException exClassCast = assertThrows(LibraryException.class, () -> storage.load(fileStringa));
        assertTrue(exClassCast.getMessage().contains("non contiene un archivio valido"));

        // Il filtro deserializzazione (ObjectInputFilter) deve bloccare PayloadMalevolo
        LibraryException exFilter = assertThrows(LibraryException.class, () -> storage.load(fileMalevolo));
        assertTrue(exFilter.getMessage().contains("incompatibile o corrotta"));
    }

    @Test
    void testErroreSalvataggioCartellaInesistente(@TempDir Path tempDir) {
        // Provo a salvare in un percorso irraggiungibile
        Path percorsoImpossibile = tempDir.resolve("cartella_fantasma").resolve("test.dat");

        LibraryException ex = assertThrows(LibraryException.class, () -> storage.save(sorgente, percorsoImpossibile));
        assertTrue(ex.getMessage().contains("errore di scrittura sul file temporaneo"));
        assertFalse(Files.exists(percorsoImpossibile));
    }
}