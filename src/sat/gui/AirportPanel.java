package sat.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import sat.radio.RadioID;

/**
 * This class draws an airport and the planes. It makes the following assumption
 * about the rest of the code. Please modify this class or your code
 * accordingly; use your good judgment to produce the most elegant code you can
 * :)
 * 
 * - It uses the method public static Tower getInstance() of the Tower class to
 * retrieve the tower singleton object. - - It uses the method public
 * HashMap<RadioID, Plane> getPlanes() of the Tower class to retrieve a list of
 * planes that are currently connected to the Tower - It uses the methods public
 * int getLocation().getX() and public int getLocation().getY() of the
 * TowerAgent class to retrieve the position of the airplanes. This position is
 * expected to satisfy 0 <= x <= 1109 and 0 <= y <= 751 if the airplane should
 * be displayed. - It considers a plane crashed when the hasCrashed() method of
 * the class TowerAgent returns true.
 */
public class AirportPanel extends JPanel {

	private static final long serialVersionUID = 5254715425216686775L;

	// The various images used in the GUI
	private static BufferedImage imgBack;
	private static BufferedImage imgPlane;
	private static BufferedImage imgTower;
	private static BufferedImage imgKboom;
	private static BufferedImage imgRadar;

	// The position of the tower in the background image
	private static final int TOWER_X = 625;
	private static final int TOWER_Y = 445;

	// The time it takes the radar beam to make a full circle, in milliseconds
	private static final int RADAR_PERIOD = 5000;

	// How long should aircraft trails be (in milliseconds)?
	private static final int AIRCRAFT_TRAIL_DURATION = 5000;

	// Desired framerate (in frames per second)
	public static final int AIRPORT_PANEL_FRAMERATE = 10;

	public AirportPanel() {
		try {
			// We actually use it in Aircraft.
			this.imgBack = ImageIO.read(new File("assets/Images/map.png"));
			this.imgPlane = ImageIO.read(new File("assets/Images/plane.png"));
			this.imgTower = ImageIO.read(new File("assets/Images/tower.png"));
			this.imgKboom = ImageIO.read(new File("assets/Images/kboom.png"));
			this.imgRadar = ImageIO.read(new File("assets/Images/radar.png"));
		}
		catch(IOException e) {
			System.err.println("Cannot read image files: " + e.getMessage());
			System.exit(1);
		}

		// Repaints the user interface regularly
		// Be careful, don't set a framerate too high
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 0, 1000 / AIRPORT_PANEL_FRAMERATE);
		//Tower.getInstance().addListener(this);
	}

	public Dimension getBackgroundDimension() {
		return new Dimension(imgBack.getWidth(), imgBack.getHeight());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(imgBack, 0, 0, imgBack.getWidth(), imgBack.getHeight(), null);
		g.drawImage(imgTower, TOWER_X-imgTower.getWidth()/2, TOWER_Y-imgTower.getHeight()/2, imgTower.getWidth(), imgTower.getHeight(), null);

		// Draw the circles around the tower
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.GREEN);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g2d.setStroke(new BasicStroke(3F));
		for(int i=0; i < 10; i++) {
			g2d.drawOval(TOWER_X-i*75,TOWER_Y-i*75,i*150,i*150);
		}

		// Draw the animated radar beam
		long currentTime = System.currentTimeMillis();
		double radarAngle = 2*Math.PI*currentTime/RADAR_PERIOD;
		g2d.setTransform(AffineTransform.getRotateInstance(radarAngle, TOWER_X, TOWER_Y));
		g2d.drawImage(imgRadar, TOWER_X, TOWER_Y, imgRadar.getWidth(), imgRadar.getHeight(), null);
		g2d.setTransform(AffineTransform.getRotateInstance(0));

		// Update Aircrafts
		for(RadioID id : GUI.aircrafts.keySet()) {
			GUI.aircrafts.get(id).update2D(g2d);
		}
	}

	public static BufferedImage getImgBack() {
		return imgBack;
	}
	public static BufferedImage getImgPlane() {
		return imgPlane;
	}
	public static BufferedImage getImgTower() {
		return imgTower;
	}
	public static BufferedImage getImgKboom() {
		return imgKboom;
	}
	public static BufferedImage getImgRadar() {
		return imgRadar;
	}
}

