package com.labsindikatua.fitxatu.laguntzailea;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.labsindikatua.fitxatu.R;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Hizkuntza klasea laguntzeko Adapterra.
 * @author: LAB Sindikatua
 */
public class HizkuntzaAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] maintitle;
    private final String[] subtitle;
    private final ShPreferences pref;

    /**
     * Adapter-a sortzeko metodoa.
     * @param context klase gurasoaren kontestua.
     * @param maintitle izenburuak.
     * @param subtitle azpitituluak.
     */
    public HizkuntzaAdapter(Activity context, String[] maintitle, String[] subtitle) {
        super(context, R.layout.ilara_hizkuntza, maintitle);

        this.context=context;
        this.maintitle=maintitle;
        this.subtitle=subtitle;
        pref = new ShPreferences(context.getApplicationContext());

    }

    /**
     * View-a lortzeko metodoa.
     * @param position zerrendako posizioa.
     * @param ilaraView ilarako View-a.
     * @param parent ViewGroup-a.
     * @return Ilara bateko View-a.
     */
    @NonNull
    public View getView(int position, View ilaraView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();

        //Ilarako View-a lortu
        ilaraView = inflater.inflate(R.layout.ilara_hizkuntza, null, true);

        TextView izenburu = ilaraView.findViewById(R.id.text1);
        TextView azpiIzenburu = ilaraView.findViewById(R.id.text2);

        //Egun erabiltzen dan hizkuntza lortu
        String hizk = pref.lorString("hizkuntza");

        //Ilarako hizkuntza egungo hizkuntza bada, markatu
        if (lorPosHiz(hizk) == position){
            ImageView ikonoView = ilaraView.findViewById(R.id.ilaikon);
            ikonoView.setVisibility(View.VISIBLE);

            izenburu.setTextColor(ContextCompat.getColor(context, R.color.gorri));
            azpiIzenburu.setTextColor(ContextCompat.getColor(context, R.color.gorri));

        }

        //Izenburua eta azpizenburuak ezarri
        izenburu.setText(maintitle[position]);
        azpiIzenburu.setText(subtitle[position]);

        return ilaraView;

    }

    /**
     * Hizkuntza kodea posiziora itzultzeko metodoa.
     * @param hizk hizkuntza.
     * @return Hizkuntzaren posizioa.
     */
    private int lorPosHiz(String hizk){
        int pos;

        switch (hizk) {
            case "eu":
                pos = 0;
                break;
            case "es":
                pos = 1;
                break;
            case "fr":
                pos = 2;
                break;
            default:
                pos = 3;
                break;
        }

        return pos;
    }
}