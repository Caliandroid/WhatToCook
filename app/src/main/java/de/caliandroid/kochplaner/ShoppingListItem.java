package de.caliandroid.kochplaner;

/**
 * Created by stefan on 07.03.16.
 */
public class ShoppingListItem {
    private String zutat;
    private int id, rezept_id, shopped;



    public ShoppingListItem(int id,String zutat,  int shopped,int rezept_id){
        this.id=id;
        this.zutat=zutat;
        this.shopped=shopped;
        this.rezept_id=rezept_id;


    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopped() {
        return shopped;
    }

    public boolean isShopped(){
        if(this.shopped==0){
            return  false;
        }
        else{
            return true;
        }
    }

    public void setShopped(int shopped) {
        this.shopped = shopped;
    }

    public String getZutat() {
        return zutat;
    }

    public void setZutat(String zutat) {
        this.zutat = zutat;
    }

    public int getRezept_id() {
        return rezept_id;
    }

    public void setRezept_id(int rezept_id) {
        this.rezept_id = rezept_id;
    }
}
