package com.labsindikatua.fitxatu.laguntzailea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import com.labsindikatua.fitxatu.DBKudeaketa;
import com.labsindikatua.fitxatu.R;
import java.io.File;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Excel exportazioak kudeatzeko klasea.
 * @author: LAB Sindikatua
 */
public class ExcelKudeaketa {

    public static Context ktst;

    /**
     * Excel fitxategia sortu eta gordetzeko metodoa.
     * @param kontest Deitzen duen aktibitatearen kontestua.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint("SimpleDateFormat")
    public static void esportatu(Context kontest) {

        ktst = kontest;

        //Parametroak hasieratu
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date data_orain = calendar.getTime();
        DateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        String orain = date.format(data_orain);
        String fitx_izen = "Fitxatu" + orain + ".xls";
        File directory = null;
        ContentResolver resolver = null;
        Uri fileUri = null;
        ShPreferences pref = new ShPreferences(kontest);
        WritableWorkbook workbook = null;

        try {

            //Fitxategia eta WorkBook-a sortu
            //Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                directory = new File(kontest.getExternalFilesDir(null), fitx_izen);
                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale(pref.lorString("hizkuntza")));

                resolver = kontest.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fitx_izen);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (fileUri != null) {
                    OutputStream outputStream = resolver.openOutputStream(fileUri);
                    if (outputStream != null) {
                        workbook = Workbook.createWorkbook(outputStream);
                    }
                }

            } else {
                File fitx_helb = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                directory = new File(fitx_helb.getAbsolutePath());
                File file = new File(directory, fitx_izen);
                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale(pref.lorString("hizkuntza")));
                workbook = Workbook.createWorkbook(file, wbSettings);
            }

        } catch (Exception e) {
            //Arazoren bat egon denaren mezua bistaratu.
            Toast.makeText(kontest, kontest.getString(R.string.oker_jaitsi), Toast.LENGTH_SHORT).show();
        }

        //Karpeta sortu existitzen ez bada
        if (directory != null) {
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }
        }

        try {
            if (workbook != null) {
                //Urteko liburua sortu
                WritableSheet libUrte = workbook.createSheet(kontest.getString(R.string.urteak), 0);

                DBKudeaketa db = new DBKudeaketa(kontest);
                Cursor kurtsore = db.irak_urtea();

                libUrte.addCell(new Label(1, 1, kontest.getString(R.string.data)));
                libUrte.addCell(new Label(2, 1, kontest.getString(R.string.denbora)));
                libUrte.addCell(new Label(3, 1, kontest.getString(R.string.jaia)));
                libUrte.addCell(new Label(4, 1, kontest.getString(R.string.extrak)));

                int ilara = 1;
                if (kurtsore.moveToFirst()) {
                    do {
                        ilara = ilara + 1;

                        String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("urte_urte"));
                        int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                        int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_jai"));

                        karg_ilarak(data, denbora, jai, null, ilara, libUrte, kontest, "urte");


                    } while (kurtsore.moveToNext());
                }

                //Kurtsorea itxi
                kurtsore.close();

                //Asteko liburua sortu
                WritableSheet libAste = workbook.createSheet(kontest.getString(R.string.asteak), 1);

                libAste.addCell(new Label(1, 1, kontest.getString(R.string.data)));
                libAste.addCell(new Label(2, 1, kontest.getString(R.string.denbora)));
                libAste.addCell(new Label(3, 1, kontest.getString(R.string.jaia)));
                libAste.addCell(new Label(4, 1, kontest.getString(R.string.extrak)));

                kurtsore = db.irak_astea();
                ilara = 1;

                if (kurtsore.moveToFirst()) {
                    do {
                        ilara = ilara + 1;
                        String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("urte_aste"));
                        int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                        int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_jai"));

                        karg_ilarak(data, denbora, jai, null, ilara, libAste, kontest, "aste");

                    } while (kurtsore.moveToNext());
                }

                //Kurtsorea itxi
                kurtsore.close();

                //Eguneko liburua sortu
                WritableSheet libEgun = workbook.createSheet(kontest.getString(R.string.egunak), 1);

                libEgun.addCell(new Label(1, 1, kontest.getString(R.string.data)));
                libEgun.addCell(new Label(2, 1, kontest.getString(R.string.denbora)));
                libEgun.addCell(new Label(3, 1, kontest.getString(R.string.jaia)));
                libEgun.addCell(new Label(4, 1, kontest.getString(R.string.extrak)));

                kurtsore = db.irak_eguna();
                ilara = 1;

                if (kurtsore.moveToFirst()) {
                    do {
                        ilara = ilara + 1;
                        String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("eguna"));
                        int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("total_denb"));
                        int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("JAI"));

                        karg_ilarak(data, denbora, jai, null, ilara, libEgun, kontest, "egun");

                    } while (kurtsore.moveToNext());
                }

                //Kurtsorea eta datu basea itxi
                kurtsore.close();
                db.close();


                //Erregistro guztien liburua sortu
                WritableSheet libGuzt = workbook.createSheet(kontest.getString(R.string.guztiak), 1);

                libGuzt.addCell(new Label(1, 1, kontest.getString(R.string.data)));
                libGuzt.addCell(new Label(2, 1, kontest.getString(R.string.denbora)));
                libGuzt.addCell(new Label(3, 1, kontest.getString(R.string.jaia)));
                libGuzt.addCell(new Label(4, 1, kontest.getString(R.string.koor)));
                libGuzt.addCell(new Label(5, 1, kontest.getString(R.string.eskuz)));

                kurtsore = db.irak_guztiak();
                ilara = 1;

                if (kurtsore.moveToFirst()) {
                    do {
                        ilara = ilara + 1;
                        String data = kurtsore.getString(kurtsore.getColumnIndexOrThrow("DATA"));
                        int denbora = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("DENBORA"));
                        int jai = kurtsore.getInt(kurtsore.getColumnIndexOrThrow("JAI"));
                        String koor = kurtsore.getString(kurtsore.getColumnIndexOrThrow("KOOR"));

                        karg_ilarak(data, denbora, jai, koor, ilara, libGuzt, kontest, "guztiak");

                    } while (kurtsore.moveToNext());
                }

                //Kurtsorea eta datu basea itxi
                kurtsore.close();
                db.close();


                //Datuen liburua sortu
                WritableSheet libDatuak = workbook.createSheet(kontest.getString(R.string.datuak), 1);

                libDatuak.addCell(new Label(1, 1, kontest.getString(R.string.iabizen)));
                libDatuak.addCell(new Label(2, 1, pref.lorString("izen")));
                libDatuak.addCell(new Label(1, 2, kontest.getString(R.string.nan)));
                libDatuak.addCell(new Label(2, 2, pref.lorString("nan")));
                libDatuak.addCell(new Label(1, 3, kontest.getString(R.string.jardun)));

                //Hizkuntzaren arabera % sinboloa alde batean ala bestean
                if (pref.lorString("hizkuntza").equals("eu")) {
                    libDatuak.addCell(new Label(2, 3, "%" +
                            pref.lorString("jardunaldia")));
                } else {
                    libDatuak.addCell(new Label(2, 3, pref.lorString("jardunaldia") +
                            "%"));
                }

                //Fitxategia gorde
                //Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (fileUri != null) {
                        OutputStream outputStream = resolver.openOutputStream(fileUri);
                        if (outputStream != null) {

                            workbook.write(); // Escribir los datos en el archivo
                            workbook.close(); // Cerrar el archivo
                            outputStream.close();

                            Toast.makeText(kontest, kontest.getString(R.string.zuzen_jaitsi), Toast.LENGTH_SHORT).show();
                            kudeNotifikazioa(kontest, directory);
                        }
                    } else {
                        Toast.makeText(kontest, kontest.getString(R.string.oker_jaitsi), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    workbook.write();
                    workbook.close();
                    Toast.makeText(kontest, kontest.getString(R.string.zuzen_jaitsi), Toast.LENGTH_SHORT).show();
                    kudeNotifikazioa(kontest, directory);
                }
            }

        } catch (Exception e) {
            //Arazoren bat egon denaren mezua bistaratu.
            Toast.makeText(kontest, kontest.getString(R.string.oker_jaitsi), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fitxategiko ilarak kargatzeko metodoa.
     * @param data Lan eguneko data.
     * @param denbora Lan egindako denbora.
     * @param jai Lan eguna jai eguna zen ala ez.
     * @param ilara Idatzi beharreko ilararen posizioa.
     * @param liburu Idatzi beharreko Exceleko liburua.
     * DefaultLocale: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     * SimpleDateFormat: Ez du aplikatzen kokapen geografiko berdinean ibiliko delako.
     */
    @SuppressLint({"DefaultLocale","SimpleDateFormat"})
    public static void karg_ilarak(String data, int denbora, int jai, String koor, int ilara, WritableSheet liburu, Context kontest, String taula) throws WriteException {

        //Orduak kalkulatu
        int minutuGuztiak = denbora / 60;
        int minutuak = minutuGuztiak % 60;
        int orduak = minutuGuztiak / 60;
        String fordenb = String.format("%02d", orduak) + ":" + String.format("%02d", minutuak);

        //Hizkuntzaren arabera data modu batean bistaratu
        ShPreferences pref = new ShPreferences(ktst);
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

        //Ilara idatzi
        liburu.addCell(new Label(1, ilara, data));
        liburu.addCell(new Label(2, ilara, fordenb));
        liburu.addCell(new Label(3, ilara, String.valueOf(jai)));
        String eskuz = kontest.getString(R.string.ez);
        if (koor != null) {
            if (koor.equals("Eskuz")) {
                koor = "-";
                eskuz = kontest.getString(R.string.bai);
            }

            liburu.addCell(new Label(4, ilara, koor));
            liburu.addCell(new Label(5, ilara, eskuz));
        }else{

            //Extrak kalkulatu
            float extrak = kalkExtrak(denbora, taula);

            //Orduak extrak formateatu
            float ext_minguztiak = extrak / 60;
            int ext_minutuak = (int) (ext_minguztiak % 60);
            int ext_orduak = (int) (ext_minguztiak / 60);
            String ext_emaitza = String.format("%02d", ext_orduak) + ":" + String.format("%02d", ext_minutuak);
            liburu.addCell(new Label(4, ilara, ext_emaitza));
        }
    }


