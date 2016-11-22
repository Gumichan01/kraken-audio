
# Notes de projet #

## UI ##

- écran creation de nom (DONE)
- écran creation de groupe (TODO)

> Info complémentaire

* gauche de l'écran - diffuseurs dans le groupe courant (DONE)
* au millieu - mixage (DONE)
* droite de l'écran - receveurs (DONE)

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

    CGRP nom_groupe nom_appareil ip_addr port
    /// CGRP : Create GRouP
    /*
    NB: *ip_addr* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    */

Avoir la liste des groupes:

    GRPL    /// GRPL : GRouP List

Avoir la liste des appareils d'un groupe donné:

    DEVL nom_groupe    /// DEVL: Device List

Rejoindre un groupe spécifique:

    JGRP nom_groupe nom_appareil ip_addr port
    /// JGRP : Join GRouP
    /*
    NB: *ip* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    */

Quitter un groupe:

    QGRP    /// QGRP : Quit GRouP

Fermer la connexion:

    EOCO    /// EOCO : End Of COnnexion

 - Syntaxe réponse **serveur**.

Après la création d'un groupe (si ok):

    GCOK    /// GCOK: Group Creation OK

Envoi de la liste de groupes:

    GRPL nombre_de_groupe
    ...
    GDAT nom_groupe nombre_appareils
    ...
    EOTR
    /*
        GRPL : GRouP List
        GDAT : Group DATa
        EOTR : EndOf TRansmission
    */

Après l'ajout d'un appareil dans un groupe (si OK).

    GJOK    /// GRPJ: GRouP Join OK

Après l'ajout d'un appareil dans un groupe (si echec).

    GJKO    /// GJKO : GRouP Join KO

Quand le téléphone quitte le groupe:

    QACK    /// Quit ACKnowledgment
    /* Puis fermeture de la connexion (côté serveur). */

Quand le téléphone ferme le connexion:

    /// Pas d'envoi, le serveur ferme juste le socket


### Implémentation ###

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

---
[1]: https://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
