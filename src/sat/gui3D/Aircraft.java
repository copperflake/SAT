package sat.gui3D;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import sat.utils.geo.*;

public class Aircraft {
	private Node mainNode;
	private Node simsWrapper;
	private Geometry model;
	private Geometry sims;
	private Vector3f currentPos, initPos;
	private AssetManager assetManager;
	private Node parent;
	private Mesh lineMesh;

	private CircularBuffer<Vector3f> path = new CircularBuffer<Vector3f>(50);
	
	public Aircraft(AssetManager assetManager, Node parent) {
		this.assetManager = assetManager;
		this.parent = parent;
		simsWrapper = new Node();
		mainNode = new Node();
		lineMesh = new Mesh();
		
		initPos = new Vector3f(0f, 0f, 0f);
		currentPos = initPos;
		drawAircraft(initPos);
		drawTrace();
		move();
	}
	
	public Node getNode() {
		return mainNode;
	}
	
	public void addDestination(Vector3f pos) {
		path.add(pos);
	}
	
	public void move() {
		if(path.size()-1 >= 0) {
			Vector3f coords = path.get(path.size()-1);
			if(!currentPos.equals(coords)) {
				mainNode.setLocalTranslation(coords.getX(), coords.getZ(), coords.getY()); // Changement de référentiel
				rotate();
				updateTrace();
				currentPos = coords;
			}
		}
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
				float fac = 100f;
				Vector3f start = path.get(path.size()-3);
				Vector3f v1 = new Vector3f(mid.getX()-start.getX(), mid.getY()-start.getY(), 0f).normalize();
				Vector3f v2 = dirYaw;
				roll = v1.angleBetween(v2)*fac;
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
	
	private void drawAircraft(Vector3f initPos) {
		model = (Geometry) assetManager.loadModel("Models/plane.obj");
		model.getMaterial().setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
		mainNode.attachChild(model);
		
		sims = (Geometry) assetManager.loadModel("Models/sims.obj");
		sims.getMaterial().setColor("Ambient", new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
		sims.scale(0.3f);
		simsWrapper.attachChild(sims);
		
		simsWrapper.setLocalTranslation(0f, 1f, 0f);
		mainNode.scale(2f);
		mainNode.attachChild(simsWrapper);
		
		path.add(initPos);
		
		parent.attachChild(mainNode);
	}
	
	private void drawTrace() {
		lineMesh.setMode(Mesh.Mode.Lines);
		lineMesh.setLineWidth(4f);
		Geometry lineGeometry = new Geometry("line", lineMesh);
		Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		lineMaterial.setColor("Color", ColorRGBA.Green);
		lineGeometry.setMaterial(lineMaterial);
		parent.attachChild(lineGeometry);
	}
	
	private void updateTrace() {
		Vector3f[] vertices = new Vector3f[path.size()];
		int[] indexes = new int[path.size()*2];
		
		for (int i=0; i < path.size(); i++) {
			vertices[i] = new Vector3f(path.get(i).getX(), path.get(i).getZ(), path.get(i).getY());
			if(i > 0) {
				indexes[i*2] = i-1;
				indexes[i*2+1] = i;
			}
		}
		
		lineMesh.updateBound();
		lineMesh.updateCounts();
		
		lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		lineMesh.setBuffer(Type.Index,    2, BufferUtils.createIntBuffer(indexes));
	}
	
	public void update(float t) {
		sims.setLocalTranslation(sims.getLocalTranslation().x, (float) (Math.sin(t*2f)+1)*sims.getLocalScale().y*2, sims.getLocalTranslation().z);
		sims.rotate(0f, 0.01f, 0f);
		move();
	}
}
