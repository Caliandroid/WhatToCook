package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
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
public class AddEditRezept extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    private EditText etTitel,etZutaten,etAnleitung,etAnzahl;
    private int rezeptID;
    private Spinner spType;
    boolean bInsert=true; //steuert, ob es ein Update oder ein Neu Insert wird
    Button button, bKamera;



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
        bKamera =(Button)findViewById(R.id.bKamera);
        bKamera.setOnClickListener(this);
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
        int CameraData = 0;

        //TODO INHALT PRÜFEN!
        if( !(etTitel.getText().toString().isEmpty() || etZutaten.getText().toString().isEmpty() || etAnleitung.getText().toString().isEmpty())) {
            //sollte Anzahl nicht gesetzt sein wird 0 gesetzt
            if(etAnzahl.getText().toString().isEmpty()){
                etAnzahl.setText("0");
            }

            DBHelper helper = new DBHelper(this);

            if (v.getId() == R.id.bEditRezept) {
                if (bInsert) {//NEUES REZEPT
                    Rezept r = new Rezept(-1, etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()), false);
                    if (!helper.doesAlreadyExist(r)) {
                        helper.insertRezept(r);
                        //TODO einen Toast anzeigen, dann Ansicht schließen
                        Toast.makeText(getApplicationContext(), "Erfolgreich gespeichert", Toast.LENGTH_LONG).show();
                        setResult(-1);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Es gibt schon ein Rezept mit diesem Titel (Typ " + spType.getSelectedItem() + ")", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // UPDATE eines Rezepts
                    Rezept r = new Rezept(getIntent().getIntExtra("id", -1), etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()), false);
                    if (!helper.doesAlreadyExist(r)) { //Duplettengenerierung bei Update vermeiden
                        helper.updateRezept(r);
                        Toast.makeText(getApplicationContext(), "Erfolgreich aktualisiert", Toast.LENGTH_LONG).show();
                        setResult(-1);
                        Intent i = new Intent(this, MainActivity.class);
                        finish();
                        startActivity(i);

                    } else {
                        Toast.makeText(getApplicationContext(), "Es gibt schon ein anderes Gericht mit diesem Titel (Typ " + spType.getSelectedItem() + ")", Toast.LENGTH_LONG).show();
                    }

                }

            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Titel, Zutaten und Anleitungen müssen eingegeben werden!", Toast.LENGTH_LONG).show();
        }

    }
    //Dialog
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Ohne Speichern zurück?");
        // alert.setMessage("Message");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();

    }

    //falls der Back Hard- oder Software Button gedrückt wurde
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();

        }
        return true;
    }
}
