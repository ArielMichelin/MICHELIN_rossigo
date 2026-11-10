package persistenza;

import eccezioni.ErroreDatabaseException;
import modello.StatoVeicolo;
import modello.TipoVeicolo;
import modello.Veicolo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Accesso alla tabella "veicoli" (singleton). */
public final class VeicoloDAO {

    private static VeicoloDAO istanza;

    private final Database db;

    private VeicoloDAO() throws ErroreDatabaseException {
        this.db = Database.getInstance();
    }

    public static synchronized VeicoloDAO getInstance() throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new VeicoloDAO();
        }
        return istanza;
    }

    public void inserisci(Veicolo v) throws ErroreDatabaseException {
        String sql = "INSERT INTO veicoli (codice, tipo, batteria, stato, caratteristica) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCodice());
            ps.setString(2, v.getTipo().name());      // polimorfismo: ogni sottoclasse dice il suo tipo
            ps.setDouble(3, v.getLivelloBatteria());
            ps.setString(4, v.getStato().name());
            ps.setInt(5, v.getCaratteristica());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw Database.errore("salvataggio del veicolo", e);
        }
    }

    public void aggiorna(Veicolo v) throws ErroreDatabaseException {
        try (Connection conn = db.apriConnessione()) {
            aggiorna(conn, v);
        } catch (SQLException e) {
            throw Database.errore("aggiornamento del veicolo", e);
        }
    }

    /** Versione che usa una connessione esistente: serve dentro le transazioni di NoleggioDAO. */
    void aggiorna(Connection conn, Veicolo v) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE veicoli SET batteria = ?, stato = ? WHERE codice = ?")) {
            ps.setDouble(1, v.getLivelloBatteria());
            ps.setString(2, v.getStato().name());
            ps.setString(3, v.getCodice());
            ps.executeUpdate();
        }
    }

    public List<Veicolo> trovaTutti() throws ErroreDatabaseException {
        List<Veicolo> veicoli = new ArrayList<>();
        try (Connection conn = db.apriConnessione();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT codice, tipo, batteria, stato, caratteristica FROM veicoli ORDER BY codice");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoVeicolo tipo = TipoVeicolo.valueOf(rs.getString("tipo"));
                veicoli.add(tipo.crea(
                        rs.getString("codice"),
                        rs.getDouble("batteria"),
                        StatoVeicolo.valueOf(rs.getString("stato")),
                        rs.getInt("caratteristica")));
            }
        } catch (SQLException e) {
            throw Database.errore("lettura della flotta", e);
        }
        return veicoli;
    }
}
