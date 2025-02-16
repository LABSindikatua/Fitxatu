package com.labsindikatua.fitxatu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.labsindikatua.fitxatu.laguntzailea.ExcelKudeaketa;
import com.labsindikatua.fitxatu.laguntzailea.IrakurProperties;
import com.labsindikatua.fitxatu.laguntzailea.ShPreferences;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Taula kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktTaula extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.akt_taula);
        Toolbar goibarra = findViewById(R.id.goibarra);
        goibarra.setTitleTextAppearance(this, R.style.FjallaoneToobar);
        setSupportActionBar(goibarra);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.zerrenda);

        //Hizkuntza hasieratu
        hasHizkuntza();
        checkHizkuntza();

        //Iragazkia bete
        Spinner dropdown = findViewById(R.id.iragazkia);
        String[] items = new String[]{getString(R.string.guztiak),getString(R.string.egunak),
                getString(R.string.asteak), getString(R.string.urteak)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);

        //Iragazkian hautatze prozesua
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                ezab_tau();

                switch (position) {
                    case 0:
                        kar_tau_guztiak();
                        break;
                    case 1:
                        kar_tau_egunak();
                        break;
                    case 2:
                        kar_tau_asteak();
                        break;
                    default:
                        kar_tau_urte();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        // Garbiketa botoia kudeatu
        ImageView garbi_boto = findViewById(R.id.irugarbitu);
        garbi_boto.setOnClickListener(v -> new AlertDialog.Builder(AktTaula.this)
                .setTitle(getString(R.string.garb_err_izbu))
                .setMessage(getString(R.string.garb_err_testu))
                .setPositiveButton(R.string.bai, (dialog, which) -> {
                    DBKudeaketa db = new DBKudeaketa(AktTaula.this);
                    db.garbitu();
                    TableLayout taula_osoa = findViewById(R.id.taulaosoa);
                    int ler_kont = taula_osoa.getChildCount();
                    if (ler_kont > 1) {
                        taula_osoa.removeViews(1, ler_kont - 1);
                    }
                })
                .setNegativeButton(R.string.ez, (dialog, which) -> {})
                .setIcon(R.drawable.arrisku)
                .show());


    }

    /**
     * Erregistro guztiak kargatzeko metodoa.
     */
    public void kar_tau_guztiak(){

        DBKudeaketa db = new DBKudeaketa(this);
        Cursor kurtsore = db.irak_guztiak();
        TextView extraIzen = findViewById(R.id.extraizen);
        extraIzen.setVisibility(View.GONE);
        ImageView garbitu = findViewById(R.id.irugarbitu);
        garbitu.setVisibility(View.VISIBLE);

        if (kurtsore.moveToFirst()) {
            do {
                String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("DATA"));
                int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("DENBORA"));
                int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("JAI"));

                karg_taula(data, denbora, jai, "guztiak");

            } while (kurtsore.moveToNext());
        }

        //Kurtsorea eta datu basea itxi
        kurtsore.close();
        db.close();
    }

    /**
     * Egunen arabera taula kargatzeko metodoa.
     */
    public void kar_tau_egunak(){

        DBKudeaketa db = new DBKudeaketa(this);
        Cursor kurtsore = db.irak_eguna();
        TextView extraIzen = findViewById(R.id.extraizen);
        extraIzen.setVisibility(View.VISIBLE);
        ImageView garbitu = findViewById(R.id.irugarbitu);
        garbitu.setVisibility(View.GONE);

        if (kurtsore.moveToFirst()) {
            do {
                String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("eguna"));
                int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("JAI"));

                karg_taula(data, denbora, jai, "egun");

            } while (kurtsore.moveToNext());
        }

        //Kurtsorea eta datu basea itxi
        kurtsore.close();
        db.close();
    }

    /**
     * Asteen arabera taula kargatzeko metodoa.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("DefaultLocale")
    public void kar_tau_asteak(){

        DBKudeaketa db = new DBKudeaketa(this);
        Cursor kurtsore = db.irak_astea();
        TextView extraIzen = findViewById(R.id.extraizen);
        extraIzen.setVisibility(View.VISIBLE);
        ImageView garbitu = findViewById(R.id.irugarbitu);
        garbitu.setVisibility(View.GONE);

        if (kurtsore.moveToFirst()) {
            do {
                String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("urte_aste"));
                int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_jai"));

                String[] zati_data = data.split("-");
                int zati_aux = Integer.parseInt(zati_data[1]) + 1;
                String data_berri = getString(R.string.data_asteak,zati_data[0], String.format("%02d", zati_aux));
                karg_taula(data_berri, denbora, jai, "aste");

            } while (kurtsore.moveToNext());
        }

        //Kurtsorea eta datu basea itxi
        kurtsore.close();
        db.close();
    }

    /**
     * Urteen arabera taula kargatzeko metodoa.
     */
    public void kar_tau_urte(){

        DBKudeaketa db = new DBKudeaketa(this);
        Cursor kurtsore = db.irak_urtea();
        TextView extraIzen = findViewById(R.id.extraizen);
        extraIzen.setVisibility(View.VISIBLE);
        ImageView garbitu = findViewById(R.id.irugarbitu);
        garbitu.setVisibility(View.GONE);

        if (kurtsore.moveToFirst()) {
            do {
                String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("urte_urte"));
                int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_jai"));

                karg_taula(data, denbora, jai,"urte");

            } while (kurtsore.moveToNext());
        }

        // Kurtsorea eta datu basea itxi
        kurtsore.close();
        db.close();
    }

    /**
     * Taula ezabatzeko metodoa.
     */
    public void ezab_tau(){

        TableLayout taula_lay = findViewById(R.id.taulaosoa);
        int count = taula_lay.getChildCount();
        for (int i = 1; i < count; i++) {
            View child = taula_lay.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
    }

    /**
     * Taula kargatzeko metodoa.
     * @param data Lerroaren data datua.
     * @param denbora Lerroaren dembora datua.
     * @param jai Lerroaren jai datua.
     * @param taula Kontuan hartu beharreko taula mota.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"DefaultLocale","SimpleDateFormat"})
    public void karg_taula(String data, int denbora, int jai, String taula){

        TableLayout tableLayout = findViewById(R.id.taulaosoa);

        //Orduak kalkulatu
        int minutuGuztiak = denbora / 60;
        int minutuak = minutuGuztiak % 60;
        int orduak = minutuGuztiak / 60;
        String fordenb = String.format("%02d", orduak) + ":" + String.format("%02d", minutuak);

        //Hizkuntzaren arabera data modu batean bistaratu
        ShPreferences pref = new ShPreferences(getBaseContext());
        String hizkuntza = pref.lorString("hizkuntza");

        if (!hizkuntza.equals("eu") && (taula.equals("guztiak") || taula.equals("egun") )) {

            SimpleDateFormat format;
            SimpleDateFormat formatBerri;
            if (taula.equals("guztiak")) {
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formatBerri = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            }else{
                format = new SimpleDateFormat("yyyy-MM-dd");
                formatBerri = new SimpleDateFormat("dd-MM-yyyy");
            }

            try {
                data = formatBerri.format(format.parse(data));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        // Taulako ilara berri bat sortu
        TableRow row = new TableRow(new ContextThemeWrapper(this, R.style.TableRowOrokor));

        // Zutabe bakoitzeko data ezarri
        TextView dataTV = new TextView(this);
        dataTV.setGravity(Gravity.START);
        dataTV.setText(data);
        int hasPaddingDat = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                7, // Padding en dp
                getResources().getDisplayMetrics()
        );
        dataTV.setPaddingRelative(hasPaddingDat, 0, 0, 0);
        row.addView(dataTV);

        TextView denboraTV = new TextView(this);
        denboraTV.setGravity(Gravity.CENTER);
        denboraTV.setText(fordenb);
        int hasPaddingDen = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                7, // Padding en dp
                getResources().getDisplayMetrics()
        );
        denboraTV.setPaddingRelative(hasPaddingDen, 0, 0, 0);
        row.addView(denboraTV);

        TextView jaiTV = new TextView(this);
        jaiTV.setGravity(Gravity.CENTER);
        jaiTV.setText(String.valueOf(jai));
        int hasPaddingJai = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                7, // Padding en dp
                getResources().getDisplayMetrics()
        );
        jaiTV.setPaddingRelative(hasPaddingJai, 0, 0, 0);
        row.addView(jaiTV);

        if (!taula.equals("guztiak")) {
            float extrak = kalkExtrak(denbora, taula);

            //Orduak extrak formateatu
            float ext_minguztiak = extrak / 60;
            int ext_minutuak = (int) (ext_minguztiak % 60);
            int ext_orduak = (int) (ext_minguztiak / 60);
            String ext_emaitza = String.format("%02d", ext_orduak) + ":" + String.format("%02d", ext_minutuak);

            TextView extraTV = new TextView(this);
            extraTV.setGravity(Gravity.CENTER);
            extraTV.setText(ext_emaitza);
            int hasPaddingExt = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    7, // Padding en dp
                    getResources().getDisplayMetrics()
            );
            extraTV.setPaddingRelative(hasPaddingExt, 0, 0, 0);
            row.addView(extraTV);
        }else{
            ImageView zaborBot = new ImageView(this);

            zaborBot.setImageResource(R.drawable.zaztar);
            zaborBot.setPadding(0,0,0,0);

            // Irudia gainean klikatzean lerroa ezabatu eta datu basetik datuak ezabatzen dira.
            String barnData = data;
            zaborBot.setOnClickListener(v -> new AlertDialog.Builder(AktTaula.this)
                    .setTitle(getString(R.string.ezb_err_izbu))
                    .setMessage(getString(R.string.ezb_err_testu, barnData))
                    .setPositiveButton(R.string.bai, (dialog, which) -> {
                        tableLayout.removeView(row);
                        DBKudeaketa db = new DBKudeaketa(AktTaula.this);
                        db.ezaba_lerro(barnData);
                    })
                    .setNegativeButton(R.string.ez, (dialog, which) -> {})
                    .setIcon(R.drawable.arrisku)
                    .show());
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT
            );
            layoutParams.gravity = Gravity.END;
            zaborBot.setLayoutParams(layoutParams);
            row.addView(zaborBot);
        }

        // Ilara taulan sartu
        tableLayout.addView(row);
    }

    /**
     * Menua bistaratzeko erabiltzen den metodoa. (Gainean idatzia)
     * @param menu Bistaratu beharreko menua.
     * @return Ondo egin den ala ez.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.men_taula, menu);
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
        if (id == R.id.deskarga) {
            azter_baimenak();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Baimenak aztertzen dituen metodoa.
     */
    private void azter_baimenak() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            //Baimenak onartuak ez badaude, onarpena eskatu. Bestela, baimenak eskatu
            if ((ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

                ExcelKudeaketa.esportatu(this);

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.baim_desk))
                        .setTitle(getString(R.string.baim_izbu))
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, i) -> {
                            ActivityCompat.requestPermissions(AktTaula.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.ezeztatu), ((dialog, i) -> dialog.dismiss()));
                builder.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }else{
            ExcelKudeaketa.esportatu(this);
        }
    }

    /**
     * Baimenen eskaeraren erantzuna lortzeko metodoa. (Gainean idatzia)
     * @param requestCode Baimenen eskaerak sortutako eskaera kodea.
     * @param permissions Eskatutako baimenen zerrenda.
     * @param grantResults Baimenaren erantzuna.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //Baimenak onartzen badira, mapa kargatu. Horrela ez bada, errorea bistaratu.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                ExcelKudeaketa.esportatu(this);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE ) || !ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
                //Baimenak ez badira onartzen
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.baim_ez_testu))
                        .setTitle(getString(R.string.baim_izbu))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.konf), (dialog, i) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.ezeztatu), ((dialog, i) -> dialog.dismiss()));
                builder.show();
            }else{
                azter_baimenak();
            }
        }else if (requestCode == 2){
            if ((grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                File fitx_helb = Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File directory = new File(fitx_helb.getAbsolutePath());
                ExcelKudeaketa.bidaliNotifikazio(this,directory);

            }
        }
    }
    /**
     * Ordu extrak kalkulatzeko metodoa
     * @param denbora Lan egindako orduak.
     * @param taula Taula mota.
     * @return Egindako ordu extrak.
     */
    private float kalkExtrak(int denbora, String taula){

        ShPreferences pref = new ShPreferences(this);
        String hitzarmena = pref.lorString("hitzarmena");
        float jardu = (float) Integer.parseInt(pref.lorString("jardunaldia")) / 100;

        IrakurProperties iraProp = new IrakurProperties(this);
        Properties prop = iraProp.lorProperties("hitzarmenak.properties");
        float ref_extra = (Float.parseFloat(prop.getProperty(hitzarmena +"_" + taula + "_orduak"))) * jardu;

        int seg_guztiak = (int) (ref_extra * 3600);

        return (float) Math.max(0, denbora - seg_guztiak);
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