package io.anyline.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//import org.jnbis.Bitmap;

/**
 * Created by tharawat.m on 13/05/2016.
 */
public class Utility {

    public static boolean checkDeviceNFC(Context context){
        boolean check = false;
        try{
            NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            if (adapter != null) {
                // adapter exists and is enabled.
                check = true;
            }
        }catch (Exception ex){

        }
        return check;
    }

    public static boolean checkEnableNFC(Context context){
        boolean check = false;
        try{
            NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            if (adapter.isEnabled()) {
                // adapter exists and is enabled.
                check = true;
            }
        }catch (Exception ex){

        }
        return check;
    }

    public static Bitmap byteArraytoBitmap(byte[] imageData){
        Bitmap bmp = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length,options);

        }catch (Exception ex){

        }
        return bmp;
    }

    public static String getDateFormatDatePicker(DatePicker datePicker){
        int   day  = datePicker.getDayOfMonth();
        int   month= datePicker.getMonth();
        int   year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String formatedDate = sdf.format(calendar.getTime());
        return formatedDate;
    }
}
