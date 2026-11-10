package app;

import eccezioni.RossiGoException;
import modello.StimaCosto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import servizio.FlottaService;

import java.util.Locale;

@Command(name = "stima", description = "Stima il costo di un viaggio e verifica che credito e batteria bastino.")
class StimaCommand extends ComandoBase {

    @Parameters(index = "0", paramLabel = "CODICE", description = "Codice del mezzo")
    private String codice;

    @Option(names = {"-m", "--minuti"}, required = true, description = "Durata prevista del viaggio")
    private int minuti;

    @Option(names = {"-u", "--utente"}, required = true, description = "Id dell'utente")
    private String idUtente;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        stampa(flotta.stimaCosto(codice, minuti, idUtente));
    }

    static void stampa(StimaCosto s) {
        Console.ok(String.format(Locale.ITALY,
                "Stima per %d min su %s: %s (credito dopo il viaggio %s, batteria all'arrivo %.0f%%)",
                s.minuti(), s.codiceVeicolo(), FlottaService.euro(s.costoTotale()),
                FlottaService.euro(s.creditoResiduo()), s.batteriaFinale()));
    }
}
