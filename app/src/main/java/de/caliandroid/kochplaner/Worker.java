package de.caliandroid.kochplaner;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
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
            //Anleitung weglassen, sonst platzt die Mail :)

            //sb.append("Anleitung:");
            //sb.append("\n");
           // sb.append(rezept.getAnleitung());
           // sb.append("\n");
            sb.append("########################");
            sb.append("\n");
            iTag++;

        }

        sb.append("\n**********************\nJetzt folgen die Anleitungen\n");
        Iterator i2 = rezepte.iterator();

        while(i2.hasNext()){
            rezept =(Rezept)i2.next();
            sb.append("\n");
            sb.append(rezept.getTitel());
            sb.append("\n---------------\n");
            sb.append(rezept.getAnleitung());
            sb.append("\n*********************");

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
               // rezepte.remove(r); führt immer mal wieder zu java.util.ConcurrentModificationException, daher besser über die remove Methode des iterators gehen
                i.remove();
                //because of unique key constraint an id can only appear once
                break; //saves time
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

                if(temp.length==7){
                    //TODO versuche ein Rezeptobjekt zu erstellen
                    r= new Rezept(-1,temp[0],temp[1],temp[2],Integer.valueOf(temp[3]),Integer.valueOf(temp[4]),temp[5],Integer.valueOf(temp[6]));
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
        File file = new File(path,fileName);
        FileReader fr= new FileReader(file);
        BufferedReader br=new BufferedReader(fr);
        String line;
        String[]temp;
        Rezept r;
        DBHelper dbhelper=new DBHelper(MainActivity.activity); //TODO Welches Context Objekt wäre angemessen?


        while ((line = br.readLine()) != null){
            temp= line.split(delimiter);

            if(temp.length==7){
                //TODO versuche ein Rezeptobjekt zu erstellen
                r= new Rezept(-1,temp[0],temp[1],temp[2],Integer.valueOf(temp[3]),Integer.valueOf(temp[4]),temp[5],Integer.valueOf(temp[6]));
                if(!dbhelper.doesAlreadyExist(r)){
                    dbhelper.insertRezept(r);
                    Log.v("CSV Import" , "Rezept " + r.getTitel() + " erfolgreich importiert");
                    results[0]++;

                }
                else{
                    Log.v("INFO","Rezept existiert schon in DB:: "+line);
                    results[1]++;
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


    public boolean exportRezepteToCSV(String path,String fileName, String delimiter,ArrayList<Rezept> rezepte) throws IOException {
        Log.v("Start", "Export gestartet (CSV)");
        int[] results = {0, 0};

        //File file = new File(Environment.getExternalStorageDirectory(),"import.csv"); <-- gibt mir /storage/emulated/0/ anstelle der realen SD Card
        File file = new File(path, fileName);
        file.delete(); //eventuell vorhandenes exportfile löschen
        file = new File(path, fileName);
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuffer content;
        String line;
        String[] temp;
        Rezept rezept;
        DBHelper dbhelper = new DBHelper(MainActivity.activity); //TODO Welches Context Objekt wäre angemessen?
        Iterator iterator = rezepte.iterator();

        while(iterator.hasNext()){
            rezept = (Rezept) iterator.next();
            bufferedWriter.write(rezept.getTitel()+delimiter+rezept.getZutaten()+delimiter+rezept.getAnleitung()+delimiter+rezept.getTyp()+delimiter+rezept.getAnzahl()+delimiter+rezept.getImageUri()+delimiter+rezept.getBlocked());
            bufferedWriter.write("\n");
        }
        bufferedWriter.close();


        return true;


    }

    public void deleteFileFromSDCard(String path){
        File file =new File(path);
        file.delete();

    }

    public String[] getRezeptTitel(ArrayList<Rezept> rezepte){
        Iterator iterator = rezepte.iterator();
        String [] rezeptTitel = new String [rezepte.size()];
        int i=0;
        Rezept rezept;
        while(iterator.hasNext()){
            rezept=(Rezept)iterator.next();
            rezeptTitel[i]=rezept.getTitel();
            i++;
        }
        return rezeptTitel;
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }



}
