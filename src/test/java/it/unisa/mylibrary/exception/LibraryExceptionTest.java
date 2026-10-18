package it.unisa.mylibrary.exception;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class LibraryExceptionTest {

    @Test
    void testCostruttoreConMessaggio() {
        LibraryException ex = new LibraryException("Errore di test");

        assertEquals("Errore di test", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testCostruttoreConMessaggioECausa() {
        IOException causa = new IOException("Errore I/O");
        LibraryException ex = new LibraryException("Errore di salvataggio", causa);

        assertEquals("Errore di salvataggio", ex.getMessage());
        assertSame(causa, ex.getCause());
    }
}