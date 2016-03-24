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

    public static synchronized DBHelper getInstance(Context context)
    {
        if (instance == null)
            instance = new DBHelper(context);

        return instance;
    }

    private DBHelper (Context context){
        super(context, DB_NAME, null, 1);
        this.context = context;
    }




    //Singleton
    private static DBHelper instance;

    private static String DB_PATH = "/data/data/de.caliandroid.kochplaner/databases/";
    private static String DB_NAME = "kochplaner.db";
    private SQLiteDatabase myDB;
    private final Context context;

    static final String ZUTATENSPLITTER ="#";

    //Statische Werte für Tabelle REZEPTE
    static final String TABELLE1 = "rezepte";
    static final String TABELLE1_1 = "_id";
    static final String TABELLE1_2 = "titel";
    static final String TABELLE1_3 = "zutaten";
    static final String TABELLE1_4 = "anleitung";
    static final String TABELLE1_5 = "typ";
    static final String TABELLE1_6= "anzahl";
    static final String TABELLE1_7= "imageUri";
    static final String TABELLE1_8 ="blocked";
    static final String [] TABELLE1_COLUMNS = {TABELLE1_1 ,TABELLE1_2 ,TABELLE1_3,TABELLE1_4,TABELLE1_5,TABELLE1_6, TABELLE1_7,TABELLE1_8};

    //Statische Werte für Tabelle PLANNED
    static final String TABELLE2 = "planned";
    static final String TABELLE2_1= "_id";
    static final String TABELLE2_2 = "rezeptid";
    static final String TABELLE2_3= "prepared";
    static final String [] TABELLE2_COLUMNS = {TABELLE2_1,TABELLE2_2,TABELLE2_3};

    //Statische Werte für Tabelle shoppingliste
    static final String TABELLE3 = "shoppingliste";
    static final String TABELLE3_1= "_id";
    static final String TABELLE3_2 = "zutat";
    static final String TABELLE3_3= "shopped";
    static final String TABELLE3_4= "planned_id";
    static final String [] TABELLE3_COLUMNS = {TABELLE3_1,TABELLE3_2,TABELLE3_3,TABELLE3_4};



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
                c = db.query(dbName, TABELLE1_COLUMNS, whereClause, selectionArgs, null, null, null, null);
                c.moveToFirst();
                rezept = new Rezept(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5),c.getString(6),c.getInt(7) );
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

        Cursor c= db.query(dbName,TABELLE1_COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
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
        Cursor c = db.query(dbName, TABELLE1_COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
            rezepte.add(rezept);
            c.moveToNext();
        }
        //Abfrage wiederholen für 2x Fleisch und 1xFisch
        selectionArgs[0]="1";
        limit="2";
        c= db.query(dbName,TABELLE1_COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
            rezepte.add(rezept);
            c.moveToNext();
        }
        selectionArgs[0]="2";
        limit="1";
        c= db.query(dbName,TABELLE1_COLUMNS,whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
            rezepte.add(rezept);
            c.moveToNext();
        }


        //Rezepte zufällig anordnen und in der Planned Tabelle eintragen ( in der gleichen Reihenfolge)
        Collections.shuffle(rezepte, new Random(System.nanoTime()));
        Iterator i = rezepte.iterator();
        while(i.hasNext()){
            rezept=(Rezept)i.next();
            //Füge in Planned ein
            insertPlanned(rezept);
            //Füge in shoppinglist ein
            insertIntoShoppinglist(rezept);
        }
        db.close();
        return rezepte;

    }

    /**
     * Holt die Rezepte anhand der Häufigkeit aus der DB mit Berücksichtigung des Typs (vegetarisch, Fleisch, Fisch etc.)
     * Holt keine Rezepte, die bereits in der Planung stecken
     * @return rezepte
     */
    public ArrayList <Rezept> getKochplanNeu(int typ,int anzahl,ArrayList<Rezept> plannedReceipts) {
        ArrayList<Rezept> rezepteNeu=new ArrayList<>();
        Worker worker = new Worker(context);
        String ids= worker.getIDs(plannedReceipts);
        StringBuffer fragezeichen=new StringBuffer();
        String []tempSplit=ids.split(",");

        int anzahlFragezeichen = tempSplit.length;
        for (int i=0;i<anzahlFragezeichen;i++){
            if(i==0){
                fragezeichen.append("?");}
            else{
                fragezeichen.append(",?");}
        }



        String[]selectionArgs = new String[anzahlFragezeichen+2];
        for(int i=0;i<selectionArgs.length;i++){
            if(i==0){
                selectionArgs[i]=String.valueOf(typ);
            }
            else if(i==1 )
            {
                selectionArgs[i]="0";
            } else  {
                selectionArgs[i]=tempSplit[i-2];
            }

        }
        for (int i=0;i<selectionArgs.length;i++){
            System.out.println("index "+i+" = "+selectionArgs[i]);
        }

        Rezept rezept;
        //die Variablen
        String dbName ="rezepte";
        String whereClause=TABELLE1_5+" =? and "+TABELLE1_8+" =? and " +TABELLE1_1+" not in ("+fragezeichen+")";; //geblockte Rezepte sollen nicht geholt werden
        //String[]selectionArgs={String.valueOf(typ),"0"};
        String  order="ANZAHL ASC";
        String limit = String.valueOf(anzahl);
        Cursor c = myDB.query(dbName, TABELLE1_COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            try{
                rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
                rezepteNeu.add(rezept);
                // erst wird noch umsortiert insertIntoShoppinglist(rezept);
            }
            catch(SQLiteException e){
                e.printStackTrace();
            }

            c.moveToNext();
        }



        return rezepteNeu;

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
        values.put(TABELLE1_2,r.getTitel());
        values.put(TABELLE1_3,r.getZutaten());
        values.put(TABELLE1_4,r.getAnleitung());
        values.put(TABELLE1_5,r.getTyp());
        values.put(TABELLE1_6,r.getAnzahl());
        values.put(TABELLE1_7,r.getImageUri());
        values.put(TABELLE1_8,r.getBlocked());
        String whereClause= "_id = ?";
        String[]whereArgs= {sID};

        try{
            db.update(TABELLE1, values, whereClause, whereArgs); //

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
        values.put(TABELLE1_2,r.getTitel());
        values.put(TABELLE1_3,r.getZutaten());
        values.put(TABELLE1_4,r.getAnleitung());
        values.put(TABELLE1_5,r.getTyp());
        values.put(TABELLE1_6,r.getAnzahl());
        values.put(TABELLE1_7,r.getImageUri());
        values.put(TABELLE1_8,r.getBlocked());

        // 3. insert
        try{
            db.insert(TABELLE1, null, values); //
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    public void deleteRezept(int id){
        SQLiteDatabase db = this.getWritableDatabase();
       // String sID =String.valueOf(r.getId());
        String whereClause="_id=?";
        String []whereArgs={String.valueOf(id)};

        try{
            db.delete(TABELLE1, whereClause, whereArgs);
            //es muss ebenfalls aus der planned und shoppinglist tabelle entfernt werden
            deletePlanned(id);
            deleteItemFromShoppinglist(id);

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
        String whereClause = TABELLE1_5+" = ? and "+TABELLE1_1+"  not in ("+fragezeichen+")";
        //String[]selectionArgs={String.valueOf(r.getTyp()), ids};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(1));

        Cursor c= db.query(TABELLE1, TABELLE1_COLUMNS, whereClause, selectionArgs, null, null, order, limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
                   rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
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

        System.out.println("blocker länge = " + blocker.size());

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

        String whereClause = TABELLE1_5+" = ? and "+TABELLE1_1+"  not in ("+fragezeichen+")";
        //String[]selectionArgs={String.valueOf(r.getTyp()), ids};
        String  order="ANZAHL ASC";
        String limit =(String.valueOf(1));

        Cursor c= db.query(TABELLE1, TABELLE1_COLUMNS, whereClause, selectionArgs, null, null, order, limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
            System.out.println("Habe ein neues Rezept für dich= " + rezept.getTitel());
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

        String whereClause = TABELLE1_2+" = ? and "+TABELLE1_5+" = ?";
        String[]selectionArgs={r.getTitel(), String.valueOf(r.getTyp())  };
        Cursor c= db.query(TABELLE1,TABELLE1_COLUMNS,whereClause,selectionArgs,null,null,null,null);

        if(c.getCount()>0){
            //für update Methode gilt die Regel, dass auch hier false übergeben wird, wenn ID vom übergebenen Rezept mit dem gefunden übereinstimmt
            //so läßt sich verhindern, dass anderweitig ohne Prüfung ein vorhandenes Rezept so umbenannt wird, dass es eine Duplette gibt.
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Rezept rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
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

    public boolean alreadyInList(Rezept r){

        String whereClause = TABELLE2_2+" = ? ";
        String[]selectionArgs={String.valueOf(r.getId())};
        Cursor c= myDB.query(TABELLE2,TABELLE2_COLUMNS,whereClause,selectionArgs,null,null,null,null);


        if(c.getCount()>0){

            return true;


        }
        else{
            //db.close();
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
        Cursor c = db.query(TABELLE1, TABELLE1_COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
            rezepte.add(rezept);
            c.moveToNext();
        }

        db.close();
        return rezepte;

    }

    /**
     * Universelle Abfrage der DB anhand der übergebenen Paramter
     * @param whereClause  Bedingung wie TYP=?
     * @param selectionArgs Array mit den Werten für die Bedingung wie 1,2
     * @param order Reihefolge (order by xy)
     * @param limit Limitierung der Ergebnisse
     * @return
     */
    public Rezept getRezept(String whereClause, String []selectionArgs,String order, String limit ) {
        SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept=null;
        ArrayList<Rezept> rezepte=new ArrayList();
        Cursor c = db.query(TABELLE1, TABELLE1_COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        try{
            rezept = new Rezept(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getInt(5),c.getString(6),c.getInt(7));
        }
        catch(CursorIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        db.close();
        return rezept;
    }

    /**
     * Fügt in die Planned Tabelle einen einzelnen Eintrag an.
     * @param r
     */
    public void insertPlanned(Rezept r){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABELLE2_2,r.getId());
        values.put(TABELLE2_3, 0);

        try{
            db.insert(TABELLE2, null, values); //
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    /**
     * Variante mit ArrayListe
     * @param rezepte
     */
    public void insertPlanned(ArrayList <Rezept> rezepte){
       // SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        Iterator iterator = rezepte.iterator();
        Rezept rezept;
        while(iterator.hasNext()){
            rezept = (Rezept) iterator.next();
            values.put(TABELLE2_2,rezept.getId());
            values.put(TABELLE2_3, 0);
            try{
                myDB.insert(TABELLE2, null, values); //
            }
            catch(SQLiteException e){
                e.printStackTrace();
            }

        }



       // db.close();
    }

    public ArrayList<Rezept> getPlannedReceipts(String whereClause, String []selectionArgs,String order, String limit){
        ArrayList<Rezept> rezepte = new ArrayList();
        //SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        Cursor c = myDB.query(TABELLE2, TABELLE2_COLUMNS, whereClause,selectionArgs,null,null,order,limit);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(In c stehen die Spalten int id,int rezeptid, int prepared

            //Rezept aus Tabelle Rezept anhand der Spalte2 aus der Planned Tabelle laden
            Cursor c2 = myDB.query(TABELLE1,TABELLE1_COLUMNS,TABELLE1_1 +" =?",new String[]{String.valueOf(c.getInt(1))},null,null,null,null);
            c2.moveToFirst();
            while (!c2.isAfterLast()) {
                rezept = new Rezept(c2.getInt(0), c2.getString(1), c2.getString(2), c2.getString(3), c2.getInt(4), c2.getInt(5), c2.getString(6), c2.getInt(7));
                rezepte.add(rezept);
                c2.moveToNext();
            }
            c.moveToNext();
        }

       // db.close();


        return rezepte;
    }


    public void deletePlanned(int rezeptid){
        SQLiteDatabase db = this.getWritableDatabase();
        String sID =String.valueOf(rezeptid);
        String whereClause="rezeptid=?";
        String []whereArgs={sID};

        try{
            db.delete(TABELLE2, whereClause, whereArgs);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    public void updatePlannedRezeptID(int alt, int neu){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABELLE2_2,neu);
        values.put(TABELLE2_3,0); //prepared muss 0 sein
        String whereClause= "rezeptid = ?";
        String[]whereArgs= {String.valueOf(alt)};

        try{
            db.update(TABELLE2, values, whereClause, whereArgs); //

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    public void deleteAllPlanned(){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            db.delete(TABELLE2, null, null);

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();


    }

    public boolean isPrepared(Rezept r){
        boolean prepared=false;
        //SQLiteDatabase db =this.getReadableDatabase();
        Rezept rezept;
        String whereClause="rezeptid = ?";
        String []selectionArgs= {String.valueOf(r.getId())};
        Cursor c = myDB.query(TABELLE2, TABELLE2_COLUMNS, whereClause,selectionArgs,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            //(int id,String titel,String anleitung, String zutaten,int typ, int anzahl){

            if(c.getInt(2)==1) {
                prepared = true;
            }
            c.moveToNext();
        }
        return prepared;
    }

    public void setPrepared(int rezept_id,int prepared){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABELLE2_3,prepared);
        String whereClause= "rezeptid = ?";
        String[]whereArgs= {String.valueOf(rezept_id)};

        try {
            db.update(TABELLE2, values, whereClause, whereArgs);

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    /**
     * Holt aus dem Feld Zutaten alle Zutaten als StringArray und speichert sie in die
     * Shoppingliste
     * @param r
     */
    public void insertIntoShoppinglist(Rezept r){
       // SQLiteDatabase db = this.getWritableDatabase();
        //Zutaten aufsplitten
        String[] zutaten = r.getZutaten().split(ZUTATENSPLITTER);

        if(zutaten!=null && zutaten.length>0){

            ContentValues values = new ContentValues();
            try{

                for(int i=0;i<zutaten.length;i++){

                    values.put(TABELLE3_4,r.getId());
                    values.put(TABELLE3_3, 0);
                    values.put(TABELLE3_2,zutaten[i]);

                    myDB.insert(TABELLE3, null, values);
                    values.clear();
                   // System.out.println("Zutat "+zutaten[i]+"["+r.getId()+"] in die neue Tabelle gesteckt");
                }
            }
            catch(SQLiteException e){
                e.printStackTrace();
            }
        }
       // db.close();

    }

    public void deleteItemFromShoppinglist(int rezeptid){
        SQLiteDatabase db = this.getWritableDatabase();
        String sID =String.valueOf(rezeptid);
        String whereClause="planned_id=?";
        String []whereArgs={sID};

        try{
            db.delete(TABELLE3, whereClause, whereArgs);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    public void deleteAllFromShoppinglist(){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            db.delete(TABELLE3, null,null);
        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }
    public void setShopped(int item_id, int rezept_id,int shopped){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABELLE3_3,shopped);
        String whereClause= "_id = ?";
        String[]whereArgs= {String.valueOf(item_id)};

        try{
            db.update(TABELLE3, values, whereClause, whereArgs);
            //zusätzlich prüfem, ob jetzt alle Zutaten auf shopped stehen und dann das prepared flag anpassen
            if(isRezeptShopped(rezept_id)){
                setPrepared(rezept_id,1);
            }
            else{
                setPrepared(rezept_id,0);
            }

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }

    public boolean isShopped(int rezept_id,String zutat){
        boolean shopped=false;
        SQLiteDatabase db =this.getReadableDatabase();
        String whereClause="planned_id = ? and zutat = ?";
        String []selectionArgs= {String.valueOf(rezept_id),zutat};
        Cursor c = db.query(TABELLE3, TABELLE3_COLUMNS, whereClause,selectionArgs,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            if(c.getInt(2)==1) {
                shopped= true;
            }
            c.moveToNext();
        }
        db.close();
        return shopped;
    }

    public boolean isRezeptShopped(int rezept_id){
        boolean shopped=true;
        SQLiteDatabase db =this.getReadableDatabase();
        String whereClause=TABELLE3_4+" = ? ";
        String []selectionArgs= {String.valueOf(rezept_id)};
        Cursor c = db.query(TABELLE3, TABELLE3_COLUMNS, whereClause,selectionArgs,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            if(c.getInt(2)==0) {
                shopped= false;
            }
            c.moveToNext();
        }
        db.close();
        return shopped;
    }

    /**
     * Universelle Abfrage. Bekommt Rezepte ArrayListe mit einem oder mehreren Rezepten und gibt dazu die Daten aus der Tabelle ShoppingListe
     * @param rezepte

     * @return
     */
    public ArrayList<ShoppingListItem> getShoppinglist(ArrayList<Rezept> rezepte){

        ArrayList <ShoppingListItem> items = new ArrayList();
        SQLiteDatabase db =this.getReadableDatabase();
        ShoppingListItem item;
        Rezept rezept;
        String whereClause =TABELLE3_4+" = ?";
        String[] selectionArgs;
        Iterator iterator = rezepte.iterator();
        while(iterator.hasNext()){

            rezept= (Rezept) iterator.next();
           // System.out.println("Fülle item Array mit "+rezept.getTitel()+" Zutaten");
            selectionArgs=new String[]{String.valueOf(rezept.getId())};

            Cursor c = db.query(TABELLE3, TABELLE3_COLUMNS, whereClause,selectionArgs,null,null,null,null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                item = new ShoppingListItem(c.getInt(0),c.getString(1),c.getInt(2),c.getInt(3));
               // System.out.println("Fülle item Array mit "+item.getZutat());
                items.add(item);
                c.moveToNext();
            }
        }


        db.close();


        return items;
    }

    /**
     * Um auf einmal alle Zutaten eines Rezepts zu aktivieren oder deaktivieren
     * @param rezept_id
     * @param shopped
     */
    public void setRezeptShopped(int rezept_id, int shopped){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABELLE3_3,shopped);
        String whereClause= TABELLE3_4+" = ?";
        String[]whereArgs= {String.valueOf(rezept_id)};

        try{
            db.update(TABELLE3, values, whereClause, whereArgs);

        }
        catch(SQLiteException e){
            e.printStackTrace();
        }
        db.close();
    }






















}
