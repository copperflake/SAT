package sat.radio;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * L'identifiant d'un pair dans un réseau radio SAT.
 */
public final class RadioID implements Serializable {
	// RadioID config

	/**
	 * Nombre de chiffres de la partie horraire de l'identifiant.
	 */
	static private final int TIME_DIGIT = 3;

	/**
	 * Nombre de chiffres de la partie aléatoire de l'identifiant.
	 */
	static private final int RAND_DIGIT = 2;

	static private final long TIME_POW = (long) Math.pow(10L, TIME_DIGIT);
	static private final long RAND_POW = (long) Math.pow(10L, RAND_DIGIT);
	static private final long GLOB_POW = (long) Math.pow(10L, TIME_DIGIT + RAND_DIGIT);

	/**
	 * Longueur en byte des identifants classiques. Ces identifiants sont
	 * compatible avec les identifiants du protocole officiel de l'ITP.
	 */
	static private final int LEGACYID_LENGHT = 8;

	/**
	 * Le label d'un identifiant. Le label préfixe le code du pair et permet de
	 * différencier les différents types d'appareils plus facilement.
	 */
	private String label;

	/**
	 * L'heure à laquel l'identifiant a été créé. Cette heure forme le premier
	 * morceau de la partie numérique de l'identifiant.
	 */
	private long time;

	/**
	 * Un identifiant généré aléatoirement. Cet identifiant forme le deuxième
	 * morceau de la partie numérique de l'identifiant.
	 */
	private long id;

	/**
	 * Indique si cet identifiant a été créé à partir d'un identifiant Legacy.
	 */
	private boolean legacy = false;

	/**
	 * Garde en mémoire l'ancien identifiant LegacyID s'il ce RadioID a été créé
	 * à partir d'un LegacyID.
	 */
	private byte[] legacyID;

	/**
	 * Création d'un identifiant.
	 * 
	 * @param label
	 *            Le label de l'identifiant. Si ce label est vide, l'identifiant
	 *            créé sera celui d'un UFO.
	 */
	public RadioID(String label) {
		this.label = label;
		generateCode();
	}

	/**
	 * Crée un identifiant à partir d'un identifiant ITP-compliant.
	 * 
	 * @param legacyID
	 *            L'identifiant ITP-compliant à la base de ce RadioID.
	 */
	public RadioID(byte[] legacyID) {
		this("L:" + new String(legacyID));

		// Store the original legacy ID
		this.legacyID = new byte[LEGACYID_LENGHT];

		int length = (legacyID.length > LEGACYID_LENGHT) ? LEGACYID_LENGHT : legacyID.length;
		System.arraycopy(legacyID, 0, this.legacyID, 0, length);

		legacy = true;
	}

	/**
	 * Génère la partie variable de l'identifiant.
	 */
	private void generateCode() {
		Date now = new Date();
		time = (int) ((now.getTime() / 1000) % TIME_POW);

		id = (int) (Math.round(Math.random() * RAND_POW) % RAND_POW);
	}

	/**
	 * Indique si l'identifiant a été créé à partir d'un identifiant
	 * ITP-compliant.
	 * 
	 * @return <code>true</code> si l'identifiant est ITP-compliant,
	 *         <code>false</code> sinon.
	 */
	public boolean isLegacy() {
		return legacy;
	}

	/**
	 * Converti l'identifiant en une chaine de caractères.
	 */
	public String toString() {
		// Si l'identifiant provient d'un identifiant legacy, on n'affiche
		// pas les données supplémentaires de RadioID.
		if(isLegacy()) {
			return label;
		}

		// Construction d'un grand nombre intégrant les informations de
		// l'identifiant (heure / id). Ceci simplifie la conversion en chaine
		// de caractères des ces informations si celles-ci débutent par un
		// ou plusieurs 0. (ex: 0042)
		String asString = new Long(GLOB_POW + time * RAND_POW + id).toString();

		// Découpage du nombre pour en extraire les parties intéressantes.
		String timeString = asString.substring(1, TIME_DIGIT + 1);
		String randString = asString.substring(TIME_DIGIT + 1);

		// Recomposition de l'identifiant sous forme de chaine de caractères.
		return label + "-" + timeString + "-" + randString;
	}

	/**
	 * Converti l'identifiant en un format respectant le format imposé par le
	 * protocol officiel de l'ITP. Si cet identifiant avait été créé à partir
	 * d'un tel identifiant, l'identifiant ITP-compliant de base est retourné.
	 * 
	 * @return Un identifiant sous forme de tableau de bytes compatible avec le
	 *         protocol officiel de l'ITP.
	 */
	public byte[] toLegacyID() {
		// Si un identifiant legacyID est déjà disponible, on le retourne,
		// tout simplement.
		if(legacyID != null)
			return legacyID;

		legacyID = new byte[LEGACYID_LENGHT];

		// "X:000-00" the LegacyPrefix
		legacyID[0] = 'X';
		legacyID[1] = ':';

		int available = LEGACYID_LENGHT - 3;

		// Computing the rand size first give a small advantage to the
		// time size. (division floors)
		int randSize = available / 2; // Length of the random segment
		String randSeg = String.valueOf(id);

		int timeSize = available - randSize; // Lenght of the time segment
		String timeSeg = String.valueOf(time);

		// Computing the ID

		// Time part
		for(int i = 0, j = timeSeg.length(); i < timeSize; i++, j--) {
			legacyID[i + 2] = (byte) ((j <= 0) ? '0' : timeSeg.charAt(i));
		}

		legacyID[timeSize + 2] = '-';

		// Rand part
		for(int i = 0, j = randSeg.length(); i < randSize; i++, j--) {
			legacyID[i + timeSize + 3] = (byte) ((j <= 0) ? '0' : randSeg.charAt(i));
		}

		return legacyID;
	}

	/**
	 * Compare deux RadioID entre eux.
	 */
	public boolean equals(Object o) {
		// Obvious equality
		if(this == o)
			return true;

		// Obvious inequality
		if((o == null) || (o.getClass() != this.getClass()))
			return false;

		RadioID rid = (RadioID) o;

		if(isLegacy() && rid.isLegacy()) {
			// Two ID are legacy
			return Arrays.equals(legacyID, rid.legacyID);
		} else if(isLegacy() || rid.isLegacy()) {
			// One ID is legacy
			if(isLegacy()) {
				return Arrays.equals(legacyID, rid.toLegacyID());
			} else {
				return Arrays.equals(toLegacyID(), rid.legacyID);
			}
		} else {
			// Two are not legacy
			return (label.equals(rid.label) && time == rid.time && id == rid.id);
		}
	}

	/**
	 * Génère un hashcode de cet ID. Le hashcode vérifie la propriété suivante:
	 * <code>id1.hashCode() == id2.hashCode()</code> si les deux ID sont égaux,
	 * c'est à dire que <code>id1.equals(id2)</code> retournerait
	 * <code>TRUE</code>. En revanche, deux ID différents peuvent avoir le même
	 * hashcode, il n'indique donc pas l'égalité de façon sûr.
	 */
	public int hashCode() {
		return Arrays.hashCode(toLegacyID());
	}

	private static final long serialVersionUID = 6714099615154964027L;
}
