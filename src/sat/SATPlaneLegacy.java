package sat;

/**
 * Legacy Plane Launcher. Cette classe sert de launcher pour les avions
 * classques. Elle appelle la méthode initPlaneLegacy au lieu de initPlane ce
 * qui permet l'utilisation des arguments sous format ITP-compliant.
 */
public class SATPlaneLegacy {
	public static void main(String[] args) {
		SAT.initPlaneLegacy(args);
	}
}
