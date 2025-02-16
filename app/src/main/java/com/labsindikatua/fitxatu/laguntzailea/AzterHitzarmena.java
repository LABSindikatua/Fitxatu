package com.labsindikatua.fitxatu.laguntzailea;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.labsindikatua.fitxatu.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Emandako orduak kudeatzeko Worker-a.
 * @author: LAB Sindikatua
 */
public class AzterHitzarmena extends Worker {

    Context ktst;
    String hitzarmena;
    float jardu;
    Date data;

    /**
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    public AzterHitzarmena(@NonNull Context kanpoKtst, @NonNull WorkerParameters workerParams) {
        super(kanpoKtst, workerParams);
        ShPreferences pref = new ShPreferences(kanpoKtst);
        hitzarmena = pref.lorString("hitzarmena");
        jardu = (float) Integer.parseInt(pref.lorString("jardunaldia")) / 100;
        String testData = getInputData().getString("data");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            data = format.parse(testData);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        ktst = kanpoKtst;
    }

    /**
     * Work-ak egin beharreko funtzioa.
     * @return Result Exekuzioa ondo egin bada
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public Result doWork() {

        // Parametroak hasieratu
        ShPreferences pref = new ShPreferences(ktst);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        String urtea = String.valueOf(calendar.get(Calendar.YEAR));
        String astea = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
        String pattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(pattern);
        String eguna = df.format(data);

        // Momentuko ordua hartu
        Date ordOrain = Calendar.getInstance().getTime();

        // Pasatako segunduak lortu, segundutan
        int ordSegunduak = Math.toIntExact((ordOrain.getTime() - data.getTime()) / 1000);

        // Hitzarmeneko ordu kantitateak lortu, orduetan
        IrakurProperties iraProp = new IrakurProperties(ktst);
        Properties prop = iraProp.lorProperties("hitzarmenak.properties");
        float egun_extra = Integer.parseInt(prop.getProperty(hitzarmena + "_egun_maxorduak"))* jardu;
        float aste_extra = Integer.parseInt(prop.getProperty(hitzarmena + "_aste_maxorduak"))* jardu;
        float urte_extra = Integer.parseInt(prop.getProperty(hitzarmena + "_urte_maxorduak"))* jardu;

        // Orain harte egindako orduak lortu, orduetan
        int ordEguna = (getInputData().getInt("ordEguna", 0) + ordSegunduak) / 3600;
        int ordAstea = (getInputData().getInt("ordAstea", 0) + ordSegunduak) / 3600;
        int ordUrtea = (getInputData().getInt("ordUrtea", 0) + ordSegunduak) / 3600;


        // Urte, aste edo eguneko orduak aztertzen ditu
        if (ordUrtea >= urte_extra && pref.lorString("jakinUrte").equals("-")) {
            bidNotifikazio(ktst.getString(R.string.jaki_extra_izbu), ktst.getString(R.string.jak_extra_urte));
            pref.gorde("jakinUrte", urtea);
            ezeztatu();
        } else if (ordAstea >= aste_extra && pref.lorString("jakinAste").equals("-")) {
            bidNotifikazio(ktst.getString(R.string.jaki_extra_izbu), ktst.getString(R.string.jak_extra_aste));
            pref.gorde("jakinAste", urtea + "-" + astea);
        } else  if (ordEguna >= egun_extra && pref.lorString("jakinEguna").equals("-")) {
            bidNotifikazio(ktst.getString(R.string.jaki_extra_izbu), ktst.getString(R.string.jak_extra_egun));
            pref.gorde("jakinEguna", eguna);
        }

        return Result.success();
    }


    /**
     * Jakinarazpena kudeatzeko metodoa
     * @param izenburu Jakinarazpenaren izenburua
     * @param mezua Jakinarazpenaren mezua
     */
    private void bidNotifikazio(String izenburu, String mezua) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8+ -rentzako notifikazio kanala sortu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ordu_azter_kanala",
                    "Ordua aztertze kanala",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Notifikazioa sortu
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "ordu_azter_kanala")
                .setSmallIcon(R.drawable.logo_txiki)
                .setContentTitle(izenburu)
                .setContentText(mezua)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Notifikazioa bistaratu
        notificationManager.notify(1, notification.build());
    }

    /**
     * Worker honen exekuzio periodikoa gelditzeko metodoa.
     */
    private void ezeztatu() {
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.cancelAllWorkByTag("jakinHitzar");
    }
}