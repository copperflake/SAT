package sat.utils.routes;

import java.util.ArrayList;

import sat.utils.geo.Coordinates;

/**
 * Une route est une suite de Waypoint à suivre.
 */
@SuppressWarnings("serial")
public class Route extends ArrayList<Waypoint> implements Cloneable {
	/**
	 * Capacité en nombre d'avions de cette route.
	 */
	private int capacity;

	/**
	 * Indique si cette piste est une piste de landing.
	 */
	private boolean landing = false;

	/**
	 * Crée une nouvelle route sans capacité.
	 */
	public Route() {
		this(-1);
	}

	/**
	 * Crée une nouvelle route de capacité donnée. Cette capacité est indicative
	 * et cette classe ne s'occupe pas des relations avions/routes.
	 */
	public Route(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Indique s'il s'agit d'une piste de type Landing.
	 */
	public boolean isLanding() {
		return landing;
	}

	/**
	 * Défini cette route en tant que route de Landing.
	 */
	public void setLanding() {
		landing = true;
	}

	/**
	 * Détermine le point de boucle de ce circuit.
	 */
	public Coordinates getLoopPoint() {
		for(int i = size() - 2; i >= 0; i--) {
			if(get(i).getType() == MoveType.STRAIGHT) {
				return get(i).getCoordiates();
			}
		}

		return null;
	}

	/**
	 * Retourne la capacité de la route telle que définie lors de sa création.
	 * Une capacité de -1 indique que la route n'a pas de capacité définie.
	 * 
	 * Une route de type Landing a toujours une capcité de 1 quelque soit la
	 * capacité passée en argument lors de sa créaction.
	 */
	public int getCapacity() {
		return (landing) ? 1 : capacity;
	}
}
