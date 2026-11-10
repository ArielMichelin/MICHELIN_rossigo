package app;

import eccezioni.RossiGoException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import servizio.FlottaService;

@Command(name = "ricarica", description = "Mette un mezzo in ricarica (o la completa con --completa).")
class RicaricaCommand extends ComandoBase {

    @Parameters(index = "0", paramLabel = "CODICE", description = "Codice del mezzo")
    private String codice;

    @Option(names = "--completa", description = "Conclude la ricarica: batteria al 100% e mezzo di nuovo libero")
    private boolean completa;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        if (completa) {
            flotta.completaRicarica(codice);
            Console.ok(codice + " ricaricato al 100% e di nuovo disponibile.");
        } else {
            flotta.mettiInRicarica(codice);
            Console.ok(codice + " messo in ricarica.");
        }
    }
}
