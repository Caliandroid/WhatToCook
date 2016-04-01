package de.caliandroid.kochplaner;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by stefan on 07.03.16.
 */
public class ShoppingListAnsicht extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ListView.OnItemClickListener, Spinner.OnItemSelectedListener {
    private Rezept rezept;
    public static ArrayList<Rezept> rezepte;
    private ArrayList<ShoppingListItem> items;
    private ShoppingListItem item;
    private Spinner spinner;
    private boolean alreadyCreated=false;

    public ShoppingListAnsicht(){
    }
    private ListView listView;
    private MyCustomAdapter dataAdapter;
    DBHelper helper = DBHelper.getInstance(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Shoppingliste");
        setContentView(R.layout.shopping_list_single);

        //Spinner
        spinner = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.shoppinglist_filter, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //ShoplistItems holen
        items=helper.getShoppinglist(rezepte);
        System.out.println("Länge ITems Arry: "+items.size());
        listView = (ListView)findViewById(R.id.listView3);
        dataAdapter = new MyCustomAdapter(this,R.layout.row_shoppinglist,items);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(this);
        dataAdapter.notifyDataSetChanged();

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

       // if (spinner.getSelectedItemPosition() == 0 && !alreadyCreated){
            //init. - do nothing
            alreadyCreated=true;
       // }

        //if (spinner.getSelectedItemPosition() == 0 && alreadyCreated){
        if (spinner.getSelectedItemPosition() == 0){
            //Alle zeigen
            items=helper.getShoppinglist(rezepte);
        }

        else{
            //nur fehlende
          //  ArrayList<ShoppingListItem> newItems= new ArrayList<>();
            Iterator i = items.iterator();
            while(i.hasNext()){
                ShoppingListItem item = (ShoppingListItem) i.next();
                if(item.getShopped()==1){
                    i.remove();
                }
            }

        }
        dataAdapter.clear();
        dataAdapter.addAll(items);


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
        items=helper.getShoppinglist(rezepte);
        if (spinner.getSelectedItemPosition() == 1){
            //nur fehlende
            Iterator i = items.iterator();
            while(i.hasNext()){
                ShoppingListItem item = (ShoppingListItem) i.next();
                if(item.getShopped()==1){
                    i.remove();
                }
            }
        }
        dataAdapter.clear();
        dataAdapter.addAll(items);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        // adapter.notifyDataSetChanged();
    }


    @Override
    protected void onStop() {
        super.onStop();

        //rezepte=helper.getRezepte(null,null,"TITEL ASC",null);
        // adapter.notifyDataSetChanged();
    }

    /**
     * Eigener Adapter zur Darstellung der Zutatenliste
     */
    private class MyCustomAdapter extends ArrayAdapter<ShoppingListItem> {


        int id1=-1,counter=1; //für die Farben in der Liste
        boolean colorChange=false,firstRunOver=false;

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
                //holder.name.setTextSize(18);
                holder.shopped=(CheckBox)convertView.findViewById(R.id.checkBox3);
                convertView.setTag(holder);


                holder.shopped.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final CheckBox cb = (CheckBox) v;
                        final ShoppingListItem item = (ShoppingListItem) cb.getTag();
                        if (cb.isChecked()) {
                            helper.setShopped(item.getId(),item.getRezept_id(), 1);
                            item.setShopped(1);
                        }
                        else{
                            helper.setShopped(item.getId(),item.getRezept_id(),0);
                            item.setShopped(0);
                        }
                    }
                });


            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            ShoppingListItem item= items.get(position);
            String[]whereArgs=new String[]{ String.valueOf(item.getRezept_id())};
            Rezept rezept = helper.getRezept("_id = ?",whereArgs,null,null);
            String rezeptTitel ="";
            try{
                rezeptTitel=rezept.getTitel().substring(0,12);
            }
            //if title is <13
            catch(StringIndexOutOfBoundsException e){
                rezeptTitel=rezept.getTitel();
            }

            holder.name.setText(item.getZutat()+"["+rezeptTitel+"]");
            /*
            * Colorierung
            *
            */

            if(!firstRunOver){
                id1=item.getRezept_id();
                firstRunOver=true;
            }

            if(id1 !=item.getRezept_id()){
                //Colorchange
                counter++;
                id1 =item.getRezept_id();
            }
            if(counter%2==0){
                holder.name.setBackgroundColor(Color.LTGRAY);
            }
            else{
                holder.name.setBackgroundColor(Color.GREEN);
            }



            holder.shopped.setTag(item);
            holder.shopped.setChecked(item.isShopped());
            holder.name.setTag(rezept);

            return convertView;


        }

    }

}
