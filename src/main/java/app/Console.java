package app;

import com.github.lalyos.jfiglet.FigletFont;

import java.io.IOException;
import java.io.InputStream;

/** Tutto ciò che viene stampato a video passa da qui: stile uniforme e nessuno stack trace. */
final class Console {

    private static final String RIGA = "-".repeat(72);

    private Console() {
    }

    /** Titolo del programma in ASCII art (font "standard" di JFiglet). */
    static void banner() {
        try {
            System.out.println(FigletFont.convertOneLine("RossiGo"));
        } catch (IOException e) {
            System.out.println("=== RossiGo ===");
        }
        System.out.println("  Bici e monopattini elettrici in sharing\n");
    }

    /** Titolo di sezione in ASCII art con il font "slant" incluso in JFiglet. */
    static void sezione(String titolo) {
        try (InputStream font = FigletFont.class.getResourceAsStream("/slant.flf")) {
            System.out.println();
            System.out.println(FigletFont.convertOneLine(font, titolo));
        } catch (IOException | RuntimeException e) {
            System.out.println("\n=== " + titolo.toUpperCase() + " ===\n");
        }
    }

    static void scenario(int numero, String titolo) {
        System.out.println(RIGA);
        System.out.printf("Scenario %d - %s%n", numero, titolo);
        System.out.println(RIGA);
    }

    static void info(String messaggio) {
        System.out.println("   " + messaggio);
    }

    static void ok(String messaggio) {
        System.out.println("   [OK] " + messaggio);
    }

    /** Messaggio d'errore per l'utente finale. */
    static void errore(String messaggio) {
        System.out.println("   [X]  " + messaggio);
    }

    /** Suggerimento operativo mostrato dopo un errore (quello che l'app mobile mostrerebbe sotto). */
    static void suggerimento(String messaggio) {
        System.out.println("        -> " + messaggio);
    }

    static void vuota() {
        System.out.println();
    }
}
