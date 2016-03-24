package de.caliandroid.kochplaner;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Created by stefan on 21.03.16.
 */
public class RetainedFragment extends Fragment {

    // data object we want to retain
    private boolean runs=false;
    SharedPreferences sharedPreferences;




    interface TaskCallbacks {
        ArrayList<Rezept> onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(ArrayList<Rezept> rezepte);
        SharedPreferences deliverPrefs();
    }

    private TaskCallbacks mCallbacks;


    //zwei Varianten. Mit Contextobjekt für API23++, mit Activityobjekt für <23
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (TaskCallbacks) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }


        // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);


    }

    public void startTask(){
        sharedPreferences = mCallbacks.deliverPrefs();
        //Starte Async Task
        if(!runs){
            DoThePlanning doThePlanning = new DoThePlanning();
            doThePlanning.execute();
            runs=true;
        }
        else{
            System.out.println("Thread läuft noch");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }





    public void setRunning(boolean runs){
        System.out.println("Wurde gesetzt auf "+runs);
        this.runs=runs;
    }

    public boolean isRunning(){
        return this.runs;
    }

    private class DoThePlanning extends AsyncTask<Void, Integer, Void> {
        ArrayList<Rezept> rezepte = new ArrayList<Rezept>();
        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            DBHelper helper  = DBHelper.getInstance(MainActivity.activity);
            helper.openDB();
            int i=0;

            //24.03.2016 keep receipts
           // helper.deleteAllPlanned();
            //helper.deleteAllFromShoppinglist();
           // rezepte.clear();
            //neue Wochenplanung durchführen

            rezepte = mCallbacks.onPreExecute();
            System.out.println("Habe Rezepte erhalten = "+rezepte.size());
            ArrayList<Rezept> neueRezepte=new ArrayList<>();



            //vegetarisch=0
            neueRezepte.addAll(helper.getKochplanNeu(0, Integer.valueOf(sharedPreferences.getString("vegetarisch", "1")),rezepte));
            i++;
            publishProgress(i);
            //Fleisch=1
            neueRezepte.addAll(helper.getKochplanNeu(1, Integer.valueOf(sharedPreferences.getString("fleisch", "1")),rezepte));
            i++;
            publishProgress(i);
            //Fisch=2
            neueRezepte.addAll(helper.getKochplanNeu(2, Integer.valueOf(sharedPreferences.getString("fisch", "1")),rezepte));
            i++;
            publishProgress(i);
            //Süß=3
            neueRezepte.addAll(helper.getKochplanNeu(3, Integer.valueOf(sharedPreferences.getString("suess", "1")),rezepte));
            i++;
            publishProgress(i);
            //Dessert=4
            neueRezepte.addAll(helper.getKochplanNeu(4, Integer.valueOf(sharedPreferences.getString("nachtisch", "1")),rezepte));
            i++;
            publishProgress(i);
            //Snack=5
            neueRezepte.addAll(helper.getKochplanNeu(5, Integer.valueOf(sharedPreferences.getString("snack", "1")),rezepte));
            i++;
            publishProgress(i);

            Collections.shuffle(neueRezepte);

            Iterator i1 = neueRezepte.iterator();
            while(i1.hasNext()){
                i++;
                publishProgress(i);
                Rezept r = (Rezept)i1.next();
                helper.insertIntoShoppinglist(r);
            }

            helper.insertPlanned(neueRezepte);
            rezepte.addAll(neueRezepte);
            i++;
            publishProgress(i);
            runs=false;
            //helper.close();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(rezepte);
            }
        }
    }
}
