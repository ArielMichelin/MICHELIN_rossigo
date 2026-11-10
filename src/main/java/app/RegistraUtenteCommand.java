package app;

import eccezioni.RossiGoException;
import modello.Utente;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import servizio.FlottaService;

@Command(name = "registra-utente", description = "Registra un nuovo utente (il codice fiscale viene validato).")
class RegistraUtenteCommand extends ComandoBase {

    @Option(names = "--id", required = true, description = "Identificativo, es. U010")
    private String id;

    @Option(names = "--nome", required = true, description = "Nome e cognome")
    private String nome;

    @Option(names = "--email", required = true, description = "Indirizzo email")
    private String email;

    @Option(names = "--cf", required = true, description = "Codice fiscale")
    private String codiceFiscale;

    @Option(names = "--credito", defaultValue = "0", description = "Credito iniziale in euro (default: ${DEFAULT-VALUE})")
    private double credito;

    @Override
    protected void esegui(FlottaService flotta) throws RossiGoException {
        flotta.registraUtente(new Utente(id, nome, email, codiceFiscale, credito));
        Console.ok("Benvenuto/a " + nome + "! Account " + id + " creato.");
    }
}
