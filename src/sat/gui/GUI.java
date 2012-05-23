package sat.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import sat.events.Event;
import sat.events.EventListener;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.tower.TowerEvent;
import sat.tower.agent.TowerAgent;

/**
 * This class represents the main window of the application. It mainly consists
 * of an AirportPanel, but also contains the two other windows of the
 * application (The journal and the list of downloaded files).
 */
public class GUI extends JFrame implements EventListener {

	private static final long serialVersionUID = -6319538639673860639L;
	public static HashMap<RadioID, Aircraft> aircrafts;
	private Radar radar;
	private TowerAgent agent;
	
	public GUI(final boolean hd, TowerAgent agent) {
		// Create a window. The program will exit when the window is closed.
		// See http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html
		super("Airport");
		
		this.agent = agent;
		agent.addListener(this);
		
		aircrafts = new HashMap<RadioID, Aircraft>();

		int width = 1500;
		int height = 950;
		int topRowHeight = 783;
		setSize(width, height);
		
		// Pop the window in the middle of the screen. (Work correctly on a dual-screen btw.)
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		/**
		 * La classe Toolkit va nous permettre de récupérer la taille de l'écran.
		 */
		Toolkit toolkit = Toolkit.getDefaultToolkit ();
		Dimension screenSize = toolkit.getScreenSize();
		
		AirportPanel airportPanel = new AirportPanel();
		airportPanel.setPreferredSize(airportPanel.getBackgroundDimension());

		JournalPanel journalPanel = new JournalPanel();
		//ChokerPanel chockerPanel = new ChokerPanel();

		JPanel downloadPanel;
		
		if(!agent.isRemote()) {
			downloadPanel = new DownloadPanel();
		}
		else {
			downloadPanel = new JPanel();
			downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
			JLabel label = new JLabel("<html>This is a remote interface.<br>If you want to see files, you have to check<br>manually on the server.</html>", SwingConstants.CENTER);
			downloadPanel.add(Box.createVerticalStrut(200));
			downloadPanel.add(label);
			downloadPanel.add(Box.createVerticalGlue());
			if(screenSize.getWidth() >= width && screenSize.getHeight() >= height) {
				downloadPanel.setPreferredSize(new Dimension(380, topRowHeight));
			}
		}
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("2D View", airportPanel);
		
		if(screenSize.getWidth() >= width && screenSize.getHeight() >= height) {
			Box topRow = Box.createHorizontalBox();
			Box botRow = Box.createHorizontalBox();
			Box main = Box.createVerticalBox();
	
			// Un bout de code affreux, mais c'est belle est bien le seul moyen d'obtenir le résultat esconté.
			// Merci Swing.
			topRow.setPreferredSize(new Dimension(topRow.getWidth(), topRowHeight));
			botRow.setPreferredSize(new Dimension(botRow.getWidth(), getHeight()-topRowHeight));
			
			topRow.add(tabbedPane);
			topRow.add(Box.createHorizontalStrut(5));
			topRow.add(downloadPanel);
			botRow.add(journalPanel);
			//botRow.add(chockerPanel);
	
			main.add(topRow);
			main.add(Box.createVerticalStrut(5));
			main.add(botRow);
			setContentPane(main);
	
			setVisible(true);
		}
		else {
			JFrame journalFrame = new JFrame();
			journalFrame.add(journalPanel);
			journalFrame.setSize(700, 500);
			journalFrame.setVisible(true);

			JFrame downloadFrame = new JFrame();
			downloadFrame.add(downloadPanel);
			downloadFrame.setSize(500, 600);
			downloadFrame.setVisible(true);

			JFrame tabbedFrame = new JFrame();
			tabbedFrame.add(tabbedPane);
			tabbedFrame.setSize(new Dimension(1116, 779));
			tabbedFrame.setLocationRelativeTo(null);
			tabbedFrame.setResizable(false);
			tabbedFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			tabbedFrame.setVisible(true);
		}
		
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
		Aircraft aircraft = new Aircraft(e.getID());
		// TODO S'assurer que radar est instancié.
		aircraft.init3D(radar.getRootNode(), radar.getAssetManager());
		aircrafts.put(e.getID(), aircraft);
	}

	public void on(RadioEvent.PlaneDisconnected e) {
		aircrafts.get(e.getID()).destroy();
		aircrafts.remove(e.getID());
	}

	public void on(TowerEvent.PlaneMoved e) {
		//System.out.println(e.getWhere().toString());
		aircrafts.get(e.getID()).addDestination(e.getWhere());
	}

	public void on(RadioEvent.PlaneDistress e) {
		// TODO Will crash if 3D Aircraft is not initialized (for example if there is no 3D GUI).
		aircrafts.get(e.getID()).setDistress();
	}
	
//	public void on(Event e) {
//		System.out.println(e);
//	}
}
