package app;

import eccezioni.RossiGoException;
import modello.Noleggio;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import servizio.FlottaService;

@Command(name = "sblocca", description = "Sblocca un mezzo e avvia il noleggio.")
class SbloccaCommand extends ComandoBase {

    @Parameters(index = "0", paramLabel = "CODICE", description = "Codice del mezzo, es. BK-0042")
    private String codice;

    @Option(names = {"-u", "--utente"}, required = true, description = "Id dell'utente, es. U001")
    private String idUtente;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        Noleggio n = flotta.sbloccaVeicolo(codice, idUtente);
        Console.ok("Mezzo sbloccato, buon viaggio! " + n);
    }
}
