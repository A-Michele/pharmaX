package com.alaia.pharmX.models.order;

public enum State {

	/* Bozza aperta/non confermata :
	 * - L’utente ha iniziato un ordine ma non lo ha confermato
	 * - Può ancora modificare quantità, indirizzi ecc.
	 */
	OPEN,


    RELEASED,

    PICKING,
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
}