package sat.tower;

/**
 * Mode de routage de la tour.
 */
public enum RoutingMode {
	/**
	 * Dans l'ordre des demandes.
	 */
	CHRONOS,

	/**
	 * Limite la consomation en faisant atterrir les avions les plus
	 * consomateurs en premier.
	 */
	FUEL,

	/**
	 * Limite le temps d'attente en faisant atterir les avions les plus remplis
	 * en premier.
	 */
	TIME
}
