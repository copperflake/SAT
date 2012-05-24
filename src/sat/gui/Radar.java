package sat.gui;

import java.util.logging.Level;

import sat.radio.RadioID;

import com.jme3.app.SimpleApplication;
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
 * <p>
 * SimpleApplication est une classe du Framework jMonkeyEngine permettant de
 * mettre en place facilement une vue tridimensionnelle. Pour ce faire,
 * jMonkeyEngine utilise LWJGL, qui lui, permet de faire fonctionner OpenGL et
 * OpenAL (qui sont codées en C) sur n'importe quel platefrome (ce qui fait la
 * force de Java).
 * </p>
 * <p>
 * En étandant la classe SimpleApplication, nous avons alors à disposition un
 * grand nombre d'objet destiné à créer des objet tridimensionnels.
 * </p>
 * <p>
 * Dans le cas présent, la classe GUI va récuperer le Canvas de cette vue pour
 * l'afficher. Certaines options de configuration sont donc contenue dans la
 * classe GUI.
 * </p>
 * <p>
 * Notons toutefois que cette classe prend du temps à s'initialiser car elle
 * doit charger tous les modèles, toutes les textures, etc.
 * </p>
 * <p>
 * Comme la plupart des systèmes utilisants des objets tridimentionnels,
 * jMonkeyEngine fonctionne avec des Nodes. Cela permet d'attacher plusieurs
 * Mesh à un Node de manière à ce que si l'on déplace, tourne, modifie l'échelle
 * du Node, les objets internes vont suivre le mouvement comme s'il ne faisait
 * qu'un. On peut aussi attacher des Node à des Node.
 * </p>
 * <p>
 * Le rootNode est donné par la SimpleApplication. Il représente le monde, donc
 * les objets sont visibles à partir du moment où ils ont été attaché.
 * </p>
 * <p>
 * Le modèle de la tour de contrôle est stocké dans un Wavefront OBJ, un format
 * basique et standard représentant un objet 3D. Il peut être lié à un .mtl
 * définissant directement ses Matériaux. (Ce sera le cas de tout nos *.obj).
 * </p>
 * <p>
 * <i>NB: Tous les modèles on été créé grâce à Blender3D.</i>
 * </p>
 */
public class Radar extends SimpleApplication {
	private long frameNumber = 0;
	private Vector3f camUp;
	private float moveSpeed, moveAltSpeed, zoomSpeed, rotSpeed;
	private Controls3D controls;
	private boolean hd;
	protected Spatial tower;

	/**
	 * Le constructeur s'occupe de faire quelques réglages quant aux
	 * informations affichées à l'utilisateur.
	 * 
	 * @param hd
	 */
	public Radar(boolean hd) {
		this.hd = hd;
		setShowSettings(false);
		setDisplayStatView(false);
		setPauseOnLostFocus(false);
		java.util.logging.Logger.getLogger("").setLevel(Level.SEVERE);
	}

	/**
	 * Cette méthode est automatiquement appelée par jMonkeyEngine une fois
	 * l'application initialisée. Elle est couramment utilisée comme suite du
	 * constructeur car il faut souvent attendre que certains objets soit
	 * initialisés.
	 */
	@SuppressWarnings("deprecation")
	public void simpleInitApp() {
		camUp = cam.getUp();
		moveSpeed = 5f;
		moveAltSpeed = 100f;
		rotSpeed = 5f;
		zoomSpeed = 10f;

		controls = new Controls3D(inputManager, this);
		assetManager.registerLocator("assets", FileLocator.class.getName());

		// Ground
		// On crée un Mash en forme de parallélépipède rectangle.
		Box zurickBox = new Box(Vector3f.ZERO, 900f, 0f, 900f);

		// On le transforme en Spatial, ce qui va permettre de lui appliquer des matériaux.
		Spatial zurick = new Geometry("Box", zurickBox);

		// On crée un matériel de type "Unshaded", c'est-à-dire qu'il ne réagit
		// pas à la lumière (et donc aux ombres). Les matériaux donne la
		// possibilité d'appliquer, non seulement une texture, mais aussi de
		// paramàtrer les effets de lumière (dureté et intensité des reflets,
		// réacion à la lumière ambiante, etc.).
		Material mat_zurick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

		// On ajoute la texture du sol selon le paramètre HD. En effet, deux
		// images de différente dimension peuvent être utilisées.
		// assets/Images/kloten15mid: 4000x4000 pixels
		// assets/Images/kloten15low: 1000x1000 pixels Notez que
		// assets/Images/kloten15 (6144x6144 pixels) existe aussi mais a été
		// retiré du code car son chargement était vraiment trop gourmant.
		// 
		// NB: Les image on été générée grâce à un script couçu pour l'occasion qui récupère
		// les tuiles de Google Maps https://github.com/BinaryBrain/Mille-feuille.
		mat_zurick.setTexture("ColorMap", assetManager.loadTexture("Images/kloten15" + ((hd) ? "mid" : "low") + ".jpg"));
		zurick.setMaterial(mat_zurick);

		zurick.setLocalTranslation(473f, 0f, 330f);
		zurick.rotate(0f, (float) (-Math.PI * 0.492), 0f);

		rootNode.attachChild(zurick);

		// Tower
		tower = assetManager.loadModel("Models/tower.obj");
		tower.scale(4f);
		tower.setLocalTranslation(600f, 0f, 400f);
		rootNode.attachChild(tower);

		// Sun (Directional Light)
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		sun.setColor(new ColorRGBA(1f, 0.95f, 0.9f, 1f));
		rootNode.addLight(sun);

		// Ambient Light
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);

