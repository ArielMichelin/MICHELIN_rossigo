package eccezioni;

/** Nessun utente registrato con l'id indicato. */
public class UtenteNonTrovatoException extends RossiGoException {

    private final String idUtente;

    public UtenteNonTrovatoException(String messaggio) {
        this(messaggio, null, null);
    }

    public UtenteNonTrovatoException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public UtenteNonTrovatoException(String messaggio, String idUtente) {
        this(messaggio, idUtente, null);
    }

    public UtenteNonTrovatoException(String messaggio, String idUtente, Throwable causa) {
        super(messaggio, causa);
        this.idUtente = idUtente;
    }

    public String getIdUtente() {
        return idUtente;
    }
}
