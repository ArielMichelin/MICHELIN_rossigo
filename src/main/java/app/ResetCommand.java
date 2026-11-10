package app;

import eccezioni.RossiGoException;
import persistenza.Database;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "reset", description = "Cancella tutti i dati salvati in rossigo.db.")
class ResetCommand implements Callable<Integer> {

    @Option(names = "--conferma", description = "Necessario per cancellare davvero i dati")
    private boolean conferma;

    @Override
    public Integer call() {
        if (!conferma) {
            Console.errore("Operazione non eseguita: aggiungi --conferma per cancellare tutti i dati.");
            return 1;
        }
        try {
            Database.getInstance().svuota();
            Console.ok("Dati cancellati. Al prossimo 'simula' verrà ricreata la flotta iniziale.");
            return 0;
        } catch (RossiGoException e) {
            Console.errore(e.getMessage());
            return 1;
        }
    }
}
