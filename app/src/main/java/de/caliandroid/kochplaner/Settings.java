package de.caliandroid.kochplaner;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by stefan on 26.02.16.
 */
public class Settings extends AppCompatActivity implements View.OnClickListener{

    public static final String IMPORTPATH = "/storage/sdcard1/kochplaner/csv/";
    public static final String IMPORT_FOLDER="/csv";
    public static final String IMPORTFILE= "import.csv";
    public static final String EXPORTFILE="export.csv";
    public static final String SPLITTER =";";
    public static final String MY_PREFS = "MyPrefs";

    String restoredPath;
    private Button bImport,bSave,bExport;
    private EditText etPath,etVeg,etFlei,etFisc, etSuess,etNach,etSnack;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load Prefs
        sharedpreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        restoredPath = sharedpreferences.getString("storagePath", null);
        setContentView(R.layout.settings);
        bImport= (Button)findViewById(R.id.bImport);
        bImport.setOnClickListener(this);
        bSave=(Button)findViewById(R.id.button4);
        bSave.setOnClickListener(this);
        bExport = (Button) findViewById(R.id.bExport);
        bExport.setOnClickListener(this);

        etPath = (EditText) findViewById(R.id.editText);
        if(restoredPath!=null){
            etPath.setText(restoredPath);
        }
        etVeg = (EditText)findViewById(R.id.textView11);
        etVeg.setText(sharedpreferences.getString("vegetarisch", "2"));
        etFlei= (EditText)findViewById(R.id.textView12);
        etFlei.setText(sharedpreferences.getString("fleisch", "1"));
        etFisc= (EditText)findViewById(R.id.textView13);
        etFisc.setText(sharedpreferences.getString("fisch", "1"));
        etSuess= (EditText)findViewById(R.id.textView14);
        etSuess.setText(sharedpreferences.getString("suess", "1"));
        etNach= (EditText)findViewById(R.id.editText2);
        etNach.setText(sharedpreferences.getString("nachtisch", "1"));
        etSnack  = (EditText)findViewById(R.id.editText3);
        etSnack.setText(sharedpreferences.getString("snack", "1"));

    }

    @Override
    public void onClick(View v) {
        if(v.getId()== bImport.getId()) {
            Worker worker = new Worker(this);
            try {

                int[]result= worker.importCSVRezepteFromSDCard(restoredPath+IMPORT_FOLDER, File.separator+IMPORTFILE, SPLITTER);
                Toast.makeText(getApplicationContext(), "Erfolgreich importiert: "+result[0]+" Rezepte. Nicht importiert: "+result[1]+" Stück", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(v.getId()== bSave.getId()) {
            saveSharedPrefs();
            restoredPath= etPath.getText().toString(); //da die Änderung auch direkt ohne erneutes OnCreate Aufrufen aktiv ist
            Toast.makeText(getApplicationContext(), "Settings gespeichert!", Toast.LENGTH_LONG).show();
        }

        if(v.getId()==bExport.getId()){
            DBHelper dbHelper = new DBHelper(this);
            ArrayList<Rezept> rezepte = dbHelper.getRezepte(null,null,null,null);
            Worker worker = new Worker(this);
            try{
                worker.exportRezepteToCSV(restoredPath+IMPORT_FOLDER, File.separator+EXPORTFILE, SPLITTER,rezepte);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        //saveSharedPrefs();


    }
    public void saveSharedPrefs(){
        //an dieser Stelle die Änderungen speichern
        editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
        editor.putString("storagePath", etPath.getText().toString());
        editor.putString("vegetarisch", etVeg.getText().toString());
        editor.putString("fleisch", etFlei.getText().toString());
        editor.putString("fisch", etFisc.getText().toString());
        editor.putString("suess", etSuess.getText().toString());
        editor.putString("nachtisch", etNach.getText().toString());
        editor.putString("snack",etSnack.getText().toString());

        editor.commit();
    }



}
