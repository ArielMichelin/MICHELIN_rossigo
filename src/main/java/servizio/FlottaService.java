package servizio;

import eccezioni.BatteriaScaricaException;
import eccezioni.CodiceFiscaleNonValidoException;
import eccezioni.CreditoInsufficienteException;
import eccezioni.DatiNonValidiException;
import eccezioni.ErroreDatabaseException;
import eccezioni.NoleggioNonAttivoException;
import eccezioni.RossiGoException;
import eccezioni.UtenteNonTrovatoException;
import eccezioni.VeicoloNonDisponibileException;
import eccezioni.VeicoloNonTrovatoException;
import it.kamaladafrica.codicefiscale.CodiceFiscale;
import modello.Noleggio;
import modello.StatoVeicolo;
import modello.StimaCosto;
import modello.Utente;
import modello.Veicolo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistenza.NoleggioDAO;
import persistenza.UtenteDAO;
import persistenza.VeicoloDAO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Regole di business di RossiGo. Lavora SEMPRE con il tipo astratto Veicolo:
 * non sa (e non deve sapere) se un mezzo è una Bici o un Monopattino.
 * Ogni modifica di stato viene salvata tramite i DAO del package persistenza.
 */
public class FlottaService {

    public static final double COSTO_SBLOCCO = 1.00;     // €
    public static final double SOGLIA_CREDITO = 1.00;    // € minimi per sbloccare
    public static final double SOGLIA_BATTERIA = 15.0;   // % minima per sbloccare

    private static final Logger log = LoggerFactory.getLogger(FlottaService.class);

    private final VeicoloDAO veicoloDAO;
    private final UtenteDAO utenteDAO;
    private final NoleggioDAO noleggioDAO;

    private final Map<String, Veicolo> veicoli = new LinkedHashMap<>();
    private final Map<String, Utente> utenti = new LinkedHashMap<>();
    private final Map<String, Noleggio> noleggiAttivi = new LinkedHashMap<>();   // chiave: codice veicolo

    /** Crea il servizio ricaricando dal database flotta, utenti e noleggi in corso. */
    public FlottaService() throws ErroreDatabaseException {
        this.veicoloDAO = VeicoloDAO.getInstance();
        this.utenteDAO = UtenteDAO.getInstance();
        this.noleggioDAO = NoleggioDAO.getInstance();

        veicoloDAO.trovaTutti().forEach(v -> veicoli.put(v.getCodice(), v));
        utenteDAO.trovaTutti().forEach(u -> utenti.put(u.getId(), u));
        noleggioDAO.trovaAttivi(utenti, veicoli).forEach(n -> noleggiAttivi.put(n.getVeicolo().getCodice(), n));
        log.info("Dati caricati: {} veicoli, {} utenti, {} noleggi attivi",
                veicoli.size(), utenti.size(), noleggiAttivi.size());
    }

    // ===================================================================== registrazioni

    public void registraVeicolo(Veicolo v) throws DatiNonValidiException, ErroreDatabaseException {
        if (v == null) {
            throw errore(new DatiNonValidiException("Nessun veicolo da registrare.", "veicolo"));
        }
        if (veicoli.containsKey(v.getCodice())) {
            throw errore(new DatiNonValidiException(
                    "Esiste già un mezzo con codice " + v.getCodice() + ".", "codice"));
        }
        veicoloDAO.inserisci(v);
        veicoli.put(v.getCodice(), v);
        log.info("Registrato veicolo {} ({})", v.getCodice(), v.descrizione());
    }

    public void registraUtente(Utente u)
            throws CodiceFiscaleNonValidoException, DatiNonValidiException, ErroreDatabaseException {
        if (u == null) {
            throw errore(new DatiNonValidiException("Nessun utente da registrare.", "utente"));
        }
        if (StringUtils.isBlank(u.getId())) {
            throw errore(new DatiNonValidiException("L'identificativo utente è obbligatorio.", "id"));
        }
        if (StringUtils.isBlank(u.getNome())) {
            throw errore(new DatiNonValidiException("Inserisci il tuo nome per completare la registrazione.", "nome"));
        }
        if (u.getCredito() < 0) {
            throw errore(new DatiNonValidiException("Il credito iniziale non può essere negativo.", "credito"));
        }
        validaCodiceFiscale(u.getCodiceFiscale());
        if (utenti.containsKey(u.getId())) {
            throw errore(new DatiNonValidiException("Esiste già un utente con id " + u.getId() + ".", "id"));
        }
        boolean cfGiaUsato = utenti.values().stream()
                .anyMatch(altro -> altro.getCodiceFiscale().equalsIgnoreCase(u.getCodiceFiscale()));
        if (cfGiaUsato) {
            throw errore(new DatiNonValidiException(
                    "Questo codice fiscale è già associato a un altro account.", "codiceFiscale"));
        }
        utenteDAO.inserisci(u);
        utenti.put(u.getId(), u);
        log.info("Registrato utente {} ({})", u.getId(), u.getNome());
    }

