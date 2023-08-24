/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    public static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int numeroHilos){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        int ocurrencesCount=0;
        List<BusquedaServidores> hilos = new ArrayList<>();
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        int checkedListsCount=0;
        int extra = 0;

        int range = skds.getRegisteredServersCount()/numeroHilos;
        int mod = skds.getRegisteredServersCount()%numeroHilos;

        for(int i =0;i<numeroHilos;i++) {
            int ini = range*i;
            int fin = range*(i+1);
            BusquedaServidores busqueda = new BusquedaServidores(ipaddress,ini,fin);
            hilos.add(busqueda);
            busqueda.start();
            extra = fin;
        }
        if(mod != 0){
            BusquedaServidores busqueda = new BusquedaServidores(ipaddress,extra,extra+mod);
            hilos.add(busqueda);
            busqueda.start();
        }
        for (BusquedaServidores hilo : hilos) {
            try {
                hilo.join();
                ocurrencesCount += hilo.getOcurrenciasCount();
                checkedListsCount += hilo.getListasNegrasCount();
                blackListOcurrences.addAll(hilo.getBlackListOcurrencias());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }


    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());



}