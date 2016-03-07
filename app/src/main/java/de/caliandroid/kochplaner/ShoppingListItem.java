package de.caliandroid.kochplaner;

/**
 * Created by stefan on 07.03.16.
 */
public class ShoppingListItem {
    private String zutat;
    private int id, rezept_id, shopped;

    public ShoppingListItem(int id,String zutat, int rezept_id, int shopped){
        this.id=id;
        this.zutat=zutat;
        this.rezept_id=rezept_id;
        this.shopped=shopped;

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

    public void setShopped(int shopped) {
        this.shopped = shopped;
    }

    public String getZutat() {
        return zutat;
    }

    public void setZutat(String zutat) {
        this.zutat = zutat;
    }
}
