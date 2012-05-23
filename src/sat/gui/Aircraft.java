package sat.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import sat.events.EventListener;
import sat.plane.Plane;
import sat.plane.PlaneType;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.utils.geo.*;

public class Aircraft {
	private Node mainNode, simsWrapper, parent;
	private Geometry model, sims;
	private Vector3f currentPos, initPos;
	private Mesh lineMesh;
	private PlaneType type;
	private RadioID id;
	private AssetManager assetManager;

	private CircularBuffer<Vector3f> path = new CircularBuffer<Vector3f>(50);

	public Aircraft() {
	}
	
	public void init3D(Node parent, AssetManager assetManager) {
		this.assetManager = assetManager;
		this.parent = parent;
		
		simsWrapper = new Node();
		mainNode = new Node();
		lineMesh = new Mesh();
		initPos = new Vector3f(0f, 0f, 0f);
		
		currentPos = initPos;
		
		drawAircraft3D(initPos);
		drawTrace3D();
		move3D();
	}
	
	public Node getNode() {
		return mainNode;
	}
	
	private void move3D() {
		if(path.size()-1 >= 0) {
			Vector3f coords = path.get(path.size()-1);
			if(!currentPos.equals(coords)) {
				mainNode.setLocalTranslation(coords.getX(), coords.getZ(), coords.getY()); // Changement de référentiel
				rotate3D();
				updateTrace3D();
				currentPos = coords;
			}
		}
	}
	
	private void rotate3D() {
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

	private void drawAircraft2D(Graphics2D g2d) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.setColor(Color.GREEN);

		Vector3f coords = path.get(path.size()-1);
		int x = (int) coords.getX();
		int y = (int) coords.getY();
		// TODO Find another solution to replace this.
		// BufferedImage planeImg = plane.hasCrashed() ? imgKboom : imgPlane;
		BufferedImage planeImg = AirportPanel.getImgPlane();
		
		g2d.setTransform(AffineTransform.getRotateInstance(0, x, y));
		g2d.drawString(id.toString()+" ("+x+", "+y+")", x+planeImg.getWidth()/2, y);

		// Compute the rotation angle of the plane, and draw it
		if(path.size()-2 >= 0) {
			double dx = path.get(path.size()-1).getX() - path.get(path.size()-2).getX();
			double dy = path.get(path.size()-1).getY() - path.get(path.size()-2).getY();
			double theta = Math.atan2(dy, dx);
			g2d.setTransform(AffineTransform.getRotateInstance(theta, x, y));
		}
		g2d.drawImage(planeImg, x-planeImg.getWidth()/2, y-planeImg.getHeight()/2, null);
	}
	
	private void drawAircraft3D(Vector3f initPos) {
		model = (Geometry) assetManager.loadModel("Models/default.obj");
		model.getMaterial().setColor("Ambient", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
		model.scale(3f);
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
	
	private void changeType(PlaneType type) {
		this.type = type;
		changeModel3D(type);
	}
	
	private void changeModel3D(PlaneType type) {
		model.removeFromParent();
		
		if(this.type == PlaneType.A320) {
			model = (Geometry) assetManager.loadModel("Models/plane.obj");
		}
		else if(this.type == PlaneType.GRIPEN) {
			model = (Geometry) assetManager.loadModel("Models/gripen.obj");
		}
		else if(this.type == PlaneType.CONCORDE) {
			model = (Geometry) assetManager.loadModel("Models/concorde.obj");
		}
		else {
			model = (Geometry) assetManager.loadModel("Models/plane.obj");
		}
		
		mainNode.attachChild(model);
	}
	
	private void drawTrace2D(Graphics2D g2d) {
		g2d.setColor(Color.CYAN);
		for(int i=1; i < path.size(); i++) {
			Point p1 = new Point((int) path.get(i).getX(), (int) path.get(i).getY());
			Point p2 = new Point((int) path.get(i-1).getX(), (int) path.get(i-1).getY());
			if(p2.getX() >= 0 && p2.getY() >= 0)
				g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}
	
	private void drawTrace3D() {
		lineMesh.setMode(Mesh.Mode.Lines);
		lineMesh.setLineWidth(4f);
		Geometry lineGeometry = new Geometry("line", lineMesh);
		Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		lineMaterial.setColor("Color", ColorRGBA.Green);
		lineGeometry.setMaterial(lineMaterial);
		parent.attachChild(lineGeometry);
	}
	
	private void updateTrace3D() {
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
	
	public void setDistress() {
		setDistress3D(true);
	}
	
	private void setDistress3D(boolean distress) {
		sims.getMaterial().setColor("Ambient", new ColorRGBA(distress?1f:0.2f, 0.2f, 0.2f, 1f));
	}
	
	public void addDestination(Vector3f dest) {
		path.add(dest);
	}
	
	public void addDestination(Coordinates dest) {
		addDestination(new Vector3f(dest.getX(), dest.getY(), dest.getZ()));
	}

	public void update2D(Graphics2D g2d) {
		drawAircraft2D(g2d);
		drawTrace2D(g2d);
	}
	
	public void update3D(float t) {
		sims.setLocalTranslation(sims.getLocalTranslation().x, (float) (Math.sin(t*2f)+1)*sims.getLocalScale().y*2, sims.getLocalTranslation().z);
		sims.rotate(0f, 0.01f, 0f);
		move3D();
	}
}
