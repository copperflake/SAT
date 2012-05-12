package sat.gui3D;

import com.jme3.asset.AssetManager;
import com.jme3.math.*;
import com.jme3.scene.*;
import sat.utils.geo.*;

public class Aircraft {
	private Node mainNode;
	private Node simsWrapper;
	private Geometry model;
	private Geometry sims;

	private CircularBuffer<Vector3f> path = new CircularBuffer<Vector3f>(50);
	
	public Aircraft(AssetManager assetManager, Node parent, Vector3f initPos) {
		simsWrapper = new Node();
		mainNode = new Node();
		
		model = (Geometry) assetManager.loadModel("Models/plane.obj");
		model.getMaterial().setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
		mainNode.attachChild(model);
		
		sims = (Geometry) assetManager.loadModel("Models/sims.obj");
		sims.getMaterial().setColor("Ambient", new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
		sims.scale(0.3f);
		simsWrapper.attachChild(sims);
		
		simsWrapper.setLocalTranslation(0f, 1f, 0f);
		mainNode.attachChild(simsWrapper);
		
		path.add(initPos);
		
		parent.attachChild(mainNode);
	}
	
	public Node getNode() {
		return mainNode;
	}
	
	public void addDestination(Vector3f pos) {
		path.add(pos);
	}
	
	public void move() {
		Vector3f coords = path.get(path.size()-1);
		mainNode.setLocalTranslation(coords.getX(), coords.getZ(), coords.getY()); // Changement de référentiel
		rotate();
	}
	
	private void rotate() {
		// According to: http://en.wikipedia.org/wiki/Yaw,_pitch_and_roll
		float yaw, pitch, roll;
		
		if(path.size()-2 >= 0) {
			roll = 0f;
			Vector3f end = path.get(path.size()-1);
			Vector3f mid = path.get(path.size()-2);
			Vector3f dirYaw = new Vector3f(end.getX()-mid.getX(), end.getY()-mid.getY(), 0f).normalize();
			Vector3f dirPitch = new Vector3f(end.getX()-mid.getX(), end.getY()-mid.getY(), end.getZ()-mid.getZ()).normalize();
			
			// Roll
			if(path.size()-3 >= 0) {
				float fac = 1f;
				Vector3f start = path.get(path.size()-3);
				Vector3f v1 = new Vector3f(mid.getX()-start.getX(), mid.getY()-start.getY(), 0f).normalize();
				Vector3f v2 = dirYaw;
				roll = v1.angleBetween(v2)*fac;
				
				if(v1.getY()-v2.getY() > 0)
					roll *= -1;
				if(v1.getX()-v2.getX() < 0)
					roll *= -1;
				if(roll > Math.PI/3)
					roll = (float) (Math.PI/3);
				if(roll < -Math.PI/3)
					roll = (float) -(Math.PI/3);
			}
			
			// Pitch and Yaw
			yaw = dirYaw.angleBetween(Vector3f.UNIT_X);
			if(dirYaw.getY() > 0)
				yaw *= -1;
			
			pitch = dirPitch.angleBetween(dirYaw);
			if(dirPitch.getZ() < 0)
				pitch *= -1;
		}
		
		else {
			yaw = 0f;
			pitch = 0f;
			roll = 0f;
		}

		mainNode.setLocalRotation(new Quaternion(new float[]{roll, yaw, pitch}));
	}
	
	public void update(float t) {
		sims.setLocalTranslation(sims.getLocalTranslation().x, (float) (Math.sin(t*2f)+1)*sims.getLocalScale().y*2, sims.getLocalTranslation().z);
		sims.rotate(0f, 0.01f, 0f);
		move();
	}
}
