package app;

import eccezioni.RossiGoException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import servizio.FlottaService;

@Command(name = "credito", description = "Ricarica il credito di un utente.")
class CreditoCommand extends ComandoBase {

    @Option(names = {"-u", "--utente"}, required = true, description = "Id dell'utente")
    private String idUtente;

    @Option(names = {"-i", "--importo"}, required = true, description = "Importo in euro, es. 10")
    private double importo;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        flotta.ricaricaCredito(idUtente, importo);
        Console.ok("Nuovo credito: " + FlottaService.euro(flotta.trovaUtente(idUtente).getCredito()));
    }
}
