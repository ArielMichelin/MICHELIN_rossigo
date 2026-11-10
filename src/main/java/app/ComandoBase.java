package app;

import eccezioni.RossiGoException;
import servizio.FlottaService;

import java.util.concurrent.Callable;

/**
 * Schema comune a tutti i comandi (template method): crea il servizio,
 * esegue il comando e trasforma qualsiasi errore in un messaggio leggibile
 * e in un codice di uscita diverso da 0.
 */
abstract class ComandoBase implements Callable<Integer> {

    @Override
    public final Integer call() {
        try {
            esegui(new FlottaService());
            return 0;
        } catch (RossiGoException e) {
            Console.errore(e.getMessage());
            return 1;
        } catch (IllegalArgumentException e) {      // dati di input rifiutati da Validate (Commons Lang)
            Console.errore(e.getMessage());
            return 2;
        }
    }

    protected abstract void esegui(FlottaService flotta) throws RossiGoException;
}
