package sat.plane;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import sat.EndOfWorldException;
import sat.events.Event;
import sat.events.EventListener;
import sat.external.twitter.TweetSender;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.radio.client.RadioClient;
import sat.radio.client.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.message.Message;
import sat.radio.message.MessageBye;
import sat.radio.message.MessageLanding;
import sat.radio.message.MessageMayDay;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKey;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.file.DataFile;
import sat.utils.geo.Coordinates;
import sat.utils.geo.InvalidCoordinatesException;
import sat.utils.routes.MoveType;
import sat.utils.routes.Route;
import sat.utils.routes.Waypoint;

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
	 * La clé de cet avion.
	 */
	private RSAKeyPair keyPair;

	/**
	 * La position de l'avion.
	 */
	private Coordinates coords;

	/**
	 * L'identifiant de l'avion.
	 */
	private RadioID id;

	private Route route;

	private PlaneType type;

	private RadioClient radio;

	private PlaneSimulator simulator;

	/**
	 * Kérozène restants. [l]
	 */
	private float kerozene;

	private boolean initDone = false;

	public Plane() {
		if(defaults == null)
			initDefaults();

		config = new Config(defaults);
		route = new Route();
	}

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("plane.debug", "no");
		defaults.setProperty("plane.twitter", "no");

		defaults.setProperty("plane.coords", "0,0,-1");    // Initial coords
		defaults.setProperty("plane.waypoint", "625,445,-1");  // Initial waypoint

		defaults.setProperty("plane.prefix", "PLN");

		defaults.setProperty("plane.type", "A320");
		defaults.setProperty("plane.update", "100");
		defaults.setProperty("plane.fuel", "200000");

		defaults.setProperty("legacy.towerkey", "tower.key");

		defaults.setProperty("radio.ciphered", "yes");
		defaults.setProperty("radio.legacy", "no");
		defaults.setProperty("radio.keylength", "1024");
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

		// Initial Coords
		
		try {
			coords = Coordinates.parseCoordinates(config.getString("plane.coords"));
		}
		catch(InvalidCoordinatesException e) {
			coords = new Coordinates(0, 0, -1);
		}
		
		// Initial Waypoint
		
		Coordinates initalWaypoint;
		try {
			initalWaypoint = Coordinates.parseCoordinates(config.getString("plane.waypoint"));
		}
		catch(InvalidCoordinatesException e) {
			initalWaypoint = new Coordinates(625f, 445f, -1f);
		}
		
		route.add(new Waypoint(MoveType.STRAIGHT, initalWaypoint.toFloats()));

		// Plane Type
		type = PlaneType.getPlaneTypeByName(config.getString("plane.type"));
		if(type == null) { // getPlaneTypeByName return null for unknown types
			throw new EndOfWorldException("Invalid plane type");
		}

		// Initial fuel
		int initialFuel = config.getInt("plane.fuel");

		if(initialFuel > type.fuel) {
			initialFuel = type.fuel;
		}

		kerozene = initialFuel;

		// ID
		id = new RadioID(config.getString("plane.prefix"));

		// Radio
		radio = new RadioClient(this, id);
		radio.addListener(this);

		radio.setCiphered(config.getBoolean("radio.ciphered"));
		radio.setLegacy(config.getBoolean("radio.legacy"));

		initDone = true;
	}

	public void connect(RadioClientEngine engine) throws IOException {
		radio.connect(engine);
	}
	
	public boolean started() {
		return (simulator != null);
	}

	public void start() {
		if(!started()) {
			simulator = new PlaneSimulator();
			simulator.start();
		}
	}

	// - - - Events - - -
	
	public void on(RadioEvent.TowerConnected e) {
		radio.sendLandingRequest();
	}
	
	public void on(Event event) {
		System.out.println(event);
	}

	// - - - Plane Simulator - - -

	private class PlaneSimulator extends Thread {
		public void run() {
			while(true) {
				try {
					int updateInterval = config.getInt("plane.update");

					move(updateInterval);
					kerozene -= type.consumption * 60 * updateInterval / 1000;
					if(kerozene < type.fuel * 0.2)
						radio.sendMayDay("Less than 20% of kerozene.");

					sleep(updateInterval);
				}
				catch(InterruptedException e) {
				}
			}
		}

		// ROUTING
		private void move(double interval) {
			if(route.size() < 1) {
				System.out.println("Plane dont know where to fly, crashing");
				System.exit(1);
			}

			Waypoint waypoint = route.get(0);

			if(waypoint.getType() == MoveType.STRAIGHT || waypoint.getType() == MoveType.LANDING || waypoint.getType() == MoveType.DESTRUCTION)
				moveInStraightLine(interval, waypoint);

			if(waypoint.getType() == MoveType.CIRCULAR)
				moveInCircle(interval, waypoint);
		}

		// deltaTimeSeconds = interval entre chaque mouvement?
		private void moveInStraightLine(double interval, Waypoint instruction) {
			// Compute the normalized direction vector (dx, dy)
			double dx = instruction.getCoordiates().getX() - coords.getX();
			double dy = instruction.getCoordiates().getY() - coords.getY();
			double length = Math.sqrt(dy * dy + dx * dx);

			if(length > 0) {
				dx = dx / length;
				dy = dy / length;
			}

			// How much time will it take us to get there?
			double timeNeeded = length / type.speedAsPxPerSec();

			// Move the plane.
			double newX = coords.getX() + dx * interval * type.speedAsPxPerSec();
			double newY = coords.getY() + dy * interval * type.speedAsPxPerSec();
			coords = new Coordinates((float) newX, (float) newY, coords.getZ());

			// If we arrived, continue with the next road instruction
			if(interval >= timeNeeded) {
				if(instruction.getType() == MoveType.LANDING) {
					radio.sendBye();

					DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
					dateFormat.format(new Date());

					tweet("Plane #" + id + " has LANDED at " + dateFormat.toString() + ". #ICAirport13 · #ICITP2012");
				}
				else if(instruction.getType() == MoveType.DESTRUCTION) {
					radio.sendBye();

					DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
					dateFormat.format(new Date());

					tweet("Plane #" + id + " has AUTODESTRUCT-ITSELF at " + dateFormat.toString() + ". #ICAirport13 · #ICITP2012");
				}

				System.out.println("Plane " + id + " arrived atwaypoint (" + instruction.getCoordiates().getX() + ", " + instruction.getCoordiates().getY() + ").");
				route.remove(0);
				move(1000 * (interval - timeNeeded));
			}
		}

		private void tweet(String tweet) {
			if(config.getBoolean("plane.twitter")) {
				TweetSender.tweet(tweet);
			}
		}

		private void moveInCircle(double interval, Waypoint instruction) {
			//Moves the plane in circle around the given center, with an objective of given angle.
			double modX = coords.getX() - instruction.getCoordiates().getX();
			double modY = coords.getY() - instruction.getCoordiates().getY();
			double instructionAngle = Math.toRadians(instruction.getAngle());
			double instructionAngleSign = Math.signum(instructionAngle);
			double r = Math.sqrt(modX * modX + modY * modY);
			double theta = computeTheta(modX, modY, 0, 0);
			double rotSpeed = type.speedAsPxPerSec() / r;
			double deltaTheta = rotSpeed * interval;

			if(deltaTheta < Math.abs(instructionAngle)) {
				theta = theta + instructionAngleSign * deltaTheta;

				// TODO Make it cleaner
				route.remove(0);
				route.add(0, new Waypoint(MoveType.CIRCULAR, new float[] { instruction.getCoordiates().getX(), instruction.getCoordiates().getY(), (float) Math.toDegrees(instructionAngle - instructionAngleSign * deltaTheta) }));
				modX = r * Math.cos(theta) + instruction.getCoordiates().getX();
				modY = r * Math.sin(theta) + instruction.getCoordiates().getY();
				coords = new Coordinates((float) modX, (float) modY, coords.getZ());
			}
			else {
				theta += instructionAngle;

				route.remove(0);

				modX = (int) (r * Math.cos(theta) + instruction.getCoordiates().getX());
				modY = (int) (r * Math.sin(theta) + instruction.getCoordiates().getY());
				coords = new Coordinates((float) modX, (float) modY, coords.getZ());

				System.out.println("Plane " + id + " arrived at waypoint (" + modX + ", " + modY + ").");

				if(deltaTheta > Math.abs(instructionAngle)) {
					double neededTime = Math.abs(instructionAngle) / rotSpeed;
					move(1000 * (interval - neededTime));
				}
			}
		}

		private double computeTheta(double px, double py, double cx, double cy) {
			return Math.atan2(py - cy, px - cx);
		}
	}

	// - - - Plane Delegate - - -

	public Coordinates getLocation() {
		return coords;
	}

	public RSAKeyPair getKeyPair() {
		if(keyPair == null) {
			try {
				keyPair = new RSAKeyPair(config.getInt("radio.keylength"));
			}
			catch(RSAException e) {
				// Invalid key length, ignore given length and use default
				keyPair = new RSAKeyPair();
			}
		}

		return keyPair;
	}

	public RSAKeyPair getLegacyTowerKey() {
		try {
			FileInputStream fis = new FileInputStream(config.getString("legacy.towerkey"));
			DataInputStream dis = new DataInputStream(fis);

			@SuppressWarnings("unused")
			int keyLenght = dis.readInt();

			int modulusLength = dis.readInt();
			byte[] modulusBuffer = new byte[modulusLength];
			dis.readFully(modulusBuffer);
			BigInteger modulus = new BigInteger(modulusBuffer);

			int exponentLength = dis.readInt();
			byte[] exponentBuffer = new byte[exponentLength];
			dis.readFully(exponentBuffer);
			BigInteger exponent = new BigInteger(exponentBuffer);

			RSAKey publicKey = new RSAKey(exponent, modulus);
			RSAKeyPair keyPair = new RSAKeyPair(publicKey);

			return keyPair;
		}
		catch(IOException e) {
			System.out.println("Error reading legacy tower key");
			e.printStackTrace();
		}

		return null;
	}
}
