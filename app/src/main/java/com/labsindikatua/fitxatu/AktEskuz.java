package com.labsindikatua.fitxatu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.labsindikatua.fitxatu.laguntzailea.ShPreferences;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Eskuz sartutako orduak kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktEskuz extends AppCompatActivity {

    private TextView data;
    private TextView ordsartu;
    private TextView ordatara;
    private Date egundata;
    private int jai = 0;
    private String hizkuntza;

    /**
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @Override
    @SuppressLint("SimpleDateFormat")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hizkuntza hasieratu
        hasHizkuntza();
        checkHizkuntza();

        setContentView(R.layout.akt_eskuz);
        Toolbar goibarra = findViewById(R.id.goibarra);
        goibarra.setTitleTextAppearance(this, R.style.FjallaoneToobar);
        setSupportActionBar(goibarra);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.eskuz);

        ShPreferences pref = new ShPreferences(getBaseContext());
        hizkuntza = pref.lorString("hizkuntza");

        data = findViewById(R.id.editData);
        ordsartu = findViewById(R.id.editsartu);
        ordatara = findViewById(R.id.editatera);
        Button botgor= findViewById(R.id.botgord);


        data.setOnClickListener(view -> dataPickerra());

        ordsartu.setOnClickListener(view -> orduPickerra(ordsartu, 8));

        ordatara.setOnClickListener(view -> orduPickerra(ordatara, 15));

        String pattern;
        if (hizkuntza.equals("eu")) {
            pattern = "yyyy/MM/dd";
        }else{
            pattern = "dd/MM/yyyy";
        }
        DateFormat df = new SimpleDateFormat(pattern);
        egundata = Calendar.getInstance().getTime();
        data.setText(df.format(egundata));

        botgor.setOnClickListener(view -> gorde());
    }

    /**
     * DataPickerra kudeatzen duen metodoa.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SetTextI18n: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void dataPickerra(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(egundata);
        int eguna = calendar.get(Calendar.DAY_OF_MONTH);
        int hilea = calendar.get(Calendar.MONTH);
        int urtea = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog;
        if (hizkuntza.equals("eu")) {
            datePickerDialog = new DatePickerDialog(this, R.style.DialogTheme ,
                    (datePicker, year, month, day) -> data.setText(year + "/"+ String.format("%02d", month + 1) +
                            "/"+ String.format("%02d", day)), urtea, hilea, eguna);
        }else{
            datePickerDialog = new DatePickerDialog(this, R.style.DialogTheme ,
                    (datePicker, year, month, day) -> data.setText(String.format("%02d", day) + "/"+ String.format("%02d", month + 1) +
                            "/"+ year ), urtea, hilea, eguna);
        }

        datePickerDialog.show();
    }


    /**
     * TimePickerra kudeatzen duen metodoa.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SetTextI18n: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * @param tv Ordua bistaratuko den TextView-a.
     * @param ord Defektuzko ordua.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SetTextI18n: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void orduPickerra(TextView tv, int ord){

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.DialogTheme,
                (timePicker, hour, minute) -> tv.setText(String.format("%02d", hour) + ":"+
                        String.format("%02d", minute)), ord, 0, true);
        timePickerDialog.show();
    }

    /**
     * Ezarritako datetan akatsik dagoen ala ez bilatzen duen metodoa.
     * @return boolean Errorea egon den ala ez.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    private boolean erroreKud() {

        Date dateHas = bihurtuHas();
        Date dateBuk = bihurtuBuk(dateHas);

        int bada_hasiera = dataBarruan(dateHas, null);
        int bada_bukaera = dataBarruan(dateBuk, null);
        int bada_osoa = dataBarruan(dateHas, dateBuk);

        if (dateHas.after(egundata)) {
            Toast.makeText(this, R.string.sartze_heldu, Toast.LENGTH_SHORT).show();
            return false;
        } else if (dateBuk.after(egundata)) {
            Toast.makeText(this, R.string.ateratze_heldu, Toast.LENGTH_SHORT).show();
            return false;
        } else if (bada_hasiera == 1 || bada_bukaera == 1 || bada_osoa == 1) {
            Toast.makeText(this, R.string.bada_erre, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Data bat erregistroko hasiera eta bukaeraren daten barruan dagoen jakiteko metodoa.
     * @param hasiDat Konparatzeko hasierako data.
     * @param bukaDat Konparatzeko bukaerako data
     * @return int Barruan badago 1, kanpoan bada 0.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    private int dataBarruan(Date hasiDat, Date bukaDat){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);

        String kontsulta;
        if (bukaDat == null) {
            kontsulta = "SELECT EXISTS (SELECT 1 FROM ostalaritza WHERE datetime('"
                    + df.format(hasiDat) + "') BETWEEN DATA AND datetime(DATA, '+' || DENBORA ||" +
                    " ' seconds')) AS emaitza";
        }else{
            kontsulta = "SELECT EXISTS (SELECT 1 FROM ostalaritza WHERE DATA " +
                    "BETWEEN datetime('" + df.format(hasiDat) + "') AND datetime('"
                    + df.format(bukaDat) + "')) AS emaitza";
        }
        DBKudeaketa db = new DBKudeaketa(this);
        Cursor kurtsore = db.egiaztapenak(kontsulta);

        int bada_gainjartzea = -1;
        if (kurtsore.moveToFirst()) {
            bada_gainjartzea = kurtsore.getInt(0);
        }

        //Kurtsorea eta datu basea itxi
        kurtsore.close();
        db.close();

        return bada_gainjartzea;
    }

    /**
     * Erregistro data eta ordua gordetzen duen metodoa.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    public void gorde(){

        if (erroreKud()) {
            CheckBox jaielem = findViewById(R.id.chekjai);
            if (jaielem.isChecked()) {
                jai = 1;
            }

            //Datu basean datuak gorde
            try (DBKudeaketa db = new DBKudeaketa(this)) {

                String pattern = "yyyy-MM-dd HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);

                Date dateHas = bihurtuHas();
                Date dateBuk = bihurtuBuk(dateHas);

                long diffInMs = dateBuk.getTime() - dateHas.getTime();
                int diffInSec = (int) TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                db.gorde(df.format(dateHas), diffInSec, jai, "Eskuz");

                Toast.makeText(this, R.string.zuzen_gorde, Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this, R.string.db_errore, Toast.LENGTH_SHORT).show();
            }


        }

    }

    /**
     * Hasierako dataren formatua aldatzen duen metodoa.
     * @return Date Lorturtako data formatu berria.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    private Date bihurtuHas(){

        String hasData = data.getText() + " " + ordsartu.getText();

        SimpleDateFormat format;
        if (hizkuntza.equals("eu")){
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        }else{
            format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        }

        Date dateHas;

        try {
            dateHas = format.parse(hasData);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return dateHas;
    }

    /**
     * Bukaerako dataren formatua aldatzen duen metodoa.
     * @param hasiData Hesierako data formatu berrian.
     * @return Date Lorturtako data formatu berria.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    private Date bihurtuBuk(Date hasiData){

        String bukData = data.getText() + " " + ordatara.getText();

        SimpleDateFormat format;
        if (hizkuntza.equals("eu")){
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        }else{
            format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        }

        Date dateBuk;
        Calendar k = Calendar.getInstance();
        try {
            dateBuk = format.parse(bukData);
            assert dateBuk != null;
            k.setTime(dateBuk);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (hasiData.after(dateBuk)) {
            k.add(Calendar.DATE, 1);
            String bukBerri = format.format(k.getTime());
            try {
                dateBuk = format.parse(bukBerri);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return dateBuk;
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