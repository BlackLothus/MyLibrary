package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReaderServiceTest {

    private Archive archive;
    private LoanService loanService;
    private ReaderService service;

    @BeforeEach
    void setUp() {
        archive = new Archive();
        loanService = new LoanService(archive, Clock.systemDefaultZone());
        service = new ReaderService(archive, loanService);
    }

    // Helper per non inquinare il codice con costruttori ripetitivi
    private Reader creaLettore(String nome, String cognome, String matricola, String email) {
        return new Reader(nome, cognome, matricola, email);
    }

    @Test
    void testCostruttoreProteggeControNull() {
        assertThrows(LibraryException.class, () -> new ReaderService(null, loanService));
        assertThrows(LibraryException.class, () -> new ReaderService(archive, null));
    }

    @Test
    void testAggiuntaLettore_ValidazioneDatiObbligatori() {
        // [ED01a, ED04] Nessun campo anagrafico base può essere nullo o vuoto
        assertThrows(LibraryException.class, () -> service.addReader(null));
        assertThrows(LibraryException.class, () -> service.addReader(
                creaLettore("Galileo", "Galilei", null, "g.galilei@studenti.unisa.it")));
        assertThrows(LibraryException.class, () -> service.addReader(
                creaLettore(null, "Alighieri", "0612701", "d.alighieri@studenti.unisa.it")));
        assertThrows(LibraryException.class, () -> service.addReader(
                creaLettore("Leonardo", "", "0612702", "l.davinci@studenti.unisa.it")));
    }

    @Test
    void testAggiuntaLettore_ValidazioneFormatoEmail() {
        // [ED03] L'email deve rispettare il pattern di base impostato nel Service
        assertThrows(LibraryException.class, () -> service.addReader(
                creaLettore("Marco", "Polo", "0612703", null)));

        LibraryException ex = assertThrows(LibraryException.class, () -> service.addReader(
                creaLettore("Marco", "Polo", "0612703", "email-senza-chiocciola.it")));
        assertEquals("Indirizzo e-mail istituzionale non valido", ex.getMessage());
    }

    @Test
    void testAggiuntaLettore_UnicitaDellaMatricola() {
        // [RD04b] La matricola identifica univocamente l'utente
        Reader studente1 = creaLettore("Rita", "Levi-Montalcini", "0612700001", "r.levi@studenti.unisa.it");
        service.addReader(studente1);
        assertEquals(1, archive.getReaders().size());

        // Tentativo di inserire uno studente diverso, ma con matricola già esistente
        Reader studenteClonato = creaLettore("Elena", "Cornaro", "0612700001", "e.cornaro@studenti.unisa.it");
        LibraryException ex = assertThrows(LibraryException.class, () -> service.addReader(studenteClonato));
        assertTrue(ex.getMessage().contains("Esiste già un lettore con matricola"));
    }

    @Test
    void testAggiornamentoAnagraficaLettore() {
        service.addReader(creaLettore("Alessandro", "Manzoni", "061", "a.manzoni@unisa.it"));

        // Modifica valida: cambiamo la mail (es. passaggio da studente a docente)
        Reader aggiornato = creaLettore("Alessandro", "Manzoni", "061", "a.manzoni@docenti.unisa.it");
        service.updateReader(aggiornato);

        Reader inArchivio = archive.getReaders().get("061");
        assertEquals("a.manzoni@docenti.unisa.it", inArchivio.getEmail());

        // Modifica fallita: matricola non presente
        Reader sconosciuto = creaLettore("Niccolò", "Machiavelli", "999", "n.machiavelli@unisa.it");
        assertThrows(LibraryException.class, () -> service.updateReader(sconosciuto));

        // Modifica fallita: dati obbligatori rimossi durante l'update
        Reader aggiornamentoInvalido = creaLettore("", "Manzoni", "061", "a.manzoni@docenti.unisa.it");
        assertThrows(LibraryException.class, () -> service.updateReader(aggiornamentoInvalido));
    }

    @Test
    void testRimozioneLettoreBase() {
        service.addReader(creaLettore("Giordano", "Bruno", "061", "g.bruno@unisa.it"));
        service.removeReader("061");
        assertTrue(archive.getReaders().isEmpty());

        assertThrows(LibraryException.class, () -> service.removeReader(null));
        assertThrows(LibraryException.class, () -> service.removeReader("999")); // Studente inesistente
    }

    @Test
    void testRimozioneLettore_BloccataPerVincoliStorici() {
        // [RD07] È vietato eliminare l'anagrafica se ci sono pendenze o uno storico
        service.addReader(creaLettore("Enrico", "Fermi", "061", "e.fermi@unisa.it"));
        Loan prestitoAttivo = new Loan("061", "9788804668238", LocalDate.now(), LocalDate.now().plusDays(15));
        archive.getLoans().add(prestitoAttivo);

        // Blocco per prestito in corso
        assertThrows(LibraryException.class, () -> service.removeReader("061"));

        // Il prestito viene restituito...
        prestitoAttivo.markReturned(LocalDate.now());

        // ...ma l'eliminazione è comunque bloccata per mantenere l'integrità dello storico [RD15, RD07]
        assertThrows(LibraryException.class, () -> service.removeReader("061"));
        assertTrue(archive.getReaders().containsKey("061"));
    }

    @Test
    void testRicercaPuntualePerMatricola() {
        Reader r = creaLettore("Grazia", "Deledda", "061", "g.deledda@unisa.it");
        service.addReader(r);

        Optional<Reader> trovato = service.findByStudentId("061");
        assertTrue(trovato.isPresent());
        assertSame(r, trovato.get());

        assertTrue(service.findByStudentId("999").isEmpty());
        assertTrue(service.findByStudentId(null).isEmpty());
    }

    @Test
    void testListeERicercheFlessibili() {
        // [RF09, RF16] Ordinamento e ricerca parziale
        service.addReader(creaLettore("Italo", "Calvino", "06127001", "i.calvino@unisa.it"));
        service.addReader(creaLettore("Luigi", "Pirandello", "06127002", "l.pirandello@unisa.it"));
        service.addReader(creaLettore("Giovanni", "Boccaccio", "99999999", "g.boccaccio@unisa.it"));

        List<Reader> ordinati = service.getAllSortedByName();
        assertEquals(3, ordinati.size());
        assertEquals("Boccaccio", ordinati.get(0).getLastName()); // Ordinamento primario per cognome
        assertEquals("Calvino", ordinati.get(1).getLastName());

        // Se non passo argomenti, torna la lista completa
        assertEquals(3, service.search(null).size());

        // Ricerca case-insensitive
        assertEquals(1, service.search("PIRANDELLO").size());

        // Ricerca parziale su matricola
        List<Reader> ricercaMatricola = service.search("2700");
        assertEquals(2, ricercaMatricola.size());

        assertTrue(service.search("D'Annunzio").isEmpty());
    }
}