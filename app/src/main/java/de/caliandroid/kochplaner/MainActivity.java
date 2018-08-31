package de.caliandroid.kochplaner;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, View.OnTouchListener, RetainedFragment.TaskCallbacks {
    public static final String MY_PREFS = "MyPrefs";
    DBHelper helper;
    public ArrayList<Rezept> rezepte = new ArrayList(); //static wieder entfernt
    Rezept rezept;
    Button button, bClear;
    ListView myListView;
    private TextView progressText;
    MyCustomAdapter dataAdapter;
    SharedPreferences.Editor editor ;
    public static Activity activity; //damit in Subclassen die Referenz zu dieser Klasse vorhanden ist
    ArrayList blocker=new ArrayList<String>();
    public static String imageUri; //zur Anwendung in AddEditRezept;
    int iPosition=-1;
    public static SharedPreferences sharedpreferences ;
    RetainedFragment retainedFragment;
    public static final String  TAG_RETAINED_FRAGMENT = "taskfragment";
    FragmentManager fragmentManager;

        @Override
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            //System.out.println("OnCreate läuft");
            setContentView(R.layout.activity_main);
            activity = this;
            progressText=(TextView)findViewById(R.id.textView17);
            fragmentManager = getFragmentManager();


            retainedFragment = (RetainedFragment) fragmentManager.findFragmentByTag(TAG_RETAINED_FRAGMENT); //Das Fragment mit dem AsyncTask

            if (retainedFragment == null) {
            retainedFragment = new RetainedFragment();
            //LÖST DEN THREAD AUS
            fragmentManager.beginTransaction().add(retainedFragment, TAG_RETAINED_FRAGMENT).commit();

        }






        //Check if DB already exists (first run)
        helper = DBHelper.getInstance(this);
        try {
            helper.createDB();
        } catch (IOException e) {
            throw new Error("Cannot initialize prepopulated db");
        }
        helper.openDB();
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



        //Send textmail
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                final View view1 = view;

                if(rezepte.size()>0){

                   //Alertdialog
                    final String [] choose=getResources().getStringArray(R.array.mail_operation);

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                    alert.setTitle("Mailversand");
                    alert.setItems(choose, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (choose[item].equals(choose[0])) { //Komplette Mail
                                Snackbar.make(view1, "Infomail versenden", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
                                Worker worker = new Worker(getApplicationContext());
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/html");
                                //intent.putExtra(Intent.EXTRA_EMAIL, "sarah@caliandro.de,stefan@caliandro.de");
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Kochplan " + sdf.format(new Date()));
                                intent.putExtra(Intent.EXTRA_TEXT, worker.getMailText(rezepte));
                                startActivity(Intent.createChooser(intent, "Send Email"));
                                }


                            else if (choose[item].equals(choose[1])) { //fehlende Zutaten
                                ArrayList<ShoppingListItem> items = helper.getMissingItems();
                                if(items.size()==0){ //no missing items
                                    Snackbar.make(view1, "Alle Zutaten bereits vorhanden", Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();

                                }
                                else {
                                    //create missing items mail
                                    Snackbar.make(view1, "Fehlende Zutaten versenden", Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();


                                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
                                    Worker worker = new Worker(getApplicationContext());

                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/html");

                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Fehlende Zutaten Stand: " + sdf.format(new Date()));
                                    intent.putExtra(Intent.EXTRA_TEXT,worker.getMailTextMissingItems(items));

                                    startActivity(Intent.createChooser(intent, "Send Email"));
                                }
                            }
                            else if (choose[item].equals(choose[2])) { //cancel
                                dialog.dismiss();
                            }


                        }
                    });
                    alert.show();

                    // Ende Alertdialog
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Zuerst eine Planung erstellen", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Zuerst muss eine Planung erstellt werden", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
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
    public  void onClick(View v) {
        if (v.getId() == R.id.button) {
            //AlertDialog
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Neue Woche planen?");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        dataAdapter.clear();
                        sharedpreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
                        //TODO in einen Async Task auslagern

                        //alles wurde in RetainedFragment ausgelagert

                       /* helper.deleteAllPlanned();
                      //  helper.deleteAllFromShoppinglist();
                       // rezepte.clear();
                        //neue Wochenplanung durchführen

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
                        //dieser Teil dauert lange, vielleicht lieber in Thread auslagern
                        dataAdapter.clear();
                        dataAdapter.addAll(rezepte);
                        dataAdapter.notifyDataSetChanged();


                       /* ///GetPlanningWeek gpw = new GetPlanningWeek();
                        retainedFragment.setData(gpw);
                        if(!retainedFragment.isRunning() || retainedFragment==null){
                            gpw=new GetPlanningWeek();
                            retainedFragment.setRunning(true);
                            retainedFragment.setData(gpw);
                            gpw.execute();
                        }*/

                       //fragmentManager.beginTransaction().add(retainedFragment, TAG_RETAINED_FRAGMENT).commit();

                       retainedFragment.startTask();






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
                    try {
                        // alle Inhalte in PLANNED und Shoppingliste entfernen
                        helper.deleteAllPlanned();
                        helper.deleteAllFromShoppinglist();
                        dataAdapter.clear();
                        rezepte.clear();
                        // dataAdapter.notifyDataSetChanged();

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
            i.putExtra("saison", rezept.getSaison());
            i.putExtra("position", iPosition);  //um bei Löschoperation das Rezept aus dem Array zu entfernen
            startActivityForResult(i, 1);

        }
    }








    //TODO Sollte es nicht protected heißen? was bringt es?
    @Override
    public void onStart() {
        super.onStart();
        //An dieser Stelle Rezepte aktualisieren
        this.rezepte =helper.getPlannedReceipts(null,null,null, null);
        dataAdapter.clear();
        dataAdapter.addAll(rezepte);


    }

    protected void onResume(){
        super.onResume();
        //System.out.println("OnResume läuft");
        //planned Rezepte laden
       // rezepte = helper.getPlannedReceipts(null,null,null,null);
        dataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        //System.out.println("OnStopCalled");
        //saveSharedPrefs();
        //DB Verbindung schließen
       // helper.close();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //28.03.2016 Test to close the DB here
        System.out.println("Close DB in OnDestroy MainAct!");
        try {
            DBHelper.getInstance(this).close();
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }

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
            ShoppingListAnsicht.rezepte=rezepte; //übergebe die Rezepte anstatt sie wieder aus der DB zu lesen
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
            //System.out.println("Habe ResultCode =" + resultCode + " erhalten");
        }
    }


    //Mehtoden des Interfaces TaskCallbacks

    @Override
    public ArrayList<Rezept> onPreExecute() {
        return this.rezepte;


    }

    @Override
    public void onProgressUpdate(int percent) {
        progressText.setText("Loading "+percent);

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(ArrayList<Rezept> rezepte) {
        progressText.setText("");
        this.rezepte=rezepte;
        dataAdapter.clear();
        dataAdapter.addAll(this.rezepte);


       // dataAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "fertig!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public SharedPreferences deliverPrefs() {
        return sharedpreferences;
    }




    /**
     * Angepaßter Adapter, der die Rezepte samt einer Auswahlcheckbox anzeigen soll
     */
    private class MyCustomAdapter extends ArrayAdapter<Rezept> {
        int i=0; //für die Farben in der Liste

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Rezept> rezepte) {
            super(context, textViewResourceId, rezepte);

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

                           //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Wurde " + rezept.getTitel() + " gekocht?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann entfernen und Häufigkeit hochsetzen
                                    helper.updateHaeufigkeit(rezept.getId());
                                    rezepte.remove(position);
                                    helper.deletePlanned(rezept.getId());

                                    cb.setChecked(false); //wichtig, da beim entfernen des Rezeptes aus der Liste ein anderes an die Stelle nachrückt (ausser beim letzten) und dann die Checkbox weiter aktiv bleiben würde
                                    Toast.makeText(getApplicationContext(), rezept.getTitel() + " gekocht :)", Toast.LENGTH_LONG).show();
                                    dataAdapter.clear();
                                    dataAdapter.addAll(rezepte);
                                    //dataAdapter.notifyDataSetChanged(); //durch die Veränderung von rezepte durch remove() kommt es so nicht im dataAdapter an und führt zu index fehlern

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
                                            cb.setChecked(true);
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
                        final  TextView tv = (TextView)v;
                        final Rezept rezept = (Rezept) tv.getTag();
                        if (rezept!=null) {


                            final String [] choose=getResources().getStringArray(R.array.operations_1);

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
                                            helper.deleteItemFromShoppinglist(rezept.getId());
                                            helper.updatePlannedRezeptID(rezept.getId(), newRezept.getId());
                                            helper.insertIntoShoppinglist(newRezept);
                                            rezepte.remove(rezept);
                                            Toast.makeText(getApplicationContext(), "Rezept ausgetauscht", Toast.LENGTH_SHORT).show();
                                            Snackbar.make(tv, "Rezept ausgetauscht", Snackbar.LENGTH_SHORT)
                                                    .setAction("Action", null).show();
                                            dataAdapter.clear();
                                            dataAdapter.addAll(rezepte);//dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde
                                            blocker.add(String.valueOf(rezept.getId()));
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Keine weiteren Rezepte zum Austauschen mehr vorhanden, bei erneutem Austausch wird wieder von vorne begonnen", Toast.LENGTH_SHORT).show();
                                            blocker.clear(); //slle Items entfernen und wieder bei null beginnen
                                        }


                                    } else if (choose[item].equals(choose[3])) { //remove

                                        helper.deletePlanned(rezept.getId());
                                        helper.deleteItemFromShoppinglist(rezept.getId());
                                        rezepte.remove(rezept);
                                        dataAdapter.clear();
                                        dataAdapter.addAll(rezepte);
                                        //dataAdapter.notifyDataSetChanged();

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
                                                //15.05.2016 das sollte bisher gefehlt haben - ToDO:Testen
                                                helper.deleteItemFromShoppinglist(rezept.getId());
                                                rezepte.remove(position);
                                                dataAdapter.clear();
                                                dataAdapter.addAll(rezepte);
                                                Toast.makeText(getApplicationContext(), rezept.getTitel() + " gekocht :)", Toast.LENGTH_LONG).show();
                                                //dataAdapter.notifyDataSetChanged(); //da AL rezepte verkürzt wurde

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
             Rezept rezept = rezepte.get(position);
             holder.name.setText(rezept.getTitel());
             holder.prepared.setTag(rezept);
             holder.prepared.setChecked(helper.isPrepared(rezept));
             holder.selected.setTag(rezept);
             holder.name.setTag(rezept);
             i++;

             return convertView;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);

    }





}

