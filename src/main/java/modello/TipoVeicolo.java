package modello;

/**
 * Tipo di mezzo, salvato nella colonna "tipo" del database.
 * Ogni costante sa ricostruire il proprio oggetto (metodo con corpo specifico per costante):
 * così chi legge dal database non ha bisogno di if/switch sul tipo.
 */
public enum TipoVeicolo {

    BICI {
        @Override
        public Veicolo crea(String codice, double batteria, StatoVeicolo stato, int caratteristica) {
            return new Bici(codice, caratteristica, batteria, stato);
        }
    },

    MONOPATTINO {
        @Override
        public Veicolo crea(String codice, double batteria, StatoVeicolo stato, int caratteristica) {
            return new Monopattino(codice, caratteristica, batteria, stato);
        }
    };

    /**
     * @param caratteristica numero di marce per la bici, velocità massima (km/h) per il monopattino
     */
    public abstract Veicolo crea(String codice, double batteria, StatoVeicolo stato, int caratteristica);
}
