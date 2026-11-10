package eccezioni;

/** Il veicolo esiste ma è già in uso oppure in ricarica. */
public class VeicoloNonDisponibileException extends RossiGoException {

    private final String codiceVeicolo;
    private final String statoAttuale;

    public VeicoloNonDisponibileException(String messaggio) {
        this(messaggio, null, null, null);
    }

    public VeicoloNonDisponibileException(String messaggio, Throwable causa) {
        this(messaggio, null, null, causa);
    }

    public VeicoloNonDisponibileException(String messaggio, String codiceVeicolo, String statoAttuale) {
        this(messaggio, codiceVeicolo, statoAttuale, null);
    }

    public VeicoloNonDisponibileException(String messaggio, String codiceVeicolo, String statoAttuale, Throwable causa) {
        super(messaggio, causa);
        this.codiceVeicolo = codiceVeicolo;
        this.statoAttuale = statoAttuale;
    }

    /** Codice del mezzo non disponibile (es. SC-0107). */
    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }

    /** Stato del mezzo (IN_USO o IN_RICARICA): l'app può scegliere icona e testo giusti. */
    public String getStatoAttuale() {
        return statoAttuale;
    }
}
