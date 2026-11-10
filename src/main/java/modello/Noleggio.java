package modello;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Un noleggio: chi, quale mezzo, quando inizia e quando finisce. */
public class Noleggio {

    private static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;                       // assegnato dal database
    private final Utente utente;
    private final Veicolo veicolo;
    private final LocalDateTime inizio;
    private LocalDateTime fine;            // null finché il noleggio è attivo
    private double costo;

    public Noleggio(Utente utente, Veicolo veicolo, LocalDateTime inizio) {
        this(null, utente, veicolo, inizio, null, 0);
    }

    public Noleggio(Long id, Utente utente, Veicolo veicolo, LocalDateTime inizio, LocalDateTime fine, double costo) {
        this.id = id;
        this.utente = utente;
        this.veicolo = veicolo;
        this.inizio = inizio;
        this.fine = fine;
        this.costo = costo;
    }

    public void chiudi(LocalDateTime fine, double costo) {
        this.fine = fine;
        this.costo = costo;
    }

    public boolean isAttivo() {
        return fine == null;
    }

    public long getMinuti() {
        return isAttivo() ? 0 : Duration.between(inizio, fine).toMinutes();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utente getUtente() {
        return utente;
    }

    public Veicolo getVeicolo() {
        return veicolo;
    }

    public LocalDateTime getInizio() {
        return inizio;
    }

    public LocalDateTime getFine() {
        return fine;
    }

    public double getCosto() {
        return costo;
    }

    @Override
    public String toString() {
        String periodo = isAttivo()
                ? "dalle " + inizio.format(ORA) + " (in corso)"
                : inizio.format(ORA) + "-" + fine.format(ORA) + String.format(Locale.ITALY, ", %d min, %.2f €", getMinuti(), costo);
        return veicolo.getCodice() + " -> " + utente.getNome() + "  " + periodo;
    }
}
