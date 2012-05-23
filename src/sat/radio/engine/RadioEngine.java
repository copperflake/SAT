package sat.radio.engine;

/**
 * Moteur de radio. Un moteur de radio est le composant de base de la
 * communication entre la tour et les avions. Il est responsable de la gestion
 * de la couche de transport TCP/UDP ou autre. Il fournit à la radio une
 * interface générale pour lire et écrire vers un système distant.
 * 
 * Le moteur est chargé de mettre en place le serveur ou le client pour le
 * protocole choisi, d'effectuer le multiplexage du flux si nécessaire et de
 * s'assurer de la fiabilité des échanges si le protocole sous-jacent ne
 * l'assure pas déjà.
 * 
 * La radio estime avoir à disposition des flux dédiés (vers / depuis un avion
 * unique) et sûr (sans pertes et respectant l'ordre des écritures). Le moteur
 * est également reponsable de surveiller l'état de la connexion et de fermer
 * les flux si le pair distant est déconnecté (la gestion de l'innactivié par
 * opposition à la déconnexion est la responsabilité de la radio elle-même).
 */
public abstract class RadioEngine {
}
