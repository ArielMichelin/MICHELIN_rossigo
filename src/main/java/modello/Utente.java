package modello;

import java.util.Locale;

/** Cliente del servizio. Solo il credito cambia nel tempo. */
public class Utente {

    private final String id;
    private final String nome;
    private final String email;
    private final String codiceFiscale;
    private double credito;

    public Utente(String id, String nome, String email, String codiceFiscale, double credito) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.codiceFiscale = codiceFiscale;
        this.credito = arrotonda(credito);
    }

    /** Scala un importo dal credito (può andare in negativo: il debito blocca il prossimo sblocco). */
    public void addebita(double importo) {
        credito = arrotonda(credito - importo);
    }

    public void ricarica(double importo) {
        credito = arrotonda(credito + importo);
    }

    private static double arrotonda(double euro) {
        return Math.round(euro * 100) / 100.0;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public double getCredito() {
        return credito;
    }

    @Override
    public String toString() {
        return String.format(Locale.ITALY, "%s  %-16s %-26s credito %7.2f €", id, nome, email, credito);
    }
}
