package com.labsindikatua.fitxatu.laguntzailea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.labsindikatua.fitxatu.R;
import java.util.Locale;
import java.util.Objects;

/**
 * Hizkuntza kudeatzeko Activity-a.
 * @author: LAB Sindikatua
 */
public class AktHizkuntza extends AppCompatActivity {

    private Activity act;
    private ShPreferences pref;

    /**
     * Activity-a sortzeko metodoa. (Gainean idatzia)
     * @param savedInstanceState Istantziaren egoera.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasHizkuntza();
        setContentView(R.layout.akt_hizkuntza);
        androidx.appcompat.widget.Toolbar goibarra = findViewById(R.id.goibarra);
        goibarra.setTitleTextAppearance(this, R.style.FjallaoneToobar);
        setSupportActionBar(goibarra);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.hizk);

        pref = new ShPreferences(getApplicationContext());

        //Activity-a lortu
        act = this;

        //Zerrenda hasieratu
        ListView zerrenda = hasiZerrenda();

        //Aukera bat hautatzean, hizkuntza gorde
        zerrenda.setOnItemClickListener((parent, view, position, id) -> {

            //Aplikazioaren hizkuntza aldatu
            String hizk = lorHizPos(position);
            Configuration config = getResources().getConfiguration();
            Locale hizconf = new Locale(hizk);
            Locale.setDefault(hizconf);
            config.setLocales(new android.os.LocaleList(hizconf));
            this.getApplicationContext().getResources().updateConfiguration(config,getResources().getDisplayMetrics());
            pref.gorde("hizkuntza", hizk);
            pref.gorde("hizkAlda", "1");

            //Activity-a itxi
            act.finish();

            //Konfiguraziorako Activity-a sortu
            Context ctx = act.getBaseContext();
            Intent i = new Intent(ctx, AktHizkuntza.class);
            startActivity(i);
        });
    }


    /**
     * Posizioa hizkuntza kodera itzultzeko metodoa.
     * @param pos posizioa.
     */
    private String lorHizPos(int pos){
        String hizk;

        if (pos == 0) {
            hizk = "eu";
        }else if (pos == 1){
            hizk = "es";
        }else{
            hizk = "fr";
        }

        return hizk;
    }


    /**
     * Bistaratuko den zerrenda hasieratzeko metodoa.
     * @return Sortutako zerrenda.
     */
    private ListView hasiZerrenda(){
        ListView resultsListView = findViewById(R.id.hizkuntzazer);

        String[] maintitle ={
                getBaseContext().getString(R.string.eu),getBaseContext().getString(R.string.es),
                getBaseContext().getString(R.string.fr),
        };

        String[] subtitle ={
                "Euskera","Español",
                "Français",
        };

        HizkuntzaAdapter adapter = new HizkuntzaAdapter(this, maintitle, subtitle);

        resultsListView.setAdapter(adapter);

        return resultsListView;
    }

    /**
     * Back botoia kudeatzen duen metodoa.
     * @param keyCode Sakatutako botoiaren kodea.
     * @param event Gertaera.
     * @return Ondo egin dan ala ez.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //Back botoia sakatu bada
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Hizkuntzaren hasieratzeko metodoa.
     */
    private void hasHizkuntza() {

        ShPreferences pref = new ShPreferences(getApplicationContext());
        String barhiz = pref.lorString("hizkuntza");

        if (barhiz.equals("-")) {
            barhiz = Locale.getDefault().getLanguage();
        }

        Configuration config = getResources().getConfiguration();
        Locale hizconf = new Locale(barhiz);
        Locale.setDefault(hizconf);
        config.setLocales(new android.os.LocaleList(hizconf));
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }
}
