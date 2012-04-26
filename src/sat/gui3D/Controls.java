package sat.gui3D;

import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

public class Controls {
	private InputManager inputManager;
	private boolean moveEnable, rotateEnable;
	private Radar app;

	public Controls(InputManager inputManager, Radar app) {
		this.inputManager = inputManager;
		this.moveEnable = false;
		this.rotateEnable = false;
		this.app = app;
	}

	public void setupControls() {
		// Removing some shortcut
		inputManager.deleteMapping("FLYCAM_Left");
		inputManager.deleteMapping("FLYCAM_Right");
		inputManager.deleteMapping("FLYCAM_Up");
		inputManager.deleteMapping("FLYCAM_Down");
		inputManager.deleteMapping("FLYCAM_StrafeLeft");
		inputManager.deleteMapping("FLYCAM_StrafeRight");
		inputManager.deleteMapping("FLYCAM_Forward");
		inputManager.deleteMapping("FLYCAM_Backward");
		inputManager.deleteMapping("FLYCAM_Rise");
		inputManager.deleteMapping("FLYCAM_Lower");
		
		// Keyboard
		// -- empty
		
		// Mouse
		inputManager.addMapping("RotateEnable", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("MoveEnable", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addMapping("MoveX-L", new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("MoveX-R", new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("MoveY-U", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping("MoveY-D", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

		moveEnable = false;
		rotateEnable = false;
		inputManager.setCursorVisible(true);

		inputManager.addListener(actionListener, new String[] { "Exit", "MoveEnable", "RotateEnable" });
		inputManager.addListener(analogListener, new String[] { "MoveX-L", "MoveX-R", "MoveY-U", "MoveY-D", "ZoomIn", "ZoomOut" });
	}

	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if(name.equals("MoveEnable")) {
				//inputManager.setCursorVisible(!keyPressed);
				moveEnable = keyPressed;
			}
			else if(name.equals("RotateEnable")) {
				//inputManager.setCursorVisible(!keyPressed);
				rotateEnable = keyPressed;
			}
		}
	};

	private AnalogListener analogListener = new AnalogListener() {
		public void onAnalog(String name, float value, float tpf) {
			if(name.equals("MoveX-L")) {
				if(moveEnable && !rotateEnable)
					app.moveCamSide(-value);
				else if(rotateEnable && !moveEnable)
					app.rotateCamH(-value);
			}
			else if(name.equals("MoveX-R")) {
				if(moveEnable && !rotateEnable)
					app.moveCamSide(value);
				else if(rotateEnable && !moveEnable)
					app.rotateCamH(value);
			}
			else if(name.equals("MoveY-U")) {
				if(moveEnable && !rotateEnable)
					app.moveCamFront(-value);
				else if(rotateEnable && !moveEnable)
					app.rotateCamV(-value);
				else if(moveEnable && rotateEnable)
					app.moveCamY(-value);
			}
			else if(name.equals("MoveY-D")) {
				if(moveEnable && !rotateEnable)
					app.moveCamFront(value);
				else if(rotateEnable && !moveEnable)
					app.rotateCamV(value);
				else if(moveEnable && rotateEnable)
					app.moveCamY(value);
			}
			else if(name.equals("ZoomIn"))
				app.zoom(value);
			else if(name.equals("ZoomOut"))
				app.zoom(-value);
		}
	};
}
