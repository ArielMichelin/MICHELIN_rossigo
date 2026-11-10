package eccezioni;

/**
 * Incapsula qualunque SQLException. Il messaggio è generico e pensato per l'utente;
 * i dettagli tecnici restano nella causa (getCause()) e finiscono solo nel file di log.
 */
public class ErroreDatabaseException extends RossiGoException {

    private final String operazione;

    public ErroreDatabaseException(String messaggio) {
        this(messaggio, null, null);
    }

    public ErroreDatabaseException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public ErroreDatabaseException(String messaggio, String operazione, Throwable causa) {
        super(messaggio, causa);
        this.operazione = operazione;
    }

    /** Operazione che è fallita (es. "salvataggio utente"), utile al supporto tecnico. */
    public String getOperazione() {
        return operazione;
    }
}
