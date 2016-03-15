package de.caliandroid.kochplaner;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 05.03.16.
 */
public class RezeptAlle extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ListView.OnItemClickListener, ListView.OnItemLongClickListener {
    private ArrayList<Rezept> rezepte;
    private ListView listView;
    ArrayAdapter <String> adapter =null;
    int iPosition=0;
    public String[] rezepteTitel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rezept_alle);

        //alle Rezepte laden
        DBHelper helper = new DBHelper(this);
        rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        Worker worker = new Worker(this);
        listView = (ListView)findViewById(R.id.listView2);
        rezepteTitel=worker.getRezeptTitel(rezepte);
        adapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1,rezepteTitel );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        adapter.notifyDataSetChanged();

    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }


    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Rezept r = rezepte.get(position);
        System.out.println("Habe "+r.getTitel()+ " an POS "+position+ " angeklickt");
        iPosition=position;
        startEditAnsicht(r, RezeptAnsicht.class);


    }


    // TODO: Die Methode gibts jetzt zweimal, einmal in MainActivity und einmal hier -> Optimieren!
    public void startEditAnsicht(Rezept rezept, Class e){
        Intent i = new Intent(this,e);
        if(rezept==null){
            startActivityForResult(i, 1);

        }
        else {
            i.putExtra("id", rezept.getId());
            i.putExtra("titel", rezept.getTitel());
            i.putExtra("zutaten", rezept.getZutaten());
            i.putExtra("anleitung", rezept.getAnleitung());
            i.putExtra("anzahl", rezept.getAnzahl());
            i.putExtra("typ", rezept.getTyp());
            i.putExtra("imageUri",rezept.getImageUri());
            i.putExtra("blocked",rezept.getBlocked());
            i.putExtra("position",iPosition);  //um bei Löschoperation das Rezept aus dem Array zu entfernen
            startActivityForResult(i, 1);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //Manuell ein Rezept hinzufügen
        final DBHelper helper = new DBHelper(this);
        final int iPos = position;

        //AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Manuell Rezept hinzufügen?");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                try {
                    MainActivity.rezepte.add((Rezept) rezepte.get(iPos));
                    helper.insertPlanned((Rezept) rezepte.get(iPos));
                    helper.insertIntoShoppinglist((Rezept) rezepte.get(iPos));
                    //zusätzlich in die planned DB eintragen

                } catch (SQLiteException e) {
                    throw e;
                }
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //nix
                    }
                });

        alert.show();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        DBHelper helper = new DBHelper(this);
        rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        DBHelper helper = new DBHelper(this);
        rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Worker worker = new Worker(this);
        // Aktuell nur ausgelöst, sollte ein Rezept gelöscht worden sein, man erhält als ResultCode die ID
        //synchronized hinzugefügt, damit es vor Erstellung der ListView ablaufen kann -> funktioniert!
        if (resultCode ==2) { //ein Rezept wurde gelöscht
            rezepte.remove(data.getIntExtra("position",-1));
            rezepteTitel=worker.getRezeptTitel(rezepte);
            //Da ansonsten die MainActivity keine Rückmeldung bekommt. dass eventuell ein Item entfernt wurde, das in der Liste ist, muss ich hier die Bereinigung aufrufen
            MainActivity.rezepte = worker.bereinigeListe(MainActivity.rezepte,data.getIntExtra("id",-1));

             //weil innerhalb des Adapters eine eigene Instanz von rezepteTitel besteht, muss ich entweder wie nun folgend
             //einen neuen Adapter erstellen oder ich muss einen eigenen Adapter erstellen, innerhalb dessen es eine refresh Funktion gibt

            //und den dann gleich als ordentlichen Adapter
            adapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1,rezepteTitel);
             listView.setAdapter(adapter);
             adapter.notifyDataSetChanged();




        }
    }

}
