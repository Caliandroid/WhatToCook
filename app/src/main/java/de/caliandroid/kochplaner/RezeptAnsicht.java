package de.caliandroid.kochplaner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by stefan on 19.02.16.
 */
public class RezeptAnsicht extends AppCompatActivity implements View.OnClickListener{

    private TextView tvTitel,tvZutaten,tvAnleitung,tvAnzahl;
    private Button bZurueck,bEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rezept_ansicht);

        tvTitel=(TextView)findViewById(R.id.tvRezeptTitel);
        tvZutaten=(TextView)findViewById(R.id.tvZutaten);
        tvAnleitung=(TextView)findViewById(R.id.tvAnleitung);
        tvAnzahl=(TextView)findViewById(R.id.tvAnzahl);
        bZurueck=(Button)findViewById(R.id.bZurueck);
        bEdit=(Button)findViewById(R.id.bEditieren);
        bEdit.setOnClickListener(this);
        bZurueck.setOnClickListener(this);

        //Daten holen
        tvTitel.setText(getIntent().getStringExtra("titel"));
        tvZutaten.setText("ZUTATEN:\n"+getIntent().getStringExtra("zutaten")+"\n\n");
        tvAnleitung.setText("ANLEITUNG:\n"+getIntent().getStringExtra("anleitung"));
        tvAnzahl.setText("Bisher gekocht: "+String.valueOf(getIntent().getIntExtra("anzahl",0))+" mal");




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
        if(v.getId() ==R.id.bEditieren){
            Intent i = new Intent(this,AddEditRezept.class);
            i.putExtra("id", this.getIntent().getIntExtra("id",0));
            i.putExtra("titel", this.getIntent().getStringExtra("titel"));
            i.putExtra("zutaten",  this.getIntent().getStringExtra("zutaten"));
            i.putExtra("anleitung",  this.getIntent().getStringExtra("anleitung"));
            i.putExtra("anzahl", this.getIntent().getIntExtra("anzahl", 0));
            i.putExtra("typ",this.getIntent().getIntExtra("typ",0));
            startActivityForResult(i, 1);



        }
        if(v.getId()== R.id.bZurueck){
            setResult(0);
            finish();
        }

    }

}
