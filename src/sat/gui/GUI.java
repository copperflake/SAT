package sat.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import sat.events.EventListener;
import sat.radio.RadioEvent;
import sat.radio.RadioID;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

/**
 * This class represents the main window of the application. It mainly consists
 * of an AirportPanel, but also contains the two other windows of the
 * application (The journal and the list of downloaded files).
 */
public class GUI extends JFrame implements EventListener {

	private static final long serialVersionUID = -6319538639673860639L;
	public static HashMap<RadioID, Aircraft> aircrafts;
	private Radar radar;
	
	public GUI(final boolean hd) {
		// Create a window. The program will exit when the window is closed.
		// See http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html
		super("Airport");
		
		aircrafts = new HashMap<RadioID, Aircraft>();
		
		setSize(1500, 1000);
		// Pop the window in the middle of the screen. (Work correctly on a dual-screen btw.) 
		//setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		/**
		 * La classe Toolkit va nous permettre de récupérer la taille de l'écran.
		 */
		Toolkit toolkit = Toolkit.getDefaultToolkit ();
		Dimension screenSize = toolkit.getScreenSize();
		
		Box topRow = Box.createHorizontalBox();
		Box botRow = Box.createHorizontalBox();
		Box main = Box.createVerticalBox();
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		AirportPanel airportPanel = new AirportPanel();
		airportPanel.setPreferredSize(airportPanel.getBackgroundDimension());
		
		tabbedPane.addTab("2D View", airportPanel);

		JournalPanel journalPanel = new JournalPanel();
		DownloadPanel downloadPanel = new DownloadPanel();

		// Un bout de code affreux, mais c'est belle est bien le seul moyen d'obtenir le résultat esconté.
		// Merci Java.
		int topRowHeight = 783;
		topRow.setPreferredSize(new Dimension(topRow.getWidth(), topRowHeight));
		botRow.setPreferredSize(new Dimension(botRow.getWidth(), getHeight()-topRowHeight));
		
		topRow.add(tabbedPane);
		topRow.add(Box.createHorizontalStrut(5));
		topRow.add(downloadPanel);
		botRow.add(journalPanel);

		main.add(topRow);
		main.add(Box.createVerticalStrut(5));
		main.add(botRow);
		setContentPane(main);

		setVisible(true);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		/**
		 * 3D Stuff
		 */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Dimension dim = new Dimension(1109, 751);

				AppSettings settings = new AppSettings(true);
				settings.setWidth((int) dim.getWidth());
				settings.setHeight((int) dim.getHeight());
				settings.setFrameRate(40);
				settings.setSamples(hd?4:0);
				
				
				radar = new Radar(hd);
				radar.setSettings(settings);
				radar.createCanvas();
				radar.startCanvas(true);

				JmeCanvasContext ctx = (JmeCanvasContext) radar.getContext();
				ctx.setSystemListener(radar);
				ctx.getCanvas().setPreferredSize(dim);
				
				tabbedPane.addTab("3D View", ctx.getCanvas());
				tabbedPane.setVisible(true);
			}
		});
	}

	public void on(RadioEvent.PlaneConnected e) {
		// TODO Add listener(aircraft);
		Aircraft aircraft = new Aircraft();
		// TODO S'assurer que radar est instancié.
		aircraft.init3D(radar.getAssetManager(), radar.getRootNode(), e.getType());
		aircrafts.put(e.getID(), aircraft);
	}

	public void on(RadioEvent.PlaneDisconnected e) {
		// TODO Remove listener(aircraft);
		aircrafts.remove(e.getID());
	}
	
	public void on(RadioEvent.PlaneMoved e) {
		aircrafts.get(e.getID()).addDestination(e.getVector3f());
	}
	
	public void on(RadioEvent.PlaneDistress e) {
		// TODO Will crash if 3D Aircraft is not initialized (for example if there is no 3D GUI).
		aircrafts.get(e.getID()).setDistress3D(e.getDistress());
	}
}