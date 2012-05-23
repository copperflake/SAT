package sat.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ChokerPanel extends JPanel {
	public ChokerPanel() {
		BoxLayout layoutX = new BoxLayout(this, BoxLayout.X_AXIS);
		BoxLayout layoutY = new BoxLayout(this, BoxLayout.Y_AXIS);
		
		setLayout(layoutX);
		
		JPanel content = new JPanel();
		content.setLayout(layoutY);
		
		JButton btn = new JButton("Unchoke");
		
		content.add(Box.createVerticalGlue());
		content.add(btn);
		content.add(Box.createVerticalGlue());

		add(Box.createHorizontalStrut(20));
		add(content);
		add(Box.createHorizontalStrut(20));
		System.out.println(btn.getWidth());
	}
}
