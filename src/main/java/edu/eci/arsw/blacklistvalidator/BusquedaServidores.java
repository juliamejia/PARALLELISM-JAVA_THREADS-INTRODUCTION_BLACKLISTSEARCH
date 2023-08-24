package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;


/**
 * Clase que representa el ciclo de vida de un hilo y hace la busqqueda de un segmento del conjunto de servidores disponibles
 *
 */
public class BusquedaServidores extends Thread {

    //dirección ip que se está buscando en las listas negras
    private String ip;
    private HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

    //Rango de las direcciones ip que se van a revisar
    private int ultimaIP;
    private int primeraIP;

    //lista que almacena los numeros de la lista negra en los que se encuentra la direccion IP
    LinkedList<Integer> blackListOcurrencias =new LinkedList<>();
    private int ocurrenciasCount = 0;
    private int ListasNegrasCount;


    public BusquedaServidores(String ipaddress, int primera, int ultima){
        this.ip =ipaddress;
        this.ultimaIP =ultima;
        this.primeraIP =primera;
    }

    // Metodo que recorre las direcciones ip y verifica si las direccion ip esta en la lista negra
    public void run(){
        for(int i = primeraIP; i< ultimaIP && ocurrenciasCount < HostBlackListsValidator.BLACK_LIST_ALARM_COUNT; i++){
            ListasNegrasCount++;

            if (skds.isInBlackListServer(i, ip)){
                blackListOcurrencias.add(i);
                ocurrenciasCount++;

            }
        }
    }


    //devuelve la lista que contiene los numeros de lista negra donde se encontró la direccion ip
    public LinkedList<Integer> getBlackListOcurrencias() {
        return blackListOcurrencias;
    }

    public int getOcurrenciasCount() {
        return ocurrenciasCount;
    }

    public int getListasNegrasCount() {
        return ListasNegrasCount;
    }
}


