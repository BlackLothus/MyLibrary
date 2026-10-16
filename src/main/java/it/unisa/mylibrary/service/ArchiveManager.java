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

    public void loadAutosave() throws LibraryException {
        // TODO: logica di ripristino
    }

    public void executeAutosave() {
        // TODO: salvataggio silente
    }

    public void loadFromFile(Path path) throws LibraryException {
        // TODO: caricamento da file utente
    }

    public void saveToFile(Path path) throws LibraryException {
        // TODO:salvataggio forzato
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }
}