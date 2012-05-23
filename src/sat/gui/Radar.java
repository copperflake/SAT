package sat.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;

/**
 * Sample 3 - how to load an OBJ model, and OgreXML model, a material/texture,
 * or text.
 */
public class Radar extends SimpleApplication {
	private long frameNumber = 0;
	private Vector3f camUp;
	private float moveSpeed, moveAltSpeed, zoomSpeed, rotSpeed;
	private Controls3D controls;
	private boolean hd;
	protected Spatial tower;
	
	public Radar(boolean hd) {
		this.hd = hd;
		setShowSettings(false);
		setDisplayStatView(false);
		setPauseOnLostFocus(false);
	}
	
	@SuppressWarnings("deprecation")
	public void simpleInitApp() {
		// TODO Add listener(this)
		
		camUp = cam.getUp();
		moveSpeed = 5f;
		moveAltSpeed = 100f;
		rotSpeed = 5f;
		zoomSpeed = 10f;

		controls = new Controls3D(inputManager, this);
		assetManager.registerLocator("assets", FileLocator.class.getName());

		// Ground
		Box zurickBox = new Box(Vector3f.ZERO, 900f, 0f, 900f);
		Spatial zurick = new Geometry("Box", zurickBox);
		Material mat_zurick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		/**
		 * On ajoute la texture du sol selon le paramètre HD. En effet, deux images de différente dimension peuvent être utilisées.
		 * assets/Images/kloten15mid: 4000x4000 pixels
		 * assets/Images/kloten15low: 1000x1000 pixels
		 * Notez que assets/Images/kloten15 (6144x6144 pixels) existe aussi mais a été retiré du code car son chargement était vraiment trop gourmant. 
		 */
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("Images/kloten15"+((hd) ? "mid" : "low")+".jpg"));
		zurick.setMaterial(mat_zurick);
		zurick.setLocalTranslation(457f, 0f, 272f);
		zurick.rotate(0f, (float) (Math.PI*0.508), 0f);
		rootNode.attachChild(zurick);
		
		// Tower
		tower = assetManager.loadModel("Models/tower.obj");
		tower.scale(4f);
		tower.setLocalTranslation(425f, 0f, 270f);
		rootNode.attachChild(tower);
		
			// DEV
			Node allPlanes = new Node();
			Spatial gripen = assetManager.loadModel("Models/gripen.obj");
			gripen.setLocalTranslation(0, 0, 10);
			Spatial concorde = assetManager.loadModel("Models/concorde.obj");
			concorde.setLocalTranslation(0, 0, -10);
			Spatial a320 = assetManager.loadModel("Models/a320.obj");
			allPlanes.attachChild(gripen);
			allPlanes.attachChild(concorde);
			allPlanes.attachChild(a320);
			allPlanes.scale(6f);
			allPlanes.setLocalRotation(new Quaternion(new float[]{0f, (float) -Math.PI/2, 0f}));
			allPlanes.setLocalTranslation(425f, 50f, 270f);
			rootNode.attachChild(allPlanes);
		
		// Sun
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		sun.setColor(new ColorRGBA(1f, 0.95f, 0.9f, 1f));
		rootNode.addLight(sun);
		
		// Counter-Sun
		DirectionalLight sun2 = new DirectionalLight();
		sun2.setDirection(new Vector3f(0.1f, -0.3f, 1.0f));
		sun2.setColor(new ColorRGBA(0.3f, 0.25f, 0.25f, 1f));
		//rootNode.addLight(sun2);

		// Ambient
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);
		
		// POST PROCESSING
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		GammaCorrectionFilter gammaFilter = new GammaCorrectionFilter(0.8f);
		fpp.addFilter(gammaFilter);
		viewPort.addProcessor(fpp);
		
		cam.setFrustumPerspective(45f, (float) cam.getWidth()/cam.getHeight(), 0.01f, 10000f);

		cam.setLocation(new Vector3f(457f, 200f, 672f));
		cam.lookAt(new Vector3f(457f, 0f, 272f), cam.getUp());
	}

	@Override
	public void simpleUpdate(float tpf) {
		if(frameNumber == 0) {
			controls.setupControls();
		}
		
		for(int i = 0; i < GUI.aircrafts.size(); i++)
			GUI.aircrafts.get(i).update3D(timer.getTimeInSeconds());
		
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
		rotateCamDetached(-value, camUp);
	}
	
	public void rotateCamV(float value) {
		rotateCamDetached(value, cam.getLeft());
	}
	
	@SuppressWarnings("deprecation")
	private void rotateCamDetached(float value, Vector3f axis) {
		Vector3f up = this.cam.getUp();
		Vector3f left = this.cam.getLeft();
		Vector3f dir = this.cam.getDirection();
		
		Matrix3f mat = new Matrix3f();
		mat.fromAngleNormalAxis(rotSpeed * value, axis);
		
		mat.mult(up, up);
		mat.mult(left, left);
		mat.mult(dir, dir);
		
		Quaternion q = new Quaternion();
		q.fromAxes(left, up, dir).normalize();	
		
		this.cam.setAxes(q);
	}
}