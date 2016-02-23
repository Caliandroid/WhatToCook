package de.caliandroid.kochplaner;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by cali on 31.01.16.
 */
public class Worker {

    public Worker(){};

    /**
     * Holt sich die ID's als kommaseparierter String zur Speicherung und Restaurierung bei App-Neustart
     * Somit kann das Programm die geplanten Rezepte speichern, ohne extra Objekte hierfür speichern zu müssen oder Extrafelder in der DB zu belegen
     * @param al
     * @return sReturn
     */
    public String getIDs (ArrayList<Rezept> al){
        String sReturn ="";
        Iterator i = al.iterator();
        Rezept rezept;
        int i1=0;
        while(i.hasNext()){
            rezept =(Rezept)i.next();
            if(i1>0){
                sReturn =sReturn+","+String.valueOf(rezept.getId());
            }
            else{
                sReturn =String.valueOf(rezept.getId());
                i1++;
            }
        }

        return sReturn;
    }

    public String getMailText(ArrayList<Rezept> rezepte){
        StringBuffer sb=new StringBuffer();
        String mailText="Fehler - Konnte keinen Wochenplan generieren";
        Rezept rezept;
        Iterator i = rezepte.iterator();
        int iTag=1;
        while(i.hasNext()){
            rezept =(Rezept)i.next();
            sb.append("Tag "+iTag);
            sb.append("\n");
            sb.append(rezept.getTitel());
            sb.append("\n");
            sb.append("Zutaten:");
            sb.append("\n");
            sb.append(rezept.getZutaten());
            sb.append("\n");
            sb.append("Anleitung:");
            sb.append("\n");
            sb.append(rezept.getAnleitung());
            sb.append("\n");
            sb.append("########################");
            sb.append("\n\n");
            iTag++;

        }
        mailText=sb.toString();
        return mailText;

    }

    //TODO Importer und Exporter zu CSV


}
