package sat.radio;

import java.io.Serializable;
import java.util.Date;

/**
 * L'identifiant d'un pair dans un réseau radio SAT.
 */
public class RadioID implements Serializable {
	/**
	 * Le label d'un identifiant. Le label préfixe le code du pair et
	 * permet de différencier les différents types d'appareils plus
	 * facilement.
	 */
	private String label;
	
	/**
	 * L'heure à laquel l'identifiant a été créé. Cette heure forme le
	 * premier morceau de la partie numérique de l'identifiant.
	 */
	private long time;

	/**
	 * Un identifiant généré aléatoirement. Cet identifiant forme le
	 * deuxième morceau de la partie numérique de l'identifiant.
	 */
	private long id;
	
	/**
	 * Indique si le pair resprésenté est un OVNI. Les ovnis apparaissent
	 * lors de la création d'un identifiant avec des informations invalides
	 * ou si l'identification du pair n'a pas pu se faire correctement.
	 */
	private boolean UFO = false;
	
	// RadioID config
	
	/**
	 * Nombre de chiffres de la partie horraire de l'identifiant.
	 */
	static private final int TIME_DIGIT = 4;
	
	/**
	 * Nombre de chiffres de la partie aléatoire de l'identifiant.
	 */
	static private final int RAND_DIGIT = 4;

	static private final long TIME_POW = (long) Math.pow(10L, TIME_DIGIT);
	static private final long RAND_POW = (long) Math.pow(10L, RAND_DIGIT);
	static private final long GLOB_POW = (long) Math.pow(10L, TIME_DIGIT+RAND_DIGIT);
	
	/**
	 * Longueur en byte des identifants classiques. Ces identifiants sont
	 * compatible avec les identifiants du protocole officiel de l'ITP.
	 */
	static private final int LEGACYID_LENGHT = 8;
	
	/**
	 * Création d'un identifiant représentant un OVNI.
	 */
	public RadioID() {
		UFO = true;
	}
	
	/**
	 * Création d'un identifiant.
	 * 
	 * @param label		Le label de l'identifiant.
	 */
	public RadioID(String label) {
		this.label = label;
		
		Date now = new Date();
		time = (int) ((now.getTime()/1000)%TIME_POW);
		
		id = (int) (Math.round(Math.random()*RAND_POW)%RAND_POW);
	}
	
	/**
	 * Indique si l'identifiant représente un OVNI ou un pair correctement
	 * identifié.
	 * 
	 * @return	<code>true</code> si l'identifiant représente un OVNI, 
	 * 			<code>false</code> sinon.
	 */
	public boolean isUFO() {
		return UFO;
	}
	
	/**
	 * Converti l'identifiant en une chaine de caractères.
	 */
	public String toString() {
		// Si l'identifiant représente un OVNI, on retourne simplement "UFO".
		if(isUFO()) return "UFO";
		
		// Construction d'un grand nombre intégrant les informations de
		// l'identifiant (heure / id). Ceci simplifie la conversion en chaine
		// de caractères des ces informations si celles-ci débutent par un
		// ou plusieurs 0. (ex: 0042)
		String asString = new Long(GLOB_POW + time*RAND_POW + id).toString();
		
		// Découpage du nombre pour en extraire les parties intéressantes.
		String timeString = asString.substring(1, TIME_DIGIT+1);
		String randString = asString.substring(TIME_DIGIT+1);
		
		// Recomposition de l'identifiant sous forme de chaine de caractères.
		return label + "-" + timeString + "-" + randString;
	}
	
	/**
	 * Converti l'identifiant en un format respectant le format imposé
	 * par le protocol officiel de l'ITP.
	 * 
	 * @return	Un identifiant sous forme de tableau de bytes compatible
	 * 			avec le protocol officiel de l'ITP.
	 */
	public byte[] toLegacyID() {
		// TODO: implements
		return new byte[LEGACYID_LENGHT];
	}
	
	private static final long serialVersionUID = 6714099615154964027L;
}
