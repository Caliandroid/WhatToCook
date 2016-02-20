package de.caliandroid.kochplaner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Selection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Stefan Caliandro on 15.09.15.
 *
 */
public class DBHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/de.caliandroid.kochplaner/databases/";
    private static String DB_NAME = "kochplaner_db";
    private SQLiteDatabase myDB;
    private final Context context;
    //COLUMN Names
    // Table ingredients
    static final String TABELLE = "rezepte";
    static final String TITEL = "titel";
    static final String ZUTATEN = "zutaten";
    static final String ANLEITUNG = "anleitung";
    static final String TYP = "typ";
    static final String ANZAHL= "anzahl";
    static final String ZULETZT= "zuletzt";


    public DBHelper(Context context){
        super(context, DB_NAME, null, 1);
        this.context = context;


    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     *
     * @return true if the database already exists
     */
    private boolean existDB(){
        SQLiteDatabase sdb =null;
        try{
            String myPath = DB_PATH + DB_NAME;
            sdb= SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        if (sdb!=null) sdb.close();

        return sdb != null;
    }


    public void createDB() throws IOException{
        if (existDB()){
            System.out.println("CheckDB: found already exiting db");
        }
        else{
            try{
                System.out.println("CheckDB: No db found (first run of this app) - Will now initialize the db");
                this.getReadableDatabase();
                initializeDB();
            }
            catch(IOException e){
                throw new Error ("One time initialisation of the database failed ");
            }
        }
    }
    /**
     * initialize the db (one time) by byte the prepared sqlite file from assets
     */
    private void initializeDB() throws IOException{
        try {
            InputStream is = context.getAssets().open((DB_NAME));
            String path = DB_PATH+DB_NAME;
            OutputStream os = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int length;
            while((length=is.read(buffer))>0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDB() {
        try{
            String path = DB_PATH + DB_NAME;
            myDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE); //??Warum stand im Bsp. READONLY??
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {

        if(myDB != null)
            myDB.close();
        super.close();

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
// Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public Cursor getRezepte(){
        SQLiteDatabase db =this.getReadableDatabase();
        String [] columns=new String[]{"_id",TITEL,ZUTATEN,ANLEITUNG,TYP,ANZAHL};
        Cursor c=db.query(TABELLE, columns,null,null, null, null, null);
        return c;
    }

    /**
     * Restauriert die Rezepte ArrayList anhand der gespeicherten ID's
     * @param ids
     * @return
     * TODO Bei Where IN Abfrage ist die Reihenfolge nicht ok, muss den Query vermutlich zu Einzelqueries aufdröseln
     */
    public ArrayList<Rezept> getGeplanteRezepte(String ids){
        if(ids==null|| ids.equals("")){

            return null;
        }
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        ArrayList<Rezept> rezepte=new ArrayList();
        Cursor c;
        //die Variablen
        String dbName ="rezepte";
        String [] columnNames = {"_id","TITEL","ZUTATEN","ANLEITUNG","TYP","ANZAHL"};
        String whereClause ="_id=?";


        final String[] split = ids.split(",");
        for (int i=0;i<split.length;i++)
        {

            String[]selectionArgs ={split[i].toString()};

            try {
                c = db.query(dbName, columnNames, whereClause, selectionArgs, null, null, null, null);
                c.moveToFirst();
                rezept = new Rezept(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5), false);
                rezepte.add(rezept);

            }
            catch(SQLiteException e){
                e.printStackTrace();
                System.out.println("C scheint leer zu sein");
            }

        }
        return rezepte;

    }

    /**
     * Holt die Rezepte anhand der Häufigkeit aus der DB mit Berücksichtigung des Typs (vegetarisch, Fleisch, Fisch etc.)
     * @param typ
     * @param tage
     * @return rezepte
     */
    public ArrayList <Rezept> getSelectedRezepte(int typ, int tage) {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        ArrayList<Rezept> rezepte=new ArrayList();
        //query() Methode

        //die Variablen
        String dbName ="rezepte";
        String [] columnNames = {"_id","TITEL","ZUTATEN","ANLEITUNG","TYP","ANZAHL"};
        String whereClause ="typ=?";
        String[]selectionArgs={String.valueOf(typ)};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(tage));

        Cursor c= db.query(dbName,columnNames,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),false);
            rezepte.add(rezept);
            c.moveToNext();
        }
        //Rezepte zufällig anordnen
        Collections.shuffle(rezepte, new Random(System.nanoTime()));
        Iterator i = rezepte.iterator();
        while(i.hasNext()){
            rezept=(Rezept)i.next();
            System.out.println(rezept.getTitel());
        }
        return rezepte;

    }

    /**
     * Setzt die Häufigkeit des Rezepts mit der ID id ++
     * @param myID
     */
    public void updateHaeufigkeit(int myID){
        SQLiteDatabase db =this.getWritableDatabase();
       // db.rawQuery("UPDATE rezepte set anzahl=anzahl+1 WHERE _id = ?", new String[]{String.valueOf(myID)});
        db.execSQL("UPDATE rezepte set anzahl=anzahl+1 WHERE _id = ?", new String[]{String.valueOf(myID)});
    }






    



}
