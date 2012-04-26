package sat.gui3D;

import java.util.ArrayList;
import java.util.Date;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import sat.utils.geo.*;

public class Aircraft {
	private Node mainNode;
	private Node simsWrapper;
	private Geometry model;
	private Geometry sims;
	
	private ArrayList<DatedCoordinates> path;
	
	public Aircraft(AssetManager assetManager, Node parent) {
		simsWrapper = new Node();
		mainNode = new Node();
		
		model = (Geometry) assetManager.loadModel("plane.obj");
		model.getMaterial().setColor("Ambient", new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
		mainNode.attachChild(model);
		
		sims = (Geometry) assetManager.loadModel("sims.obj");
		sims.getMaterial().setColor("Ambient", new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
		sims.scale(0.3f);
		simsWrapper.attachChild(sims);
		
		simsWrapper.setLocalTranslation(0f, 1f, 0f);
		mainNode.attachChild(simsWrapper);
		
		mainNode.setLocalTranslation((float) Math.random()*40f-20f, (float) Math.random()*20f+1f, (float) Math.random()*40f-20f);
		mainNode.setLocalRotation(new Quaternion(new float[]{0f, (float) (Math.random()*Math.PI*2), 0f}));
		
		parent.attachChild(mainNode);
	}
	
	public void addDestination(FloatCoordinates pos, Date date) {
		path.add(new DatedCoordinates(pos, date));
	}
	
	private void moveTo(FloatCoordinates coords) {
		//if(...)
			moveStraightTo(coords);
		//else
			//moveStraightTo(coords);
	}
	
	private void moveStraightTo(FloatCoordinates coords) {
		
	}

	private void moveCircleTo(FloatCoordinates coords) {
		
	}
	public Node getNode() {
		return mainNode;
	}
	
	public void update(float t) {
		sims.setLocalTranslation(sims.getLocalTranslation().x, (float) (Math.sin(t*2f)+1)*sims.getLocalScale().y*2, sims.getLocalTranslation().z);
		sims.rotate(0f, 0.01f, 0f);
	}
}
