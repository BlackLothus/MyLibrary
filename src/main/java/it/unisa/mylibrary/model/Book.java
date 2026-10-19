package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.util.*;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String isbn;
    private String title;
    private List<String> authors;
    private int year;
    private int totalCopies;
    //solo per versione minima MVP

    public Book(String isbn, String title, List<String> authors, int year, int totalCopies) {
        this.isbn = normalizeIsbn(isbn);
        this.title = title;
        this.authors = copyOrEmpty(authors);
        this.year = year;
        this.totalCopies = totalCopies;
    }
    private static List<String> copyOrEmpty(List<String> src) { ////disaccoppiamento totale, faccio dipendere la lista da ora solo da Book
        return src == null ? new ArrayList<>() : new ArrayList<>(src);
    }
    public static String normalizeIsbn (String rawIsbn){
        if (rawIsbn == null){
            return null;
        }
        return rawIsbn.replaceAll("[- ]","").toUpperCase(Locale.ROOT); //metto in maiuscolo la X finale che sta negli ISBN10, Locale.ROOT na finezza se ci si trova in turchia o altro, in questo caso intuile
    }
    public String getIsbn() {
        return isbn;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public List<String> getAuthors() {
        return Collections.unmodifiableList(authors); //// Vista in sola lettura invece di un semplice return authors
    }
    public void setAuthors(List<String> authors) {
        this.authors = copyOrEmpty(authors);
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getTotalCopies() {
        return totalCopies;
    }
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book)) {
            return false; // non uso getClass() che è molto più restrittivo, instanceof guarda anche la aprentela (o == null) è coperto
        }
        Book libro = (Book) o;
        if (this.isbn ==null){
            return libro.isbn ==null; //evito NullPointerException ISBN non sia ancora inizializzato, //potrei usare
        }
        return this.isbn.equals(libro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
    @Override
    public String toString(){
        return "Book isbn=" + isbn + ", title=" + title; //lascio Book così capisco subito a chi mi riferisco , tanto l'utente non vedrà mai questo
    }
}