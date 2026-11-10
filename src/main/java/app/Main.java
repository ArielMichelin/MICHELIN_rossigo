package app;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Punto di ingresso. Picocli trasforma il programma in una CLI con sottocomandi;
 * senza argomenti parte la simulazione della giornata.
 */
@Command(name = "rossigo",
        mixinStandardHelpOptions = true,
        version = "RossiGo 1.0.0",
        description = "Gestione di sblocco, noleggio e ricarica di bici e monopattini elettrici.",
        subcommands = {
                SimulaCommand.class,
                StatoCommand.class,
                SbloccaCommand.class,
                TerminaCommand.class,
                RicaricaCommand.class,
                StimaCommand.class,
                CreditoCommand.class,
                RegistraUtenteCommand.class,
                RegistraVeicoloCommand.class,
                ResetCommand.class,
                CommandLine.HelpCommand.class
        })
public class Main implements Runnable {

    @Spec
    private CommandSpec spec;

    /** Eseguito solo se l'utente scrive "rossigo" senza sottocomando valido: mostra l'aiuto. */
    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    public static void main(String[] args) {
        Console.banner();
        String[] argomenti = args.length == 0 ? new String[]{"simula"} : args;
        int codiceUscita = new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)     // --tipo bici oppure --tipo BICI
                .execute(argomenti);
        System.exit(codiceUscita);
    }
}
