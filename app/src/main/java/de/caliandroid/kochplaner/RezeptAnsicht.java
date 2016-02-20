package de.caliandroid.kochplaner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by stefan on 19.02.16.
 */
public class RezeptAnsicht extends AppCompatActivity {

    private TextView tvTitel,tvZutaten,tvAnleitung,tvAnzahl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rezept_ansicht);

        tvTitel=(TextView)findViewById(R.id.tvRezeptTitel);
        tvZutaten=(TextView)findViewById(R.id.tvZutaten);
        tvAnleitung=(TextView)findViewById(R.id.tvAnleitung);
        tvAnzahl=(TextView)findViewById(R.id.tvAnzahl);

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

}
