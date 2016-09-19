
# Multi-diffuseur musical - Specification

Ce programme permettra d'effectuer une écoute musicale en
multi-diffusion entre plusieurs dispositifs (smartphone, tablette).

## Fonctionnement

Dans un groupe d'appareils, il y aura un dispositif hôte et des dispositifs clients.

Le dispositif hôte sera en charge de diffuser le flux audio (musique, son)
vers les appareils clients.
Ce flux sera généré à partir de données provenant d'un fichier audio local au dispositif,
ou bien d'un autre flux audio entrant envoyé soit par un autre appareil,
soit un serveur distant (ex: webradio).

L'hôte devra dans un premier temps enregistrer les informations relatives
à chaque appareil souhaitant s'enregistrer avant de diffuser
le flux vers l'appareil cible.

Un appareil client devra s'enregistrer auprès de l'appareil hôte
afin de recevoir le flux audio.
Après enregistrement, celui-ci pourra "écouter" l'appareil hôte.
Lors de la réception du flux audio, l'appareil client devra le lire
afin que l'utilisateur puisse écouter la musique.
Ce même utilisateur devra, si cela est possible, pouvoir accéder aux métadonnées
relatives à ce qu'il écoute.

- - -

L'utilisateur de l'appareil hôte devra dans un premier temps
sélectionner le ou les fichiers audio à diffuser.
Sinon, il peut également se connecter à un dispositif hôte ou même
une webradio qui diffusera le flux audio, faisant du teléphone de l'utilisateur un relais.
Lorsque ce choix sera effectué, le diffusion pourra démarrer.

Pendant la diffusion, si l'utilisateur a définie une liste musicale
à partir de fichiers audio locaux à son appareil, il pourra décider
d'ajouter *à la volée* des fichiers audio supplémentaires,
mais ne pourra pas modifer le contenu existant.

Seul l'utilisateur de l'appareil hôte decide de lancer, mettre en pause,
ou bien arrêter la diffusion vers les autres appareils.

L'utilisateur d'un appareil client pourra "mettre en pause" la réception
du flux de l'hôte, et reprendre là où il s'est arrêter,
ou bien arrêter la réception à n'importe quel moment.

- - -
