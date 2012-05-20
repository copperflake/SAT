package sat.plane;

import java.io.IOException;

import sat.EndOfWorldException;
import sat.events.EventListener;
import sat.radio.RadioID;
import sat.radio.client.RadioClient;
import sat.radio.client.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;

public class Plane implements EventListener, RadioClientDelegate {
	/**
	 * La configuration par défaut d'un avion. Sert de modèle à la contruction
	 * de la configuration spécifique aux instances d'avions.
	 */
	private static Config defaults;

	/**
	 * La configuration spécifique à une instance d'un avion.
	 */
	private Config config;

	/**
	 * La position de l'avion.
	 */
	private Coordinates coords;

	/**
	 * L'identifiant de l'avion.
	 */
	private RadioID id;

	private PlaneType type;

	private RadioClient radio;

	private PlaneSimulator simulator;

	private boolean initDone = false;

	public Plane() {

		if(defaults == null)
			initDefaults();

		config = new Config(defaults);
	}

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("radio.debug", "no");
		defaults.setProperty("radio.ciphered", "yes");
		defaults.setProperty("radio.legacy", "no");

		defaults.setProperty("plane.coords", "0,0,0");
		defaults.setProperty("plane.type", "A320");
		defaults.setProperty("plane.prefix", "PLN");
	}

	/**
	 * Retourne l'objet de configuration de l'avion.
	 */
	public Config getConfig() {
		return config;
	}

	public void init() {
		if(initDone) {
			return;
		}

		coords = new Coordinates(0, 0, 0);

		type = PlaneType.getPlaneTypeByName(config.getString("plane.type"));
		if(type == null) {
			throw new EndOfWorldException("Invalid plane type");
		}

		id = new RadioID(config.getString("plane.prefix"));

		initDone = true;
	}

	public void connect(RadioClientEngine engine) throws IOException {
		if(radio == null) {
			radio = new RadioClient(this, id);
		}

		radio.connect(engine);
	}

	public void start() {
		simulator = new PlaneSimulator();
		simulator.start();
	}

	// - - - Plane Simulator - - -

	private class PlaneSimulator extends Thread {
		public void run() {
			// TODO temp param
			long time = 1000;
			float consumption;
			while(true) {
				try {
					// TODO Diminuer le Kerozène
					// TODO Un avion envoie un message MAYDAY si son niveau de kérosène est inférieur à 20% de sa capacité maximale.
					// TODO Calculer la position de l'avion selon la route
					// TODO Si l'avion est arrivé au bout de sa route, on remove la dernière instruction.
					sleep(time);
				}
				catch(InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// ROUTING
	private void moveInStraightLine(double deltaTimeSeconds, RoadInstruction instruction) {
		// Compute the normalized direction vector (dx, dy)
		double dx = instruction.getxCoord() - roadMap.getCurrentPosx();
		double dy = instruction.getyCoord() - roadMap.getCurrentPosy();
		double length = Math.sqrt(dy * dy + dx * dx);
		if(length > 0) {
			dx = dx / length;
			dy = dy / length;
		}
		// How much time will it take us to get there?
		double timeNeeded = length / engine.getSpeed();
		double timeTraveled = (timeNeeded > deltaTimeSeconds) ? deltaTimeSeconds : timeNeeded;
		// Move the plane.
		double newX = roadMap.getCurrentPosx() + dx * timeTraveled * engine.getSpeed();
		double newY = roadMap.getCurrentPosy() + dy * timeTraveled * engine.getSpeed();
		roadMap.setNewPlanePosition(newX, newY);
		// If we arrived, continue with the next road instruction
		if(timeTraveled == timeNeeded) {
			System.out.println("Plane " + planeId + " arrived atwaypoint (" + instruction.getxCoord() + ", " + instruction.getyCoord() + ").");
			roadMap.removeFirstInstruction();
			movePlane(1000 * (deltaTimeSeconds - timeTraveled));
		}
	}
	
	private void moveInCircle(double deltaTimeSeconds, RoadInstruction current) {
		//Moves the plane in circle around the given center, with an objective of given angle.
		RotationInstruction instruction = (RotationInstruction) current;
		double modX = roadMap.getCurrentPosx() - instruction.getxCoord();
		double modY = roadMap.getCurrentPosy() - instruction.getyCoord();
		double instructionAngle = Math.toRadians(instruction.getAngle());
		double instructionAngleSign = Math.signum(instructionAngle);
		double r = Math.sqrt(modX * modX + modY * modY);
		double theta = computeTheta(modX, modY, 0, 0);
		double rotSpeed = engine.getSpeed() / r;
		double deltaTheta = rotSpeed * deltaTimeSeconds;
		if(deltaTheta < Math.abs(instructionAngle)) {
			theta = theta + instructionAngleSign * deltaTheta;
			roadMap.removeFirstInstruction();
			roadMap.addNewFirstRoadPoint(new RotationInstruction(instruction.getxCoord(), instruction.getyCoord(), Math.toDegrees(instructionAngle - instructionAngleSign * deltaTheta)));
			modX = (int) (r * Math.cos(theta) + instruction.getxCoord());
			modY = (int) (r * Math.sin(theta) + instruction.getyCoord());
			roadMap.setNewPlanePosition(modX, modY);
		}
		else {
			theta = theta + instructionAngle;
			roadMap.removeFirstInstruction();
			modX = (int) (r * Math.cos(theta) + instruction.getxCoord());
			modY = (int) (r * Math.sin(theta) + instruction.getyCoord());
			roadMap.setNewPlanePosition(modX, modY);
			System.out.println("Plane " + planeId + " arrived atwaypoint (" + modX + ", " + modY + ").");
			if(deltaTheta > Math.abs(instructionAngle)) {
				double neededTime = Math.abs(instructionAngle) / rotSpeed;
				movePlane(1000 * (deltaTimeSeconds - neededTime));
			}
		}
	}
	
	private double computeTheta(double px, double py, double cx, double cy) {
		return Math.atan2(py - cy, px - cx);
	}

	// - - - Plane Delegate - - -

	public Coordinates getLocation() {
		return coords;
	}
}
