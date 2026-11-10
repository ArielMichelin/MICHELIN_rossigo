package eccezioni;

/** Il codice fiscale fornito in registrazione non è valido (formato o carattere di controllo errati). */
public class CodiceFiscaleNonValidoException extends RossiGoException {

    private final String codiceFornito;

    public CodiceFiscaleNonValidoException(String messaggio) {
        this(messaggio, null, null);
    }

    public CodiceFiscaleNonValidoException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public CodiceFiscaleNonValidoException(String messaggio, String codiceFornito) {
        this(messaggio, codiceFornito, null);
    }

    public CodiceFiscaleNonValidoException(String messaggio, String codiceFornito, Throwable causa) {
        super(messaggio, causa);
        this.codiceFornito = codiceFornito;
    }

    /** Il valore inserito, utile all'app per evidenziare il campo del form. */
    public String getCodiceFornito() {
        return codiceFornito;
    }
}
