package de.caliandroid.kochplaner;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by stefan on 26.02.16.
 */
public class Settings extends AppCompatActivity implements View.OnClickListener{
    private Button bImport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        bImport= (Button)findViewById(R.id.bImport);
        bImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Worker worker = new Worker(this);
        try{
            worker.importCSVRezepteFromSDCard("/storage/sdcard1/","import.csv","\t");
        }
        catch(IOException e){
            e.printStackTrace();
        }


    }


}