    /** Usa la libreria codice-fiscale: controlla formato, data, comune e carattere di controllo. */
    private void validaCodiceFiscale(String cf) throws CodiceFiscaleNonValidoException {
        if (StringUtils.isBlank(cf)) {
            throw errore(new CodiceFiscaleNonValidoException("Il codice fiscale è obbligatorio.", cf));
        }
        try {
            CodiceFiscale.of(cf.trim().toUpperCase(Locale.ITALY));
        } catch (IllegalArgumentException e) {
            throw errore(new CodiceFiscaleNonValidoException(
                    "Il codice fiscale '" + cf + "' non è valido: controlla di averlo scritto correttamente.", cf, e));
        }
    }

    // ===================================================================== noleggi

    public Noleggio sbloccaVeicolo(String codice, String idUtente)
            throws UtenteNonTrovatoException, VeicoloNonTrovatoException, VeicoloNonDisponibileException,
                   BatteriaScaricaException, CreditoInsufficienteException, ErroreDatabaseException {
        Utente utente = trovaUtente(idUtente);
        Veicolo veicolo = trovaVeicolo(codice);

        if (!veicolo.isLibero()) {
            String motivo = veicolo.getStato() == StatoVeicolo.IN_USO ? "è già in uso" : "è in ricarica";
            throw errore(new VeicoloNonDisponibileException(
                    "Il mezzo " + codice + " " + motivo + ". Scegline un altro dalla mappa.",
                    codice, veicolo.getStato().name()));
        }
        if (veicolo.getLivelloBatteria() < SOGLIA_BATTERIA) {
            throw errore(new BatteriaScaricaException(String.format(Locale.ITALY,
                    "Batteria al %.0f%%, scegli un altro mezzo.", veicolo.getLivelloBatteria()),
                    veicolo.getLivelloBatteria()));
        }
        if (utente.getCredito() < SOGLIA_CREDITO) {
            throw errore(new CreditoInsufficienteException(String.format(Locale.ITALY,
                    "Credito insufficiente: hai %s, per sbloccare servono almeno %s. Ricarica il tuo account.",
                    euro(utente.getCredito()), euro(SOGLIA_CREDITO)),
                    utente.getCredito(), SOGLIA_CREDITO));
        }

        veicolo.sblocca();
        Noleggio noleggio = new Noleggio(utente, veicolo, LocalDateTime.now().withSecond(0).withNano(0));
        noleggioDAO.registraApertura(noleggio);
        noleggiAttivi.put(codice, noleggio);
        log.info("SBLOCCO {} da parte di {} (batteria {}%)", codice, idUtente, veicolo.getLivelloBatteria());
        return noleggio;
    }

    public Noleggio terminaNoleggio(String codice, int minutiUsati)
            throws VeicoloNonTrovatoException, NoleggioNonAttivoException, DatiNonValidiException,
                   ErroreDatabaseException {
        Veicolo veicolo = trovaVeicolo(codice);
        Noleggio noleggio = noleggiAttivi.get(codice);
        if (noleggio == null) {
            throw errore(new NoleggioNonAttivoException(
                    "Il mezzo " + codice + " non risulta noleggiato: non c'è nulla da terminare.", codice));
        }
        if (minutiUsati < 0) {
            throw errore(new DatiNonValidiException("La durata non può essere negativa.", "minuti"));
        }

        // POLIMORFISMO: calcolaCosto e consumoBatteriaPerMinuto cambiano con il tipo reale del mezzo,
        // senza nessun if/instanceof qui dentro.
        double costo = COSTO_SBLOCCO + veicolo.calcolaCosto(minutiUsati);
        noleggio.getUtente().addebita(costo);
        veicolo.consumaBatteria(minutiUsati);
        veicolo.libera();
        noleggio.chiudi(noleggio.getInizio().plusMinutes(minutiUsati), costo);

        noleggioDAO.registraChiusura(noleggio);
        noleggiAttivi.remove(codice);
        log.info("FINE NOLEGGIO {}: {} min, {} addebitati a {}", codice, minutiUsati, euro(costo),
                noleggio.getUtente().getId());
        return noleggio;
    }

    // ===================================================================== ricarica

    public void mettiInRicarica(String codice)
            throws VeicoloNonTrovatoException, VeicoloNonDisponibileException, ErroreDatabaseException {
        Veicolo veicolo = trovaVeicolo(codice);
        if (veicolo.getStato() == StatoVeicolo.IN_USO) {
            throw errore(new VeicoloNonDisponibileException(
                    "Il mezzo " + codice + " è in uso: potrà essere ricaricato a fine corsa.",
                    codice, veicolo.getStato().name()));
        }
        veicolo.mettiInRicarica();
        veicoloDAO.aggiorna(veicolo);
        log.info("RICARICA avviata per {} (batteria {}%)", codice, veicolo.getLivelloBatteria());
    }

