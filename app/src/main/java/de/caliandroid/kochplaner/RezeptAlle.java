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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rezept_alle);

        //alle Rezepte laden
        DBHelper helper = new DBHelper(this);
        rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        Worker worker = new Worker(this);
        listView = (ListView)findViewById(R.id.listView2);
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, worker.getRezeptTitel(rezepte));
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
        System.out.println("Rezept "+r.getTitel()+" angeklickt");
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
            startActivityForResult(i, 1);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("Lange geklickt!");
        //TODO Handling fürs REzepte hinzufügen komplettieren
        // MainActivity.rezepte.add((Rezept) rezepte.get(position));
        return true;
    }
}
