package sat.gui3D;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

/**
 * This class represents the main window of the application. It mainly consists
 * of an AirportPanel, but also contains the two other windows of the
 * application (The journal and the list of downloaded files).
 */
public class AirportGUI extends JFrame {

	private static final long serialVersionUID = -6319538639673860639L;
	
	public AirportGUI(final boolean hd) {
		// Create a window. The program will exit when the window is closed.
		// See http://docs.oracle.com/javase/tutorial/uiswing/components/frame.html
		super("Airport");
		
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
				
				
				Radar radar = new Radar(hd);
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
