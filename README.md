# RossiGo

Nucleo backend, testabile da terminale, per una startup di **sharing di bici e monopattini elettrici**.
Gestisce sblocco, noleggio, fine corsa e ricarica dei mezzi; modella ogni caso limite con un'eccezione
dedicata (pensata per l'app mobile) e salva flotta, utenti, crediti e noleggi in un database **SQLite**.

- Java 25, Maven
- Persistenza: SQLite tramite JDBC (driver Xerial)
- CLI con sottocomandi (Picocli), banner ASCII art (JFiglet), log su file (SLF4J + Logback)

## Requisiti ed esecuzione

Serve solo un **JDK 25**. Maven non è necessario: il progetto include il *Maven Wrapper* (`mvnw` / `mvnw.cmd`),
che scarica da solo la versione giusta di Maven.

```bash
# compila e crea target/rossigo.jar (jar eseguibile con tutte le dipendenze)
./mvnw clean package          # Windows: mvnw.cmd clean package

# simulazione della giornata (è anche il comando di default senza argomenti)
java -jar target/rossigo.jar simula

# riavvia il programma e verifica che i dati ci siano ancora
java -jar target/rossigo.jar stato
```

Altri comandi (`java -jar target/rossigo.jar --help` per l'elenco completo):

| Comando | Esempio |
|---|---|
| `stato` | `java -jar target/rossigo.jar stato` |
| `sblocca` | `java -jar target/rossigo.jar sblocca BK-0044 --utente U001` |
| `termina` | `java -jar target/rossigo.jar termina BK-0044 --minuti 12` |
| `ricarica` | `java -jar target/rossigo.jar ricarica SC-0108` / `... ricarica SC-0108 --completa` |
| `stima` (bonus) | `java -jar target/rossigo.jar stima SC-0108 --minuti 30 --utente U002` |
| `credito` | `java -jar target/rossigo.jar credito --utente U002 --importo 10` |
| `registra-utente` | `java -jar target/rossigo.jar registra-utente --id U010 --nome "Anna Gialli" --email anna@example.com --cf <CF>` |
| `registra-veicolo` | `java -jar target/rossigo.jar registra-veicolo SC-0200 --tipo monopattino --caratteristica 20` |
| `reset` | `java -jar target/rossigo.jar reset --conferma` (svuota il database) |

Il database `rossigo.db` e il log `logs/rossigo.log` vengono creati nella cartella da cui si lancia il programma.
Se in un terminale Windows il simbolo € appare come `?`, eseguire prima `chcp 65001`.

## Struttura

```
src/main/java
├── app          Main (CLI Picocli), comandi, SimulazioneGiornata, Console (output per l'utente)
├── modello      Veicolo (astratta), Bici, Monopattino, StatoVeicolo, TipoVeicolo, Utente, Noleggio, StimaCosto
├── eccezioni    RossiGoException e le sue sottoclassi
├── servizio     FlottaService: tutte le regole di business
└── persistenza  Database, VeicoloDAO, UtenteDAO, NoleggioDAO (singleton, JDBC)
```

- **Ereditarietà e polimorfismo**: `Veicolo` dichiara `calcolaCosto(int)` e `consumoBatteriaPerMinuto()`;
  `Bici` (0,15 €/min, 0,2 %/min, `getNumeroMarce()`) e `Monopattino` (0,25 €/min, 0,8 %/min,
  `isLimitatoInZonaPedonale()`) li implementano. `FlottaService` usa solo il tipo `Veicolo`:
  `terminaNoleggio` calcola `1 € + veicolo.calcolaCosto(minuti)` senza `if` né `instanceof`.
- **Eccezioni**: `RossiGoException` (checked) → `VeicoloNonDisponibileException` (`getStatoAttuale()`),
  `BatteriaScaricaException` (`getLivelloAttuale()`), `CreditoInsufficienteException` (`getImportoMancante()`),
  `UtenteNonTrovatoException`, `CodiceFiscaleNonValidoException` (`getCodiceFornito()`),
  `ErroreDatabaseException`, più `VeicoloNonTrovatoException`, `NoleggioNonAttivoException`, `DatiNonValidiException`.
  Ognuna ha i costruttori (messaggio) e (messaggio, causa).
- **Persistenza**: tabelle `veicoli` (con colonna `tipo`), `utenti`, `noleggi`, create al primo avvio con
  `CREATE TABLE IF NOT EXISTS`. Solo `PreparedStatement` e try-with-resources; ogni `SQLException` diventa
  `ErroreDatabaseException` con un messaggio generico (i dettagli vanno solo nel log). Apertura e chiusura di
  un noleggio sono transazioni (noleggio + veicolo + credito salvati insieme o per niente).

## Scenari simulati (`simula`)

| # | Scenario | Esito |
|---|---|---|
| 1 | Apertura: al primo avvio crea flotta e utenti, dal secondo li ritrova nel DB | persistenza |
| 2 | Paolo si registra con un codice fiscale sbagliato, poi lo corregge | `CodiceFiscaleNonValidoException` |
| 3 | Mario sblocca il monopattino SC-0107 | ok |
| 4 | Luca prova a sbloccare SC-0107, già in uso | `VeicoloNonDisponibileException` |
| 5 | Giulia, con 0,50 € di credito, prova a sbloccare SC-0108 | `CreditoInsufficienteException` |
| 6 | Luca prova la bici BK-0043 con la batteria al 9% | `BatteriaScaricaException` |
| 7 | Luca sblocca la bici BK-0042 | ok |
| 8 | Un id utente inesistente (U999) prova a sbloccare | `UtenteNonTrovatoException` |
| 9 | Fine corse: Mario 18 min in monopattino, Luca 25 min in bici, costi e batteria diversi | ok (polimorfismo) |
| 10 | Listino: costo di 30 minuti su ogni mezzo con la stessa chiamata | ok (polimorfismo) |
| 11 | Mario ricarica il credito | ok |
| 12 | Bonus stima: un viaggio possibile, uno senza credito, uno senza autonomia | `CreditoInsufficienteException`, `BatteriaScaricaException` |
| 13 | Ricarica notturna: Luca prova a prendere un mezzo in carica | `VeicoloNonDisponibileException` |
| 14 | Riavvio: nuovo `FlottaService` che rilegge tutto dal DB e confronta il credito | persistenza |
| 15 | Stato finale letto dal database | ok |

**Prova di persistenza tra due esecuzioni**: lanciando `simula` una seconda volta, lo scenario 1 stampa
"Bentornati! Dati ritrovati in rossigo.db" con il credito di Mario rimasto dalla volta prima, e lo scenario 2
mostra che Paolo risulta già registrato. Anche `stato` dopo `simula` mostra gli stessi crediti.

## Librerie scelte

| Libreria | Perché e dove |
|---|---|
| `org.xerial:sqlite-jdbc` 3.49.1.0 | Obbligatoria: driver JDBC che include SQLite, nessun server da installare. Usato in `persistenza.Database`. |
| `it.kamaladafrica:codice-fiscale` 1.6.0 | Valida davvero il codice fiscale (formato, data, comune, carattere di controllo) invece di una regex fatta a mano. Usata in `FlottaService.registraUtente`. |
| `com.github.lalyos:jfiglet` 0.0.9 | Stampa "RossiGo" e i titoli delle sezioni (Mattina/Pomeriggio/Sera) in ASCII art. Usata in `app.Console`. |
| `org.apache.commons:commons-lang3` 3.20.0 | `Validate.isTrue` controlla il formato dei codici veicolo (BK-0042, SC-0107) nel costruttore di `Veicolo`; `StringUtils.isBlank` controlla id e nome in `FlottaService`. |
| `info.picocli:picocli` 4.7.7 | Trasforma il Main in una vera CLI con sottocomandi, opzioni obbligatorie e `--help` generato. Usata nel package `app`. |
| `org.slf4j:slf4j-api` 2.0.17 + `ch.qos.logback:logback-classic` 1.5.18 | Registra ogni sblocco, fine noleggio, ricarica ed errore in `logs/rossigo.log`, lasciando la console pulita per l'utente. Usati in `FlottaService` e `Database`. |

Il `maven-shade-plugin` impacchetta tutto in un unico `target/rossigo.jar` eseguibile.
