package it.unisa.mylibrary.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String isbn;
    private String title;
    private List<String> authors;
    private int year;
    private int totalCopies;
    //solo per versione minima MVP

    public Book(String isbn, String title, List<String> authors, int year, int totalCopies) {
        this.isbn = isbn; // La normalizzazione verrà aggiunta in fase di implementazione logica (ossia togliere trattini e x finale se isbn10)
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.totalCopies = totalCopies;
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
        return authors;
    }
    public void setAuthors(List<String> authors) {
        this.authors = authors;
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
            return libro.isbn ==null; //evito NullPointerException ISBN non sia ancora inizializzato
        }
        return this.isbn.equals(libro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
    // non usare gli alberi che sennò non puoi ordinare come vuoi in modo semplice
}