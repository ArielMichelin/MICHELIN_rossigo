package app;

import eccezioni.BatteriaScaricaException;
import eccezioni.CodiceFiscaleNonValidoException;
import eccezioni.CreditoInsufficienteException;
import eccezioni.DatiNonValidiException;
import eccezioni.ErroreDatabaseException;
import eccezioni.RossiGoException;
import eccezioni.UtenteNonTrovatoException;
import eccezioni.VeicoloNonDisponibileException;
import modello.Bici;
import modello.Monopattino;
import modello.Noleggio;
import modello.Utente;
import modello.Veicolo;
import servizio.FlottaService;

import java.util.List;
import java.util.Locale;

import static servizio.FlottaService.euro;

/**
 * Una giornata in città: scenari che vanno a buon fine e scenari che generano eccezioni.
 * Ogni scenario gestisce i propri errori con try/catch e stampa solo messaggi per l'utente.
 */
class SimulazioneGiornata {

    /** Un passo della simulazione che può fallire con un errore di RossiGo. */
    @FunctionalInterface
    private interface Azione {
        void esegui() throws RossiGoException;
    }

    private final FlottaService flotta;
    private int numeroScenario = 0;

    SimulazioneGiornata(FlottaService flotta) {
        this.flotta = flotta;
    }

    void esegui() throws RossiGoException {
        Console.sezione("Mattina");
        scenario("Apertura: dati nuovi o recuperati da rossigo.db?", this::apertura);
        scenario("Registrazione con codice fiscale sbagliato", this::registrazioneConCodiceFiscaleErrato);
        scenario("Mario sblocca il monopattino SC-0107", this::marioSbloccaMonopattino);
        scenario("Luca prova a prendere SC-0107, che è già in uso", this::lucaProvaMezzoInUso);
        scenario("Giulia, con 0,50 euro di credito, prova a sbloccare SC-0108", this::giuliaSenzaCredito);
        scenario("Luca prova la bici BK-0043 con la batteria quasi a zero", this::lucaBatteriaScarica);
        scenario("Luca ripiega sulla bici BK-0042", this::lucaPrendeLaBici);
        scenario("Un utente sconosciuto (U999) prova a sbloccare BK-0044", this::utenteSconosciuto);

        Console.sezione("Pomeriggio");
        scenario("Fine delle corse: stesso calcolo, prezzi diversi", this::fineCorse);
        scenario("Listino polimorfico: quanto costa 30 minuti su ogni mezzo?", this::listino);
        scenario("Mario ricarica il credito", this::marioRicaricaCredito);
        scenario("Bonus - stima del costo prima di partire", this::stime);

        Console.sezione("Sera");
        scenario("Ricarica notturna: Luca prova a prendere un mezzo in carica", this::ricaricaNotturna);
        scenario("Riavvio: i dati sopravvivono?", this::riavvio);
        scenario("Stato finale salvato nel database", () -> StatoCommand.stampa(flotta));
    }

    /**
     * Esegue uno scenario: le eccezioni attese sono gestite dentro lo scenario stesso;
     * qui arriva solo ciò che non era previsto, comunque mostrato in modo leggibile.
     */
    private void scenario(String titolo, Azione azione) {
        Console.scenario(++numeroScenario, titolo);
        try {
            azione.esegui();
        } catch (RossiGoException e) {
            Console.errore(e.getMessage());
        }
        Console.vuota();
    }

    // ------------------------------------------------------------------ MATTINA

    private void apertura() throws RossiGoException {
        if (flotta.isVuoto()) {
            Console.info("Primo avvio: il database è vuoto, registro la flotta e i clienti iniziali.");
            popolaDatiIniziali();
            Console.ok(flotta.getVeicoli().size() + " mezzi e " + flotta.getUtenti().size()
                    + " utenti salvati in rossigo.db");
        } else {
            Utente mario = flotta.trovaUtente("U001");
            Console.ok("Bentornati! Dati ritrovati in rossigo.db: " + flotta.getVeicoli().size() + " mezzi, "
                    + flotta.getUtenti().size() + " utenti, " + flotta.contaNoleggiConclusi() + " noleggi in archivio.");
            Console.info("Credito di Mario recuperato dall'esecuzione precedente: " + euro(mario.getCredito()));
        }
    }

