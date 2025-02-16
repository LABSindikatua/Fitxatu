package com.labsindikatua.fitxatu;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import com.labsindikatua.fitxatu.laguntzailea.ShPreferences;

/**
 * Hasierako funtzioak kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktHasiera extends Activity {

    private static final long SPLASH_SCREEN_DELAY = 2000;

    /**
     * Activity-a sortzeko metodoa. (Gainean idatzia)
     * @param savedInstanceState Istantziaren egoera.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Pantaila horizontalean jarri
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        //Hizkuntza hasieratu
        hasHizkuntza();
        checkHizkuntza();

        //Goiko barra ezkutatu
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.akt_hasiera);

        //Jakinarazpena aztertu
        jakinAzter();

        //Bertsioa ezarri
        String ber;

        try {
            PackageInfo pInfo = getBaseContext().getPackageManager().
                    getPackageInfo(getBaseContext().getPackageName(), 0);
            ber = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            ber = "??";
        }
        TextView bertsioa= findViewById(R.id.bertsioText);
        String berText = getString(R.string.bertsioa) + ber;
        bertsioa.setText(berText);

        ShPreferences pref = new ShPreferences(getApplicationContext());

        //Erabiltzailearen datuak ezarrita badaude
        if (pref.lorString("izen").equals("-")){
            Intent mainIntent = new Intent().setClass(
                    AktHasiera.this, AktDatuak.class);
            startActivityForResult(mainIntent, 1);

        }else {
            hasieratu();
        }

    }

    /**
     * Activity-a hasieratzeko metodoa.
     */
    private void hasieratu(){

        //Activity-a bistaratu
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                Intent mainIntent = new Intent().setClass(
                        AktHasiera.this, AktZenbatzaile.class);
                startActivity(mainIntent);

                finish();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }


    /**
     * Beste Activity batetik bueltatzeko kudeatzeko metodoa
     * @param requestCode Activity-aren eskaera kodea.
     * @param resultCode Activity-aren bukaeraren emaitzaren kodea.
     * @param data Jatorrizko Intent-a
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            Intent mainIntent = new Intent().setClass(
                    AktHasiera.this, AktZenbatzaile.class);
            startActivity(mainIntent);

            finish();
        }
    }

    /**
     * Jakinarazpenak aztertu eta, beharrezkoa bada, hasieratzeko metodoa.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    private void jakinAzter(){

        //Egungo datuak lortu
        Calendar calendar = Calendar.getInstance();
        String urtea = String.valueOf(calendar.get(Calendar.YEAR));
        String astea = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));

        Date date = calendar.getTime();
        String pattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(pattern);
        String eguna = df.format(date);

        ShPreferences pref = new ShPreferences(getBaseContext());

        if (!pref.lorString("jakinUrte").equals(urtea) && !pref.lorString("jakinUrte").equals("-")){
            pref.gorde("jakinUrte", "-");
        }

        if (!pref.lorString("jakinAste").equals(astea) && !pref.lorString("jakinAste").equals("-")){
            pref.gorde("jakinAste", "-");
        }

        if (!pref.lorString("jakinEguna").equals(eguna) && !pref.lorString("jakinEguna").equals("-")){
            pref.gorde("jakinEguna", "-");
        }
    }

    /**
     * Hizkuntza berria ezarri denean hizkuntza aldatzeko metodoa.
     */
    private void checkHizkuntza() {

        ShPreferences pref = new ShPreferences(getApplicationContext());
        String hiz = pref.lorString("hizkuntza");
        String barhiz = Locale.getDefault().getLanguage();

        if (!(hiz.equals(barhiz))) {
            Configuration config = getResources().getConfiguration();
            Locale hizconf = new Locale(hiz);
            Locale.setDefault(hizconf);
            config.setLocales(new android.os.LocaleList(hizconf));
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    /**
     * Hizkuntzaren hasierako analisia egiteko metodoa.
     */
    private void hasHizkuntza() {

        ShPreferences pref = new ShPreferences(getApplicationContext());

        String hiz = pref.lorString("hizkuntza");
        String barhiz = Locale.getDefault().getLanguage();

        if (hiz.equals("-")) {

            if (barhiz.equals("es") || barhiz.equals("eu") || barhiz.equals("fr")) {
                pref.gorde("hizkuntza", barhiz);

            } else {
                pref.gorde("hizkuntza", "eu");
            }
        }

    }
}
