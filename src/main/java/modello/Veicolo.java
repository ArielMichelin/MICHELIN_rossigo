package modello;

import org.apache.commons.lang3.Validate;

import java.util.Locale;

/**
 * Mezzo generico della flotta. Contiene stato e comportamento comuni;
 * tariffa e consumo sono astratti e vengono decisi dalle sottoclassi.
 */
public abstract class Veicolo {

    private final String codice;
    private double livelloBatteria;
    private StatoVeicolo stato;

    /**
     * @param prefisso prefisso atteso del codice ("BK" o "SC"), passato dalla sottoclasse
     */
    protected Veicolo(String codice, String prefisso, double livelloBatteria, StatoVeicolo stato) {
        // Commons Lang: verifica il formato del codice (es. BK-0042, SC-0107)
        Validate.isTrue(codice != null && codice.matches(prefisso + "-\\d{4}"),
                "Codice veicolo non valido: '%s' (formato atteso %s-0000)", codice, prefisso);
        Validate.inclusiveBetween(0.0, 100.0, livelloBatteria,
                "Il livello della batteria deve essere compreso tra 0 e 100");
        Validate.notNull(stato, "Lo stato del veicolo è obbligatorio");
        this.codice = codice;
        this.livelloBatteria = livelloBatteria;
        this.stato = stato;
    }

    // ---- metodi astratti: ogni sottoclasse DEVE implementarli ----

    /** Costo della sola corsa (senza lo sblocco) per i minuti indicati. */
    public abstract double calcolaCosto(int minuti);

    /** Batteria consumata in % per ogni minuto di utilizzo. */
    public abstract double consumoBatteriaPerMinuto();

    /** Tipo da salvare nel database. */
    public abstract TipoVeicolo getTipo();

    /** Dato tecnico specifico del tipo (marce per la bici, km/h per il monopattino). */
    public abstract int getCaratteristica();

    /** Descrizione leggibile, diversa per ogni tipo di mezzo. */
    public abstract String descrizione();

    // ---- comportamento comune ----

    /** Scala la batteria in base ai minuti di utilizzo, senza scendere sotto 0. */
    public void consumaBatteria(int minuti) {
        livelloBatteria = Math.max(0, livelloBatteria - minuti * consumoBatteriaPerMinuto());
    }

    /** Minuti che il mezzo può ancora percorrere con la carica attuale. */
    public int autonomiaMinuti() {
        return (int) Math.floor(livelloBatteria / consumoBatteriaPerMinuto());
    }

    public boolean isLibero() {
        return stato == StatoVeicolo.LIBERO;
    }

    public void sblocca() {
        stato = StatoVeicolo.IN_USO;
    }

    public void libera() {
        stato = StatoVeicolo.LIBERO;
    }

    public void mettiInRicarica() {
        stato = StatoVeicolo.IN_RICARICA;
    }

    public void completaRicarica() {
        livelloBatteria = 100;
        stato = StatoVeicolo.LIBERO;
    }

    public String getCodice() {
        return codice;
    }

    public double getLivelloBatteria() {
        return livelloBatteria;
    }

    public StatoVeicolo getStato() {
        return stato;
    }

    @Override
    public String toString() {
        return String.format(Locale.ITALY, "%s  %-38s batteria %5.1f%%  %s",
                codice, descrizione(), livelloBatteria, stato);
    }
}
