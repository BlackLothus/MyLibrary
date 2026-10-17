package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void replaceContentWith(Archive altroArchivio) {
        if (altroArchivio == this) {
            return;
        } //prendo la mappa e la rimpazzo semplicemente
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