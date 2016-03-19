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
import android.provider.MediaStore;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.SharedPreferences.*;
import static android.view.View.INVISIBLE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, View.OnTouchListener {
    public static final String MY_PREFS = "MyPrefs";
    DBHelper helper;
    public static ArrayList<Rezept> rezepte = new ArrayList();
    Rezept rezept;
    Button button, bClear;
    ListView myListView;
    MyCustomAdapter dataAdapter;
    SharedPreferences.Editor editor ;
    public static Activity activity; //damit in Subclassen die Referenz zu dieser Klasse vorhanden ist
    ArrayList blocker=new ArrayList<String>();
    public static String imageUri; //zur Anwendung in AddEditRezept;
    int iPosition=-1;
    SharedPreferences sharedpreferences ;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("OnCreate läuft");
        setContentView(R.layout.activity_main);
        activity=this;

        //Check if DB already exists (first run)
        helper = new DBHelper(this);
        try {
            helper.createDB();
        } catch (IOException e) {
            throw new Error("Cannot initialize prepopulated db");
        }
        //planned Rezepte laden
        rezepte =helper.getPlannedReceipts(null,null,null,null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        myListView = (ListView) findViewById(R.id.listView);
        //myListView.setOnItemClickListener(this); Nicht mehr benötigt, da in CustomAdapter integriert
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        bClear = (Button) findViewById(R.id.bClear);
        bClear.setOnClickListener(this);
        dataAdapter = new MyCustomAdapter(this,R.layout.row, rezepte);
        myListView.setAdapter(dataAdapter);

        dataAdapter.notifyDataSetChanged();
        myListView.setOnTouchListener(this);




        //Hier eine Textmail mit den Rezepten samt Details erstellen
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Infomail versenden", Snackbar.LENGTH_SHORT)
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

        if (item.getItemId() == R.id.action_editRezept) {
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
                        // alle Inhalte in PLANNED und Shoppingliste entfernen
                        helper.deleteAllPlanned();
                        helper.deleteAllFromShoppinglist();
                        rezepte.clear();
                        //neue Wochenplanung durchführen
                        /**
                         * Alte Variante statisch rezepte = helper.getKochplan();
                         * TODO
                         * in der neuen Variante wird aus den SharedPrefs ausgelesen, wie die Planung aussehen soll und dann jeweils die Kochliste aufgerufen
                         *
                         **/
                        sharedpreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);

                        //vegetarisch=0
                        rezepte= helper.getKochplanNeu(0, Integer.valueOf(sharedpreferences.getString("vegetarisch", "2")), rezepte);
                        //Fleisch=1
                        rezepte= helper.getKochplanNeu(1,Integer.valueOf(sharedpreferences.getString("fleisch","1")),rezepte);
                        //Fisch=2
                        rezepte= helper.getKochplanNeu(2,Integer.valueOf(sharedpreferences.getString("fisch","1")),rezepte);
                        //Süß=3
                        rezepte= helper.getKochplanNeu(3,Integer.valueOf(sharedpreferences.getString("suess","1")),rezepte);
                        //Dessert=4
                        rezepte= helper.getKochplanNeu(4,Integer.valueOf(sharedpreferences.getString("nachtisch","1")),rezepte);
                        //Snack=5
                        rezepte =helper.getKochplanNeu(5,Integer.valueOf(sharedpreferences.getString("snack","1")),rezepte);

                        //wenn fertig, dann noch in die Planned Tabelle einfügen
                        helper.insertPlanned(rezepte);
                        dataAdapter.notifyDataSetChanged();

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
        if(v.getId()==R.id.bClear){
            //AlertDialog
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Komplette Planung leeren?");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    helper = new DBHelper(MainActivity.activity);
                    try {
                        // alle Inhalte in PLANNED und Shoppingliste entfernen
                        helper.deleteAllPlanned();
                        helper.deleteAllFromShoppinglist();
                        rezepte.clear();
                        dataAdapter.notifyDataSetChanged();

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
            i.putExtra("imageUri", rezept.getImageUri());
            i.putExtra("blocked", rezept.getBlocked());
            i.putExtra("position", iPosition);  //um bei Löschoperation das Rezept aus dem Array zu entfernen
            startActivityForResult(i, 1);

        }
    }






    //TODO Sollte es nicht protected heißen? was bringt es?
    @Override
    public void onStart() {
        super.onStart();
        //An dieser Stelle aktualisieren, weil die Methode immer aufgerufen wird, wenn die Activity wieder gerufen wird.
        System.out.println("OnStart läuft");
        dataAdapter.notifyDataSetChanged();

    }

    protected void onResume(){
        super.onResume();
        System.out.println("OnResume läuft");
        //planned Rezepte laden
       // rezepte = helper.getPlannedReceipts(null,null,null,null);
        dataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        //saveSharedPrefs();
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
        if (item.getItemId() == R.id.action_shoppinglist) {
            //starte  Settings
            Intent i = new Intent(this,ShoppingListAnsicht.class);
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

    /**
     * Wird nicht mehr verwendet, alles in DB gespeichert
     */
    public void saveSharedPrefs(){
        //an dieser Stelle die Änderungen speichern
        editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
        Worker myWorker = new Worker(this);
        editor.putString("plannedIDs", myWorker.getIDs(rezepte));
        editor.commit();

    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Aktuell nur ausgelöst, sollte ein Rezept gelöscht worden sein, man erhält als ResultCode die ID
        if(resultCode==2){ //Rezept wurde gelöscht
            Worker worker = new Worker(this);
            rezepte = worker.bereinigeListe(rezepte,data.getIntExtra("id",-1));
           //rezepte.remove(data.getIntExtra("loeschposition", -1));
            dataAdapter.notifyDataSetChanged();
            //jetzt erst das Rezept endgültig aus der DB entfernen
            helper.deleteRezept(data.getIntExtra("id", -1));
        }
        else{
            System.out.println("Habe ResultCode =" + resultCode + " erhalten");
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
            CheckBox prepared;
            CheckBox selected;

        }



         @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            //Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.textView1);
                holder.name.setTextSize(18);
                holder.prepared=(CheckBox)convertView.findViewById(R.id.checkBox2);
                holder.selected = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.selected.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final CheckBox cb = (CheckBox) v;
                        final Rezept rezept = (Rezept) cb.getTag();

                       // if (rezept.getBlocked()==1) { hier gibts nichts zu prüfen. Wenn gekocht, dann fliegt das Item aus der Liste

                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Wurde " + rezept.getTitel() + " gekocht?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann entfernen und Häufigkeit hochsetzen
                                    helper.updateHaeufigkeit(rezept.getId());
                                    helper.deletePlanned(rezept.getId());
                                    rezepte.remove(position);
                                    cb.setChecked(false); //wichtig, da beim entfernen des Rezeptes aus der Liste ein anderes an die Stelle nachrückt (ausser beim letzten) und dann die Checkbox weiter aktiv bleiben würde
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


                        //}

                    }

                });
                holder.prepared.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final CheckBox cb = (CheckBox) v;
                        final Rezept rezept = (Rezept) cb.getTag();

                        //rezept.setSelected(cb.isChecked());
                        if (cb.isChecked()) { //prepared ist bereits gecheckt

                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Alle Zutaten für " + rezept.getTitel() + " besorgt?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann prepared auf 1 in planned setzen
                                    helper.setPrepared(rezept.getId(),1);
                                    //alle Zutaten in der Shoppingliste ebenfalls markieren
                                    helper.setRezeptShopped(rezept.getId(),1);


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
                        //TODO Lieber hier nur einen Hinweis auf die Shoppingliste und dann diese öffnen, sonst werden ja alle Zutaten auf shopped=false gesetzt
                        else{//deaktivieren
                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Fehlen noch Zutaten für" + rezept.getTitel() + " ?");
                            alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann prepared auf 1 in planned setzen
                                    helper.setPrepared(rezept.getId(),0);
                                    helper.setRezeptShopped(rezept.getId(),0);


                                }
                            });

                            alert.setNegativeButton("Nein",
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
                        iPosition=position;
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
                            //final CharSequence[] choose = {"Ansehen", "Austauschen", "Entfernen","Cancel" };
                            final String [] choose=getResources().getStringArray(R.array.operations_1);

                           // final CharSequence[] choose = R.array.operations_1;
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Was tun mit "+rezept.getTitel() + " ?");
                            alert.setItems(choose, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (choose[item].equals(choose[2])) { //Replace
                                        Worker myWorker = new Worker(activity);
                                        Rezept newRezept = helper.replaceRezept(rezept, myWorker.getIDs(rezepte), blocker);
                                        if (newRezept != null) {
                                            rezepte.add(rezepte.indexOf(rezept), newRezept);
                                            rezepte.remove(rezept);
                                            helper.updatePlannedRezeptID(rezept.getId(), newRezept.getId());
                                            helper.deleteItemFromShoppinglist(rezept.getId());
                                            helper.insertIntoShoppinglist(newRezept);
                                            Toast.makeText(getApplicationContext(), "Rezept ausgetauscht", Toast.LENGTH_SHORT).show();
                                            dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde
                                            blocker.add(String.valueOf(rezept.getId()));
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Keine weiteren Rezepte zum Austauschen mehr vorhanden, bei erneutem Austausch wird wieder von vorne begonnen", Toast.LENGTH_SHORT).show();
                                            blocker.clear(); //slle Items entfernen und wieder bei null beginnen
                                        }


                                    } else if (choose[item].equals(choose[3])) { //remove

                                        helper.deletePlanned(rezept.getId());
                                        helper.deleteItemFromShoppinglist(rezept.getId());
                                        rezepte.remove(rezept);
                                        dataAdapter.notifyDataSetChanged();

                                    } else if (choose[item].equals(choose[0])) {
                                        iPosition=position;
                                        startEditAnsicht(rezept, RezeptAnsicht.class);

                                    } else if (choose[item].equals(choose[1])) { //als gekocht markieren und entfernen

                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                                        alert.setTitle("Wurde " + rezept.getTitel() + " gekocht?");
                                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                //falls selektiert, dann entfernen und Häufigkeit hochsetzen
                                                helper.updateHaeufigkeit(rezept.getId());
                                                helper.deletePlanned(rezept.getId());
                                                rezepte.remove(position);
                                                Toast.makeText(getApplicationContext(), rezept.getTitel() + " gekocht :)", Toast.LENGTH_LONG).show();
                                                dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde

                                            }
                                        });

                                        alert.setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {

                                                    }
                                                });

                                        alert.show();

                                    } else if (choose[item].equals(choose[4])) { //cancel
                                        dialog.dismiss();
                                    }


                                }
                            });
                           /* alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //neues Rezept gleichen Typs laden (unter der Berücksichtung der bereits geplannten Rezepte in der Liste
                                    Worker myWorker = new Worker(activity);
                                    Rezept newRezept = helper.replaceRezept(rezept, myWorker.getIDs(rezepte), blocker);
                                    if (newRezept != null) {
                                        rezepte.add(rezepte.indexOf(rezept), newRezept);
                                        rezepte.remove(rezept);
                                        helper.updatePlannedRezeptID(rezept.getId(), newRezept.getId());
                                        helper.deleteItemFromShoppinglist(rezept.getId());
                                        helper.insertIntoShoppinglist(newRezept);
                                        Toast.makeText(getApplicationContext(), "Rezept ausgetauscht", Toast.LENGTH_SHORT).show();
                                        dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde
                                        blocker.add(String.valueOf(rezept.getId()));
                                    } else {
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
                                    });*/

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

            holder.prepared.setTag(rezept);
            holder.prepared.setChecked(helper.isPrepared(rezept));
            /**
             * Variante 1, um die Checkbox Prepared korrekt zu markieren
             * In der Planned DB anhand der RezeptID prüfen, wie der Zustand von prepared ist und dann hier markieren
             */


           // holder.selected.setChecked(rezept.isSelected());
            // holder.selected.setChecked(false); //darf nie vorab aktiviert sein
            holder.selected.setTag(rezept);

            holder.name.setTag(rezept);
            i++;

            return convertView;


        }

    }


}

