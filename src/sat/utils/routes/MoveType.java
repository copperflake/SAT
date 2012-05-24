package sat.utils.routes;

/**
 * Types de mouvement possible.
 */
public enum MoveType {
	/**
	 * En ligne droite
	 */
	STRAIGHT,

	/**
	 * Circulaire
	 */
	CIRCULAR,

	/**
	 * Atterrissage
	 */
	LANDING,

	/**
	 * Sur place pendant 10 secondes
	 */
	NONE,

	/**
	 * Auto-destruction
	 */
	DESTRUCTION
}
