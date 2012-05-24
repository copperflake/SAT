package sat.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
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
	private float defaultAltitude;

	private CircularBuffer<Vector3f> path = new CircularBuffer<Vector3f>(500);

	public Aircraft(RadioID id) {
		this.id = id;
	}
	
	public void init3D(Node parent, AssetManager assetManager) {
		this.assetManager = assetManager;
		this.parent = parent;
		
		simsWrapper = new Node();
		mainNode = new Node();
		lineMesh = new Mesh();
		initPos = new Vector3f(0f, 0f, 0f);
		defaultAltitude = (float) Math.random()*80f+30f;
		
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
				float fac = 10f;
				Vector3f start = path.get(path.size()-3);
				Vector3f v1 = new Vector3f(mid.getX()-start.getX(), mid.getY()-start.getY(), 0f).normalize();
				Vector3f v2 = dirYaw;
				roll = v1.angleBetween(v2)*fac;
				
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
		if (path.size() < 1) {
			return;
		}
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.setColor(Color.GREEN);

		Vector3f coords = path.get(path.size()-1);
		float x = coords.getX();
		float y = coords.getY();
		
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
		g2d.drawImage(planeImg, (int) (x-planeImg.getWidth()/2), (int) (y-planeImg.getHeight()/2), null);
		g2d.setTransform(new AffineTransform());
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
		
		ParticleEmitter smoke = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 500);
		Material smoke_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
		smoke_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
		smoke.setMaterial(smoke_mat);
		smoke.setImagesX(1); smoke.setImagesY(3); // 1x3 texture animation
		smoke.setEndColor(new ColorRGBA(1f, 1f, 1f, 0.3f));
		smoke.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
		smoke.getParticleInfluencer().setInitialVelocity(new Vector3f(0f,0.5f,0f));
		smoke.setStartSize(1.5f);
		smoke.setEndSize(0.1f);
		smoke.setGravity(0,0,0);
		smoke.setLowLife(20);
		smoke.setHighLife(30);
		smoke.setParticlesPerSec(20);
		smoke.getParticleInfluencer().setVelocityVariation(0.5f);
		mainNode.attachChild(smoke);
		
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
		
	}
	
	private void updateTrace3D() {
		Vector3f[] vertices = new Vector3f[path.size()];
		int[] indexes = new int[path.size()*2];
		
		for(int i=0; i < path.size(); i++) {
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
	
	public void setDistress2D() {
	}
	
	public void setDistress3D(boolean distress) {
		if(sims != null) {
			sims.getMaterial().setColor("Ambient", new ColorRGBA(distress?1f:0.2f, 0.2f, 0.2f, 1f));
		}
	}
	
	public void addDestination(Vector3f dest) {
		dest.setZ(defaultAltitude);
		path.add(dest);
	}
	
	public void addDestination(Coordinates dest) {
		addDestination(new Vector3f(dest.getX(), dest.getY(), dest.getZ()));
	}
	
	/**
	 * Même principe qu'un destructeur en C++ sauf qu'il doit être appelé à la main.
	 */
	public void destroy() {
		mainNode.removeFromParent();
	}

	public void update2D(Graphics2D g2d) {
		drawAircraft2D(g2d);
		drawTrace2D(g2d);
	}
	
	public void update3D(float t) {
		if(sims != null) {
			sims.setLocalTranslation(sims.getLocalTranslation().x, (float) (Math.sin(t*2f)+1)*sims.getLocalScale().y*2, sims.getLocalTranslation().z);
			sims.rotate(0f, 0.01f, 0f);
		}
		move3D();
	}
}
