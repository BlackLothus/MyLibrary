package it.unisa.mylibrary.model;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate DUE = LocalDate.of(2026, 10, 15);

    // Helper per creare al volo un prestito base
    private Loan creaPrestito() {
        return new Loan("0612700001", "9788800000001", START, DUE);
    }

    // [ED01a] Dati obbligatori Prestito
    @Test
    void testCreazioneNuovoPrestito() {
        Loan prestito = creaPrestito();

        // Appena creato deve essere attivo,avere un ID univoco e nessuna data di ritorno
        assertTrue(prestito.isActive());
        assertNull(prestito.getReturnDate());
        assertNotNull(prestito.getId());

        // Verifica l'univocità dell'ID
        assertNotEquals(prestito.getId(), creaPrestito().getId());
    }

    @Test
    void testVerificaRitardo() {
        Loan prestito = creaPrestito();

        // È in ritardo solo il giorno DOPO la scadenza
        assertTrue(prestito.isOverdue(DUE.plusDays(1)));

        // Non è in ritardo il giorno della scadenza o prima
        assertFalse(prestito.isOverdue(DUE));
        assertFalse(prestito.isOverdue(DUE.minusDays(1)));
    }

    @Test
    void testChiusuraPrestito() {
        Loan prestito = creaPrestito();
        LocalDate dataRestituzione = LocalDate.of(2026, 10, 10);

        prestito.markReturned(dataRestituzione);

        // Un prestito restituito non è più attivo e non risulta mai in ritardo
        assertFalse(prestito.isActive());
        assertEquals(dataRestituzione, prestito.getReturnDate());
        assertFalse(prestito.isOverdue(DUE.plusDays(10)));
    }

    @Test
    void testEqualsEHashCode() {
        Loan prestito1 = creaPrestito();
        Loan prestito2 = creaPrestito(); // Stessi dati, ma ID diverso generato

        assertTrue(prestito1.equals(prestito1));
        assertEquals(prestito1.hashCode(), prestito1.hashCode());

        // Non sono uguali perché l'identità si basa sull'UUID interno
        assertFalse(prestito1.equals(prestito2));

        assertFalse(prestito1.equals(null));
        assertFalse(prestito1.equals("Stringa"));
    }

    @Test
    void testToStringDatiChiave() {
        Loan prestito = creaPrestito();
        String risultato = prestito.toString();

        // Verifichiamo solo che i dati identificativi siano presenti nella stringa
        assertTrue(risultato.contains(prestito.getId()));
        assertTrue(risultato.contains("0612700001"));
        assertTrue(risultato.contains("9788800000001"));
    }
}