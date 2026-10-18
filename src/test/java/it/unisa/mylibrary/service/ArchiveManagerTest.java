package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Archive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveManagerTest {

    // Storage fittizio isolato dal vero File System per test veloci e affidabili
    static class FakeStorage implements LibraryStorage {
        Archive savedArchive = null;
        boolean fileExists = false;
        boolean loadCalled = false;
        boolean shouldThrowOnLoad = false;
        boolean shouldThrowOnSave = false;

        @Override
        public boolean exists(Path path) {
            return fileExists;
        }

        @Override
        public void save(Archive archive, Path path) throws LibraryException {
            if (shouldThrowOnSave) throw new LibraryException("Finto errore in scrittura");
            this.savedArchive = new Archive();
            this.savedArchive.replaceContentWith(archive);
        }

        @Override
        public Archive load(Path path) throws LibraryException {
            loadCalled = true;
            if (shouldThrowOnLoad) throw new LibraryException("Finto errore in lettura");
            if (savedArchive == null) throw new LibraryException("File non trovato");

            Archive copy = new Archive();
            copy.replaceContentWith(savedArchive);
            return copy;
        }
    }

    private Archive archive;
    private FakeStorage fintoStorage;
    private ArchiveManager manager;
    private Path fintoAutosavePath;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        fintoStorage = new FakeStorage();
        fintoAutosavePath = Path.of("dummy_autosave.dat");
        manager = new ArchiveManager(archive, fintoStorage, fintoAutosavePath);
    }

    @Test
    void testSalvataggioEAutosalvataggio() throws LibraryException {
        archive.getBooks().put("321", new Book("321", "Java", List.of("De Sio Cesari"), 2020, 1));

        // Test autosalvataggio (silente)
        manager.executeAutosave();
        assertNotNull(fintoStorage.savedArchive);
        assertTrue(fintoStorage.savedArchive.getBooks().containsKey("321"));

        // Svuoto per il test successivo
        fintoStorage.savedArchive = null;

        // Test salvataggio esplicito delegato
        manager.saveToFile(Path.of("manual_save.dat"));
        assertNotNull(fintoStorage.savedArchive);
        assertTrue(fintoStorage.savedArchive.getBooks().containsKey("321"));
    }

    @Test
    void testCaricamentoManualeDaFileValido() throws LibraryException {
        Archive archivioSimulato = new Archive();
        archivioSimulato.getBooks().put("222", new Book("222", "Test", List.of("De Sio Cesari"), 2020, 1));
        fintoStorage.savedArchive = archivioSimulato;

        manager.loadFromFile(Path.of("dummy.dat"));
        assertTrue(archive.getBooks().containsKey("222"));
    }

    @Test
    void testFallimentoCaricamentoPreservaStatoOriginale() {
        archive.getBooks().put("111", new Book("111", "Java", List.of("De Sio Cesari"), 2020, 1));

        // Provoco un errore durante il caricamento del nuovo file
        fintoStorage.shouldThrowOnLoad = true;

        assertThrows(LibraryException.class, () -> manager.loadFromFile(Path.of("dummy.dat")));

        // Verifica RNF04: se il caricamento fallisce, l'archivio corrente non viene sovrascritto
        assertEquals(1, archive.getBooks().size());
        assertTrue(archive.getBooks().containsKey("111"));
    }

    @Test
    void testFallimentoSilenteAutosalvataggio() {
        fintoStorage.shouldThrowOnSave = true;

        // Non deve esplodere, bloccare il programma o disabilitare la funzionalità
        assertDoesNotThrow(() -> manager.executeAutosave());
        assertTrue(manager.isAutosaveEnabled());
    }

    @Test
    void testGestioneAutosalvataggioAllAvvio() throws LibraryException {
        //Caso1: il file non esiste (primo avvio in assoluto)
        fintoStorage.fileExists = false;
        assertFalse(manager.loadAutosave());
        assertFalse(fintoStorage.loadCalled);
        assertTrue(manager.isAutosaveEnabled());

        //Caso2: il file esiste ed è valido
        fintoStorage.fileExists = true;
        fintoStorage.savedArchive = new Archive();
        fintoStorage.savedArchive.getBooks().put("988", new Book("988", "Vita da pupazzo", List.of("Gabibbo"), 2020, 1));

        assertTrue(manager.loadAutosave());
        assertTrue(archive.getBooks().containsKey("988"));
    }

    @Test
    void testProtezioneDatiConAutosalvataggioCorrotto() {
        fintoStorage.fileExists = true;
        fintoStorage.shouldThrowOnLoad = true; //simulo file corrotto o incompatibile

        LibraryException ex = assertThrows(LibraryException.class, () -> manager.loadAutosave());
        assertTrue(ex.getMessage().contains("disattivato per questa sessione"));

        // Verifica sicurezza: l'autosalvataggio in chiusura si è spento per evitare di distruggere i dati buoni
        assertFalse(manager.isAutosaveEnabled());

        //se provo a eseguire l'autosalvataggio non deve fare nulla
        fintoStorage.shouldThrowOnLoad = false;
        fintoStorage.savedArchive = null;
        manager.executeAutosave();
        assertNull(fintoStorage.savedArchive);

        //se carico manualmente un file sano, l'autosalvataggio si riaccende in automatico
        fintoStorage.savedArchive = new Archive();
        assertDoesNotThrow(() -> manager.loadFromFile(Path.of("backup_sano.dat")));
        assertTrue(manager.isAutosaveEnabled());
    }
}