    public void completaRicarica(String codice)
            throws VeicoloNonTrovatoException, DatiNonValidiException, ErroreDatabaseException {
        Veicolo veicolo = trovaVeicolo(codice);
        if (veicolo.getStato() != StatoVeicolo.IN_RICARICA) {
            throw errore(new DatiNonValidiException("Il mezzo " + codice + " non è in ricarica.", "stato"));
        }
        veicolo.completaRicarica();
        veicoloDAO.aggiorna(veicolo);
        log.info("RICARICA completata per {}: di nuovo disponibile al 100%", codice);
    }

    public void ricaricaCredito(String idUtente, double importo)
            throws UtenteNonTrovatoException, DatiNonValidiException, ErroreDatabaseException {
        Utente utente = trovaUtente(idUtente);
        if (importo <= 0) {
            throw errore(new DatiNonValidiException("L'importo della ricarica deve essere positivo.", "importo"));
        }
        utente.ricarica(importo);
        utenteDAO.aggiornaCredito(utente);
        log.info("RICARICA CREDITO {} per {}: nuovo saldo {}", euro(importo), idUtente, euro(utente.getCredito()));
    }

    // ===================================================================== bonus: stima

    /**
     * Stima il costo di un viaggio e verifica che credito e batteria bastino per arrivare.
     * Non modifica nulla.
     */
    public StimaCosto stimaCosto(String codice, int minuti, String idUtente)
            throws VeicoloNonTrovatoException, UtenteNonTrovatoException, DatiNonValidiException,
                   CreditoInsufficienteException, BatteriaScaricaException {
        Veicolo veicolo = trovaVeicolo(codice);
        Utente utente = trovaUtente(idUtente);
        if (minuti <= 0) {
            throw errore(new DatiNonValidiException("Indica una durata del viaggio maggiore di zero.", "minuti"));
        }

        double costo = COSTO_SBLOCCO + veicolo.calcolaCosto(minuti);
        if (utente.getCredito() < costo) {
            throw errore(new CreditoInsufficienteException(String.format(Locale.ITALY,
                    "Per %d minuti con %s servono circa %s, ma hai %s: ricarica almeno %s per arrivare a destinazione.",
                    minuti, codice, euro(costo), euro(utente.getCredito()), euro(costo - utente.getCredito())),
                    utente.getCredito(), costo));
        }
        double batteriaFinale = veicolo.getLivelloBatteria() - minuti * veicolo.consumoBatteriaPerMinuto();
        if (batteriaFinale < 0) {
            throw errore(new BatteriaScaricaException(String.format(Locale.ITALY,
                    "Con la batteria al %.0f%% il mezzo %s regge circa %d minuti: per %d minuti scegline un altro.",
                    veicolo.getLivelloBatteria(), codice, veicolo.autonomiaMinuti(), minuti),
                    veicolo.getLivelloBatteria()));
        }
        return new StimaCosto(codice, minuti, costo, utente.getCredito() - costo, batteriaFinale);
    }

    // ===================================================================== consultazione

    public Utente trovaUtente(String idUtente) throws UtenteNonTrovatoException {
        Utente u = utenti.get(idUtente);
        if (u == null) {
            throw errore(new UtenteNonTrovatoException(
                    "Non troviamo nessun account con id '" + idUtente + "'. Controlla di aver effettuato l'accesso.",
                    idUtente));
        }
        return u;
    }

    public Veicolo trovaVeicolo(String codice) throws VeicoloNonTrovatoException {
        Veicolo v = veicoli.get(codice);
        if (v == null) {
            throw errore(new VeicoloNonTrovatoException(
                    "Nessun mezzo con codice '" + codice + "'. Controlla il codice sul manubrio.", codice));
        }
        return v;
    }

    public Collection<Veicolo> getVeicoli() {
        return Collections.unmodifiableCollection(veicoli.values());
    }

    public Collection<Utente> getUtenti() {
        return Collections.unmodifiableCollection(utenti.values());
    }

    public Collection<Noleggio> getNoleggiAttivi() {
        return Collections.unmodifiableCollection(noleggiAttivi.values());
    }

    public int contaNoleggiConclusi() throws ErroreDatabaseException {
        return noleggioDAO.contaConclusi();
    }

    public boolean isVuoto() {
        return veicoli.isEmpty() && utenti.isEmpty();
    }

    // ===================================================================== utilità

    /** Registra nel log ogni errore di business prima di lanciarlo. */
    private static <E extends RossiGoException> E errore(E e) {
        log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        return e;
    }

    public static String euro(double importo) {
        return String.format(Locale.ITALY, "%.2f €", importo);
    }
}
