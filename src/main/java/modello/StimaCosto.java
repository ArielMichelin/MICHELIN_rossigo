package modello;

/**
 * Risultato del comando di stima (bonus).
 *
 * @param costoTotale          sblocco + corsa
 * @param creditoResiduo       credito dell'utente dopo il viaggio
 * @param batteriaFinale       batteria stimata all'arrivo (%)
 */
public record StimaCosto(String codiceVeicolo, int minuti, double costoTotale,
                         double creditoResiduo, double batteriaFinale) {
}
