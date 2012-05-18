package sat.gui3D;

import java.util.ArrayList;
import java.util.HashMap;

import sat.events.EventListener;
import sat.plane.Plane;
import sat.plane.PlaneType;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.tower.Tower;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.*;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;

/**
 * Sample 3 - how to load an OBJ model, and OgreXML model, a material/texture,
 * or text.
 */
public class Radar extends SimpleApplication implements EventListener {
	private long frameNumber = 0;
	private ArrayList<Aircraft> aircrafts;
	private Vector3f center, camUp;
	private float moveSpeed, moveAltSpeed, rotSpeed, zoomSpeed, firstPersonRotSpeed;
	private Controls controls;
	private boolean camLookAt, hd;
	protected Node tower;
	private HashMap<RadioID, Plane> planes;

	@SuppressWarnings("deprecation")
	@Override
	public void simpleInitApp() {
		Tower.getInstance().addListener(this);
		hd = false;
		boolean gamma = hd;
		
		camUp = cam.getUp();
		camLookAt = false;
		moveSpeed = 5f;
		moveAltSpeed = 100f;
		rotSpeed = 6f;
		firstPersonRotSpeed = 5f;
		zoomSpeed = 5f;

		center = new Vector3f(0, 0, 0);
		aircrafts = new ArrayList<Aircraft>();
		controls = new Controls(inputManager, this);
		assetManager.registerLocator("assets", FileLocator.class.getName());

		// Ground
		Box zurickBox = new Box(Vector3f.ZERO, 900f, 0f, 900f);
		Spatial zurick = new Geometry("Box", zurickBox);
		Material mat_zurick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("Images/kloten15"+((hd) ? "" : "low")+".jpg"));
		zurick.setMaterial(mat_zurick);
		zurick.setLocalTranslation(457f, 0f, 272f);
		zurick.rotate(0f, (float) (Math.PI*0.508), 0f);
		rootNode.attachChild(zurick);
		
		// PISTE (DEV)
		Box b1 = new Box(Vector3f.ZERO, 0.2f,0.1f,0.2f);
		Spatial p1 = new Geometry("Box", b1);
		Material pm = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		pm.setColor("Color", ColorRGBA.Blue);
		p1.setMaterial(pm);
		p1.setLocalTranslation(400f,0f,166f);
		rootNode.attachChild(p1);
		
		Box b2 = new Box(Vector3f.ZERO, 0.2f,0.1f,0.2f);
		Spatial p2 = new Geometry("Box", b2);
		p2.setMaterial(pm);
		p2.setLocalTranslation(533f,0f,437f);
		rootNode.attachChild(p2);
		
		// Tower
		tower = (Node) assetManager.loadModel("Models/tower.obj");
		tower.scale(3f);
		tower.setLocalTranslation(457f, 0f, 272f);
		rootNode.attachChild(tower);
		
		// Sun
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		sun.setColor(new ColorRGBA(1f, 0.95f, 0.9f, 1f));
		rootNode.addLight(sun);

		// Ambient
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);

		//rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

		// POST PROCESSING
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		if(gamma) {
			GammaCorrectionFilter gammaFilter = new GammaCorrectionFilter(0.8f);
			fpp.addFilter(gammaFilter);
		}
		viewPort.addProcessor(fpp);
		
		cam.setFrustumPerspective(45f, (float) cam.getWidth()/cam.getHeight(), 0.01f, 10000f);

		cam.setLocation(new Vector3f(457f, 200f, 672f));
		cam.lookAt(new Vector3f(457f, 0f, 272f), cam.getUp());
	}
	
	public ArrayList<Aircraft> getAircrafts() {
		return aircrafts;
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		if(frameNumber == 0) {
			controls.setupControls();
		}
		if(camLookAt)
			cam.lookAt(center, new Vector3f(0, 1, 0));
		
		for(int i = 0; i < aircrafts.size(); i++)
			aircrafts.get(i).update(timer.getTimeInSeconds());
		
		// DEV
		aircrafts.get(0).addDestination(new Vector3f((float) Math.sin(timer.getTimeInSeconds()/2f)*100+457f,(float) Math.cos(timer.getTimeInSeconds()/2f)*70,10f));
		
		frameNumber++;
	}
	
	public void moveCamFront(float value) {
		Vector3f v = cam.getDirection().setY(0).normalize().mult(value * moveSpeed * cam.getLocation().getY());
		cam.setLocation(cam.getLocation().add(v));
	}

	public void moveCamSide(float value) {
		Vector3f v = cam.getLeft().normalize().mult(value * moveSpeed * cam.getLocation().getY());
		cam.setLocation(cam.getLocation().add(v));
	}

	public void moveCamY(float value) {
		Vector3f v = Vector3f.UNIT_Y.mult(value * moveAltSpeed);
		Vector3f loc = cam.getLocation().add(v);
		if(loc.getY() > 1)
			cam.setLocation(loc);
	}

	public void zoom(float value) {
		Vector3f v = cam.getDirection().normalize().mult(value * zoomSpeed);
		Vector3f loc = cam.getLocation().add(v);
		if(loc.getY() > 1)
			cam.setLocation(loc);
	}

	public void rotateCamH(float value) {
		if(camLookAt) {
			Vector3f v = cam.getLeft().normalize().mult(value * rotSpeed);
			cam.setLocation(cam.getLocation().add(v));
		}
		else {
			rotateCamDetached(-value, camUp);
		}
	}
	
	public void rotateCamV(float value) {
		if(this.camLookAt) {
			Vector3f v = cam.getUp().normalize().mult(value * rotSpeed);
			cam.setLocation(cam.getLocation().add(v));
		}
		else {
			rotateCamDetached(value, cam.getLeft());
		}
	}
	
	private void rotateCamDetached(float value, Vector3f axis) {
		Vector3f up = this.cam.getUp();
		Vector3f left = this.cam.getLeft();
		Vector3f dir = this.cam.getDirection();
		
		Matrix3f mat = new Matrix3f();
		mat.fromAngleNormalAxis(firstPersonRotSpeed * value, axis);
		
		mat.mult(up, up);
		mat.mult(left, left);
		mat.mult(dir, dir);
		
		Quaternion q = new Quaternion();
		q.fromAxes(left, up, dir).normalize();	
		
		this.cam.setAxes(q);
	}
	
	public void on(RadioEvent.PlaneConnected e) {
		planes.put(e.getId(), e.getPlane());
		aircrafts.add(new Aircraft(assetManager, rootNode, e.getPlane().type));
	}

	public void on(RadioEvent.PlaneDisconnected e) {
		planes.remove(e.getId());
	}

	public void on(RadioEvent.PlaneMoved e) {
		planes.get(e.getId()).setLocation(e.getPlane().getLocation());
	}
}