    private void popolaDatiIniziali() throws RossiGoException {
        List<Veicolo> mezzi = List.of(
                new Bici("BK-0042", 7, 80),
                new Bici("BK-0043", 3, 9),
                new Bici("BK-0044", 21, 100),
                new Monopattino("SC-0107", 25, 90),
                new Monopattino("SC-0108", 20, 60),
                new Monopattino("SC-0109", 6, 100));
        for (Veicolo v : mezzi) {
            flotta.registraVeicolo(v);          // il servizio riceve un Veicolo, non sa se è bici o monopattino
        }
        flotta.registraUtente(new Utente("U001", "Mario Rossi", "mario.rossi@example.com", "RSSMRA80A01H501U", 20.00));
        flotta.registraUtente(new Utente("U002", "Giulia Bianchi", "giulia.bianchi@example.com", "BNCGLI95C52F205W", 0.50));
        flotta.registraUtente(new Utente("U003", "Luca Verdi", "luca.verdi@example.com", "VRDLCU01L23L219T", 100.00));
    }

    private void registrazioneConCodiceFiscaleErrato() throws RossiGoException {
        // l'ultimo carattere (di controllo) è sbagliato: X invece di W
        Utente paoloSbagliato = new Utente("U004", "Paolo Neri", "paolo.neri@example.com", "NREPLA90E04F839X", 15.00);
        try {
            flotta.registraUtente(paoloSbagliato);
            Console.ok("Registrato (non doveva succedere!)");
        } catch (CodiceFiscaleNonValidoException e) {
            Console.errore(e.getMessage());
            Console.suggerimento("L'app evidenzia il campo con il valore '" + e.getCodiceFornito() + "'");
        }

        Console.info("Paolo corregge il codice fiscale e riprova...");
        try {
            flotta.registraUtente(new Utente("U004", "Paolo Neri", "paolo.neri@example.com", "NREPLA90E04F839W", 15.00));
            Console.ok("Benvenuto Paolo! Account U004 creato con " + euro(15));
        } catch (DatiNonValidiException e) {
            // succede dalla seconda esecuzione in poi: Paolo è già nel database
            Console.errore(e.getMessage());
            Console.suggerimento("Paolo era già registrato in un'esecuzione precedente: i dati sono persistenti");
        }
    }

    private void marioSbloccaMonopattino() throws RossiGoException {
        Noleggio n = flotta.sbloccaVeicolo("SC-0107", "U001");
        Console.ok("Sbloccato! " + n.getVeicolo().descrizione() + ", noleggio n. " + n.getId());
    }

    private void lucaProvaMezzoInUso() throws RossiGoException {
        try {
            flotta.sbloccaVeicolo("SC-0107", "U003");
        } catch (VeicoloNonDisponibileException e) {
            Console.errore(e.getMessage());
            Console.suggerimento("Stato ricevuto dall'app: " + e.getStatoAttuale() + " -> icona grigia sulla mappa");
        }
    }

    private void giuliaSenzaCredito() throws RossiGoException {
        try {
            flotta.sbloccaVeicolo("SC-0108", "U002");
        } catch (CreditoInsufficienteException e) {
            Console.errore(e.getMessage());
            Console.suggerimento("Pulsante proposto: \"Ricarica " + euro(e.getImportoMancante()) + " e parti\"");
        }
    }

    private void lucaBatteriaScarica() throws RossiGoException {
        try {
            flotta.sbloccaVeicolo("BK-0043", "U003");
        } catch (BatteriaScaricaException e) {
            Console.errore(e.getMessage());
            Console.suggerimento(String.format(Locale.ITALY,
                    "L'app mostra la barra della batteria al %.0f%% in rosso", e.getLivelloAttuale()));
        }
    }

    private void lucaPrendeLaBici() throws RossiGoException {
        Noleggio n = flotta.sbloccaVeicolo("BK-0042", "U003");
        Console.ok("Sbloccata! " + n.getVeicolo().descrizione() + ", noleggio n. " + n.getId());
    }

