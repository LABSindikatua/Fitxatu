package com.labsindikatua.fitxatu.laguntzailea;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preference-ak kudeatzeko klasea.
 * @author: LAB Sindikatua
 */
public class ShPreferences {

    private final SharedPreferences sharedPref;

    public ShPreferences(Context context){
        this.sharedPref = context.getSharedPreferences("fitxatu", Context.MODE_PRIVATE);
    }

    /**
     * Preferencean datuak gordetzeko metodoa.
     * @param key Gordetako datuaren gakoa.
     * @param value Gorde beharreko balioa.
     */
    public void gorde(String key, String value) {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Preferenc-ean gordetako datua lortzeko metodoa.
     * @param key Gordetako datuaren gakoa.
     * @return Preference-an gordetako datua.
     */
    public String lorString(String key){

        return sharedPref.getString(key, "-");
    }
}
