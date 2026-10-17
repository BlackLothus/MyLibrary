package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import java.nio.file.Path;

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

    public boolean loadAutosave() throws LibraryException {
        if (storage.exists(autosavePath)) {
            try {
                Archive loaded = storage.load(autosavePath);
                archive.replaceContentWith(loaded);
                return true;
            } catch (LibraryException ex) {
                autosaveEnabled = false;
                throw new LibraryException("Il file di autosalvataggio è corrotto o incompatibile.\nL'autosalvataggio disattivato per questa sessione per evitare la perdita di dati.", ex);
            }
        }
        return false;
    }

    public void executeAutosave() {
        if (autosaveEnabled) {
            try {
                storage.save(archive, autosavePath);
            } catch (LibraryException ex) {
                System.err.println("Impossibile creare l'autosalvataggio: " + ex.getMessage());
            }
        }
    }

    public void loadFromFile(Path path) throws LibraryException {
        Archive archivioCaricato = storage.load(path);
        archive.replaceContentWith(archivioCaricato);
        autosaveEnabled = true;    }

    public void saveToFile(Path path) throws LibraryException {
        storage.save(archive, path);
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }
}