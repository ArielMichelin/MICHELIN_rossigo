package modello;

import org.apache.commons.lang3.Validate;

/** Monopattino elettrico: tariffa più alta e consumo maggiore (va solo a motore). */
public class Monopattino extends Veicolo {

    public static final String PREFISSO = "SC";
    public static final double TARIFFA_AL_MINUTO = 0.25;          // €/min
    public static final double CONSUMO_AL_MINUTO = 0.8;           // %/min
    public static final int LIMITE_ZONA_PEDONALE_KMH = 6;         // limite di legge nelle aree pedonali

    private final int velocitaMassima;

    public Monopattino(String codice, int velocitaMassima, double livelloBatteria) {
        this(codice, velocitaMassima, livelloBatteria, StatoVeicolo.LIBERO);
    }

    public Monopattino(String codice, int velocitaMassima, double livelloBatteria, StatoVeicolo stato) {
        super(codice, PREFISSO, livelloBatteria, stato);
        Validate.inclusiveBetween(1, 25, velocitaMassima, "La velocità massima deve essere tra 1 e 25 km/h");
        this.velocitaMassima = velocitaMassima;
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
        return TipoVeicolo.MONOPATTINO;
    }

    @Override
    public int getCaratteristica() {
        return velocitaMassima;
    }

    @Override
    public String descrizione() {
        return "Monopattino " + velocitaMassima + " km/h"
                + (isLimitatoInZonaPedonale() ? " (limitato in ZTL)" : "");
    }

    /** Caratteristica propria: se il mezzo supera i 6 km/h va limitato nelle zone pedonali. */
    public boolean isLimitatoInZonaPedonale() {
        return velocitaMassima > LIMITE_ZONA_PEDONALE_KMH;
    }

    public int getVelocitaMassima() {
        return velocitaMassima;
    }
}
