package com.labsindikatua.fitxatu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.labsindikatua.fitxatu.laguntzailea.ShPreferences;
import java.util.Locale;
import java.util.Objects;

/**
 * Erabiltzailearen datuak kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktDatuak extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.akt_datuak);
        Toolbar goibarra = findViewById(R.id.goibarra);
        goibarra.setTitleTextAppearance(this, R.style.FjallaoneToobar);
        setSupportActionBar(goibarra);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.datuak));

        //Hizkuntza hasieratu
        hasHizkuntza();
        checkHizkuntza();

        //Hitzarmenak bete
        Spinner dropdown = findViewById(R.id.hitzarmena);
        String[] items = new String[]{getString(R.string.hitzarmen0)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);

        kargatu();
        kudeBotoia();
    }

    /**
     * Botoiaren kudeaketa daraman metodoa.
     */
    public void kudeBotoia() {

        Button bothasi = findViewById(R.id.botgorde);


        // Botoia kudeatzeko listenerra
        bothasi.setOnClickListener(v -> {

            ShPreferences pref = new ShPreferences(getApplicationContext());

            EditText nan = findViewById(R.id.nan);
            EditText izenabizen = findViewById(R.id.izena);
            EditText jardunaldi = findViewById(R.id.jardun_ehun);
            Spinner hitzarmen = findViewById(R.id.hitzarmena);

            String datNan = String.valueOf(nan.getText());
            String datIzen = String.valueOf(izenabizen.getText());
            String datJard = String.valueOf(jardunaldi.getText());
            int datHitz = hitzarmen.getSelectedItemPosition();

            boolean datHutsik = false;

            if (datNan.isEmpty()){
                datNan = "-";
                datHutsik = true;
            }
            if (datIzen.isEmpty()){
                datIzen = "-";
                datHutsik = true;
            }
            if (datJard.isEmpty()){
                datJard = "-";
                datHutsik = true;
            }

            pref.gorde("nan", datNan);
            pref.gorde("izen", datIzen);
            pref.gorde("jardunaldia", datJard);
            pref.gorde("hitzarmena", String.valueOf(datHitz));

            if (datHutsik){
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dat_huts_izbu))
                        .setMessage(getString(R.string.dat_huts_testu))
                        .setPositiveButton(getString(R.string.bai),null)
                        .setNegativeButton(getString(R.string.ez), (dialog, whichButton) -> finish())
                        .setIcon(R.drawable.arrisku)
                        .show();
            }else{

                Toast.makeText(this, getString(R.string.dat_zuzen), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Gordetako datuak kargatzeko metodoa.
     */
    public void kargatu(){

        ShPreferences pref = new ShPreferences(getApplicationContext());

        if (!pref.lorString("izen").equals("-")){
            EditText izenabizen = findViewById(R.id.izena);
            izenabizen.setText(pref.lorString("izen"));
        }

        if (!pref.lorString("nan").equals("-")){
            EditText nan = findViewById(R.id.nan);
            nan.setText(pref.lorString("nan"));
        }

        if (!pref.lorString("jardunaldia").equals("-")){
            EditText jardunaldi = findViewById(R.id.jardun_ehun);
            jardunaldi.setText(pref.lorString("jardunaldia"));
        }

        if (!pref.lorString("hitzarmena").equals("-")){
            Spinner hitzarmen = findViewById(R.id.hitzarmena);
            hitzarmen.setSelection(Integer.parseInt(pref.lorString("hitzarmena")));
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