package sat.tower.gui;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.*;

import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;

public class Map implements GLEventListener {
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		
		GLCanvas canvas = new GLCanvas();
		
		canvas.addGLEventListener(new Map());

		frame.add(canvas);
		final Animator animator = new Animator(canvas);
		
		frame.setSize(500, 300);
		frame.setLocation(400, 200);
		frame.setVisible(true);

		animator.start();
	}

	@Override
	public void display(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}
}
