package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by stefan on 20.02.16.
 */
public class AddEditRezept extends AppCompatActivity implements View.OnClickListener {

    private EditText etTitel,etZutaten,etAnleitung,etAnzahl;
    private int rezeptID;
    private Spinner spType;
    boolean bInsert=true; //steuert, ob es ein Update oder ein Neu Insert wird
    Button button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rezept_insert);
        etTitel=(EditText)findViewById(R.id.etTitel);
        etZutaten=(EditText)findViewById(R.id.etZutaten);
        etAnleitung=(EditText)findViewById(R.id.etAnleitung);
        etAnzahl=(EditText)findViewById(R.id.etAnzahl);
        button =(Button)findViewById(R.id.bEditRezept);
        button.setOnClickListener(this);
        spType = (Spinner)findViewById(R.id.spType);
        //Array ist in den String Values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.typen_array, android.R.layout.simple_spinner_item);
        spType.setAdapter(adapter);



        if(getIntent().hasExtra("id")){
            bInsert=false;
            //Rezept wurde übermittelt, daher EditAnsicht
            etTitel.setText(getIntent().getStringExtra("titel"));
            etZutaten.setText(getIntent().getStringExtra("zutaten"));
            etAnleitung.setText(getIntent().getStringExtra("anleitung"));
            etAnzahl.setText(String.valueOf(getIntent().getIntExtra("anzahl",0)));
            rezeptID = getIntent().getIntExtra("id",0);
            //Spinner setzen
            spType.setSelection(getIntent().getIntExtra("typ", 0));



        }
        else{
            //keine Übergabe eines Rezepts, daher InsertAnsicht
            etAnzahl.setText("0");
        }

        /*tvTitel=(TextView)findViewById(R.id.tvRezeptTitel);
        tvZutaten=(TextView)findViewById(R.id.tvZutaten);
        tvAnleitung=(TextView)findViewById(R.id.tvAnleitung);
        tvAnzahl=(TextView)findViewById(R.id.tvAnzahl);

        //Daten holen
        tvTitel.setText(getIntent().getStringExtra("titel"));
        tvZutaten.setText("ZUTATEN:\n"+getIntent().getStringExtra("zutaten")+"\n\n");
        tvAnleitung.setText("ANLEITUNG:\n"+getIntent().getStringExtra("anleitung"));
        tvAnzahl.setText("Bisher gekocht: "+String.valueOf(getIntent().getIntExtra("anzahl",0))+" mal");*/




    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        DBHelper helper = new DBHelper(this);
        if(v.getId()==R.id.bEditRezept){
            if(bInsert){//NEUES REZEPT

                helper.insertRezept(etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()));
                //TODO einen Toast anzeigen, dann Ansicht schließen
                Toast.makeText(getApplicationContext(), "Erfolgreich gespeichert", Toast.LENGTH_LONG).show();
                setResult(0);
                finish();

            }
            else{
                // UPDATE eines Rezepts
                helper.updateRezept(getIntent().getIntExtra("id", -1), etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()));
                //TODO einen Toast anzeigen, dann Ansicht schließen
                Toast.makeText(getApplicationContext(), "Erfolgreich aktualisiert", Toast.LENGTH_LONG).show();
                setResult(0);
                Intent i = new Intent(this,MainActivity.class);
                finish();
                startActivity(i);





            }

        }
    }
}
