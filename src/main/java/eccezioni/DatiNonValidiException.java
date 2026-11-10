package eccezioni;

/** Dati di input non accettabili (nome vuoto, id duplicato, minuti negativi, importo non valido...). */
public class DatiNonValidiException extends RossiGoException {

    private final String campo;

    public DatiNonValidiException(String messaggio) {
        this(messaggio, null, null);
    }

    public DatiNonValidiException(String messaggio, Throwable causa) {
        this(messaggio, null, causa);
    }

    public DatiNonValidiException(String messaggio, String campo) {
        this(messaggio, campo, null);
    }

    public DatiNonValidiException(String messaggio, String campo, Throwable causa) {
        super(messaggio, causa);
        this.campo = campo;
    }

    /** Nome del campo errato (es. "nome", "minuti"): l'app può evidenziarlo nel form. */
    public String getCampo() {
        return campo;
    }
}
