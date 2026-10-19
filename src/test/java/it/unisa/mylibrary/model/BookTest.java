package it.unisa.mylibrary.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testAutoriSolaLettura() {
        Book libro = new Book("111", "Titolo", Arrays.asList("Autore"), 2020, 1);
        assertThrows(UnsupportedOperationException.class, () -> libro.getAuthors().add("Intruso"));
    }

    @Test
    void testNormalizzazioneIsbnValido() {
        assertEquals("885417238X", Book.normalizeIsbn(" 88-541-7238-x"));
    }

    @Test
    void testNormalizzazioneIsbnNull() {
        assertNull(Book.normalizeIsbn(null));
    }

    @Test
    void testCostruttoreInizializzaCorrettamente() {
        List<String> autori = new ArrayList<>(Arrays.asList("Mario Rossi", "Luca Bianchi"));
        Book libro = new Book("978-88-00", "Manuale Java", autori, 2023, 5);

        // Verifica che l'ISBN venga normalizzato già nel costruttore
        assertEquals("9788800", libro.getIsbn());
        assertEquals("Manuale Java", libro.getTitle());
        assertEquals(2, libro.getAuthors().size());
        assertEquals(2023, libro.getYear());
        assertEquals(5, libro.getTotalCopies());
    }

    @Test
    void testCostruttoreConAutoriNull() {
        // Se passo null, mi aspetto una lista vuota e non una NullPointerException in futuro
        Book libro = new Book("111", "Titolo a caso", null, 2020, 1);

        assertNotNull(libro.getAuthors());
        assertTrue(libro.getAuthors().isEmpty());
    }

    @Test
    void testSettersEGetters() {
        Book libro = new Book("111", "Vecchio Titolo", Arrays.asList("Vecchio Autore"), 2020, 1);

        libro.setTitle("Nuovo Titolo");
        libro.setYear(2021);
        libro.setTotalCopies(10);
        libro.setAuthors(Arrays.asList("Nuovo Autore"));

        assertEquals("Nuovo Titolo", libro.getTitle());
        assertEquals(2021, libro.getYear());
        assertEquals(10, libro.getTotalCopies());
        assertEquals("Nuovo Autore", libro.getAuthors().get(0));
    }

    @Test
    void testSetAutoriNull() {
        Book libro = new Book("111", "Titolo", Arrays.asList("Autore"), 2020, 1);
        libro.setAuthors(null);

        assertNotNull(libro.getAuthors());
        assertTrue(libro.getAuthors().isEmpty());
    }

    @Test
    void testEqualsConStessoIsbn() {
        Book libro1 = new Book("123", "Java 1", Arrays.asList("A"), 2020, 1);
        Book libro2 = new Book("123", "Java 2", Arrays.asList("B"), 2021, 2);

        // Libri con stesso ISBN sono lo stesso libro per il nostro dominio
        assertEquals(libro1, libro2);
    }

    @Test
    void testEqualsConIsbnDiverso() {
        Book libro1 = new Book("123", "Java", Arrays.asList("A"), 2020, 1);
        Book libro2 = new Book("456", "Java", Arrays.asList("A"), 2020, 1);

        assertNotEquals(libro1, libro2);
    }

    @Test
    void testEqualsConOggettiDiversi() {
        Book libro = new Book("123", "Java", Arrays.asList("A"), 2020, 1);

        assertNotEquals(null, libro);
        assertNotEquals("123", libro);
    }

    @Test
    void testHashCode() {
        Book libro1 = new Book("123", "Java 1", Arrays.asList("A"), 2020, 1);
        Book libro2 = new Book("123", "Java 2", Arrays.asList("B"), 2021, 2);

        assertEquals(libro1.hashCode(), libro2.hashCode());
    }
}