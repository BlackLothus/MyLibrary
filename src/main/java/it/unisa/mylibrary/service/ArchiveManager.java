package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import java.nio.file.Path;

/**
 * Coordina il salvataggio, il caricamento e l'autosalvataggio dell'archivio
 * appoggiandosi all'astrazione {@link LibraryStorage} (RF26, RF27).
 */
public class ArchiveManager {
    private final Archive archive;
    private final LibraryStorage storage;
    private final Path autosavePath;
    private boolean autosaveEnabled = true;

    public ArchiveManager(Archive arc, LibraryStorage str, Path autPath) {
        this.archive = arc;
        this.storage = str;
        this.autosavePath = autPath;
    }

    /**
     * Cerca di recuperare l'autosalvataggio all'avvio del programma.
     * Se il file risulta corrotto, spegne la funzione di autosave per evitare
     * di sovrascrivere e perdere i dati della sessione corrente.
     *
     * @return true se ha caricato i dati, false se il file non esiste
     * @throws LibraryException se il file c'è ma non è leggibile
     */
    public boolean loadAutosave() throws LibraryException {
        if (storage.exists(autosavePath)) {
            try {
                Archive loaded = storage.load(autosavePath);
                archive.replaceContentWith(loaded);
                return true;
            } catch (LibraryException ex) {
                autosaveEnabled = false;
                throw new LibraryException("Il file di autosalvataggio è corrotto o incompatibile.\nL'autosalvataggio è disattivato per questa sessione per evitare la perdita di dati.", ex);
            }
        }
        return false;
    }

    /**
     * Esegue il salvataggio automatico in chiusura se abilitato, ignorando errori
     * per non bloccare l'uscita dall'applicazione.
     */
    public void executeAutosave() {
        if (autosaveEnabled) {
            try {
                storage.save(archive, autosavePath);
            } catch (LibraryException ex) {
                System.err.println("Impossibile creare l'autosalvataggio: " + ex.getMessage());
            }
        }
    }

    /**
     * Carica un archivio da file e aggiorna lo stato in memoria solo a lettura riuscita (RF27, RNF04).
     * Riabilita anche l'autosalvataggio se era stato disattivato all'avvio.
     *
     * @param path percorso del file da caricare
     * @throws LibraryException se il caricamento fallisce; l'archivio in memoria resta uguale
     */
    public void loadFromFile(Path path) throws LibraryException {
        Archive archivioCaricato = storage.load(path);
        archive.replaceContentWith(archivioCaricato);
        autosaveEnabled = true;
    }

    /**
     * Salva lo stato corrente dell'archivio nel percorso specificato (RF26, RNF03).
     *
     * @param path percorso di destinazione
     * @throws LibraryException se si verifica un errore durante il salvataggio
     */
    public void saveToFile(Path path) throws LibraryException {
        storage.save(archive, path);
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }
}