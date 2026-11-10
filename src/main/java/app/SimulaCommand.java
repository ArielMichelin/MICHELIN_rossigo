package app;

import eccezioni.RossiGoException;
import picocli.CommandLine.Command;
import servizio.FlottaService;

@Command(name = "simula", description = "Simula una giornata in città con successi ed errori.")
class SimulaCommand extends ComandoBase {

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        new SimulazioneGiornata(flotta).esegui();
    }
}
