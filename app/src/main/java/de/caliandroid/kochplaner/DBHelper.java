package de.caliandroid.kochplaner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
    private static String DB_NAME = "kochplaner.db";
    private SQLiteDatabase myDB;
    private final Context context;

    //CStatische Werte
    static final String [] COLUMNS = {"_id","TITEL","ZUTATEN","ANLEITUNG","TYP","ANZAHL","IMAGEURI"};
    static final String TABELLE = "rezepte";
    static final String ID = "_id";
    static final String TITEL = "titel";
    static final String ZUTATEN = "zutaten";
    static final String ANLEITUNG = "anleitung";
    static final String TYP = "typ";
    static final String ANZAHL= "anzahl";
    static final String IMAGEURI= "imageUri";



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
        String whereClause ="_id=?";


        final String[] split = ids.split(",");
        for (int i=0;i<split.length;i++)
        {

            String[]selectionArgs ={split[i].toString()};

            try {
                c = db.query(dbName, COLUMNS, whereClause, selectionArgs, null, null, null, null);
                c.moveToFirst();
                rezept = new Rezept(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5),c.getString(6), false);
                rezepte.add(rezept);

            }
            catch(SQLiteException e){
                e.printStackTrace();
                System.out.println("C scheint leer zu sein");
            }
            catch(CursorIndexOutOfBoundsException e){
                e.printStackTrace(); System.out.println("Hinweis: Eine gerade gelöschte ID ist bei den geplanten Rezepten dabei\\nWird beim nächsten Speichern autom. bereinigt");
            }


        }
        db.close();

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
        String whereClause ="typ=?";
        String[]selectionArgs={String.valueOf(typ)};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(tage));

        Cursor c= db.query(dbName,COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
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
        db.close();
        return rezepte;

    }

    /**
     * Holt die Rezepte anhand der Häufigkeit aus der DB mit Berücksichtigung des Typs (vegetarisch, Fleisch, Fisch etc.)
     * @return rezepte
     */
    public ArrayList <Rezept> getKochplan() {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        ArrayList<Rezept> rezepte=new ArrayList();
        //query() Methode

        //die Variablen
        String dbName ="rezepte";
        String whereClause ="typ=?";
        String[]selectionArgs={String.valueOf(0)};
        String  order="ANZAHL ASC";
        String limit = "4";
        Cursor c = db.query(dbName, COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
            rezepte.add(rezept);
            c.moveToNext();
        }
        //Abfrage wiederholen für 2x Fleisch und 1xFisch
        selectionArgs[0]="1";
        limit="2";
        c= db.query(dbName,COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
            rezepte.add(rezept);
            c.moveToNext();
        }
        selectionArgs[0]="2";
        limit="1";
        c= db.query(dbName,COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
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
        db.close();
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

    /**
     *
     * @param r
     */
    public void updateRezept(Rezept r){
        SQLiteDatabase db = this.getWritableDatabase();
        String sID =String.valueOf(r.getId());
        ContentValues values = new ContentValues();
        values.put(TITEL,r.getTitel());
        values.put(ZUTATEN,r.getZutaten());
        values.put(ANLEITUNG,r.getAnleitung());
        values.put(TYP,r.getTyp());
        values.put(ANZAHL,r.getAnzahl());
        values.put(IMAGEURI,r.getImageUri());
        String whereClause= "_id = ?";
        String[]whereArgs= {sID};

        try{
            db.update(TABELLE, values, whereClause, whereArgs); //

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    /**
     *
     * @param r
     */
    public void insertRezept(Rezept r){
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TITEL,r.getTitel());
        values.put(ZUTATEN,r.getZutaten());
        values.put(ANLEITUNG,r.getAnleitung());
        values.put(TYP,r.getTyp());
        values.put(ANZAHL,r.getAnzahl());
        values.put(IMAGEURI,r.getImageUri());

        // 3. insert
        try{
            db.insert(TABELLE, null, values); //
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();


    }

    public void deleteRezept(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sID =String.valueOf(id);
        String whereClause="_id=?";
        String []whereArgs={sID};

        try{
            db.delete(TABELLE, whereClause, whereArgs);

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    /**
     * Tauscht ein Rezept per Knopfdruck aus
     * TODO das ausgetauschte Rezept sollte nicht sofort beim zweiten Aufruf dieser Funktion wieder erscheinen, daher muss es temporär geblockt werden
     * @param r
     * @param ids
     * @return
     */
    public Rezept replaceRezept(Rezept r, String ids) {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept =null;
        StringBuffer fragezeichen=new StringBuffer();
        String []tempSplit=ids.split(",");

        int anzahlFragezeichen = tempSplit.length;
        for (int i=0;i<anzahlFragezeichen;i++){
            if(i==0){
                fragezeichen.append("?");}
            else{
                fragezeichen.append(",?");}
        }

        String[]selectionArgs = new String[anzahlFragezeichen+1];
        for(int i=0;i<=anzahlFragezeichen;i++){
            if(i==0){
                selectionArgs[i]=String.valueOf(r.getTyp());
            }
            else{
                selectionArgs[i]=tempSplit[i-1];
            }

        }

        //die Variablen
        String whereClause = TYP+" = ? and "+ID+"  not in ("+fragezeichen+")";
        //String[]selectionArgs={String.valueOf(r.getTyp()), ids};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(1));

        Cursor c= db.query(TABELLE,COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
                   rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
            System.out.println("Habe ein neues Rezept für dich= "+rezept.getTitel());
            c.moveToNext();

        }

        db.close();
        return rezept;

    }

    /**
     * Tauscht ein Rezept per Knopfdruck aus - überladene Methode Variante mit Blocker
     * TODO das ausgetauschte Rezept sollte nicht sofort beim zweiten Aufruf dieser Funktion wieder erscheinen, daher muss es temporär geblockt werden
     * TODO funktioniert noch nicht
     * @param r
     * @param ids Die ID's der aktuell geplanten Rezepte
     * @param blocker Auf den Blocker kommen temporär die letzte ausgetauschte ID
     * @return
     */
    public Rezept replaceRezept(Rezept r, String ids, ArrayList<String> blocker) {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept =null;
        StringBuffer fragezeichen=new StringBuffer();
        String []tempSplit=ids.split(",");
        int anzahlFragezeichen=0;
        String[]selectionArgs;

        System.out.println("blocker länge = "+blocker.size());

        anzahlFragezeichen = tempSplit.length+blocker.size(); //um Anzahl der Items in Blocker verlängert
        selectionArgs = new String[anzahlFragezeichen+1]; //



        for (int i=0;i<anzahlFragezeichen;i++){
            if(i==0){
                fragezeichen.append("?");}
            else{
                fragezeichen.append(",?");}
        }



        int i2=0;
        for(int i=0;i<=anzahlFragezeichen;i++){
            if(i==0){ //das ist der Rezept-Typ
                selectionArgs[i]=String.valueOf(r.getTyp());
            }
            else{
                if(i<=tempSplit.length) {
                    System.out.println("in I steht jetzt = "+i);
                    selectionArgs[i] = tempSplit[i - 1];
                }
                else{
                    System.out.println("in I (else) steht jetzt = " + i);
                    //jetzt die Inhalte aus dem Blocker einfügen
                    System.out.println("in Blocker steht "+blocker.get(i2));
                    selectionArgs[i]= (String)blocker.get(i2);
                    i2++;
                }
            }

        }

        //die Variablen
        String whereClause = TYP+" = ? and "+ID+"  not in ("+fragezeichen+")";
        //String[]selectionArgs={String.valueOf(r.getTyp()), ids};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(1));

        Cursor c= db.query(TABELLE,COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
            System.out.println("Habe ein neues Rezept für dich= "+rezept.getTitel());
            c.moveToNext();

        }

        db.close();
        return rezept;

    }

    /**
     * Prüft, ob Titel in Kombi mit Typ schon existiert
     * @param r
     * @param
     * @return
     */
    public boolean doesAlreadyExist(Rezept r){
        SQLiteDatabase db =this.getReadableDatabase();

        String whereClause = TITEL+" = ? and "+TYP+" = ?";
        String[]selectionArgs={r.getTitel(), String.valueOf(r.getTyp())  };
        Cursor c= db.query(TABELLE,COLUMNS,whereClause,selectionArgs,null,null,null,null);

        if(c.getCount()>0){
            //für update Methode gilt die Regel, dass auch hier false übergeben wird, wenn ID vom übergebenen Rezept mit dem gefunden übereinstimmt
            //so läßt sich verhindern, dass anderweitig ohne Prüfung ein vorhandenes Rezept so umbenannt wird, dass es eine Duplette gibt.
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Rezept rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
                if(rezept.getId()==r.getId()){
                    db.close();
                    return false; //existiert schon, aber da gleiche ID läuft eine Prüfung für ein Update
                }
                c.moveToNext();

            }

            db.close();
            return true;
        }
        else{
            db.close();
            return false;
        }





    }

    /**
     * Universelle Abfrage der DB anhand der übergebenen Paramter
     * @param whereClause  Bedingung wie TYP=?
     * @param selectionArgs Array mit den Werten für die Bedingung wie 1,2
     * @param order Reihefolge (order by xy)
     * @param limit Limitierung der Ergebnisse
     * @return
     */
    public ArrayList <Rezept> getRezepte(String whereClause, String []selectionArgs,String order, String limit ) {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        ArrayList<Rezept> rezepte=new ArrayList();
        Cursor c = db.query(TABELLE, COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),false);
            rezepte.add(rezept);
            c.moveToNext();
        }

        db.close();
        return rezepte;

    }






    



}
