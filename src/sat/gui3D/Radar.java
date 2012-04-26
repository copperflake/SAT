package sat.gui3D;

import java.util.ArrayList;
import sat.utils.geo.FloatCoordinates;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.GammaCorrectionFilter;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.math.ColorRGBA;

import de.lessvoid.nifty.spi.input.InputSystem;

/**
 * Sample 3 - how to load an OBJ model, and OgreXML model, a material/texture,
 * or text.
 */
public class Radar extends SimpleApplication {
	public static void main(String[] args) {
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

	protected Node wrapper;
	protected Geometry plane;
	protected Geometry sims;

	private long frameNumber = 0;
	private ArrayList<Aircraft> aircrafts;
	private Vector3f center, camUp;
	private float moveSpeed, rotSpeed, zoomSpeed, firstPersonRotSpeed;
	private Controls controls;
	private boolean camLookAt;
	private boolean camCenteredOnTower;

	@SuppressWarnings("deprecation")
	@Override
	public void simpleInitApp() {
		boolean cartoon = false;
		boolean DoF = false;
		boolean effects = false; 
		
		camUp = cam.getUp();
		camLookAt = false;
		camCenteredOnTower = false;
		moveSpeed = 100f;
		rotSpeed = 300f;
		firstPersonRotSpeed = 5f;
		zoomSpeed = 5f;

		center = new Vector3f(0, 0, 0);
		aircrafts = new ArrayList<Aircraft>();
		controls = new Controls(inputManager, this);
		assetManager.registerLocator("resources/blender", FileLocator.class.getName());

		// Ground
		Box zurickBox = new Box(Vector3f.ZERO, 100f, 0f, 100f);
		Spatial zurick = new Geometry("Box", zurickBox);
		Material mat_zurick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("map.png"));
		zurick.setMaterial(mat_zurick);
		zurick.setLocalTranslation(0f, 0f, 0f);
		rootNode.attachChild(zurick);

		Box soilBox = new Box(Vector3f.ZERO, 1000f, 0f, 1000f);
		Spatial soil = new Geometry("Box", soilBox);
		Material mat_soil = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("map.png"));
		//mat_soil.setColor("ColorMap", ColorRGBA.DarkGray);
		soil.setMaterial(mat_zurick);
		soil.setLocalTranslation(0f, -0.1f, 0f);
		rootNode.attachChild(soil);

		// Sun
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		sun.setColor(new ColorRGBA(1f, 0.95f, 0.9f, 1f));
		rootNode.addLight(sun);

		// Ambient
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);

		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

		// Tower bêta
		Box box = new Box(Vector3f.ZERO, 1f, 1f, 1f);
		Spatial wall = new Geometry("Box", box);
		Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_brick.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
		wall.setMaterial(mat_brick);
		wall.setLocalTranslation(Vector3f.ZERO);
		rootNode.attachChild(wall);

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
			depth.setFocusDistance(10f);
			depth.setFocusRange(10f);
			depth.setBlurScale(1f);
			fpp.addFilter(depth);
		}
		if(effects) {
			RadialBlurFilter blur = new RadialBlurFilter(1f, 0.7f);
			fpp.addFilter(blur);
			GammaCorrectionFilter gamma = new GammaCorrectionFilter(0.8f);
			fpp.addFilter(gamma);
		}
		viewPort.addProcessor(fpp);

		for(int i = 0; i < 10; i++)
			onNewAircraft();

		cam.setLocation(new Vector3f(0f, 20f, 100f));
	}

	public void onNewAircraft() {
		aircrafts.add(new Aircraft(assetManager, rootNode));
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

		frameNumber++;
	}
	
	public void moveCamFront(float value) {
		Vector3f v = cam.getDirection().setY(0).normalize().mult(value * moveSpeed);
		cam.setLocation(cam.getLocation().add(v));
		// center = center.add(v);
	}

	public void moveCamY(float value) {
		Vector3f v = cam.getUp().normalize().mult(value * moveSpeed);
		cam.setLocation(cam.getLocation().add(v));
		// center = center.add(v);
	}

	public void moveCamSide(float value) {
		Vector3f v = cam.getLeft().normalize().mult(value * moveSpeed);
		cam.setLocation(cam.getLocation().add(v));
		// center = center.add(v);
	}

	public void zoom(float value) {
		Vector3f v = cam.getDirection().normalize().mult(value * zoomSpeed);
		cam.setLocation(cam.getLocation().add(v));
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
		q.fromAxes(left, up, dir);
		q.normalize();
		
		this.cam.setAxes(q);
	}
}
