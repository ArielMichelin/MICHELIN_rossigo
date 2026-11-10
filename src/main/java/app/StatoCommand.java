package app;

import eccezioni.RossiGoException;
import picocli.CommandLine.Command;
import servizio.FlottaService;

@Command(name = "stato", description = "Mostra flotta, utenti e noleggi in corso (letti da rossigo.db).")
class StatoCommand extends ComandoBase {

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        stampa(flotta);
    }

    static void stampa(FlottaService flotta) throws RossiGoException {
        Console.info("FLOTTA");
        flotta.getVeicoli().forEach(v -> Console.info("  " + v));
        Console.vuota();
        Console.info("UTENTI");
        flotta.getUtenti().forEach(u -> Console.info("  " + u));
        Console.vuota();
        Console.info("NOLEGGI IN CORSO");
        if (flotta.getNoleggiAttivi().isEmpty()) {
            Console.info("  nessuno");
        }
        flotta.getNoleggiAttivi().forEach(n -> Console.info("  " + n));
        Console.vuota();
        Console.info("Noleggi conclusi in archivio: " + flotta.contaNoleggiConclusi());
    }
}
