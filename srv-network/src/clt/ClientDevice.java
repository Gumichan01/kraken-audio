package clt;

public class ClientDevice {
	public ClientDevice(){
		
	}
	
	// Création d'un groupe:
	
    //CGRP nom_groupe nom_appareil ip_addr port
    /// CGRP : Create GRouP
    /*
    NB: *ip_addr* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    */
	
	// Rejoindre un groupe spécifique:
	
    //JGRP nom_groupe nom_appareil ip_addr port
    /// JGRP : Join GRouP
    /*
    NB: *ip* et *port* correspondent respectivement à l'adresse IP
    et au n° de port de l'appareil.
    */
    
	// Avoir la liste des groupes:
	
	//GRPL    /// GRPL : GRouP List
	
	// (Supprimer un groupe) → pour l'instant on ne met pas, on verra si besoin.
	
	// Quitter un groupe:
	//QGRP    /// QGRP : Quit GRouP
}
