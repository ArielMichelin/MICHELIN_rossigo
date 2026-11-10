package app;

import eccezioni.RossiGoException;
import modello.StatoVeicolo;
import modello.TipoVeicolo;
import modello.Veicolo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import servizio.FlottaService;

@Command(name = "registra-veicolo", description = "Aggiunge un mezzo alla flotta.")
class RegistraVeicoloCommand extends ComandoBase {

    @Parameters(index = "0", paramLabel = "CODICE", description = "BK-0000 per le bici, SC-0000 per i monopattini")
    private String codice;

    @Option(names = "--tipo", required = true, description = "BICI o MONOPATTINO")
    private TipoVeicolo tipo;

    @Option(names = "--caratteristica", required = true,
            description = "Numero di marce (bici) o velocità massima in km/h (monopattino)")
    private int caratteristica;

    @Option(names = "--batteria", defaultValue = "100", description = "Livello batteria in % (default: ${DEFAULT-VALUE})")
    private double batteria;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        Veicolo v = tipo.crea(codice, batteria, StatoVeicolo.LIBERO, caratteristica);
        flotta.registraVeicolo(v);
        Console.ok("Aggiunto alla flotta: " + v);
    }
}
