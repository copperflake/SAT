package sat.utils.routes;

import java.io.Serializable;

import sat.utils.geo.Coordinates;

/**
 * Une étape du routage d'un avion.
 */
@SuppressWarnings("serial")
public class Waypoint implements Serializable {
	/**
	 * Type de routage.
	 */
	private MoveType type;

	/**
	 * Arguments de routage. Le contenu du ce tableau
	 */
	private float[] args;

	/**
	 * Crée un nouveau point de routage de type donnée et avec les arguments
	 * donnés.
	 * 
	 * @param type
	 *            Le type de routage
	 * @param args
	 *            Les arguments associés avec cette étape de routage. Dans le
	 *            cas d'un routage en cercle, ce tableau contient 4 floats
	 *            (respectivement x,y,z,angle). Dans tous les autre cas il ne
	 *            contient que les trois coordonnées (x,y,z).
	 */
	public Waypoint(MoveType type, float[] args) {
		this.type = type;
		this.args = args;
	}

	/**
	 * Retourne le type de routage vers ce point.
	 */
	public MoveType getType() {
		return type;
	}

	/**
	 * Retourne les coordonnées associée à ce point de routage.
	 */
	public Coordinates getCoordiates() {
		return new Coordinates(args[0], args[1], args[2]);
	}

	/**
	 * Retourne l'angle de rotation d'un mouvement circulaire. ATTENTION: dans
	 * un mouvement non circulaire, cette méthode lance une exception.
	 */
	public float getAngle() {
		// TODO : same, but better.
		return args[3];
	}
}
