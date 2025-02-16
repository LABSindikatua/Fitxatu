package com.labsindikatua.fitxatu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


/**
 * Datu basea kudeatzeko klasea.
 * @author: LAB Sindikatua
 */
public class DBKudeaketa extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Ostalaritza.db";
    public static final String TABLE_NAME = "ostalaritza";

    /**
     * Klasea sortzeko metodoa.
     * @param context Kontestua.
     */
    public DBKudeaketa(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * Klasea hasieratzeko metodoa.
     * @param db Datu basea.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(DATA DATETIME , DENBORA INT, JAI INT, KOOR VARCHAR(50))");
    }

    /**
     * Taula sortzeko metodoa.
     * @param db Datu basea.
     * @param oldVersion Bertsio zaharra.
     * @param newVersion Bertsioa berria.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    /**
     * Datu basean erregistroak gordetzeko metodoa.
     * @param data Erregistroaren data.
     * @param denbora Erregistroaren iraupena.
     * @param jai Erregistroaren data jai eguna den ala ez.
     * @param koor Erregistroa egin den koordenatuak.
     */
    public void gorde(String data, int denbora, int jai, String koor){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("DATA", data);
        values.put("DENBORA", denbora);
        values.put("JAI", jai);
        values.put("KOOR", koor);

        db.insert(TABLE_NAME, null, values);

    }

    /**
     * Erregistro guztiak lortzeko metodoa.
     * @return Datu basearen kurtsorea.
     */
    public Cursor irak_guztiak() {
        String[] columns = new String[] { "DATA", "DENBORA", "JAI", "KOOR" };
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor kurtsorea = db.query(TABLE_NAME, columns, null, null, null, null, "DATA DESC");

        if (kurtsorea != null) {
            kurtsorea.moveToFirst();
        }
        return kurtsorea;
    }

    /**
     * Egunetako erregistroak lortzeko metodoa.
     * @return Datu basearen kurtsorea.
     */
    public Cursor irak_eguna() {

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(DENBORA) AS total_denb, strftime('%Y-%m-%d', DATA) AS eguna, " +
                "MAX(JAI) AS JAI " +
                "FROM ostalaritza " +
                "GROUP BY strftime('%Y-%m-%d', eguna) " +
                "ORDER BY strftime('%Y-%m-%d', eguna) DESC";
        Cursor kurtsorea = db.rawQuery(query, null);

        if (kurtsorea != null) {
            kurtsorea.moveToFirst();
        }
        return kurtsorea;
    }

    /**
     * Egun konkretu baten ordu kopuaura jakiteko metodoa.
     * @param eguna Bilatutako eguna
     * @return Ordu kopurua.
     */
    public int orduak_eguna(String eguna) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(DENBORA) AS total_denb, strftime('%Y-%m-%d', DATA) AS eguna " +
                "FROM ostalaritza " +
                "WHERE strftime('%Y-%m-%d', DATA) = '" + eguna + "'";
        Cursor kurtsorea = db.rawQuery(query, null);

        int denbora;
        if (kurtsorea.moveToFirst()) {
            denbora = kurtsorea.getInt(kurtsorea.getColumnIndexOrThrow("total_denb"));
        }else{
            denbora = 0;
        }

        kurtsorea.close();

        return denbora;
    }

    /**
     * Asteetako erregistroak lortzeko metodoa.
     * @return Datu basearen kurtsorea.
     */
    public Cursor irak_astea() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%Y-%W', urte_aste) AS urte_aste, " +
                "SUM(total_denb) AS total_denb, " +
                "SUM(total_jai) AS total_jai " +
                "FROM ( " +
                "SELECT strftime('%Y-%m-%d', DATA) AS urte_aste, " +
                "SUM(DENBORA) AS total_denb, " +
                "MAX(JAI) AS total_jai " +
                "FROM ostalaritza " +
                "GROUP BY strftime('%Y-%m-%d', DATA) " +
                "ORDER BY strftime('%Y-%m-%d', DATA) " +
                ") AS egunak " +
                "GROUP BY strftime('%Y-%W', urte_aste) " +
                "ORDER BY strftime('%Y-%W', urte_aste) DESC";
        Cursor kurtsorea = db.rawQuery(query, null);
        if (kurtsorea != null) {
            kurtsorea.moveToFirst();
        }

        return kurtsorea;
    }

    /**
     * Aste konkretu baten ordu kopuaura jakiteko metodoa.
     * @param astea Bilatutako astea
     * @return Ordu kopurua.
     */
    public int orduak_astea(String astea) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%Y-%W', urte_aste) AS urte_aste, " +
                "SUM(total_denb) AS total_denb " +
                "FROM ( " +
                "SELECT strftime('%Y-%m-%d', DATA) AS urte_aste, " +
                "SUM(DENBORA) AS total_denb, " +
                "MAX(JAI) AS total_jai " +
                "FROM ostalaritza " +
                "GROUP BY strftime('%Y-%m-%d', DATA) " +
                "ORDER BY strftime('%Y-%m-%d', DATA)" +
                ") AS egunak "+
                "WHERE strftime('%Y-%W', urte_aste) = '" + astea + "'" +
                "GROUP BY strftime('%Y-%W', urte_aste) " +
                "ORDER BY strftime('%Y-%W', urte_aste)";
        Cursor kurtsorea = db.rawQuery(query, null);

        int denbora;
        if (kurtsorea.moveToFirst()) {
            denbora = kurtsorea.getInt(kurtsorea.getColumnIndexOrThrow("total_denb"));
        }else{
            denbora = 0;
        }

        kurtsorea.close();

        return denbora;
    }

    /**
     * Urteetako erregistroak lortzeko metodoa.
     * @return Datu basearen kurtsorea.
     */
    public Cursor irak_urtea() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%Y', urte_urte) AS urte_urte, " +
                "SUM(total_denb) AS total_denb, " +
                "SUM(total_jai) AS total_jai " +
                "FROM ( " +
                "SELECT strftime('%Y-%m-%d', DATA) AS urte_urte, " +
                "SUM(DENBORA) AS total_denb, " +
                "MAX(JAI) AS total_jai " +
                "FROM ostalaritza " +
                "GROUP BY strftime('%Y-%m-%d', DATA) " +
                "ORDER BY strftime('%Y-%m-%d', DATA) " +
                ") AS egunak " +
                "GROUP BY strftime('%Y', urte_urte) " +
                "ORDER BY strftime('%Y', urte_urte) DESC";
        Cursor kurtsorea = db.rawQuery(query, null);
        if (kurtsorea != null) {
            kurtsorea.moveToFirst();
        }
        return kurtsorea;
    }

    /**
     * Urte konkretu baten ordu kopuaura jakiteko metodoa.
     * @param urtea Bilatutako urtea
     * @return Ordu kopurua.
     */
    public int orduak_urtea(String urtea) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%Y', urte_aste) AS urte_aste, " +
                "SUM(total_denb) AS total_denb " +
                "FROM ( " +
                "SELECT strftime('%Y-%m-%d', DATA) AS urte_aste, " +
                "SUM(DENBORA) AS total_denb, " +
                "MAX(JAI) AS total_jai " +
                "FROM ostalaritza " +
                "GROUP BY strftime('%Y-%m-%d', DATA) " +
                "ORDER BY strftime('%Y-%m-%d', DATA)" +
                ") AS egunak "+
                "WHERE strftime('%Y', urte_aste) = '" + urtea + "'" +
                "GROUP BY strftime('%Y', urte_aste) " +
                "ORDER BY strftime('%Y', urte_aste)";
        Cursor kurtsorea = db.rawQuery(query, null);

        int denbora;
        if (kurtsorea.moveToFirst()) {
            denbora = kurtsorea.getInt(kurtsorea.getColumnIndexOrThrow("total_denb"));
        }else{
            denbora = 0;
        }

        kurtsorea.close();

        return denbora;
    }

    /**
     * Egiaztapenak egiteko erabilitako metodoa.
     * @param kontsulta Exekutatu beharreko kontsulta.
     * @return Datu basearen kurtsorea.
     */
    public Cursor egiaztapenak(String kontsulta){

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(kontsulta, null);
    }

    /**
     * Erregistroko lerro bat ezabatzeko metodoa.
     * @param data Erregistroaren data.
     */
    public void ezaba_lerro(String data) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "DATA" + " = ?", new String[]{data});
        db.close();
    }

    /**
     * Erregistro guztiak ezabatazeko metodoa.
     */
    public void garbitu() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
