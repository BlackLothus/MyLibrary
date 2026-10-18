package it.unisa.mylibrary.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {

    @Test
    void testCostruttoreEGetters() {
        Reader lettore = new Reader("Browser", "Kuppa", "0612701", "b.kuppa@studenti.unisa.it");

        assertEquals("Browser", lettore.getFirstName());
        assertEquals("Kuppa", lettore.getLastName());
        assertEquals("0612701", lettore.getStudentId());
        assertEquals("b.kuppa@studenti.unisa.it", lettore.getEmail());
    }

    @Test
    void testSetters() {
        Reader lettore = new Reader("Vecchio", "Nome", "061", "vecchia@studenti.unisa.it");

        // Simulo un aggiornamento anagrafico (la matricola è final e non ha setter)
        lettore.setFirstName("Wario");
        lettore.setLastName("boh");
        lettore.setEmail("nuova@studenti.unisa.it");

        assertEquals("Wario", lettore.getFirstName());
        assertEquals("boh", lettore.getLastName());
        assertEquals("nuova@studenti.unisa.it", lettore.getEmail());
    }

    @Test
    void testEqualsEHashCodeBasatiSuMatricola() {
        Reader lettore1 = new Reader("Mario", "Bros", "061", "a@unisa.it");
        Reader lettore2 = new Reader("Luigi", "Bros", "061", "b@unisa.it"); // Stessa matricola, dati diversi
        Reader lettoreDiverso = new Reader("Mario", "Bros", "062", "a@unisa.it");

        // L'identità di un lettore nel sistema è determinata unicamente dalla sua matricola
        assertEquals(lettore1, lettore2);
        assertEquals(lettore1.hashCode(), lettore2.hashCode());

        assertNotEquals(lettore1, lettoreDiverso);
        assertNotEquals(null, lettore1);
        assertNotEquals("061", lettore1);
    }

    @Test
    void testToStringDatiChiave() {
        Reader lettore = new Reader("Ciccio", "Esposito", "0612701", "c.esposito@studenti.unisa.it");
        String risultato = lettore.toString();

        //presenza delle informazioni essenziali a prescindere da spazi o etichette
        assertTrue(risultato.contains("Ciccio"));
        assertTrue(risultato.contains("Esposito"));
        assertTrue(risultato.contains("0612701"));
    }
}