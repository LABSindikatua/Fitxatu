package com.labsindikatua.fitxatu.laguntzailea;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties fitxategiak irakurtzeko klasea.
 * @author: LAB Sindikatua
 */
public class IrakurProperties {

    private final Context kontestu;
    private final Properties properties;

    public IrakurProperties(Context context){
        this.kontestu =context;
        properties = new Properties();
    }

    public Properties lorProperties(String file){
        try{
            AssetManager assetManager = kontestu.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        }catch (Exception e){
            System.out.print(e.getMessage());
        }
        return properties;
    }
}
