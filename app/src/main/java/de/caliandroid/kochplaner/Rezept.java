package de.caliandroid.kochplaner;

/**
 * Created by cali on 31.01.16.
 */
public class Rezept {

    private int id,typ,anzahl,blocked;
    private String titel,anleitung,zutaten,imageUri,saison;


    public Rezept(int id,String titel,String zutaten, String anleitung,int typ, int anzahl,String imageUri, int blocked, String saison){
        this.id = id;
        this.titel=titel;
        this.zutaten=zutaten;
        this.anleitung=anleitung;
        this.typ=typ;
        this.anzahl=anzahl;
        this.imageUri=imageUri;
        this.blocked = blocked;
        this.saison = saison;
    }

    public int getId(){
        return this.id;
    }
    public int getAnzahl(){
        return this.anzahl;
    }
    public String getTitel(){
        return this.titel;
    }
    public String getZutaten(){
        return this.zutaten;
    }
    public String getAnleitung(){
        return this.anleitung;
    }
    public int getTyp(){
        return  this.typ;
    }
    public String getImageUri(){
        return this.imageUri;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }
    public String getSaison() {  return this.saison; }



}

