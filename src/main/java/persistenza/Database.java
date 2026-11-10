package persistenza;

import eccezioni.ErroreDatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Singleton che conosce l'URL del database, crea le tabelle al primo avvio
 * e fornisce le connessioni ai DAO dello stesso package.
 */
public final class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    /** Il file può essere cambiato con -Drossigo.db=altro.db (utile per i test). */
    private static final String FILE_PREDEFINITO = "rossigo.db";

    private static final String[] SCHEMA = {
            """
            CREATE TABLE IF NOT EXISTS veicoli (
                codice          TEXT PRIMARY KEY,
                tipo            TEXT NOT NULL CHECK (tipo IN ('BICI', 'MONOPATTINO')),
                batteria        REAL NOT NULL,
                stato           TEXT NOT NULL,
                caratteristica  INTEGER NOT NULL  -- marce (bici) o velocita' massima km/h (monopattino)
            )""",
            """
            CREATE TABLE IF NOT EXISTS utenti (
                id              TEXT PRIMARY KEY,
                nome            TEXT NOT NULL,
                email           TEXT,
                codice_fiscale  TEXT NOT NULL UNIQUE,
                credito         REAL NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS noleggi (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                id_utente       TEXT NOT NULL REFERENCES utenti(id),
                codice_veicolo  TEXT NOT NULL REFERENCES veicoli(codice),
                inizio          TEXT NOT NULL,
                fine            TEXT,
                costo           REAL NOT NULL DEFAULT 0
            )"""
    };

    private static Database istanza;

    private final String url;

    private Database() throws ErroreDatabaseException {
        this.url = "jdbc:sqlite:" + System.getProperty("rossigo.db", FILE_PREDEFINITO);
        creaTabelle();
    }

    /** Punto di accesso unico: la prima chiamata crea l'istanza (e le tabelle). */
    public static synchronized Database getInstance() throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new Database();
        }
        return istanza;
    }

    private void creaTabelle() throws ErroreDatabaseException {
        try (Connection conn = apriConnessione()) {
            for (String ddl : SCHEMA) {
                try (PreparedStatement ps = conn.prepareStatement(ddl)) {
                    ps.executeUpdate();
                }
            }
            log.info("Database pronto ({})", url);
        } catch (SQLException e) {
            throw errore("inizializzazione del database", e);
        }
    }

    /** Svuota tutte le tabelle (comando "reset"). */
    public void svuota() throws ErroreDatabaseException {
        try {
            inTransazione(conn -> {
                for (String tabella : new String[]{"noleggi", "utenti", "veicoli"}) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tabella)) {
                        ps.executeUpdate();
                    }
                }
            });
            log.info("Database svuotato");
        } catch (SQLException e) {
            throw errore("svuotamento del database", e);
        }
    }

    // ---------- strumenti per i DAO (visibilità di package) ----------

    Connection apriConnessione() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA foreign_keys = ON")) {
            ps.execute();
        }
        return conn;
    }

    @FunctionalInterface
    interface OperazioneSql {
        void esegui(Connection conn) throws SQLException;
    }

    /** Esegue più istruzioni come un'unica transazione: o vanno tutte a buon fine, o nessuna. */
    void inTransazione(OperazioneSql operazione) throws SQLException {
        try (Connection conn = apriConnessione()) {
            conn.setAutoCommit(false);
            try {
                operazione.esegui(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Traduce una SQLException in un'eccezione per l'utente, lasciando i dettagli solo nel log. */
    static ErroreDatabaseException errore(String operazione, SQLException causa) {
        log.error("Errore SQL durante {}", operazione, causa);
        return new ErroreDatabaseException(
                "Al momento non riusciamo ad accedere ai dati (" + operazione + "). Riprova tra qualche minuto.",
                operazione, causa);
    }
}
