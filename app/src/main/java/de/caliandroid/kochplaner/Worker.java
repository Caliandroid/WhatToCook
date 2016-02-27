package de.caliandroid.kochplaner;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by cali on 31.01.16.
 */
public class Worker {
    private Context myContext;

    public Worker(Context myContext){
        this.myContext=myContext;
    }; //brauche für manche Funktionen das Contextobjekt der aufrufenden Klasse

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


    public ArrayList<Rezept> bereinigeListe(ArrayList<Rezept>rezepte, int id){
        Rezept r;
        Iterator i = rezepte.iterator();
        while(i.hasNext()){
            r = (Rezept)i.next();
            if(r.getId()==id){
                rezepte.remove(r);
            }

        }
        return rezepte;

    }

    public int[] importCSVRezepte(String fileName, String delimiter) throws IOException{
        Log.v("Start","Import gestartet (CSV)");
        int[]results={0,0};
        AssetManager manager = myContext.getApplicationContext().getAssets();
        InputStream inStream = null;

        //FileReader fr = new FileReader(descriptor.getFileDescriptor());
        inStream = manager.open(fileName);
        BufferedReader br=new BufferedReader(new InputStreamReader(inStream));
        String line;
        String[]temp;
        Rezept r;
        DBHelper dbhelper=new DBHelper(MainActivity.activity); //TODO Welches Context Objekt wäre angemessen?


        while ((line = br.readLine()) != null){
                temp= line.split(delimiter);

                if(temp.length==5){
                    //TODO versuche ein Rezeptobjekt zu erstellen
                    r= new Rezept(-1,temp[0],temp[1],temp[2],Integer.valueOf(temp[3]),Integer.valueOf(temp[4]),false);
                    if(!dbhelper.doesAlreadyExist(r)){
                        dbhelper.insertRezept(r);
                        Log.v("CSV Import" , "Rezept " + r.getTitel() + " erfolgreich importiert");
                        results[0]=+1;

                    }
                    else{
                        Log.v("INFO","Rezept existiert schon in DB:: "+line);
                        results[1]=+1;
                    }



                }
                else{
                    Log.v("Error","Konnte nichts lesen in Zeile:: "+line+"\nDer Split hat die Länge="+temp.length);
                    results[1]=+1;
                }

        }
        br.close();
        inStream.close();
        return results;
    }
    public int[] importCSVRezepteFromSDCard(String path,String fileName, String delimiter) throws IOException{
        Log.v("Start","Import gestartet (CSV)");
        int[]results={0,0};

        //File file = new File(Environment.getExternalStorageDirectory(),"import.csv"); <-- gibt mir /storage/emulated/0/ anstelle der realen SD Card
        File file = new File(path,"import.csv");
        FileReader fr= new FileReader(file);
        BufferedReader br=new BufferedReader(fr);
        String line;
        String[]temp;
        Rezept r;
        DBHelper dbhelper=new DBHelper(MainActivity.activity); //TODO Welches Context Objekt wäre angemessen?


        while ((line = br.readLine()) != null){
            temp= line.split(delimiter);

            if(temp.length==5){
                //TODO versuche ein Rezeptobjekt zu erstellen
                r= new Rezept(-1,temp[0],temp[1],temp[2],Integer.valueOf(temp[3]),Integer.valueOf(temp[4]),false);
                if(!dbhelper.doesAlreadyExist(r)){
                    dbhelper.insertRezept(r);
                    Log.v("CSV Import" , "Rezept " + r.getTitel() + " erfolgreich importiert");
                    results[0]=+1;

                }
                else{
                    Log.v("INFO","Rezept existiert schon in DB:: "+line);
                    results[1]=+1;
                }



            }
            else{
                Log.v("Error","Konnte nichts lesen in Zeile:: "+line+"\nDer Split hat die Länge="+temp.length);
                results[1]=+1;
            }

        }
        br.close();
        fr.close();
        return results;
    }


}