    private static void kudeNotifikazioa(Context kontest, File fitxategi) {


        if (ActivityCompat.checkSelfPermission(kontest, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions((Activity) kontest, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
            }
        }else{
            bidaliNotifikazio(kontest, fitxategi);
        }

    }

    /**
     * Fitxategia deskargatzean agertzen den notifikazioa sortzeko metodoa.
     * @param kontest Jatorrizko kontestua.
     * @param fitxategi Karpetaren helbidea.
     * MissingPermission - Baimenak arinago eskatzen dira.
     */
    @SuppressLint("MissingPermission")
    public static void bidaliNotifikazio(Context kontest, File fitxategi){

        // Notifikazioa kanala sortu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Deskarga abisua";
            String description = "Fitxategia deskargatu dala abisatzeko";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("exportNoti", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) kontest.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

        }

        // Karpeta irekitzeko Intent-a sortu
        Uri uri = FileProvider.getUriForFile(kontest, kontest.getApplicationContext().getPackageName() + ".provider", fitxategi);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // PendingIntent sortu
        PendingIntent pendingIntent = PendingIntent.getActivity(kontest, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Notifikazioa sortu
        NotificationCompat.Builder builder = new NotificationCompat.Builder(kontest, "exportNoti")
                .setSmallIcon(R.drawable.logo_txiki)
                .setContentTitle(kontest.getResources().getString(R.string.noti_izen))
                .setContentText(kontest.getResources().getString(R.string.noti_deskri))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Notifikazioa bistaratu
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(kontest);
        notificationManager.notify(1, builder.build());
    }

    /**
     * Ordu extrak kalkulatzeko metodoa
     * @param denbora Lan egindako orduak.
     * @param taula Taula mota.
     * @return Egindako ordu extrak.
     */
    public static float kalkExtrak(int denbora, String taula) {

        ShPreferences pref = new ShPreferences(ktst);
        String hitzarmena = pref.lorString("hitzarmena");
        float jardu = (float) Integer.parseInt(pref.lorString("jardunaldia")) / 100;

        IrakurProperties iraProp = new IrakurProperties(ktst);
        Properties prop = iraProp.lorProperties("hitzarmenak.properties");
        float ref_extra = (Float.parseFloat(prop.getProperty(hitzarmena +"_" + taula + "_orduak"))) * jardu;

        int seg_guztiak = (int) (ref_extra * 3600);

        return (float) Math.max(0, denbora - seg_guztiak);
    }
}