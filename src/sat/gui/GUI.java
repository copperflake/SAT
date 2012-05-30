package sat.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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

import sat.events.EventListener;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.radio.message.Message;
import sat.tower.TowerEvent;
import sat.tower.agent.TowerAgent;

/**
 * Cette classe va initialiser le GUI, l'afficher et récupérer les évenements
 * émis par le ToweeAgent. Elle créera une ou plusieurs fenêtres selon la taille
 * de l'écran de l'utilisateur.
 */
public class GUI extends JFrame implements EventListener {
	private static final long serialVersionUID = -6319538639673860639L;

	public static HashMap<RadioID, Aircraft> aircrafts;
	private Radar radar;
	private JPanel remoteMsg;
	private JournalPanel journalPanel;
	private DownloadPanel downloadPanel;
	private ArrayList<File> fileList;
	private boolean enable3D = true;

	/**
	 * Le constructeur crée ou non un onglet contenant la vue tridimentionnelle
	 * selon les paramètres du constructeur. Le paramètre HD permet de définir
	 * si la vue 3D doit être de haute qualité ou non.
	 * 
	 * @param hd
	 * @param agent
	 * @param enable3D
	 */
	public GUI(final boolean hd, TowerAgent agent, boolean enable3D) {
		// Create a window. The program will exit when the window is closed.
		// See http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html
		super("Airport");

		this.fileList = new ArrayList<File>();
		this.enable3D = enable3D;

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
		 * La classe Toolkit va nous permettre de récupérer la taille de
		 * l'écran.
		 */
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();

		AirportPanel airportPanel = new AirportPanel();
		airportPanel.setPreferredSize(airportPanel.getBackgroundDimension());

		journalPanel = new JournalPanel();

		ChokerPanel chockerPanel = new ChokerPanel(agent);

		if(!agent.isRemote()) {
			downloadPanel = new DownloadPanel();
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
			botRow.setPreferredSize(new Dimension(botRow.getWidth(), getHeight() - topRowHeight));

			topRow.add(tabbedPane);
			topRow.add(Box.createHorizontalStrut(5));

			if(!agent.isRemote()) {
				topRow.add(downloadPanel);
			}
			else {
				remoteMsg = new JPanel();
				remoteMsg.setLayout(new BoxLayout(remoteMsg, BoxLayout.Y_AXIS));
				JLabel label = new JLabel("<html>This is a remote interface.<br>If you want to see files, you have to check<br>manually on the server.</html>", SwingConstants.CENTER);
				remoteMsg.add(Box.createVerticalStrut(200));
				remoteMsg.add(label);
				remoteMsg.add(Box.createVerticalGlue());

				if(screenSize.getWidth() >= width && screenSize.getHeight() >= height) {
					remoteMsg.setPreferredSize(new Dimension(380, topRowHeight));
				}
				topRow.add(remoteMsg);
			}

			botRow.add(journalPanel);
			botRow.add(chockerPanel);

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

			if(!agent.isRemote()) {
				JFrame downloadFrame = new JFrame();
				downloadFrame.add(downloadPanel);
				downloadFrame.add(downloadPanel);
				downloadFrame.setSize(500, 600);
				downloadFrame.setVisible(true);
			}

			JFrame tabbedFrame = new JFrame();
			tabbedFrame.add(tabbedPane);
			tabbedFrame.setSize(new Dimension(1116, 779));
			tabbedFrame.setLocationRelativeTo(null);
			tabbedFrame.setResizable(false);
			tabbedFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			tabbedFrame.setVisible(true);
		}

		if(enable3D) {
			/**
			 * Initialise, configure et lance la vue 3D dans un nouvel onglet.
			 * Doit être invoquer une fois que le reste du GUI est prêt (selon
			 * la doc de jMonkeyEngine).
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Dimension dim = new Dimension(1109, 751);

					AppSettings settings = new AppSettings(true);
					settings.setWidth((int) dim.getWidth());
					settings.setHeight((int) dim.getHeight());
					settings.setFrameRate(40);
					settings.setSamples(hd ? 4 : 0);

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
	}

	/**
	 * Créer un Aircraft quand un avion se connecte.
	 * 
	 * @param e
	 */
	public void on(RadioEvent.PlaneConnected e) {
		Aircraft aircraft = new Aircraft(e.getID());
		aircrafts.put(e.getID(), aircraft);

		if(enable3D && radar != null) {
			aircraft.init3D(radar.getRootNode(), radar.getAssetManager());
		}
	}

	/**
	 * Détruit l'Aircraft quand un avion se déconnecte.
	 * 
	 * @param e
	 */
	public void on(RadioEvent.PlaneDisconnected e) {
		if(enable3D && radar != null) {
			aircrafts.get(e.getID()).destroy();
		}

		aircrafts.remove(e.getID());
	}

	/**
	 * Synchronise la position de l'avion dès qu'on reçoit sa position.
	 * 
	 * @param e
	 */
	public void on(TowerEvent.PlaneMoved e) {
		aircrafts.get(e.getID()).addDestination(e.getWhere());
	}

	/**
	 * Passe l'Aircraft en mode MayDay
	 * 
	 * @param e
	 */
	public void on(TowerEvent.PlaneDistress e) {
		aircrafts.get(e.getID()).setDistress2D();

		if(enable3D && radar != null) {
			aircrafts.get(e.getID()).setDistress3D(true);
		}
	}

	/**
	 * Ajoute un fichier au DownloadPanel dès que l'on en reçoit un.
	 * 
	 * @param e
	 */
	public void on(TowerEvent.TransferComplete e) {
		fileList.add(new File(e.getPath()));
		downloadPanel.addFilesToDownloadBox(fileList);
	}

	public void on(TowerEvent.PlaneIdentified e) {

	}

	/**
	 * Ajoute les Messages au JournalPanel.
	 * 
	 * @param m
	 */
	@SuppressWarnings("unchecked")
	public void on(RadioEvent.MessageReceived e) {
		Message m = e.getMessage();
		Vector v = new Vector();
		v.add(m.getPriority());
		v.add(m.getType().toString());
		v.add(m.getID().toString());
		v.add("Tower");
		v.add(m.getDate());
		journalPanel.addEvent(v);
	}

	/**
	 * Ajoute les Messages au JournalPanel.
	 * 
	 * @param m
	 */
	@SuppressWarnings("unchecked")
	public void on(RadioEvent.MessageSent e) {
		Message m = e.getMessage();
		Vector v = new Vector();
		v.add(m.getPriority());
		v.add(m.getType().toString());
		v.add("Tower");
		v.add(m.getID().toString());
		v.add(m.getDate());
		journalPanel.addEvent(v);
	}
}
