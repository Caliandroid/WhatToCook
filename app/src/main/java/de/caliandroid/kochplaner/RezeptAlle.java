package de.caliandroid.kochplaner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 05.03.16.
 */
public class RezeptAlle extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ListView.OnItemClickListener, ListView.OnItemLongClickListener, Spinner.OnItemSelectedListener {
    private ArrayList<Rezept> rezepte;
    private ListView listView;
    ArrayAdapter <String> adapter =null;
    MyCustomAdapter myCustomAdapter;
    Spinner spinner;
    int iPosition=0;
    public String[] rezepteTitel;
    DBHelper helper;
    boolean alreadyCreated=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Alle Rezepte");
        setContentView(R.layout.rezept_alle);


        //alle Rezepte laden
        helper = DBHelper.getInstance(this);
        rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        listView = (ListView)findViewById(R.id.listView2);

        myCustomAdapter = new MyCustomAdapter(this,R.layout.rezeptalle_element,rezepte);
        listView.setAdapter(myCustomAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        myCustomAdapter.notifyDataSetChanged();
        spinner = (Spinner)findViewById(R.id.spinner);
        //Array ist in den String Values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_array, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

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
           iPosition = position;
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
            i.putExtra("saison", rezept.getSaison());
            i.putExtra("position",iPosition);  //um bei Löschoperation das Rezept aus dem Array zu entfernen
            startActivityForResult(i, 1);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //Manuell ein Rezept hinzufügen
        final int iPos = position;

        //AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Manuell Rezept hinzufügen?");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                try {
                    Rezept rezept = (Rezept)rezepte.get(iPos);

                    //check
                    if( helper.alreadyInList(rezept)){
                        Toast.makeText(getApplicationContext(), "Bereits in der Liste", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        helper.insertPlanned(rezept);
                        helper.insertIntoShoppinglist(rezept);
                        Toast.makeText(getApplicationContext(), rezept.getTitel()+" aufgenommen", Toast.LENGTH_SHORT).show();

                    }

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

        //rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
       // adapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
       // adapter.notifyDataSetChanged();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Worker worker = new Worker(this);
        if (resultCode ==2) { //ein Rezept wurde gelöscht
        // ;
            rezepte = worker.bereinigeListe(rezepte,data.getIntExtra("id",-1));
            //Da ansonsten die MainActivity keine Rückmeldung bekommt. dass eventuell ein Item entfernt wurde, das in der Liste ist, muss ich hier die Bereinigung aufrufen
           // MainActivity.rezepte = worker.bereinigeListe(MainActivity.rezepte,data.getIntExtra("id",-1)); in OnResume dort neuladen
             myCustomAdapter.notifyDataSetChanged();
            //jetzt erst das Rezept endgültig aus der DB entfernen
            helper.deleteRezept(data.getIntExtra("id", -1));




        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //use boolean alreadyCreated to avoid recalling rezepte from database on activtiy creation. This happens already in onCreate
        if (spinner.getSelectedItemPosition() == 0 && alreadyCreated) {
            System.out.println("onItemSelected triggerd");
            //no filtering
            rezepte = helper.getRezepte(null, null, "TITEL ASC", null);
            myCustomAdapter.clear();
            myCustomAdapter.addAll(rezepte);


        }
        else{
            if (spinner.getSelectedItemPosition()==0 && !alreadyCreated) {
                alreadyCreated = true;

            }
            else {
                if (spinner.getSelectedItemPosition() > 0) {
                    System.out.println("onItemSelected triggerd");
                    //filtering. Filter typ = position-1
                    String[] selectionArgs = {String.valueOf(spinner.getSelectedItemPosition() - 1)};
                    rezepte = helper.getRezepte("typ = ? ", selectionArgs, "TITEL ASC", null);
                    //Da das neue Rezepte jetzt deutlich kürzer als bisher sein kann, muss ich clear aufrufen, um die bisherigen Objekte im Adapter zu bereinigen und dann neu hinzuzufügen
                    myCustomAdapter.clear();
                    myCustomAdapter.addAll(rezepte);
                }
            }
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println("onNothingSelected gewählt");

    }

    /**
     * Eigener Adapter zur Darstellung der Zutatenliste
     */
    private class MyCustomAdapter extends ArrayAdapter<Rezept> {


        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Rezept> rezepte) {

            super(context, textViewResourceId, rezepte);



        }

        private class ViewHolder{
            TextView rezeptTitel;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            //Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.rezeptalle_element, null);

                holder = new ViewHolder();
                holder.rezeptTitel = (TextView) convertView.findViewById(R.id.textView2);
                convertView.setTag(holder);

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Rezept rezept=rezepte.get(position);
            holder.rezeptTitel.setText(rezept.getTitel());

            return convertView;


        }


    }

}
