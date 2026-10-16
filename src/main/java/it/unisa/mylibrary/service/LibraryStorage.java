package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import java.nio.file.Path;
 //Interfaccia per isolare la logica applicativa dai dettagli di I/O
public interface LibraryStorage {
    boolean exists(Path path); //verifica se il file o la destinazione esiste già (utile per l'autosave all'avvio)
    void save(Archive archive, Path path) throws LibraryException; // Salva l'intero stato dell'archivio sul percorso specificato
    Archive load(Path path) throws LibraryException; // Ricarica e restituisce l'archivio letto dal percorso
}