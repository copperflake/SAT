package sat.gui3D;

import java.util.ArrayList;

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
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.util.BufferUtils;

/**
 * Sample 3 - how to load an OBJ model, and OgreXML model, a material/texture,
 * or text.
 */
public class Radar extends SimpleApplication {
	public static void launch() {
		Radar app = new Radar();
		AppSettings settings = new AppSettings(true);
		settings.setResolution(800, 600);
		settings.setResolution(1680, 1050);
		settings.setTitle("SAT - Radar");
		app.setShowSettings(false);
		app.setDisplayStatView(false);
		app.setSettings(settings);
		app.start();
	}

	private long frameNumber = 0;
	private ArrayList<Aircraft> aircrafts;
	private Vector3f center, camUp;
	private float moveSpeed, rotSpeed, zoomSpeed, firstPersonRotSpeed;
	private Controls controls;
	private boolean camLookAt;
	private boolean hd;
	protected Node tower;

	@SuppressWarnings("deprecation")
	@Override
	public void simpleInitApp() {
		hd = false;
		boolean cartoon = false;
		boolean DoF = false;
		boolean blur = hd;
		boolean gamma = hd;
		
		camUp = cam.getUp();
		camLookAt = false;
		moveSpeed = 70f;
		rotSpeed = 2f;
		firstPersonRotSpeed = 5f;
		zoomSpeed = 5f;

		center = new Vector3f(0, 0, 0);
		aircrafts = new ArrayList<Aircraft>();
		controls = new Controls(inputManager, this);
		assetManager.registerLocator("assets", FileLocator.class.getName());

		// Ground
		Box zurickBox = new Box(Vector3f.ZERO, 600f, 0f, 600f);
		Spatial zurick = new Geometry("Box", zurickBox);
		Material mat_zurick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("Images/kloten15"+((hd) ? "" : "low")+".jpg"));
		zurick.setMaterial(mat_zurick);
		zurick.setLocalTranslation(0f, 0f, 0f);
		rootNode.attachChild(zurick);
		
		
		/*
		Box soilBox = new Box(Vector3f.ZERO, 1000f, 0f, 1000f);
		Spatial soil = new Geometry("Box", soilBox);
		Material mat_soil = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_zurick.setTexture("ColorMap", assFilterPostProcessoretManager.loadTexture("Images/map.png"));
		//mat_soil.setColor("ColorMap", ColorRGBA.DarkGray);
		soil.setMaterial(mat_zurick);
		soil.setLocalTranslation(0f, -0.1f, 0f);
		rootNode.attachChild(soil);
		*/

		// Tower

		tower = (Node) assetManager.loadModel("Models/tower.obj");
		tower.scale(1.2f);
		tower.setLocalTranslation(0f, 0f, 0f);
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
		if(cartoon) {
			CartoonEdgeFilter toon = new CartoonEdgeFilter();
			toon.setEdgeWidth(0.5f);
			toon.setEdgeIntensity(1.0f);
			toon.setNormalThreshold(0.8f);
			fpp.addFilter(toon);
		}
		if(DoF) {
			DepthOfFieldFilter depth = new DepthOfFieldFilter();
			depth.setFocusDistance(50f);
			depth.setFocusRange(10f);
			depth.setBlurScale(1f);
			fpp.addFilter(depth);
		}
		if(false) {
			RadialBlurFilter blurFilter = new RadialBlurFilter(1f, 0.7f);
			fpp.addFilter(blurFilter);
		}
		if(gamma) {
			GammaCorrectionFilter gammaFilter = new GammaCorrectionFilter(0.8f);
			fpp.addFilter(gammaFilter);
		}
		viewPort.addProcessor(fpp);
		
		onNewAircraft(new Vector3f(10f, 10f, 10f));
		
		cam.setFrustumPerspective(45f, (float) cam.getWidth()/cam.getHeight(), 0.01f, 10000f);

		cam.setLocation(new Vector3f(0f, 20f, 100f));
	}

	public void onNewAircraft(Vector3f coords) {
		aircrafts.add(new Aircraft(assetManager, rootNode, coords));
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
		aircrafts.get(0).addDestination(new Vector3f((float) Math.sin(frameNumber/100f)*100,(float) Math.cos(frameNumber/100f)*100,10f));
		frameNumber++;
	}
	
	public void moveCamFront(float value) {
		Vector3f v = cam.getDirection().setY(0).normalize().mult(value * moveSpeed);
		cam.setLocation(cam.getLocation().add(v));
	}

	public void moveCamY(float value) {
		Vector3f v = cam.getUp().normalize().mult(value * moveSpeed);
		Vector3f loc = cam.getLocation().add(v);
		if(loc.getY() > 1)
			cam.setLocation(loc);
	}

	public void moveCamSide(float value) {
		Vector3f v = cam.getLeft().normalize().mult(value * moveSpeed);
		cam.setLocation(cam.getLocation().add(v));
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
}
