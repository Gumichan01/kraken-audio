
# Notes de projet #

## UI ##

- écran creation de nom (DONE)
- écran creation de groupe (TODO)

> Info complémentaire

* gauche de l'écran - diffuseurs dans le groupe courant (DONE)
* au millieu - mixage (DONE)
* droite de l'écran - receveurs (DONE)

## Communication ##

> Serveur

- Gérer un annuaire d'appareils (nom, adresse IP, numéro de port)
- Création de groupe
- Intégrer un appareil dans un groupe
- Enlever un appareil d'un groupe
- Envoyer les informations d'un/plusieurs appareil(s) au client

> Client

- Créer un groupe
- Rejoindre un groupe
- Avoir la liste des groupes
- (Supprimer un groupe)
- Quitter un groupe


> Remarques

 - Syntaxe requêtes **client**:

Création groupe:

    CGRP nom_groupe ip_addr port

Avoir la liste des groupes:

    GRPLIST

Avoir la liste des appareils:

    DEVLIST

Rejoindre un groupe spécifique:

    JGRP nom_groupe ip port

Quitter un groupe:

    QGRP

 - Syntaxe réponse **serveur**.

Après la création d'un groupe (si ok):

    GRPOK

Envoi de la liste de groupes:

    GRPL nombre_de_groupe
    ...
    GRPDATA nom_groupe nombre_appareils
    ...
    EOT

Après l'ajout d'un appareil dans un groupe (si OK).

    GRPADDED
    // Puis envoi de la liste des appareils dans le groupe
    DEVL nombre_appareils
    ...
    DEVDATA ip port
    ...
    EOT

Quand le téléphone quitte le groupe:

    GRPQUIT

Puis fermeture de la connexion (côté serveur).
