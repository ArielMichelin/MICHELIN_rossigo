package eccezioni;

/** Si prova a terminare un noleggio su un mezzo che non è noleggiato. */
public class NoleggioNonAttivoException extends RossiGoException {

    private final String codiceVeicolo;

    public NoleggioNonAttivoException(String messaggio) {
        this(messaggio, null, null);
    }

    public NoleggioNonAttivoException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public NoleggioNonAttivoException(String messaggio, String codiceVeicolo) {
        this(messaggio, codiceVeicolo, null);
    }

    public NoleggioNonAttivoException(String messaggio, String codiceVeicolo, Throwable causa) {
        super(messaggio, causa);
        this.codiceVeicolo = codiceVeicolo;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }
}
