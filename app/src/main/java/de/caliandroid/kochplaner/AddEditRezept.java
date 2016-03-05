package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by stefan on 20.02.16.
 */
public class AddEditRezept extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    private static final String IMAGELOCATION="/storage/sdcard1/kochplaner/images";
    private static final int CAMERADATA = 0;


    private EditText etTitel,etZutaten,etAnleitung,etAnzahl;
    private TextView tvImageUri;
    private int rezeptID;
    private Spinner spType;
    boolean bInsert=true; //steuert, ob es ein Update oder ein Neu Insert wird
    Button button, bKamera;
    private static final int CAMERA_REQUEST = 0;
    private Uri selectedImageUri = null;
    private String sUri=null;




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
        tvImageUri=(TextView)findViewById(R.id.tvImageUri);
        /*if(MainActivity.imageUri!=null){
            tvImageUri.setText(MainActivity.imageUri);
        }*/
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
            etAnzahl.setText(String.valueOf(getIntent().getIntExtra("anzahl", 0)));
            rezeptID = getIntent().getIntExtra("id", 0);
            sUri= getIntent().getStringExtra("imageUri");
            if(sUri!=null){
                bKamera.setText("Foto aktualisieren");
                tvImageUri.setText(sUri);
            }
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
    public void onResume() {
        System.out.println("onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        System.out.println("onPause");
        super.onPause();
    }


    @Override
    public void onStop() {
        System.out.println("onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvImageUri.setText(savedInstanceState.getString("imageUri", null));

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("imageUri", tvImageUri.getText().toString());
        super.onSaveInstanceState(outState);

    }



    @Override
    public void onClick(View v) {


        //TODO INHALT PRÜFEN!
        if( !(etTitel.getText().toString().isEmpty() || etZutaten.getText().toString().isEmpty() || etAnleitung.getText().toString().isEmpty())) {
            //sollte Anzahl nicht gesetzt sein wird 0 gesetzt
            if(etAnzahl.getText().toString().isEmpty()){
                etAnzahl.setText("0");
            }

            DBHelper helper = new DBHelper(this);

            if (v.getId() == R.id.bEditRezept) {
                if (bInsert) {//NEUES REZEPT

                    /*if(selectedImageUri!=null){
                        sUri=selectedImageUri.toString();
                    }*/
                    Rezept r = new Rezept(-1, etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()),tvImageUri.getText().toString(), false);
                    if (!helper.doesAlreadyExist(r)) {
                        helper.insertRezept(r);
                        //TODO einen Toast anzeigen, dann Ansicht schließen
                        Toast.makeText(getApplicationContext(), "Erfolgreich gespeichert", Toast.LENGTH_LONG).show();
                       // MainActivity.imageUri=null;//wieder leeren
                        setResult(-1);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Es gibt schon ein Rezept mit diesem Titel (Typ " + spType.getSelectedItem() + ")", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // UPDATE eines Rezepts
                    /**TODO ImageURi hinzufügen
                     * Wenn schon eine Uri existiert, diese wieder übernehmen. Ansonsten Null bzw. die neugesetze nutzen
                     */


                    System.out.println("in ImageUri steht "+tvImageUri.getText());

                    Rezept r = new Rezept(getIntent().getIntExtra("id", -1), etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()),tvImageUri.getText().toString(), false);
                    if (!helper.doesAlreadyExist(r)) { //Duplettengenerierung bei Update vermeiden
                        helper.updateRezept(r);
                        Toast.makeText(getApplicationContext(), "Erfolgreich aktualisiert", Toast.LENGTH_LONG).show();

                        //sofern altes Foto vorhanden und ein neues gesetzt wurde  muss dieses gelöscht werden:
                        if(!tvImageUri.getText().toString().equals(getIntent().getStringExtra("imageUri"))){
                            try { // try und catch sollte nicht notwendig sein, aber wenn das Löschen einer alten Bilddatei nicht klappt, soll deswegen nicht die App abstürzen
                                Worker worker = new Worker(this);
                                worker.deleteFileFromSDCard(Uri.parse(getIntent().getStringExtra("imageUri")));
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
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
        //Kameraintegration
        if(v.getId()==R.id.bKamera){
            getCameraPic();




        }
    }


    protected void  onActivityResult(int requestCode, int resultCode, Intent data)  {
        //ImageHelper help= new ImageHelper(this);
        if (requestCode == CAMERA_REQUEST) {
            //schreibe die URI in das Textfeld
            try{
                tvImageUri.setText(selectedImageUri.toString());
            }
            catch(Exception e){
                e.printStackTrace();

            }




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

    public void getCameraPic(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());

        //Ordner auf sdcard erstellen, falls nicht existent:
        File imageDirectory = new File(IMAGELOCATION);
        imageDirectory.mkdirs();

        //falls Titel bereits existiert wird er mit in den filenamen aufgenommen
        File photo = new File(IMAGELOCATION, File.separator+etTitel.getText().toString()+"_"+sdf.format(new Date())+".jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        selectedImageUri = Uri.fromFile(photo);
        startActivityForResult(intent, CAMERADATA);
    }
}
