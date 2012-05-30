ReadMe
======

Lancement de l'application
--------------------------

La classe `sat.SAT` sert de point d'entrée dans notre application. Sa méthode main permet de lancer un composant spécifique.

Notre application est accompagnée d'un script Bash permettant de simplifier le lancement du programme. Ce script charge également toutes les dépendances nécessaire à l'execution de la tour de contrôle.

`./sat <command>`

 - `tower [tower.rc]` - Lance une tour de contrôle et un CLI associé.
 - `plane [plane.rc]` -  Lance un avion et un CLI associé.
 - `cli` -  Lance un cli "distant", se connectant sur localhost:4242.
 - `remote` - Lance un "event-dumper" qui affiche simplement les événements générés par une tour.
 - `legacyplane` - Un avion "legacy" imitant l'interface de l'avion ITP.

Les commandes `tower` et `plane` autorisent un paramètre [plane/tower.rc] qui est un fichier d'initialisation dont les instructions sont executées successivement au lancement de l'application. Ces fichiers permettent d'automatiser la configuration des paramètres courants de la tour.

Des fichier Tower.rc (configure la tour en mode debug, active la connexion distance, et écoute le port par défaut) et Plane.rc (active le mode debug, se connecte à la tour) sont fournis à la racine de notre projet. Il est donc possible d'utiliser:

 - `./sat tower tower.rc`
 - `./sat plane plane.rc``

Pour lancer respectivement une tour et un avion.

`legacyplane` permet l'utilisation d'option avec le style-ITP. Ces options sont directement transformées en options spécifiques à nos avions au comportement similaire. Il est par exemple possible d'executer:

`./sat legacyplane --encryption-enabled false --initial-x 100 --initial-y 100`

Pour obtenir une interface totalement compatible avec l'ITP, un .jar executable utilisant `sat.SATPlaneLegacy` au lieu de `sat.SAT` comme classe principale peut être utilisé.

### Note sur la vue 3D

La vue 3D dépend de nombreuses bibliothèques et peut avoir des problèmes à fonctionner sur certaines plateformes (MacOS X avec JRE >= 1.7, ...). Dans un tel cas, l'utilisation de `gui2d` permet de contourner le problème.

À propos du contrôle à distance
-------------------------------

L'utilisation de TowerAgent permet au CLI / GUI d'être détaché totalement du processus principal de la tour voir même s'executer sur une autre machine.

Dans des soucis de performances, nous n'avons pas souhaité réutiliser le chiffrement RSA que nous avons mis en place pour le chiffrement des messages. L'interface de contrôle à distance ne présente aucun méchanisme de sécurité, ni chiffrement, ni authentification. En revanche, le serveur gérant la communication avec les clients distant n'écoute que l'adresse locale (127.0.0.1) sur le port 4242. Il est donc inaccessible depuis l'extérieur.

Il est possible d'ajouter un niveau de sécurité et la gestion de l'accès à distance avec l'utilisation d'un tunnel SSH.

La commande `ssh user@128.178.145.28 -L4242:localhost:4242` permet de rediriger son port local 4242 vers le port 4242 _local_ de la machine distante, et donc d'utiliser les versions à distances du CLI/GUI sans modification dans le code de la tour de contrôle.

La session SSH doit rester ouverte pendant toute la durée de la connexion.


Référence CLI de la tour
------------------------

 - `config` - Affiche la configuration de la tour.
 - `get <key>` - Affiche une clé de configuration précise.
 - `set <key> <value>` - Défini la clé de configuration _key_ à _value_.
 - `store <file>` - Enregistre la configuration actuelle dans un fichier texte.
 - `load <file>` - Charge une configuration préalablement créée par `store`.
 - `loadroute <file> <capacity>` - Charge une route depuis le fichier spécifié et défini sa capacité à _capacity_.
 - `init` - Valide la configuration actuelle et termine l'initialisation de la tour. Ceci est nécessaire car certains paramètres ne peuvent plus être modifié une fois la tour initialisée. 
 - `listen` - Active le serveur de la tour qui peut maintenant être contactée par des clients.
 - `gui`, `gui2d`, `fastgui` - Lance l'interface graphique. Respectivement: avec la vue 3D, sans la vue 3D, et avec une vue 3D basse résolution.
 - `agentserver` - Lance le serveur de TowerAgent, qui permet la connexion de clients distants.
 - `writekey` - Ecrit la clé de la tour dans un fichier qui peut être lue par les avions ITP.

Options de configuration de la tour
-----------------------------------

 - `tower.debug [no]` - Mode débug, dans ce mode, la tour émet des événements de type DebugEvent.
 - `tower.prefix [TWR]` - Le préfix de l'identifiant radio de la tour.
 - `tower.downloads [downloads/]` - Le dossier de téléchargement.
 - `tower.routing [chronos]` - Le mode de routage de la tour.
 - `tower.graveyard [600,100,-1]` - La route d'autodestruction si la tour n'a plus de places disponibles sur les circuits d'attente.
 - `radio.ciphered [yes]` - Permet de désactiver le chiffrement de la communication.
 - `radio.legacy [no]` - Si cette option est définie à `yes`, l'avion ne tentera pas d'utiliser le mode étendu.
 - `radio.keylength [1024]` - La longueur de la clé à générer pour le chiffrement.

----------

Architecture du projet
======================

Dès la première semaine du projet, nous avons imaginé une architecture complète pour notre application. Plus tard, nous avons découvert dans les ITP que le projet était finalement relativement guidé et qu'une architecture générale était suggérée. Nous avons néanmoins choisi de conserver nos idées et de s'adapter avec les différentes parties du projet.

Les composants de la tour ont été développés avec pour but de les partager le plus facilement possible avec nos avions. En pratique, les deux programmes utilisent le même code de base et partagent beaucoups de similitudes.

Séparation de l'application en trois niveaux
--------------------------------------------

Lors de l'implémentation de nos classes, nous avons souhaité avoir une architecture très modulaire et avec des composants les plus indépendants possible. Nous avons donc séparé notre programme en trois niveau:

 - Une Tower ou un Plane travaille sur un flux séquentiel d'objet Message triés selon leur priorité, pour cela, ils embarquent un objet Radio fournissant des méthodes très haut niveau pour manipuler ces messages (ce niveau le plus élevé d'abstraction ne devrait même pas avoir à créer lui-même un objet Message, mais simplement manipuler des objets créés par la Radio).
 - La Radio est responsable du routage des messages (envoyer le bon message au bon avion) et de leur création selon les instructions de la couche supérieure. Elle gère aussi l'écoute et l'écriture d'un flux abstrait (appelé RadioSocket) représentant un canal bidirectionnel avec un client (avion). Néanmoins elle ne fait pas directement d'entrée-sortie au-delà de la manipulation de ces flux abstraits fournis par la couche inférieur (au niveau conceptuel, ces flux ne sont pas réellement abstract).
 - La couche la plus basse est le RadioServerEngine respectivement RadioClientEngine. Ces deux classes sont abstraites et sont étendues par exemple par RadioServerTCPEngine. L'intérêt des classes moteurs et de fournir la gestion effective de l'entrée/sortie. Dans certains cas la gestion peut être extrêmement simple: le moteur TCP ouvre un socket en écoute et englobe simplement les flux du socket TCP dans le RadioSocket avant de le passer au niveau supérieur (c'est un moteur direct). Au contraire, un éventuel moteur RadioServerUDPEngine devra gérer le multiplexage du flux d'entrée/sortie unique vers le réseau (contrairement à TCP qui propose un flux par client). Dans un tel moteur, le RadioSocket n'est pas directement connecté au réseau mais effectue des appels à des méthodes chargées du multiplexage (c'est un moteur indirect). Dans les deux cas, la couche supérieur Radio se voit fournir des flux distinct d'entrée/sortie pour chaque avion et les utilise indifféremment.

Il existe certaines exceptions à cette architecture à trois niveaux. Bien que la Radio ne gère en principe pas les messages eux-même et se contente de les passer à la tour de contrôle. Nous avons mis en place ce que nous avons appelé le court-circuitage protocolaire. C'est à dire qu'à la réception d'un message de type HELLO ou SEND_RSA_KEY par exemple, ce que nous appelons un message protocolaire, la radio s'occupe de le gérer elle-même et de renvoyer la réponse appropriée à l'avion. Ce fonctionnement nous semble cohérent car c'est la radio qui doit gérer les différents avions, le routage des messages et l'encryption RSA. Par conséquent, la tour de contrôle n'est prévenue de la connexion d'un avion que lorsque l'initialisation de la connexion est complètement terminée.

Gestion du protocole
--------------------

Lors de l'implémentation de nos classes et du protocole de communication entre les avions et la tour, nous nous sommes heurtés à la rigidité du format du protocole de communication tour-avion imposé par l'ITP. Par exemple, nos identifiants pour les avions et la tour sont des objets de type RadioID qui sont défini par un label et deux parties numériques (par exemple: PLN-347-04). Ce format n'étant pas compatible avec le protocole officiel qui s'attend à un tableau de 8 bytes, nous avons dû mettre en place de mécanisme de conversion, l'identifiant est converti sous la forme "X:347045" ce qui permet de l'utiliser avec des éléments utilisant le protocole officiel.

De plus, nous avons décidé d'étendre ce protocole de telle façon que nos avions puissent profiter des fonctionnalités additionnelles de nos classes.

Néanmoins le problème se présentait de la compatibilité avec les avions créés par les autres groupes ainsi que le respect de façon général des standards demandés par le projet. Le but étant de construire des systèmes interopérables, nous ne pouvions pas simplement nous contenter d'utiliser notre protocole étendu, mais nous devions également gérer le protocole original.

La solution, au final, a été de mettre en place une double gestion de protocole permettant de communiquer à la fois selon les standards de l'ITP et selon nos propres standards.

Le protocole ITP a été désigné par "Legacy Mode", et notre protocole étendu, tout simplement comme "Extended Mode".

### Différenciation

Le défi était alors de permettre aux avions et aux tours qu'ils soient en mode legacy ou étendu de communiquer entre eux selon le principe du plus petit dénominateur commun: seuls les fonctionnalités gérées par les deux parties seront utilisées. En pratique, toute les fonctionnalités du mode legacy sont supportée directement en mode extended (puisque nous avons modifié nos classes en fonction), il nous suffit donc de se restreindre à utiliser les fonctionnalités de bases du protocole lorsque l'autre pair ne les supporte pas. Pour déterminer dynamiquement si le pair avec qui l'on communique est en mode Legacy ou Extended, on utilise le byte reserved du message Hello, tout comme on le fait pour déterminer si la communication doit être cryptée (à la différence que le choix se fait de façon bilatérale et non unilatéral comme pour le chiffrement):

 - En temps normal, l'avion envoie un message Hello, avec un byte reserved sous la forme [000C 0000], avec C le bit indiquant si la communication doit être ou non cryptée.

En mode étendu, l'avion envoie un byte reserved de la forme `100C 0000`. De cette façon, la tour est en mesure de déterminer que l'avion est capable de gérer le protocole étendu. Mais pour l'instant l'avion ne fait rien.

À la réception de ce message par la tour de contrôle, deux situations se présentent:

 - La tour est incapable de gérer le mode étendu (LegacyTower), elle va dans ce cas simplement ignorer ce bit qui n'est normalement pas utilisé et continuera le handshake normalement qui impose notamment de renvoyer à l'avion un message HELLO similaire (mais pas une copie, sans quoi le byte de protocole étendu serait copié avec). Cette réponse ne comportera donc pas le bit d'extension.
 - Si la tour est capable de gérer le mode étendu, elle renvoie comme d'habitude un message Hello en précisant également via le byte reserved qu'elle supporte le mode étendu. Elle passe de son côté en mode étendu.

À la réception de la réponse de la tour, l'avion est capable de savoir si la tour supporte ou non le mode étendu et de basculer en mode étendu si c'est nécessaire. Il pourra ensuite effectuer la suite du Handshake selon les spécifications du protocole étendu. Dans le cas où la tour n'a pas renvoyé le bit Extended, l'avion en déduit qu'elle ne le gère pas et reste en mode Legacy.
                                         
Programmation événementielle
----------------------------

La programmation événementielle est l'organisation d'une application autour du concept d'événements. Un événement survient lorsque quelque chose d'intéressant se produit à l'intérieur ou à l'extérieur du programme. C'est évnement est ensuite "émis" à l'interieur du programme pour notifier les différents composants qui pourraient s'en intéresser.

Lorsqu'il provient de l'extérieur, cet événement permet un style de programmation asynchone: au lieu de créer de multiples threads pour la plupart en attente du monde extérieur (lecture / écriture dans un fichier ou socket par exemple), l'application signale simplement son intérêt à recevoir les données entrantes d'un socket et est notifiée automatiquement par le système lorsque ces informations sont disponibles. Par exemple, au lieu d'effectuer à un appel à `read()` qui bloquera l'execution jusqu'à ce que des données soient disponible, l'application s'enregistrera en tant que listener sur l'événement `data` (par exemple) et sera appelée par le système lorsque ces données seront disponibles, de façon asynchrone.

Lorsqu'ils sont utilisé en interne, les événements sont un moyen de découpler fortement les différents composants de l'application. Par définition, un événement est simplement émis par le composant lorsque quelque chose se produit. Si cet événement n'intéresse personne, aucun gestionnaire n'aura été enregistré pour cet événements et personne ne sera averti. L'application n'a pas besoin de se préoccuper de la distribution à proprement parler de cet événement ni même de savoir si des gestionnaires ont été enregistré pour cet événement.

Ce concept d'événement est particulièrement adapté dans un programme fortement basé sur l'entrée/sortie et où les données nécessaires ne sont pas rapidement accessibles (lire et écrire sur le réseau est lent). Dans notre cas la tour de contrôle s'adapte plutôt bien à un modèle événementiel dans lequel le système signal de lui même la connexion de nouveaux avions, l'arrivée de messages, etc.

### Les événements dans Java

Ce qui s'approche le plus du concept d'événement en Java est le couple de classes Observable/Observer. L'observer est attaché à un observable et est notifié lorsque celui-ci change. Bien que le concept de base soit celui de la programmation événementielle, il nous manquait une composante essentielle pour l'utiliser efficassement dans notre programmes: l'observer ne reçoit pas (ou presque) d'information sur "ce qui a changé" sur son sujet d'observation (l'observable). Il y a bien la possibilité de passer un objet en paramètre, mais dans un but de généricité, celui-ci est typé `Object`. Il est donc inutilisable sans traitement supplémentaire une fois la notification de changement reçue. Dans notre conception de l'événement, celui-ci possède un type qui le défini et permet de savoir à quoi il correspond, Observer/Observable ne le permet pas (directement).

Une autre solution est la définition d'une interface listant tous les événements pouvant être émis par un objet et devant être implémentée par le gestionnaire d'événements. Il devient donc possible d'appeler explicitement la méthode correspondant à l'événement généré. Cette solution un peu plus agréable lors du traitement des exceptions nous aurait imposé de mettre en place notre propre méthode `addObserver` et notre distribution d'événement. Elle a également le défaut d'être une solution "tout ou rien". Dès le moment où un objet implémente l'interface d'événements d'un autre objet, ce dernier est forcé de gérer chaque événement que l'objet émetteur pourrait lancer. Dans notre cas ceci ne nous convenait pas. L'objet RadioServer s'occupant de la communication avec les avions devait pouvoir lancer des événements à l'intention de la radio. Parmi ces événements, il y a par exemple `PlaneConnected` qui signale la connexion réussie d'une avion, et l'événement `UncaughtException` qui signal qu'une erreur est survenue lors de l'execution de la radio. Le premier est intéressant pour la tour qui doit prévoir un routage pour cet avion et lui envoyer ces intructions. Le second n'est d'aucun intérêt pour la tour, savoir qu'une erreur est survenue est principalement destiné à l'opérateur de la tour de contrôle ainsi qu'au développeur. Savoir qu'un avion a innopinément fermé sa connexion n'intéresse pas la tour qui reçoit de toute façon l'événement `PlaneDisconnected`.

### Les événements dans notre projet

Nous souhaitions avoir les caractéristiques suivantes:

 - Système simple à la fois pour l'émission et la réception d'événement.
 - Un événement est caractérisé par une classe et donc potentiellement des méthodes spécifiques adaptée à chaque événement
 - Un écouteur d'événement est capable de ne capter que certains événements parmis tous ceux qui sont émis, en fonction de la classe. Il doit aussi être capable de récupérer un groupe d'événement en utilisant une super-classe dont ils héritent.

Un tel système n'existant pas en Java, nous avons développer le package `sat.events` qui contient les classes nécessaire à la mise en place du système d'événement que nous souhaitions. Les commentaires et le code sources des quelques fichiers de ce package devraient résumer assez bien le fonctionnement de notre système d'événement.

Au final, nous auront fait une utilisation intensive de ce framework événementiel en l'utilisant de façon systématique lorsqu'il était nécessaire de délivrer des messages de façon simple. Ce système d'événement joue également un rôle clé dans l'implémentation de notre "amélioration".

Le mode Remote
--------------

Etant donné la très faible inter-dépendance produite par le système événentiel, il devient très simple de découpler les éléments les uns des autres et de sérialiser ces événements sur un canal tel qu'un socket.

Nous avons donc mis en place une interface entièrement événementielle à la tour de contrôle (TowerAgent) destinée à fournir une interface vers la tour de contrôle entièrement basée sur les événements et les appels différés avec méthodes de *callback*. En interne, ces appels asynchrone sont transformés en événement comme n'importe quel autre.

Nous avons ensuite pu mettre en place un système sérialisant ces événements sur le réseau et permettant l'accès à la tour de contrôle depuis un autre processus, de façon totalement transparente pour le code de niveau supérieur. (On remplace simplement un TowerAgent par un RemoteTowerAgent qui en est une classe-fille).

Au final nous avons ajouté deux couches supplémentaires au dessus des trois prévues initialement:

 - Le TowerAgent, interface événementielle et asynchrone vers la tour de contrôle
 - Le GUI, totalement détaché des couches inférieures.

CLI
---

Nous avons également mis en place un système de CLI, une interface en ligne de commande. Afin d'avoir la possibilité de contrôler nos composants avant la mise en place de l'interface graphique.

Pour cela, nous nous sommes basés sur les fonctionnalités d'introspection de Java. Les classes de réflexion permettent d'analyser les méthodes définies dans un objet et de parcourir sa hiérarchie d'héritage. Dans notre cas, pour la création d'un CLI, il est nécessaire d'étendre la classe originale et de spécifier une méthode publique par commande disponible. Lors de l'initialisation de l'objet. La classe principale va parcourir l'ensemble des méthodes de la classe-fille et de toutes les classes dans sa hiérarchie d'héritage jusqu'à arriver à la classe-mère CLI. Au cours de cette analyse, la classe principale enregistre toutes les méthodes disponible et construit l'API publique du CLI utilisable dans la ligne de commande.

Lorsque l'utilisateur entre une ligne de commande dans l'interface CLI, cette ligne est découpée et analysée, et passée à la méthode correspondante en s'assurant que le nombre de paramètres soient correct. Le CLI ajoute des chaines vides pour chaque paramètre manquant, et tronque les paramètres superflus obtenu par la ligne de commande.