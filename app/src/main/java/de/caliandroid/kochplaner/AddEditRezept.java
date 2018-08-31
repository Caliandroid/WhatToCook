package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    private static final String IMAGELOCATIONPREFIX="file://";
    private static final String IMAGE_FOLDER ="/images";  //geladen werden soll dann noch /storage/sdcard1/kochplaner

    //private static final String IMAGELOCATION="/storage/sdcard1/kochplaner/images";
    private static final int CAMERADATA = 0;
    public static final String MY_PREFS = "MyPrefs";

    private static final int SELECT_FILE = 100;
    private static final int REQUEST_CAMERA = 110;

    private EditText etTitel,etZutaten,etAnleitung,etAnzahl,etSeason;
    private CheckBox blocked;
    private TextView tvImageUri;
    private int rezeptID,iBlocked;
    private Spinner spType;
    boolean bInsert=true; //steuert, ob es ein Update oder ein Neu Insert wird
    Button button, bKamera;
    private static final int CAMERA_REQUEST = 0;
    private Uri selectedImageUri = null;
    private String filename=null;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor ;
    String restoredPath;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());

    //Wileyfox
    File sdcard;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Rezept erfassen/ändern");

        //pfad laden
        sharedpreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        restoredPath = sharedpreferences.getString("storagePath", null);

        //Wileyfox CynOS12.1 Workaround
        //restoredPath="/kochplaner";
        sdcard =Environment.getExternalStorageDirectory();
        System.out.println("External Storage Directory = "+sdcard.getAbsolutePath());

        setContentView(R.layout.rezept_insert);
        etTitel=(EditText)findViewById(R.id.etTitel);
        etZutaten=(EditText)findViewById(R.id.etZutaten);
        etAnleitung=(EditText)findViewById(R.id.etAnleitung);
        etAnzahl=(EditText)findViewById(R.id.etAnzahl);
        etSeason = (EditText)findViewById(R.id.etSeason);
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
        blocked=(CheckBox)findViewById(R.id.checkBox);



        if(getIntent().hasExtra("id")){
            bInsert=false;
            //Rezept wurde übermittelt, daher EditAnsicht
            etTitel.setText(getIntent().getStringExtra("titel"));
            etZutaten.setText(getIntent().getStringExtra("zutaten"));
            etAnleitung.setText(getIntent().getStringExtra("anleitung"));
            etAnzahl.setText(String.valueOf(getIntent().getIntExtra("anzahl", 0)));
            rezeptID = getIntent().getIntExtra("id", 0);
            filename= getIntent().getStringExtra("imageUri");
            if(filename!=null){
                bKamera.setText("Foto aktualisieren");
                tvImageUri.setText(filename);
            }
            etSeason.setText(getIntent().getStringExtra("saison"));
            //Spinner setzen
            spType.setSelection(getIntent().getIntExtra("typ", 0));
            iBlocked=getIntent().getIntExtra("blocked",0);
            if(iBlocked==1){
                blocked.setChecked(true);
            }




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
        filename=savedInstanceState.getString("filename", null);
        tvImageUri.setText(filename);
        System.out.println("RESTOREDINSTANCE");

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("filename", filename);
        System.out.println("SAVEDINSTANCE");
        super.onSaveInstanceState(outState);

    }



    @Override
    public void onClick(View v) {

        //Kameraintegration
        if(v.getId()==R.id.bKamera){
            if(etTitel.getText().toString().isEmpty()){

                Toast.makeText(getApplicationContext(), "Zuerst muss einen Titel vergeben werden", Toast.LENGTH_LONG).show();

            }
            else{

                final CharSequence[] choose = { "Neues Foto", "Aus der Galerie", "Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEditRezept.this);
                builder.setTitle("Foto hinzufügen");
                builder.setItems(choose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (choose[item].equals("Neues Foto")) {
                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            //Ordner auf sdcard erstellen, falls nicht existent:
                            File imageDirectory = new File(restoredPath+IMAGE_FOLDER);
                            imageDirectory.mkdirs();
                            //falls Titel bereits existiert wird er mit in den filenamen aufgenommen
                            File photo = new File(restoredPath+IMAGE_FOLDER,File.separator+etTitel.getText().toString()+"_"+sdf.format(new Date())+".jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                            selectedImageUri = Uri.fromFile(photo);
                            filename = photo.getName();
                            startActivityForResult(intent, REQUEST_CAMERA);

                        } else if (choose[item].equals("Aus der Galerie")) {
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

                        } else if (choose[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }





        }
        else {

            //prüfen, ob alle Felder ausgefüllt wurden
            if (!(etTitel.getText().toString().isEmpty() || etZutaten.getText().toString().isEmpty() || etAnleitung.getText().toString().isEmpty())) {
                //sollte Anzahl nicht gesetzt sein wird 0 gesetzt
                if (etAnzahl.getText().toString().isEmpty()) {
                    etAnzahl.setText("0");
                }
                if (etSeason.getText().toString().isEmpty()){
                    etSeason.setText("01,02,03,04,05,06,07,08,09,10,11,12");
                }
                if (blocked.isChecked()) {
                    iBlocked = 1;
                } else {
                    iBlocked = 0;
                }


                DBHelper helper = DBHelper.getInstance(this);

                if (v.getId() == R.id.bEditRezept) {
                    if (bInsert) {//NEUES REZEPT


                        Rezept r = new Rezept(-1, etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()), filename, iBlocked, etSeason.getText().toString());
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


                        Rezept r = new Rezept(getIntent().getIntExtra("id", -1), etTitel.getText().toString(), etZutaten.getText().toString(), etAnleitung.getText().toString(), spType.getSelectedItemPosition(), Integer.valueOf(etAnzahl.getText().toString()), filename, iBlocked,etSeason.getText().toString());
                        if (!helper.doesAlreadyExist(r)) { //Duplettengenerierung bei Update vermeiden
                            helper.updateRezept(r);
                            Toast.makeText(getApplicationContext(), "Erfolgreich aktualisiert", Toast.LENGTH_LONG).show();

                            //sofern altes Foto vorhanden und ein neues gesetzt wurde  muss dieses gelöscht werden:

                            try { // try und catch sollte nicht notwendig sein, aber wenn das Löschen einer alten Bilddatei nicht klappt, soll deswegen nicht die App abstürzen
                                if (!filename.equals(getIntent().getStringExtra("imageUri"))) {
                                    Worker worker = new Worker(this);
                                    worker.deleteFileFromSDCard(restoredPath + IMAGE_FOLDER + File.separator + getIntent().getStringExtra("imageUri"));
                                }
                            } catch (Exception e) {
                                //harmlos - wird ausgelöst, wenn kein Bild vorhanden ist
                                // e.printStackTrace();
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
            } else {
                Toast.makeText(getApplicationContext(), "Titel,Zutaten und Anleitungen müssen eingegeben werden!", Toast.LENGTH_LONG).show();
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


        //Ordner auf sdcard erstellen, falls nicht existent:

        File imageDirectory = new File(restoredPath+IMAGE_FOLDER);
        imageDirectory.mkdirs();


        //falls Titel bereits existiert wird er mit in den filenamen aufgenommen
        File photo = new File(restoredPath+IMAGE_FOLDER,File.separator+etTitel.getText().toString()+"_"+sdf.format(new Date())+".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        selectedImageUri = Uri.fromFile(photo);
        filename = photo.getName();
        startActivityForResult(intent, CAMERADATA);
    }

    public void saveSharedPrefs(){
        //an dieser Stelle die Änderungen speichern
      //  editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
      //  Worker myWorker = new Worker(this);
  //      editor.putString("plannedIDs", myWorker.getIDs(rezepte));
     //   editor.commit();
    }


    /***
     * Dev Branch CameraAndGallery
     * Select picture over gallery or camera and compress and resize it as jpg
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                tvImageUri.setText(filename);
                //constraint Anleitung darf nicht leer sein notfalls mit folgendem Text bedienen
                if(etAnleitung.getText().toString().isEmpty()){
                    etAnleitung.setText("siehe Bild");
                    System.out.println("Keine Anleitung gefunden");
                }



            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 1024;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                //In Datei schreiben:
                System.out.println("Image ="+selectedImagePath);
                filename =etTitel.getText().toString()+"_"+sdf.format(new Date())+".jpg";
                File photo = new File(restoredPath+IMAGE_FOLDER,File.separator+filename);
                Worker worker = new Worker(this);
                File photoAusGalerie = new File(selectedImagePath);

                try {
                    worker.copyFile( photoAusGalerie,photo);
                    tvImageUri.setText(filename);
                    if(etAnleitung.getText().toString().isEmpty()){
                        etAnleitung.setText("siehe Bild");
                        System.out.println("Keine Anleitung gefunden");
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }







}
