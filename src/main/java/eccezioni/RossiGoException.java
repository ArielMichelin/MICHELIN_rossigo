package eccezioni;

/**
 * Radice della gerarchia di eccezioni di RossiGo.
 * È checked (estende Exception): chi chiama le operazioni del servizio
 * è obbligato dal compilatore a gestire i casi di errore.
 * Il messaggio è sempre scritto per l'utente finale.
 */
public class RossiGoException extends Exception {

    public RossiGoException(String messaggio) {
        super(messaggio);
    }

    public RossiGoException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
