package eccezioni;

/** Il credito dell'utente non copre l'importo richiesto (sblocco o tragitto stimato). */
public class CreditoInsufficienteException extends RossiGoException {

    private final double creditoAttuale;
    private final double importoRichiesto;

    public CreditoInsufficienteException(String messaggio) {
        this(messaggio, Double.NaN, Double.NaN, null);
    }

    public CreditoInsufficienteException(String messaggio, Throwable causa) {
        this(messaggio, Double.NaN, Double.NaN, causa);
    }

    public CreditoInsufficienteException(String messaggio, double creditoAttuale, double importoRichiesto) {
        this(messaggio, creditoAttuale, importoRichiesto, null);
    }

    public CreditoInsufficienteException(String messaggio, double creditoAttuale, double importoRichiesto, Throwable causa) {
        super(messaggio, causa);
        this.creditoAttuale = creditoAttuale;
        this.importoRichiesto = importoRichiesto;
    }

    public double getCreditoAttuale() {
        return creditoAttuale;
    }

    public double getImportoRichiesto() {
        return importoRichiesto;
    }

    /** Quanto manca: l'app può proporre subito una ricarica di questo importo. */
    public double getImportoMancante() {
        return Math.max(0, importoRichiesto - creditoAttuale);
    }
}
