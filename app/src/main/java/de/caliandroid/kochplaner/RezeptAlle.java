package de.caliandroid.kochplaner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by stefan on 05.03.16.
 */
public class RezeptAlle extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ListView.OnItemClickListener, ListView.OnItemLongClickListener {
    private ArrayList<Rezept> rezepte;
    private ListView listView;
    ArrayAdapter adapter =null;
    int iPosition=0;
    String[] rezepteTitel;

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
        System.out.println("Lange geklickt!");
        //TODO Handling fürs REzepte hinzufügen komplettieren
        MainActivity.rezepte.add((Rezept) rezepte.get(position));
        DBHelper helper = new DBHelper(this);
        helper.insertIntoShoppinglist((Rezept) rezepte.get(position));
        //zusätzlich in die planned DB eintragen
        helper.insertPlanned((Rezept) rezepte.get(position));
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
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Aktuell nur ausgelöst, sollte ein Rezept gelöscht worden sein, man erhält als ResultCode die ID
        //synchronized hinzugefügt, damit es vor Erstellung der ListView ablaufen kann -> funktioniert!
        if (resultCode >= 0) {
            Worker worker = new Worker(this);
            rezepte.remove(resultCode);
            rezepteTitel=worker.getRezeptTitel(rezepte);
            //weil innerhalb des Adapters eine eigene Instanz von rezepteTitel besteht, muss ich entweder wie nun folgend
            //einen neuen Adapter erstellen oder ich muss einen eigenen Adapter erstellen, innerhalb dessen es eine refresh Funktion gibt.
            adapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1,rezepteTitel);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            //Ebenfalls aus dem MainAct.Rezepte dieses Element entfernen (sofern vorhanden)

        }
    }

}
