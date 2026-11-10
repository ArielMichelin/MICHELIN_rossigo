package eccezioni;

/** Nessun veicolo della flotta ha il codice indicato. */
public class VeicoloNonTrovatoException extends RossiGoException {

    private final String codiceVeicolo;

    public VeicoloNonTrovatoException(String messaggio) {
        this(messaggio, null, null);
    }

    public VeicoloNonTrovatoException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public VeicoloNonTrovatoException(String messaggio, String codiceVeicolo) {
        this(messaggio, codiceVeicolo, null);
    }

    public VeicoloNonTrovatoException(String messaggio, String codiceVeicolo, Throwable causa) {
        super(messaggio, causa);
        this.codiceVeicolo = codiceVeicolo;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }
}
