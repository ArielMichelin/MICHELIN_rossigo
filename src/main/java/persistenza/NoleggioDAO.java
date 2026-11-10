package persistenza;

import eccezioni.ErroreDatabaseException;
import modello.Noleggio;
import modello.Utente;
import modello.Veicolo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Accesso alla tabella "noleggi" (singleton).
 * Apertura e chiusura di un noleggio toccano più tabelle: per questo
 * sono eseguite in un'unica transazione.
 */
public final class NoleggioDAO {

    private static NoleggioDAO istanza;

    private final Database db;
    private final VeicoloDAO veicoloDAO;
    private final UtenteDAO utenteDAO;

    private NoleggioDAO() throws ErroreDatabaseException {
        this.db = Database.getInstance();
        this.veicoloDAO = VeicoloDAO.getInstance();
        this.utenteDAO = UtenteDAO.getInstance();
    }

    public static synchronized NoleggioDAO getInstance() throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new NoleggioDAO();
        }
        return istanza;
    }

    /** Inserisce il noleggio e segna il veicolo come IN_USO, tutto insieme. */
    public void registraApertura(Noleggio n) throws ErroreDatabaseException {
        String sql = "INSERT INTO noleggi (id_utente, codice_veicolo, inizio) VALUES (?, ?, ?)";
        try {
            db.inTransazione(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, n.getUtente().getId());
                    ps.setString(2, n.getVeicolo().getCodice());
                    ps.setString(3, n.getInizio().toString());
                    ps.executeUpdate();
                    try (ResultSet chiavi = ps.getGeneratedKeys()) {
                        if (chiavi.next()) {
                            n.setId(chiavi.getLong(1));
                        }
                    }
                }
                veicoloDAO.aggiorna(conn, n.getVeicolo());
            });
        } catch (SQLException e) {
            throw Database.errore("registrazione del noleggio", e);
        }
    }

    /** Chiude il noleggio, aggiorna batteria/stato del veicolo e credito dell'utente, tutto insieme. */
    public void registraChiusura(Noleggio n) throws ErroreDatabaseException {
        String sql = "UPDATE noleggi SET fine = ?, costo = ? WHERE id = ?";
        try {
            db.inTransazione(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, n.getFine().toString());
                    ps.setDouble(2, n.getCosto());
                    ps.setLong(3, n.getId());
                    ps.executeUpdate();
                }
                veicoloDAO.aggiorna(conn, n.getVeicolo());
                utenteDAO.aggiornaCredito(conn, n.getUtente());
            });
        } catch (SQLException e) {
            throw Database.errore("chiusura del noleggio", e);
        }
    }

    /** Ricostruisce i noleggi ancora aperti collegandoli agli oggetti già caricati. */
    public List<Noleggio> trovaAttivi(Map<String, Utente> utenti, Map<String, Veicolo> veicoli)
            throws ErroreDatabaseException {
        List<Noleggio> attivi = new ArrayList<>();
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, id_utente, codice_veicolo, inizio FROM noleggi WHERE fine IS NULL");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                attivi.add(new Noleggio(
                        rs.getLong("id"),
                        utenti.get(rs.getString("id_utente")),
                        veicoli.get(rs.getString("codice_veicolo")),
                        LocalDateTime.parse(rs.getString("inizio")),
                        null, 0));
            }
        } catch (SQLException e) {
            throw Database.errore("lettura dei noleggi attivi", e);
        }
        return attivi;
    }

    /** Numero totale di noleggi conclusi salvati: cresce a ogni esecuzione. */
    public int contaConclusi() throws ErroreDatabaseException {
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM noleggi WHERE fine IS NOT NULL");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw Database.errore("conteggio dei noleggi", e);
        }
    }
}
