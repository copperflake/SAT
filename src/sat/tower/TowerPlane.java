package sat.tower;

import sat.plane.PlaneType;
import sat.radio.RadioID;

/**
 * Un avion interne à la tour. Il maintient une série d'informations nécessaires
 * au bon fonctionnement de la tour.
 * 
 * Cette classe ne fourni pas une interface à un avion, simplement un collection
 * de valeurs attachées à un avion particulier.
 */
public class TowerPlane {
	// - - - Landing Tickets System - - -

	/**
	 * Le prochain ID d'atterrissage.
	 */
	private static int nextLandingID = 0;

	/**
	 * Génère et retourne le prochain ID d'atterrissage.
	 */
	private synchronized static int getNextLandingID() {
		return nextLandingID++;
	}

	/**
	 * L'ID de cet avion.
	 */
	private RadioID id;

	/**
	 * Son numéro d'atterrissage, ou -1 si cet avion n'a pas demandé à atterrir.
	 */
	private int landingID = -1;

	/**
	 * La route actuelle de cet avion, ou -1 s'il n'a jamais été routé ou s'il
	 * est envoyé au cimetierre.
	 */
	private int currentRoute = -1;

	/**
	 * Indique si cet avion est en situation critique.
	 */
	private boolean mayDay = false;

	/**
	 * Le type de cet avion.
	 */
	private PlaneType type;

	/**
	 * Indique si cet avion est en cours d'atterrissage. Dans cet état, un
	 * avions ne plus plus être modifié et son atterrissage ne peut être annulé.
	 */
	private boolean landing;

	/**
	 * Crée un nouvel avion interne à la tour.
	 */
	public TowerPlane(RadioID id) {
		this.id = id;
	}

	/**
	 * La route actuelle.
	 */
	public int getCurrentRoute() {
		return currentRoute;
	}

	/**
	 * Modifie la route actuelle.
	 */
	public void setCurrentRoute(int currentRoute) {
		this.currentRoute = currentRoute;
	}

	/**
	 * L'avion est en sitation de MayDay.
	 */
	public boolean isMayDay() {
		return mayDay;
	}

	/**
	 * Défini l'état d'alerte pour cet avion. Cette méthode est sans effet si
	 * l'avion est en cours d'atterrissage.
	 */
	public void setMayDay(boolean mayDay) {
		if(!isLanding()) {
			this.mayDay = mayDay;
		}
	}

	/**
	 * Retourne l'identifiant de cet avion.
	 */
	public RadioID getID() {
		return id;
	}

	/**
	 * L'ID d'atterrissage de cet avion.
	 */
	public int getLandingID() {
		return landingID;
	}

	/**
	 * Déclanche la demande d'atterrissage et d'obtention de ticket
	 * d'atterrissage.
	 */
	public void landingRequested() {
		if(landingID == -1) {
			landingID = getNextLandingID();
		}
	}

	/**
	 * Retourne le type de l'avion.
	 */
	public PlaneType getType() {
		return type;
	}

	/**
	 * Défini le type de cet avion. Cette méthode est sans effet si l'avion est
	 * en cours d'atterrissage.
	 */
	public void setType(PlaneType type) {
		if(!isLanding()) {
			this.type = type;
		}
	}

	/**
	 * Indique si cet avion est en cours d'atterrissage.
	 */
	public boolean isLanding() {
		return landing;
	}

	/**
	 * Place cet avion en mode atterrissage.
	 */
	public void setLanding() {
		this.landing = true;
	}
}
