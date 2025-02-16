package com.labsindikatua.fitxatu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.Menu;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.labsindikatua.fitxatu.laguntzailea.AktHizkuntza;
import com.labsindikatua.fitxatu.laguntzailea.AzterHitzarmena;
import com.labsindikatua.fitxatu.laguntzailea.ShPreferences;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

/**
 * Zenbatzailea kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktZenbatzaile extends AppCompatActivity implements LocationListener {
    public int counter = 1;
    public Date data = null;
    public int jai = 0;
    public Timer T;
    private double longitude = 0;
    private double latitude = 0;
    private UUID workID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hizkuntza hasieratu
        hasHizkuntza();
        checkHizkuntza();

        setContentView(R.layout.akt_zenbatzaile);
        Toolbar goibarra = findViewById(R.id.goibarra);
        TextView goiTest = findViewById(R.id.goi_test);
        goibarra.setTitleTextAppearance(this, R.style.FjallaoneToobar);
        setSupportActionBar(goibarra);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        goiTest.setText("Fitxatu");

        sorBotoia();

        gpsKudeatu();

    }

    //Grafikoak sortzeko metodoak

    /**
     * Botoiaren kudeaketa daraman metodoa.
     */
    public void sorBotoia() {

        Button bothasi = findViewById(R.id.bothasi);
        Button botesku = findViewById(R.id.botesku);

        // Botoia kudeatzeko listenerra
        bothasi.setOnClickListener(v -> {

            String textbotoi = getString(R.string.hasi);
            if ((bothasi.getText()).equals(textbotoi)) {
                //GPS gaitua dagoen. Badago, dana normal, ez badago abisua eman.
                if(gpsDago()) {
                    //Koordenadak badauden. Badago, normal, ez badago itxarotea eskatu
                    if (longitude != 0) {
                        ShPreferences pref = new ShPreferences(getBaseContext());
                        extraDialog();
                        if (data == null) {
                            data = Calendar.getInstance().getTime();
                        }

                        pref.gorde("koor", latitude + ";" + longitude);
                        kudKontadore();
                        bothasi.setText(R.string.bukatu);
                    }else{
                        new AlertDialog.Builder(AktZenbatzaile.this)
                                .setTitle(R.string.gpskoka)
                                .setMessage(R.string.gpskokam)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }else {
                    //Galdetu GPS nahi duen. Nahi badu, GPS gaitu, ez badu nahi normala.
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.gpsbaim)
                            .setMessage(R.string.gpsbaimm)
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);ActivityCompat.requestPermissions(AktZenbatzaile.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                String textbotoi1 = getString(R.string.hasi);
                                if ((bothasi.getText()).equals(textbotoi1)) {
                                    extraDialog();
                                    if (data == null) {
                                        data = Calendar.getInstance().getTime();
                                    }
                                    ShPreferences pref = new ShPreferences(getBaseContext());
                                    pref.gorde("koor", "-");
                                    kudKontadore();
                                    bothasi.setText(R.string.bukatu);

                                } else {
                                    bothasi.setText(R.string.hasi);
                                    T.cancel();
                                    WorkManager.getInstance(this).cancelWorkById(workID);
                                    gorde();
                                }
                            })
                            .setIcon(R.drawable.arrisku)
                            .show();
                }
            } else {
                bothasi.setText(R.string.hasi);
                T.cancel();
                WorkManager.getInstance(this).cancelWorkById(workID);
                gorde();
            }
        });

        // Eskuz activity-a hasieratzeko botoiaren listenerra
        botesku.setOnClickListener(v -> {
            Intent intent = new Intent(AktZenbatzaile.this, AktEskuz.class);
            startActivity(intent);
        });

        // Jai egunaren botoia kudeatzeko listenerra
        CheckBox jaielem = findViewById(R.id.chekjai);
        jaielem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (jaielem.isChecked()) {
                jai = 1;
            } else {
                jai = 0;
            }
        });
    }

    /**
     * Menua bistaratzeko erabiltzen den metodoa. (Gainean idatzia)
     * @param menu Bistaratu beharreko menua.
     * @return Ondo egin den ala ez.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.men_zenbatzaile, menu);
        return true;
    }

    /**
     * Menuan hautatutako aukeraren arabera Activitya bistaratzeko metodoa. (Gainean idatzia)
     * @param item Hautatutako aukera.
     * @return Ondo egin den ala ez.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Taula sakatu bada.
        if (id == R.id.taula) {
            Intent i = new Intent(this, AktTaula.class);
            startActivity(i);
        } else if (id == R.id.hizkuntza) {
            Intent i = new Intent(this, AktHizkuntza.class);
            startActivityForResult(i, 1);

        } else if (id == R.id.datuak) {
            Intent i = new Intent(this, AktDatuak.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


    //Kudeaketa metodoak

    /**
     * Zenbatzailea kudeatzeko metodoa.
     * SetTextI18n: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    public void kudKontadore() {

        ShPreferences pref = new ShPreferences(getBaseContext());

        // Urteko jakinarazpena ez bada bidali
        if (pref.lorString("jakinUrte").equals("-")) {
            //Orduen azterketarako datuak lortu eta bidali
            Calendar calendar = Calendar.getInstance();
            int egUrtea = calendar.get(Calendar.YEAR);
            int egAstea = calendar.get(Calendar.WEEK_OF_YEAR);

            Date date = calendar.getTime();
            String pattern = "yyyy-MM-dd";
            DateFormat df = new SimpleDateFormat(pattern);
            String gaur = df.format(date);

            DBKudeaketa db = new DBKudeaketa(this);
            int ordEguna = db.orduak_eguna(gaur);
            int ordAstea = db.orduak_astea(egUrtea + "-" + egAstea);
            int ordUrtea = db.orduak_urtea(String.valueOf(egUrtea));

            pattern = "yyyy-MM-dd HH:mm:ss";
            df = new SimpleDateFormat(pattern);

            Data bidData = new Data.Builder()
                    .putInt("ordEguna", ordEguna)
                    .putInt("ordAstea", ordAstea)
                    .putInt("ordUrtea", ordUrtea)
                    .putString("data", df.format(data))
                    .build();

            //15 minuturo orduak aztertzeko
            WorkManager.getInstance(this).cancelAllWork();
            PeriodicWorkRequest azterordu = new PeriodicWorkRequest.Builder(AzterHitzarmena.class, 15, TimeUnit.MINUTES)
                    .setInputData(bidData)
                    .addTag("jakinHitzar")
                    .build();

            workID = azterordu.getId();
            WorkManager.getInstance(this).enqueue(azterordu);
        }


        TextView textView = findViewById(R.id.textData);
        T = new Timer();

        T.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    NumberFormat f = new DecimalFormat("00");
                    long hour = (counter * 1000L / 3600000) % 24;
                    long min = (counter * 1000L / 60000) % 60;
                    long sec = (counter * 1000L / 1000) % 60;
                    textView.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                    counter++;
                });
            }
        }, 1000, 1000);
    }


    /**
     * Fitxaketa gordetzeko metodoa.
     * SetTextI18n: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    public void gorde() {
        TextView textView = findViewById(R.id.textData);
        textView.setText("00:00:00");

        CheckBox jaielem = findViewById(R.id.chekjai);
        if (jaielem.isChecked()) {
            jai = 1;
        }

        ShPreferences pref = new ShPreferences(getBaseContext());

        //Datu basean datuak gorde
        DBKudeaketa db = new DBKudeaketa(this);

        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        db.gorde(df.format(data), counter, jai, pref.lorString("koor"));

        counter = 1;
        data = null;
        jai = 0;
        pref.gorde("data", "-");
        pref.gorde("gmt", "-");
        pref.gorde("jai", "-");
        pref.gorde("koor", "-");

        Toast.makeText(this, R.string.zuzen_gorde, Toast.LENGTH_SHORT).show();

    }

    /**
     * Activity-a berriro hastean, zenbaketaren egoera kudatzeko metodoa.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    public void ber_egoera() {

        // Datuak berreskuratu
        ShPreferences pref = new ShPreferences(getBaseContext());
        String gord_data = pref.lorString("data");
        String gord_gmt = pref.lorString("gmt");


        // Zenbaketarik gordeta dagoen ala ez
        if (!gord_data.equals("-")) {
            int gord_jai = Integer.parseInt(pref.lorString("jai"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT+" + gord_gmt));
            Date data_berri;
            try {
                data_berri = format.parse(gord_data);
            } catch (ParseException e) {
                data_berri = null;
            }

            // Jai eguna kudeatu
            CheckBox jaielem = findViewById(R.id.chekjai);
            if (gord_jai == 1) {
                jaielem.setChecked(true);
            }

            // Momentuko GMTa lortu
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                    Locale.getDefault());
            Date data_orain = calendar.getTime();
            data = data_berri;

            // Data ezberdintasuna kalkulatu segundutan
            long milisegunduak = data_orain.getTime() - Objects.requireNonNull(data_berri).getTime();
            counter = (int) TimeUnit.MILLISECONDS.toSeconds(milisegunduak);

            // Zenbatzailea hasieratu
            // Zenbatzailea aktibatua dagoen ala ez
            if (T == null) {
                Button bothasi = findViewById(R.id.bothasi);
                kudKontadore();
                bothasi.setText(R.string.bukatu);
            }
        }
    }



    /**
     * Beste Activity batetik bueltatzeko kudeatzeko metodoa
     * @param requestCode Activity-aren eskaera kodea.
     * @param resultCode Activity-aren bukaeraren emaitzaren kodea.
     * @param data Jatorrizko Intent-a
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Hizkuntza aldatzen bada
        if (requestCode == 1) {
            ShPreferences pref = new ShPreferences(getApplicationContext());
            if (pref.lorString("hizkAlda").equals("1")) {
                pref.gorde("hizkAlda", "0");
                finish();
                startActivity(getIntent());
            }
        }
    }

    /**
     * Aplikaziotik urtetzean exekutatzen den metodoa. (Gainean idatzia)
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    @Override
    public void onPause() {

        super.onPause();

        // Momentuko ordua lortu
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date data_orain = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        String gmt = date.format(data_orain);
        gmt = gmt.replace("+", "");
        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Data egotekotan momentuko egoera gorde
        if (data != null) {
            ShPreferences pref = new ShPreferences(getBaseContext());
            pref.gorde("data", date.format(data));
            pref.gorde("gmt", gmt);
            pref.gorde("jai", String.valueOf(jai));
        }
    }

    /**
     * Aplikaziora itzultzerakoan exekutatzen den metodoa. (Gainean idatzia)
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Zenbatzailearen egoera
        ber_egoera();
    }


    //GPS-a kudeaketzeko metodoak


    /**
     * GPS-a kudeatzeko metodoa.
     * UseCompatLoadingForDrawables - minSDKVersion 21 baino handiagoa da.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private void gpsKudeatu() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            azterBaimen();
        }else{
            LocationManager locationManager =
                    (LocationManager) getSystemService(LOCATION_SERVICE);

            ImageView gpsIrudi = findViewById(R.id.gpsIrudi);

            if (!gpsDago()) {
                gpsIrudi.setImageDrawable(getResources().getDrawable(R.drawable.gps_ez,
                        getApplicationContext().getTheme()));
            }else{
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5000, 10, this);
                gpsIrudi.setImageDrawable(getResources().getDrawable(R.drawable.gps_agian,
                        getApplicationContext().getTheme()));
            }
        }
    }

    /**
     * Lokalizazioaren aldaketa egiten den bakoitzeko kudeaketa egiten duen metodoa. (Gainean idatzia)
     * UseCompatLoadingForDrawables - minSDKVersion 21 baino handiagoa da.
     * @param lok Momentuko lokalizazioa.
     *
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onLocationChanged(Location lok) {

        longitude = lok.getLongitude();
        latitude = lok.getLatitude();

        ImageView gpsIrudi = findViewById(R.id.gpsIrudi);
        gpsIrudi.setImageDrawable(getResources().getDrawable(R.drawable.gps_bai,
                getApplicationContext().getTheme()));

    }

    /**
     * GPS desgaitu egitzean egin beharrekoa kudeatzen duen metodoa. (Gainean idatzia)
     * UseCompatLoadingForDrawables - minSDKVersion 21 baino handiagoa da.
     * @param provider Lokalizazio hornitzaile mota.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        ImageView gpsIrudi = findViewById(R.id.gpsIrudi);

        longitude = 0;
        latitude = 0;

        gpsIrudi.setImageDrawable(getResources().getDrawable(R.drawable.gps_ez,
                getApplicationContext().getTheme()));
    }

    /**
     * GPS gaitu egitzean egin beharrekoa kudeatzen duen metodoa. (Gainean idatzia)
     * UseCompatLoadingForDrawables - minSDKVersion 21 baino handiagoa da.
     * @param provider Lokalizazio hornitzaile mota.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        ImageView gpsIrudi = findViewById(R.id.gpsIrudi);

        gpsIrudi.setImageDrawable(getResources().getDrawable(R.drawable.gps_agian,
                getApplicationContext().getTheme()));

    }

    /**
     * Kokapenaren lortzeko beharrezkoak diren baimenen baieztapena aztertu.
     */
    private void azterBaimen() {

        //Baimenak onartuak ez badaude, onarpena eskatu. Bestela, baimenak eskatu
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }else{
            new AlertDialog.Builder(this)
                    .setTitle(R.string.gpsbaim)
                    .setMessage(R.string.gpsbaime)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> ActivityCompat.requestPermissions(AktZenbatzaile.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1))
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.arrisku)
                    .show();
        }
    }
    /**
     * Baimenen eskaeraren erantzuna lortzeko metodoa. (Gainean idatzia)
     * @param requestCode Baimenen eskaerak sortutako eskaera kodea.
     * @param permissions Eskatutako baimenen zerrenda.
     * @param grantResults Baimenaren erantzuna.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Baimenak onartzen badira, mapa kargatu. Horrela ez bada, errorea bistaratu.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                //Baimenak ez badira onartzen
                new AlertDialog.Builder(this)
                        .setTitle(R.string.gpsbaim)
                        .setMessage(R.string.gpsbaime)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> ActivityCompat.requestPermissions(AktZenbatzaile.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1))
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.arrisku)
                        .show();

            }else{
                gpsKudeatu();
            }
        }
    }

    /**
     * GPS gaitua dagoen jakiteko metodoa.
     * @return GPS gaitua dagoen ala ez.
     */
    public boolean gpsDago(){
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }

    /**
     * Ordu extretetan edo maximoetan dagoena abisua kudeatzeko metodoa.
     */
    public void extraDialog(){
        ShPreferences pref = new ShPreferences(getBaseContext());
        if (!pref.lorString("jakinUrte").equals("-") ||
                !pref.lorString("jakinUrte").equals("-") ||
                !pref.lorString("jakinUrte").equals("-")){
            String mezua;
            if (!pref.lorString("jakinUrte").equals("-")){
                mezua = getString(R.string.extr_hasi_urte);
            }else{
                mezua = getString(R.string.extra_hasi_extra);
            }
            new AlertDialog.Builder(AktZenbatzaile.this)
                    .setTitle(getString(R.string.extra_hasi_izbu))
                    .setMessage(mezua)
                    .setPositiveButton("OK",null)
                    .setIcon(R.drawable.arrisku)
                    .show();
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