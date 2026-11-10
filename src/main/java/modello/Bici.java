package modello;

import org.apache.commons.lang3.Validate;

/** Bici a pedalata assistita: tariffa bassa e consumo ridotto. */
public class Bici extends Veicolo {

    public static final String PREFISSO = "BK";
    public static final double TARIFFA_AL_MINUTO = 0.15;   // €/min
    public static final double CONSUMO_AL_MINUTO = 0.2;    // %/min: la pedalata aiuta il motore

    private final int numeroMarce;

    public Bici(String codice, int numeroMarce, double livelloBatteria) {
        this(codice, numeroMarce, livelloBatteria, StatoVeicolo.LIBERO);
    }

    public Bici(String codice, int numeroMarce, double livelloBatteria, StatoVeicolo stato) {
        super(codice, PREFISSO, livelloBatteria, stato);
        Validate.isTrue(numeroMarce >= 1, "Una bici deve avere almeno una marcia");
        this.numeroMarce = numeroMarce;
    }

    @Override
    public double calcolaCosto(int minuti) {
        return minuti * TARIFFA_AL_MINUTO;
    }

    @Override
    public double consumoBatteriaPerMinuto() {
        return CONSUMO_AL_MINUTO;
    }

    @Override
    public TipoVeicolo getTipo() {
        return TipoVeicolo.BICI;
    }

    @Override
    public int getCaratteristica() {
        return numeroMarce;
    }

    @Override
    public String descrizione() {
        return "Bici elettrica a " + numeroMarce + " marce";
    }

    /** Caratteristica propria della bici. */
    public int getNumeroMarce() {
        return numeroMarce;
    }
}
