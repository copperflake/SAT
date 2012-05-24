package sat.utils.routes;

/**
 * Type de routage
 */
public enum RoutingType {
	/**
	 * Cet ordre est effectué avant tous les autres.
	 */
	NEWFIRST,

	/**
	 * Cet ordre est executé après les ordres déjà reçus.
	 */
	LAST,

	/**
	 * Cet ordre est executé immédiatement et remplace tous les autres ordres.
	 */
	REPLACEALL
}