    private void utenteSconosciuto() throws RossiGoException {
        try {
            flotta.sbloccaVeicolo("BK-0044", "U999");
        } catch (UtenteNonTrovatoException e) {
            Console.errore(e.getMessage());
            Console.suggerimento("L'app riporta alla schermata di login (id ricevuto: " + e.getIdUtente() + ")");
        }
    }

    // ------------------------------------------------------------------ POMERIGGIO

    private void fineCorse() throws RossiGoException {
        Noleggio mario = flotta.terminaNoleggio("SC-0107", 18);
        Console.ok("Mario: " + mario + "  -> batteria ora al "
                + String.format(Locale.ITALY, "%.1f%%", mario.getVeicolo().getLivelloBatteria()));
        Noleggio luca = flotta.terminaNoleggio("BK-0042", 25);
        Console.ok("Luca:  " + luca + "  -> batteria ora al "
                + String.format(Locale.ITALY, "%.1f%%", luca.getVeicolo().getLivelloBatteria()));
        Console.info("Crediti aggiornati: Mario " + euro(mario.getUtente().getCredito())
                + ", Luca " + euro(luca.getUtente().getCredito()));
    }

    private void listino() {
        // Polimorfismo puro: stessa chiamata, risultato diverso a seconda del tipo reale.
        for (Veicolo v : flotta.getVeicoli()) {
            Console.info(String.format(Locale.ITALY, "%s  %-38s 30 min = %s  (consumo %.1f%%/min)",
                    v.getCodice(), v.descrizione(),
                    euro(FlottaService.COSTO_SBLOCCO + v.calcolaCosto(30)), v.consumoBatteriaPerMinuto()));
        }
    }

    private void marioRicaricaCredito() throws RossiGoException {
        flotta.ricaricaCredito("U001", 5.00);
        Console.ok("Mario aggiunge " + euro(5) + ": nuovo credito " + euro(flotta.trovaUtente("U001").getCredito()));
    }

    private void stime() throws RossiGoException {
        Console.info("Mario vuole fare 30 minuti con la bici BK-0044:");
        StimaCommand.stampa(flotta.stimaCosto("BK-0044", 30, "U001"));

        Console.info("Giulia vuole fare 20 minuti con il monopattino SC-0109:");
        try {
            flotta.stimaCosto("SC-0109", 20, "U002");
        } catch (CreditoInsufficienteException e) {
            Console.errore(e.getMessage());
        }

        Console.info("Luca vuole fare 100 minuti con il monopattino SC-0108:");
        try {
            flotta.stimaCosto("SC-0108", 100, "U003");
        } catch (BatteriaScaricaException e) {
            Console.errore(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ SERA

    private void ricaricaNotturna() throws RossiGoException {
        flotta.mettiInRicarica("SC-0107");
        flotta.mettiInRicarica("BK-0042");
        Console.ok("SC-0107 e BK-0042 sono in carica alla stazione di piazza Duomo");
        try {
            flotta.sbloccaVeicolo("SC-0107", "U003");
        } catch (VeicoloNonDisponibileException e) {
            Console.errore(e.getMessage());
        }
        flotta.completaRicarica("SC-0107");
        flotta.completaRicarica("BK-0042");
        Console.ok("Ricarica completata: SC-0107 e BK-0042 di nuovo al 100% e disponibili");
    }

    private void riavvio() throws ErroreDatabaseException, UtenteNonTrovatoException {
        double creditoInMemoria = flotta.trovaUtente("U001").getCredito();
        // Nuova istanza = come riavviare il programma: legge SOLO da rossigo.db, non dalla memoria.
        FlottaService dopoRiavvio = new FlottaService();
        double creditoDalDatabase = dopoRiavvio.trovaUtente("U001").getCredito();
        Console.info("Credito di Mario prima del riavvio: " + euro(creditoInMemoria));
        Console.info("Credito di Mario riletto dal DB:    " + euro(creditoDalDatabase));
        if (creditoInMemoria == creditoDalDatabase) {
            Console.ok("I dati coincidono: tutto è stato salvato.");
        } else {
            Console.errore("Attenzione: i dati non coincidono!");
        }
        Console.info("Prova reale: chiudi il programma e rilancia 'simula' o 'stato'.");
    }
}
