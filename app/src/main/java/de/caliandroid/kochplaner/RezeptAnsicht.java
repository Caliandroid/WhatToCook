package de.caliandroid.kochplaner;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.sql.Types;

/**
 * Created by stefan on 19.02.16.
 */
public class RezeptAnsicht extends AppCompatActivity implements View.OnClickListener{

    private static final String IMAGELOCATIONPREFIX="file://";
    private static final String IMAGE_FOLDER ="/images";  //geladen werden soll dann noch /storage/sdcard1/kochplaner


    private TextView tvTitel,tvZutaten,tvAnleitung,tvAnzahl,tvBlocked,tvSaison;
    private int id;
    private Button bZurueck,bEdit,bDelete;
    public static Activity activity;
    String imageUri = null;
    SharedPreferences sharedpreferences;
    int iPosition=-1;
    String restoredPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTitle("Rezeptdetails");
        //pfad laden
        sharedpreferences = getSharedPreferences(MainActivity.MY_PREFS, MODE_PRIVATE);
        restoredPath = sharedpreferences.getString("storagePath", null);
        setContentView(R.layout.rezept_ansicht);
        activity=this;

        tvTitel=(TextView)findViewById(R.id.tvRezeptTitel);
        tvZutaten=(TextView)findViewById(R.id.tvZutaten);
        tvAnleitung=(TextView)findViewById(R.id.tvAnleitung);
        tvAnzahl=(TextView)findViewById(R.id.tvAnzahl);
        bZurueck=(Button)findViewById(R.id.bZurueck);
        bEdit=(Button)findViewById(R.id.bEditieren);
        bDelete=(Button)findViewById(R.id.bDelete);
        bEdit.setOnClickListener(this);
        bZurueck.setOnClickListener(this);
        bDelete.setOnClickListener(this);
        ImageView imageView =(ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        tvBlocked = (TextView)findViewById(R.id.textView4);
        tvSaison = (TextView)findViewById(R.id.tvSeason);

        //Daten holen

        //01.09.2018 add typ to the title, f.e.  Sushi => Sushi [Fish]
        String sTyp="";
        int iTyp  = getIntent().getIntExtra("typ", 0);
        switch (iTyp){
            case 0:
                sTyp="Vegtarisch";
                break;
            case 1:
                sTyp="Fleisch";
                break;
            case 2:
                sTyp="Fisch";
                break;
            case 3:
                sTyp="Süßspeise";
                break;
            case 4:
                sTyp="Dessert";
                break;
            case 5:
                sTyp="Snack";
                break;



        }


        id = getIntent().getIntExtra("id",-1);
        tvTitel.setText(getIntent().getStringExtra("titel")+" ["+sTyp+"]\n");




        //Format Zutaten

        tvZutaten.setText("ZUTATEN:\n"+getIntent().getStringExtra("zutaten").replace("#","\n")+"\n\n");


        tvAnleitung.setText("ANLEITUNG:\n" + getIntent().getStringExtra("anleitung"));
        tvAnzahl.setText("Bisher gekocht: " + String.valueOf(getIntent().getIntExtra("anzahl", 0)) + " mal");

        tvSaison.setText("Saison: " + getIntent().getStringExtra("saison"));
        if( getIntent().getIntExtra("blocked", 0)==1 ){
            tvBlocked.setText("geblockt!");
        }
        iPosition=getIntent().getIntExtra("position",-1);

        imageUri = getIntent().getStringExtra("imageUri");
        if(imageUri!=null){

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(IMAGELOCATIONPREFIX+restoredPath+IMAGE_FOLDER+File.separator+imageUri));
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
               //no image even if the filename is in the db -> shrink the imageview to 0,0
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                params.width = 0;
                params.height=0;
                imageView.setLayoutParams(params);

            }
        }
        else{
            //shrink imageview to 0,0
           LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            params.width = 0;
            params.height=0;
            imageView.setLayoutParams(params);

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

        if(v.getId() ==R.id.imageView) {
            try{
                //Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(getIntent().getStringExtra("imageUri")).build();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri uri = Uri.parse(IMAGELOCATIONPREFIX+restoredPath+IMAGE_FOLDER+File.separator+imageUri);
                intent.setDataAndType(uri, "image/*");
                //Intent intent = new Intent(android.content.Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }catch(ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(v.getId() ==R.id.bEditieren){
            Intent i = new Intent(this,AddEditRezept.class);
            i.putExtra("id", id);
            i.putExtra("titel", this.getIntent().getStringExtra("titel"));
            i.putExtra("zutaten",  this.getIntent().getStringExtra("zutaten"));
            i.putExtra("anleitung",  this.getIntent().getStringExtra("anleitung"));
            i.putExtra("anzahl", this.getIntent().getIntExtra("anzahl", 0));
            i.putExtra("typ",this.getIntent().getIntExtra("typ",0));
            i.putExtra("imageUri",this.getIntent().getStringExtra("imageUri"));
            i.putExtra("blocked",this.getIntent().getIntExtra("blocked",0));
            i.putExtra( "saison", this.getIntent().getStringExtra("saison"));
            startActivityForResult(i, 1);



        }
        if(v.getId()== R.id.bZurueck){

            setResult(-1);
            finish();
        }
        if(v.getId()== R.id.bDelete){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Dieses Rezept wirklich löschen?");
            // alert.setMessage("Message");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    Toast.makeText(getApplicationContext(), tvTitel.getText() + " wurde gelöscht)", Toast.LENGTH_LONG).show();
                    //TODO Problem: Die RezeptAnsicht Activity kann aus MainActivity oder aus RezepteAlle aufgerufen werden
                    Intent i= new Intent();
                    i.putExtra("id",id);
                    i.putExtra("position",iPosition); //only for RezepteAlle
                    setResult(2, i); //2=delete
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

    }

}
