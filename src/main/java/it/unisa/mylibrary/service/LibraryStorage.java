package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import java.nio.file.Path;

/**
 * Interfaccia per il salvataggio e caricamento dei dati.
 * Isola la logica dell'applicazione dai dettagli del file system (DIP, RNF09).
 */
public interface LibraryStorage {

    boolean exists(Path path);

    /**
     * Salva l'archivio sul disco.
     * Non deve corrompere eventuali file già presenti in caso di errore.
     */
    void save(Archive archive, Path path) throws LibraryException;

    /**
     * Legge i dati dal file.
     * Se la lettura fallisce, non deve alterare l'archivio già in memoria (RNF04).
     */
    Archive load(Path path) throws LibraryException;
}