package persistenza;

import eccezioni.ErroreDatabaseException;
import modello.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Accesso alla tabella "utenti" (singleton). */
public final class UtenteDAO {

    private static UtenteDAO istanza;

    private final Database db;

    private UtenteDAO() throws ErroreDatabaseException {
        this.db = Database.getInstance();
    }

    public static synchronized UtenteDAO getInstance() throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new UtenteDAO();
        }
        return istanza;
    }

    public void inserisci(Utente u) throws ErroreDatabaseException {
        String sql = "INSERT INTO utenti (id, nome, email, codice_fiscale, credito) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getCodiceFiscale());
            ps.setDouble(5, u.getCredito());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw Database.errore("salvataggio dell'utente", e);
        }
    }

    public void aggiornaCredito(Utente u) throws ErroreDatabaseException {
        try (Connection conn = db.apriConnessione()) {
            aggiornaCredito(conn, u);
        } catch (SQLException e) {
            throw Database.errore("aggiornamento del credito", e);
        }
    }

    void aggiornaCredito(Connection conn, Utente u) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE utenti SET credito = ? WHERE id = ?")) {
            ps.setDouble(1, u.getCredito());
            ps.setString(2, u.getId());
            ps.executeUpdate();
        }
    }

    public List<Utente> trovaTutti() throws ErroreDatabaseException {
        List<Utente> utenti = new ArrayList<>();
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nome, email, codice_fiscale, credito FROM utenti ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                utenti.add(new Utente(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("codice_fiscale"),
                        rs.getDouble("credito")));
            }
        } catch (SQLException e) {
            throw Database.errore("lettura degli utenti", e);
        }
        return utenti;
    }
}
