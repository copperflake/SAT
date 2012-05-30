package sat.tower;

import sat.events.Event;
import sat.plane.PlaneType;
import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

/**
 * Evenements générés par la tour.
 */
@SuppressWarnings("serial")
public abstract class TowerEvent extends Event {
	/**
	 * Evenement généré par la tour et lié à un avion.
	 */
	public static abstract class PlaneTowerEvent extends TowerEvent {
		/**
		 * L'id de l'avion auquel se rapport ce événement.
		 */
		RadioID id;

		/**
		 * Crée un nouvel événement d'avion.
		 */
		public PlaneTowerEvent(RadioID id) {
			this.id = id;
		}

		/**
		 * Retourne l'identifiant de l'avion.
		 */
		public RadioID getID() {
			return id;
		}
	}

	/**
	 * L'avion a bougé.
	 */
	public static class PlaneMoved extends PlaneTowerEvent {
		/**
		 * Les nouvelles coordonnées de cet avion.
		 */
		private Coordinates where;

		public PlaneMoved(RadioID id, Coordinates where) {
			super(id);
			this.where = where;
		}

		public Coordinates getWhere() {
			return where;
		}
	}

	/**
	 * Cet évenement est utilisé lorsque l'avion envoi un MayDay à la tour de
	 * contrôle ou enlève son MayDay pour signaler que le problème à été résolu
	 * (si cela est possible).
	 */
	public static class PlaneDistress extends PlaneTowerEvent {
		public PlaneDistress(RadioID id) {
			super(id);
		}
	}

	/**
	 * L'avion a été identifié.
	 */
	public static class PlaneIdentified extends PlaneTowerEvent {
		/**
		 * Le type de l'avion nouvellement identifié.
		 */
		private PlaneType type;

		public PlaneIdentified(RadioID id, PlaneType type) {
			super(id);
			this.type = type;
		}

		public PlaneType getType() {
			return type;
		}
	}

	public static class TransferComplete extends TowerEvent {
		/**
		 * Chemin vers le fichier téléchargé.
		 */
		private String path;

		public TransferComplete(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}
}
