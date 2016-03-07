package de.caliandroid.kochplaner;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by stefan on 07.03.16.
 */
public class ShoppingListAnsicht extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ListView.OnItemClickListener {
    private Rezept rezept;
    private ArrayList<Rezept> rezepte;
    private ArrayList<ShoppingListItem> items;
    private ShoppingListItem item;
    public ShoppingListAnsicht(ArrayList<Rezept> rezepte){
        this.rezepte=rezepte;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list_single);
        //ShoplistItems holen
        DBHelper helper = new DBHelper(this);
        items=helper.getZutatenListe(rezepte);
        Worker worker = new Worker(this);
        listView = (ListView)findViewById(R.id.listView2);
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, worker.getRezeptTitel(rezepte));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    /**
     * Angepaßter Adapter, der die Rezepte samt einer Auswahlcheckbox anzeigen soll
     */
    private class MyCustomAdapter extends ArrayAdapter<ShoppingListItem> {


        int i=0; //für die Farben in der Liste

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<ShoppingListItem> items) {
            super(context, textViewResourceId, items);

        }

        private class ViewHolder{
            TextView name;
            CheckBox shopped;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            //Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row_shoppinglist, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.textView2);
                holder.name.setTextSize(18);
                holder.shopped=(CheckBox)convertView.findViewById(R.id.checkBox3);

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
                                    helper.deletePlanned(rezept.getId());
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
                holder.prepared.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final CheckBox cb = (CheckBox) v;
                        final Rezept rezept = (Rezept) cb.getTag();
                        rezept.setSelected(cb.isChecked());
                        if (rezept.isSelected()) {

                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Alle Zutaten für " + rezept.getTitel() + " besorgt?");
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann prepared auf 1 in planned setzen
                                    helper.setPrepared(rezept.getId(),1);


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
                        else{//deaktivieren
                            //AlertDialog - um Tippfehler auszuschließen
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.activity);
                            alert.setTitle("Fehlen noch Zutaten für" + rezept.getTitel() + " ?");
                            alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //falls selektiert, dann prepared auf 1 in planned setzen
                                    helper.setPrepared(rezept.getId(),0);


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
                                        rezepte.add(rezepte.indexOf(rezept), newRezept);
                                        rezepte.remove(rezept);
                                        helper.updatePlannedRezeptID(rezept.getId(), newRezept.getId());
                                        helper.deleteItemFromShoppinglist(rezept);
                                        helper.insertIntoShoppinglist(rezept);
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
            holder.prepared.setTag(rezept);
            holder.prepared.setChecked(helper.isPrepared(rezept));
            /**
             * Variante 1, um die Checkbox Prepared korrekt zu markieren
             * In der Planned DB anhand der RezeptID prüfen, wie der Zustand von prepared ist und dann hier markieren
             */


            // holder.selected.setChecked(rezept.isSelected());
            holder.selected.setTag(rezept);
            holder.name.setTag(rezept);
            i++;

            return convertView;


        }

    }

}
