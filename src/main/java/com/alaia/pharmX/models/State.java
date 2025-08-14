package com.alaia.pharmX.models;

public enum State {

	/* Bozza aperta/non confermata :
	 * - L’utente ha iniziato un ordine ma non lo ha confermato
	 * - Può ancora modificare quantità, indirizzi ecc.
	 */
	OPEN,

	/* Ordine creato ma non ancora evaso :
	 * - L’utente ha appena confermato l’ordine
	 * - Il pagamento non è stato ancora processato (se richiesto)
     * - Nessuna attività logistica avviata
	 */
    PENDING,

    /* In fase di spedizione/consegna :
     * - Il magazzino ha prelevato e imballato i prodotti
	 * - È stato creato un tracking di spedizione
	 * - L’ordine è fisicamente in transito
     */
    SHIPPING,

    /* Completato con successo:
     * - Il corriere ha confermato la consegna
	 * - Eventuali pagamenti sono confermati
     * - Non sono più attese azioni logistiche
     */
    COMPLETED,

    /* Annullato
     * - Annullato dall’utente o dal venditore
	 * - Pagamento rimborsato (se presente)
	 * - Nessuna spedizione in corso
     */
    CANCELED;

	//Per controllare se un cambio di stato è valido
    public boolean canTransitionTo(State next) {
        return switch (this) {
            case OPEN -> next == PENDING || next == CANCELED;
            case PENDING -> next == SHIPPING || next == CANCELED;
            case SHIPPING -> next == COMPLETED || next == CANCELED;
            case COMPLETED, CANCELED -> false;
        };
    }
}
