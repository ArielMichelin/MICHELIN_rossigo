package eccezioni;

/** La batteria del mezzo è sotto la soglia minima (o non basta per il tragitto stimato). */
public class BatteriaScaricaException extends RossiGoException {

    private final double livelloAttuale;

    public BatteriaScaricaException(String messaggio) {
        this(messaggio, Double.NaN, null);
    }

    public BatteriaScaricaException(String messaggio, Throwable causa) {
        this(messaggio, Double.NaN, causa);
    }

    public BatteriaScaricaException(String messaggio, double livelloAttuale) {
        this(messaggio, livelloAttuale, null);
    }

    public BatteriaScaricaException(String messaggio, double livelloAttuale, Throwable causa) {
        super(messaggio, causa);
        this.livelloAttuale = livelloAttuale;
    }

    /** Livello batteria in %: l'app può scrivere "Batteria al 9%, scegli un altro mezzo". */
    public double getLivelloAttuale() {
        return livelloAttuale;
    }
}
