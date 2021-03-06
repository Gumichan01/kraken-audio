
# Notes de projet #

## UI ##

- écran creation de nom
- écran creation de groupe

> Info complémentaire

* gauche de l'écran - diffuseurs dans le groupe courant
* au millieu - mixage
* droite de l'écran - receveurs

## Communication ##

### Specification ###

> Serveur

- Gérer un annuaire d'appareils (nom, adresse IP, numéro de port)
- Création de groupe
- Intégrer un appareil dans un groupe
- (Enlever un appareil d'un groupe) → pour l'instant on ne met pas, on verra si besoin.
- Envoyer les informations d'un/plusieurs appareil(s) au client

> Client

- Créer un groupe
- Rejoindre un groupe
- Avoir la liste des groupes
- (Supprimer un groupe) → pour l'instant on ne met pas, on verra si besoin.
- Quitter un groupe


### Syntaxe des requetes ###


 - Syntaxe requêtes du **client**:

Création groupe:

    CGRP nom_groupe nom_appareil ip_addr port_info port_diff
    /// CGRP : Create GRouP
    /*
    NB: *ip_addr* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    \*/

Avoir la liste des groupes:

    GRPL    /// GRPL : GRouP List

Avoir la liste des appareils d'un groupe donné:

    DEVL nom_groupe    /// DEVL: Device List

Rejoindre un groupe spécifique:

    JGRP nom_groupe nom_appareil ip_addr port_info port_diff
    /// JGRP : Join GRouP
    /*
    NB: *ip* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    \*/

Quitter un groupe:

    QGRP nom_groupe nom_appareil /// QGRP : Quit GRouP

Fermer la connexion:

    EOCO    /// EOCO : End Of COnnexion


Signalement de présence:

    IAMH nom_appareil	/// IAMH : I AM Here

Liaison de graphe (pour la création de graphe orienté):

	GRPH nom_appareil_source op nom_appareil_cible		/// GRPH GRaPHe
	/// op ∈ { '->', 'x' }
	/// A -> B : A diffuse un flux audio vers B
	/// A x B : A diffuse pas/plus un flux audio vers B

Obtention d'un graphe:

	GGPH	/// GGPH: Get GraPHe



 - Syntaxe réponse **serveur**.

Après la création d'un groupe (si ok):

    GCOK    /// GCOK: Group Creation OK

Envoi de la liste de groupes:

    GDAT nom_groupe nombre_appareils
    ...
    EOTR
    /*
        GRPL : GRouP List
        GDAT : Group DATa
        EOTR : EndOf TRansmission
    \*/


Envoi de la liste d'appareils':

    DDAT nom_appareil ip_addr port_info port_diff
    ...
    EOTR
    /*
        DDAT : Device DATa
        EOTR : EndOf TRansmission
    \*/

Après l'ajout d'un appareil dans un groupe (si OK).

    GJOK    /// GRPJ: GRouP Join OK


Quand le téléphone quitte le groupe:

    QACK    /// Quit ACKnowledgment
    /* Puis fermeture de la connexion (côté serveur). \*/

Quand le téléphone ferme le connexion:

    /// Pas d'envoi, le serveur ferme juste le socket

Quand le téléphone signal sa présence (il est toujours là):

	UACK	/// UACK: Update ACKnolegment

Quand le teléphone annonce une diffusion (en gros il diffuse vers un appareil cible)

	GPOk	/// GPOK: GraPhe update OK

Quand le téléphone veut le graphe:

	VRTX id₁ nom₁
	VRTX id₂ nom₂
	...
	LINE id₁ idr₁|...|idrₙ		/// LINE: chemin
	…
	EOTR	/// EOTR: End Of TRansmission

	/*
	 *	VRTX donne le sommet d'un graphe.
	 *	Ce sommet est défini par le couple (id, nom).
	 *
	 *	LINE est une chaine de caractère qui défini les adjacences, 
	 *	i.e. les appareils qui, énumérés dans l'ordre, indique que:
	 *		- idₙ est la source qui diffuse le message (n ∈ ℕ*).
	 *		- idrₙ (n ∈ ℕ*) est l'appareil au bout du chemin.
	 */

En cas d'echec quelconque:

    FAIL

### Implémentation ###

 - Un client ne peut pas créer un groupe qui existe déjà.
 - Un client ne peut pas rejoindre ou quitter un groupe qui n'existe pas
 - Un serveur a plusieurs groupes de diffusions.
 - Un groupe de diffusion a plusieurs appareils (téléphones, tablettes, ...).
 - Les groupes du serveur seront stockés dans une [Hashtable][1], de la manière suivante :


    Hashtable<String,GroupInfo>

 - Les appareils dans un groupe donné seront aussi dans une [Hashtable][1]

 - Chaque groupe contient :
    * Un nom
    * L'annuaire des appareils défini de la manière suivante :


    Hashtable<String,DeviveData>

 - Chaque appareil est définie par :
    * Son adreese IP
    * Son numéro de port

 - Le graphe est défini comme une HasTable de la manière suivante :
	
	
	Hashtable<String,Gedge>

 - Chaque **Gedge** est défini par :
	* Son nom
	* La liste de ses successeurs

	
### TODO ###

Kenny : afficher le graphe dans un Toast (il faut lui expliquer comment j'ai représenter le graphe)

---
[1]: https://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
