package sat.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ChokerPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JButton choke;
	private boolean choked;
	
	public ChokerPanel() {
		choked = false;
		choke = new JButton("Choke");
		
		choke.setPreferredSize(new Dimension(100, 25));
		choke.addActionListener(this);
		
		add(Box.createHorizontalStrut(20));
		add(choke);
		add(Box.createHorizontalStrut(20));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj == choke) {
			if(!choked) {
				choke.setText("Unchoke");
			}
			else {
				choke.setText("Choke");
			}
			choked = !choked;
		}
	}
}
