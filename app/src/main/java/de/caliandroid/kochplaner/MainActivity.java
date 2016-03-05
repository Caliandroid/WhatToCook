package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.SharedPreferences.*;
import static android.view.View.INVISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, View.OnTouchListener {
    public static final String MY_PREFS = "MyPrefs";
    DBHelper helper;
    ArrayList<Rezept> rezepte = new ArrayList();
    Rezept rezept;
    Button button;
    ListView myListView;
    MyCustomAdapter dataAdapter;
    //NICHT MEHR IM EINSATZ:  ArrayAdapter<String> myArrayAdapter;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor ;
    public static Activity activity; //damit in Subclassen die Referenz zu dieser Klasse vorhanden ist
    String restoredIDs;
    ArrayList blocker=new ArrayList<String>();
    public static String imageUri; //zur Anwendung in AddEditRezept;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity=this;

        //Check if DB already exists (first run)
        helper = new DBHelper(this);
        try {
            helper.createDB();
        } catch (IOException e) {
            throw new Error("Cannot initialize prepopulated db");
        }

        SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        restoredIDs = prefs.getString("plannedIDs", null);
        //nur falls etwas zum Wiederherstellen existiert
        if (restoredIDs!=null && !restoredIDs.isEmpty()) {
            helper = new DBHelper(this);
            rezepte = helper.getGeplanteRezepte(restoredIDs);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        myListView = (ListView) findViewById(R.id.listView);
        //myListView.setOnItemClickListener(this); Nicht mehr benötigt, da in CustomAdapter integriert
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        dataAdapter = new MyCustomAdapter(this,R.layout.row, rezepte);
        myListView.setAdapter(dataAdapter);

        dataAdapter.notifyDataSetChanged();
        myListView.setOnTouchListener(this);




        //Hier eine Textmail mit den Rezepten samt Details erstellen
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //Mail senden
                Worker worker =new Worker(getApplicationContext());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, "sarah@caliandro.de,stefan@caliandro.de");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Kochplan dieser Woche");
                intent.putExtra(Intent.EXTRA_TEXT,worker.getMailText(rezepte));
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_editRezept) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Für den Button
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            //AlertDialog
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Neue Woche planen?");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    helper = new DBHelper(MainActivity.activity);
                    try {
                        // helper.createDB();
                        rezepte = helper.getKochplan();


                        //myArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getStringArray(rezepte));

                        dataAdapter = new MyCustomAdapter(MainActivity.activity, R.layout.row, rezepte); //MainActivity.activity anstelle von this
                        myListView.setAdapter(dataAdapter);
                        dataAdapter.notifyDataSetChanged();
                /*editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                Worker myWorker = new Worker();
                editor.putString("plannedIDs", myWorker.getIDs(rezepte));
                editor.commit();*/


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

            //Toast.makeText(getApplicationContext(),"Button geklickt", Toast.LENGTH_LONG).show();

        }

    }


    /**
     * Öffnet die Detailansicht oder EditAnsicht und wird in der OnItemClick Methode innerhalb des CustomAdapter aufgerufen
     * @param rezept
     */
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        //an dieser Stelle die Änderungen speichern
        saveSharedPrefs();
        //DB Verbindung schließen
        helper.close();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_editRezept) {
            //starte leere Rezepteingabe
            startEditAnsicht(null,AddEditRezept.class);
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            //starte  Settings
            Intent i = new Intent(this,Settings.class);
            startActivityForResult(i, 1);
            return true;
        }
        if (item.getItemId() == R.id.action_alleRezepte) {
            //starte  Settings
            Intent i = new Intent(this,RezeptAlle.class);
            startActivityForResult(i, 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //wird aktuell nicht genutzt;
        return false;
    }

    public void saveSharedPrefs(){
        //an dieser Stelle die Änderungen speichern
        editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
        Worker myWorker = new Worker(this);
        editor.putString("plannedIDs", myWorker.getIDs(rezepte));
        editor.commit();

    }

    @Override
    protected synchronized  void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Aktuell nur ausgelöst, sollte ein Rezept gelöscht worden sein, man erhält als ResultCode die ID
        //synchronized hinzugefügt, damit es vor Erstellung der ListView ablaufen kann -> funktioniert!
        if(resultCode>=0){
            //Liste der geplanten Rezepte erneut einlesen, das nicht mehr existierende wird dabei verworfen und entfernt -
            Worker worker= new Worker(this);
           rezepte= worker.bereinigeListe(rezepte,resultCode);
            // rezepte = helper.getGeplanteRezepte(restoredIDs); TODO Testen, ob nötig oder nicht
            dataAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Angepaßter Adapter, der die Rezepte samt einer Auswahlcheckbox anzeigen soll
     */
    private class MyCustomAdapter extends ArrayAdapter<Rezept> {

        //private ArrayList<Rezept> rezepte1;
        int i=0; //für die Farben in der Liste

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Rezept> rezepte) {
            super(context, textViewResourceId, rezepte);
            // benötige keine lokale Version, da ich direkt mit der AL rezepte arbeiten kann
           // this.rezepte1 = new ArrayList<Rezept>(); this.rezepte1.addAll(rezepte);
        }

        private class ViewHolder{
            TextView name;

            CheckBox selected;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.textView1);
                holder.name.setTextSize(23);
                holder.selected = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.selected.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final CheckBox cb = (CheckBox) v;
                        final Rezept rezept = (Rezept) cb.getTag();
                        rezept.setSelected(cb.isChecked());
                        if (rezept.isSelected()) {

                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Wurde " + rezept.getTitel() + " gekocht?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann entfernen und Häufigkeit hochsetzen
                                    helper.updateHaeufigkeit(rezept.getId());
                                    rezepte.remove(position);
                                    Toast.makeText(getApplicationContext(), rezept.getTitel() + " gekocht :)", Toast.LENGTH_LONG).show();
                                    dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde

                                }
                            });

                            alert.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //deselektieren
                                            cb.setChecked(false);
                                        }
                                    });

                            alert.show();


                        }

                    }

                });
                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        TextView tv = (TextView) v;
                        Rezept rezept = (Rezept) tv.getTag();
                        //Öffne RezeptAnsicht mit Inhalt des Rezepts
                        startEditAnsicht(rezept, RezeptAnsicht.class);

                    }

                });
                holder.name.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //TODO Rezept an dieser Stelle austauschen
                        final  TextView tv = (TextView)v;
                        final Rezept rezept = (Rezept) tv.getTag();
                        if (rezept!=null) {

                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Keine Lust auf "+ rezept.getTitel() + " ?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //neues Rezept gleichen Typs laden (unter der Berücksichtung der bereits geplannten Rezepte in der Liste
                                    Worker myWorker=new Worker(activity);
                                    Rezept newRezept=helper.replaceRezept(rezept, myWorker.getIDs(rezepte), blocker);
                                    if(newRezept!=null){
                                        rezepte.add(rezepte.indexOf(rezept), helper.replaceRezept(rezept, myWorker.getIDs(rezepte), blocker));
                                        rezepte.remove(rezept);
                                        Toast.makeText(getApplicationContext(), "Rezept ausgetauscht", Toast.LENGTH_SHORT).show();
                                        dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde
                                        blocker.add( String.valueOf(rezept.getId()));
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Keine weiteren Rezepte zum Austauschen mehr vorhanden, bei erneutem Austausch wird wieder von vorne begonnen", Toast.LENGTH_SHORT).show();
                                        blocker.clear(); //slle Items entfernen und wieder bei null beginnen
                                    }



                                }
                            });

                            alert.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //nichts zu tun

                                        }
                                    });

                            alert.show();


                        }
                        return true; //falls hier false übergeben wird, ist der Callback nicht verbraucht und löst auch noch den onClickListener aus.
                    }

                });

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }


            //TODO Wenn ein Rezept komplett aus der DB gelöscht wurde aber in den geplanten Rezepten existiert,
            //gibt es eine java.lang.NullPointerException: Attempt to invoke virtual method 'int android.view.View.getImportantForAccessibility()' on a null object reference
            //Das Programm stürzt ab und beim nächsten Laden paßt es wieder.
            Rezept rezept = rezepte.get(position);
            holder.name.setText(rezept.getTitel());
            /*
            * Colorierung klappt nicht so recht TODO
            if( i % 2 ==0){

                holder.name.setBackgroundColor(Color.LTGRAY);
            }*/
            holder.selected.setChecked(rezept.isSelected());
            holder.selected.setTag(rezept);
            holder.name.setTag(rezept);
            i++;

            return convertView;


        }

    }


}

