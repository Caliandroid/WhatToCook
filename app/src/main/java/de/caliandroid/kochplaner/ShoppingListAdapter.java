package de.caliandroid.kochplaner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * UNUSED!
 * Created by stefan on 16.03.16.
 */
public class ShoppingListAdapter extends ArrayAdapter<ShoppingListItem> {
    private Context context;
    private ShoppingListItem item;
    private ArrayList<ShoppingListItem> items;
    private DBHelper dbHelper=new DBHelper(getContext());


    public ShoppingListAdapter(Context context, int textViewResourceId, ArrayList<ShoppingListItem> items) {
        super(context, textViewResourceId,  items);
        this.context=context;
        this.items=items;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View element = convertView; //sofern eine bereits existierende View übergeben wurde, wird diese recycled
        TextView zutat;
        CheckBox shopped;

        if(element==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            element = inflater.inflate(R.layout.shoppinglist_element,null);
        }
        else
        {
            System.out.println("View was recycled");
        }
        zutat = (TextView) element.findViewById(R.id.textView1);
        shopped = (CheckBox) element.findViewById(R.id.checkBox);
        item = (ShoppingListItem) items.get(position);
        zutat.setText(item.getZutat());
        shopped.setChecked(item.isShopped());
        shopped.setTag(item.getZutat()); //vermutlich gar nicht nötig

        return element;



    }


}
