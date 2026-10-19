package it.unisa.mylibrary.persistence;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.service.LibraryStorage;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Implementazione dello storage su file system locale tramite serializzazione binaria (RF26, RF27).
 * Si occupa solo di I/O senza dipendere dalla UI (RNF09).
 */
public class ArchiveStorage implements LibraryStorage {

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);         //verifica la presenza del file sul disco
    }

    /**
     * Salva l'archivio su file passando per un file temporaneo, così da garantire
     * l'atomicità e non corrompere il salvataggio precedente in caso di errore (RNF03, RNF06).
     *
     * @param archive l'archivio da serializzare
     * @param path    il percorso di destinazione
     * @throws LibraryException se la scrittura o la sostituzione del file falliscono
     */
    @Override
    public void save(Archive archive, Path path) throws LibraryException {
        // Non scrivo subito sul file finale per sicurezza: uso prima un file temporaneo
        Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");

        try {
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
                out.writeObject(archive);
            } catch (IOException e) {
                throw new LibraryException("Impossibile salvare l'archivio: errore di scrittura sul file temporaneo.", e);
            }

            // Sostituzione atomica del file per evitare corruzioni in caso di crash
            try {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback se il file system non supporta lo spostamento atomico
                try {
                    Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new LibraryException("Impossibile completare il salvataggio: errore durante la sovrascrittura del file (fallback).", ex);
                }
            } catch (IOException e) {
                throw new LibraryException("Impossibile completare il salvataggio: errore durante la sovrascrittura del file.", e);
            }
        } finally {
            // Elimino il file temporaneo se è rimasto sul disco
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Carica l'archivio dal file.
     * Applica un filtro per bloccare eventuali classi sospette o non previste nel file.
     *
     * @param path il percorso del file da leggere
     * @return l'archivio caricato
     * @throws LibraryException se il file non c'è, è corrotto o impossibile da leggere
     */
    @Override
    public Archive load(Path path) throws LibraryException {
        try (InputStream input = Files.newInputStream(path);
             ObjectInputStream objInput = new ObjectInputStream(input)) {

            // Filtro di sicurezza per la deserializzazione: ammette solo classi del model e java base
            ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                    "it.unisa.mylibrary.model.*;java.lang.*;java.util.*;java.time.*;!*"
            );;
            objInput.setObjectInputFilter(filter);

            return (Archive) objInput.readObject();
        } catch (EOFException e) {
            throw new LibraryException("Impossibile caricare: il file di salvataggio è vuoto o incompleto.", e);
        } catch (StreamCorruptedException e) {
            throw new LibraryException("Impossibile caricare: il formato del file non è valido o i dati sono corrotti.", e);
        } catch (InvalidClassException e) {
            throw new LibraryException("Impossibile caricare: la versione del file di salvataggio è incompatibile o corrotta.", e);
        } catch (NoSuchFileException | FileNotFoundException e) {
            throw new LibraryException("Impossibile caricare: il file di salvataggio specificato non è stato trovato.", e);
        } catch (IOException e) {
            throw new LibraryException("Impossibile caricare: si è verificato un errore di lettura del file.", e);
        } catch (ClassNotFoundException e) {
            throw new LibraryException("Impossibile caricare: il file di salvataggio è incompatibile con questa versione dell'applicazione.", e);
        } catch (ClassCastException e) {
            throw new LibraryException("Impossibile caricare: il file non contiene un archivio valido.", e);
        } catch (Exception e) {
            throw new LibraryException("Errore imprevisto durante il caricamento dell'archivio: " + e.getMessage(), e);
        }
    }
}