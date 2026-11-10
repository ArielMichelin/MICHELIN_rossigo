package app;

import eccezioni.RossiGoException;
import modello.Noleggio;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import servizio.FlottaService;

@Command(name = "termina", description = "Termina il noleggio di un mezzo e addebita il costo.")
class TerminaCommand extends ComandoBase {

    @Parameters(index = "0", paramLabel = "CODICE", description = "Codice del mezzo")
    private String codice;

    @Option(names = {"-m", "--minuti"}, required = true, description = "Minuti di utilizzo")
    private int minuti;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        Noleggio n = flotta.terminaNoleggio(codice, minuti);
        Console.ok("Corsa terminata: " + n);
        Console.info("Credito residuo di " + n.getUtente().getNome() + ": "
                + FlottaService.euro(n.getUtente().getCredito()));
    }
}
