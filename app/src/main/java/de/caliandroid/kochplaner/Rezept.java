package de.caliandroid.kochplaner;

/**
 * Created by cali on 31.01.16.
 */
public class Rezept {

    private int id,typ,anzahl;
    private String titel,anleitung,zutaten,imageUri;
    private boolean selected;

    public Rezept(int id,String titel,String zutaten, String anleitung,int typ, int anzahl,String imageUri, boolean selected){
        this.id = id;
        this.titel=titel;
        this.zutaten=zutaten;
        this.anleitung=anleitung;
        this.typ=typ;
        this.anzahl=anzahl;
        this.imageUri=imageUri;
        this.selected=selected;
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
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

