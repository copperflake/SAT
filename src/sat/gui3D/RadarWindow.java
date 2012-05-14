package sat.gui3D;

import javax.swing.*;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

public class RadarWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8433057670591374294L;

	public RadarWindow() {
		this.setSize(1000, 1000);
		
		Radar radar = new Radar();
		
		AppSettings settings = new AppSettings(true);
		radar.setShowSettings(false);
		radar.setDisplayStatView(false);
		radar.setPauseOnLostFocus(false);
		settings.setFrameRate(40);
		radar.setSettings(settings);
		
		radar.createCanvas();
		JmeCanvasContext ctx = (JmeCanvasContext) radar.getContext();
		add(ctx.getCanvas());
		
		setVisible(true);
		setTitle("GUI · Secure Airport Tower");
	}
}