		// POST PROCESSING
		// because Gamma correction looks great
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		GammaCorrectionFilter gammaFilter = new GammaCorrectionFilter(0.8f);
		fpp.addFilter(gammaFilter);
		viewPort.addProcessor(fpp);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 10000f);

		cam.setLocation(new Vector3f(457f, 200f, 672f));
		cam.lookAt(new Vector3f(457f, 0f, 272f), cam.getUp());
	}

	@Override
	/**
	 * Boucle principale du thread appelée automatiquement par jMonkeyEngine.
	 * @param tpf
	 */
	public void simpleUpdate(float tpf) {
		if(frameNumber == 0) {
			controls.setupControls();
		}

		for(RadioID key : GUI.aircrafts.keySet()) {
			GUI.aircrafts.get(key).update3D(timer.getTimeInSeconds());
		}

		frameNumber++;
	}

	/**
	 * Bouge la caméra vers l'avant en restant à la même altitude.
	 * @param value
	 */
	public void moveCamFront(float value) {
		Vector3f v = cam.getDirection().setY(0).normalize().mult(value * moveSpeed * cam.getLocation().getY());
		cam.setLocation(cam.getLocation().add(v));
	}

	/**
	 * Bouge la caméra de côté en restant à la même altitude..
	 * @param value
	 */
	public void moveCamSide(float value) {
		Vector3f v = cam.getLeft().normalize().mult(value * moveSpeed * cam.getLocation().getY());
		cam.setLocation(cam.getLocation().add(v));
	}

	/**
	 * Bouge la caméra en hauteur.
	 * @param value
	 */
	public void moveCamY(float value) {
		Vector3f v = Vector3f.UNIT_Y.mult(value * moveAltSpeed);
		Vector3f loc = cam.getLocation().add(v);
		if(loc.getY() > 1)
			cam.setLocation(loc);
	}

	/**
	 * Bouge la caméra dans la direction où pointe la caméra.
	 * @param value
	 */
	public void zoom(float value) {
		Vector3f v = cam.getDirection().normalize().mult(value * zoomSpeed);
		Vector3f loc = cam.getLocation().add(v);
		if(loc.getY() > 1)
			cam.setLocation(loc);
	}
	
	/**
	 * Fait tourner la caméra dans le plan horizontal.
	 * @param value
	 */
	public void rotateCamH(float value) {
		rotateCam(-value, camUp);
	}

	/**
	 * Fait tourner la caméra dans le plan vertical.
	 * @param value
	 */
	public void rotateCamV(float value) {
		rotateCam(value, cam.getLeft());
	}

	/**
	 * Fait tourner la caméra en first person mode.
	 */
	private void rotateCam(float value, Vector3f axis) {
		Vector3f up = this.cam.getUp();
		Vector3f left = this.cam.getLeft();
		Vector3f dir = this.cam.getDirection();

		Matrix3f mat = new Matrix3f();
		mat.fromAngleNormalAxis(rotSpeed * value, axis);

		mat.mult(up, up);
		mat.mult(left, left);
		mat.mult(dir, dir);

		Quaternion q = new Quaternion();
		q.fromAxes(left, up, dir).normalizeLocal();

		this.cam.setAxes(q);
	}
}
