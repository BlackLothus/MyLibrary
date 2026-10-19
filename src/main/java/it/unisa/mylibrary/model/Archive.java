package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contenitore dello stato globale della biblioteca (libri, lettori e prestiti).
 * Viene serializzato interamente per gestire il salvataggio su file (RF26, RF27, RNF03).
 */
public class Archive implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, Reader> readers = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    public Map<String, Book> getBooks() {
        return books; //i libri nel sistema, li metto per ISBN
    }

    public Map<String, Reader> getReaders() {
        return readers; //lettori nel sistema, li metto per matricola
    }

    public List<Loan> getLoans() {
        return loans; //i prestiti nel sistema
    }

    /**
     * Sostituisce i dati correnti con quelli di un altro archivio senza cambiare
     * le istanze delle collezioni, così da non perdere i riferimenti nei Service.
     *
     * @param altroArchivio l'archivio da cui copiare il nuovo stato
     */
    public void replaceContentWith(Archive altroArchivio) {
        if (altroArchivio == this) {
            return;
        } //prendo la mappa e la rimpiazzo semplicemente
        Map<String,Book> newBooks = new HashMap<>(altroArchivio.books);
        Map<String, Reader> newReaders = new HashMap<>(altroArchivio.readers);
        List<Loan> newLoans = new ArrayList<>(altroArchivio.loans);

        books.clear();
        readers.clear();
        loans.clear();

        books.putAll(newBooks);
        readers.putAll(newReaders);
        loans.addAll(newLoans);
    }
}