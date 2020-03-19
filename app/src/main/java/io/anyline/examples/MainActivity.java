package io.anyline.examples;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.centerm.smartpos.aidl.printer.AidlPrinter;
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener;
import com.centerm.smartpos.aidl.printer.PrintDataObject;
import com.centerm.smartpos.aidl.printer.PrinterParams;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.constant.DeviceErrorCode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends devBase {
    private AidlPrinter printDev = null;
    private AidlPrinterStateChangeListener callback = new MainActivity.PrinterCallback();
    ImageView imgVtest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

//        actionBar.setIcon(R.drawable.camera1);

    }

    public void printText(View v) throws Exception {

//        android.graphics.Bitmap imageicon = BitmapFactory.decodeResource(getResources(), R.id.imgVtest);
//        imageicon = Bitmap.createScaledBitmap(imageicon, 130, 130, false);
//        List<PrinterParams> textList = new ArrayList<PrinterParams>();
//        PrinterParams printerParams = new PrinterParams();
//        printerParams.setAlign(PrinterParams.ALIGN.CENTER);
//        printerParams.setDataType(PrinterParams.DATATYPE.IMAGE);
//        printerParams.setBitmap(imageicon);
//        textList.add(printerParams);
//        try {
//            printDev.printData(textList);
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    private class PrinterCallback extends AidlPrinterStateChangeListener.Stub {

        @Override
        public void onPrintError(int arg0) throws RemoteException {
            getMessStr(arg0);
        }

        @Override
        public void onPrintFinish() throws RemoteException {
            //showMessage(getString(R.string.printer_finish), "", Color.BLACK);
        }

        @Override
        public void onPrintOutOfPaper() throws RemoteException {
            //showMessage(getString(R.string.printer_need_paper), "", Color.RED);
        }
    }

    private void getMessStr(int ret) {
        switch (ret) {
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_BUSY:
                showMessage(getString(R.string.printer_device_busy));
                break;
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_OK:
                showMessage(getString(R.string.printer_success));
                break;
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_OUT_OF_PAPER:
                showMessage(getString(R.string.printer_lack_paper));
                break;
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_HEAD_OVER_HEIGH:
                showMessage(getString(R.string.printer_over_heigh));
                break;
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_OVER_HEATER:
                showMessage(getString(R.string.printer_over_heat));
                break;
            case DeviceErrorCode.DEVICE_PRINTER.DEVICE_PRINTER_LOW_POWER:
                showMessage(getString(R.string.printer_low_power));
                break;
            default:
                showMessage(getString(R.string.printer_other_exception_code) + ret);
                break;
        }

    }

    private void showMessage(String _xx){}

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            printDev = AidlPrinter.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void showICCard(View v) {
        Intent intent = new Intent("android.intent.action.ICCardActivity");
        startActivity(intent);
    }

    public void showDriver(View v) {
        Intent intent = new Intent("android.intent.action.SwipeCardActivity");
        startActivity(intent);
    }

    public void showVisa(View v) {
        Intent intent = new Intent("android.intent.action.VisaActivity");
        startActivity(intent);
    }

    public void showMaster(View v) {
        Intent intent = new Intent("android.intent.action.MasterActivity");
        startActivity(intent);
    }

    public void showElevenCard(View v) {
        Intent intent = new Intent("android.intent.action.ElevenActivity");
        startActivity(intent);
    }

    public void showPassport(View v) {
       Intent intent = new Intent("android.intent.action.PassportReader");
        startActivity(intent);
    }

    public void showIC_nopicCard(View v) {
        Intent intent = new Intent("android.intent.action.MainICNopicActivity");
        startActivity(intent);
    }

    public void showSign(View v) {
        Intent intent = new Intent("android.intent.action.SignActivity");
        startActivityForResult(intent,101);
    }

    public void showCamera(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String message;
        if (requestCode == 2) {
            message = data.getStringExtra("MESSAGE");
            Intent intent = new Intent("android.intent.action.PrintActivity");
            startActivity(intent);
        }

        if (requestCode == 3) {
            message = data.getStringExtra("MESSAGE");
            String _xx = message;
        }

        if (requestCode == 101) {
//            imgVtest = (ImageView)findViewById(R.id.imgVtest);
            Bitmap bitmap = getIntent().getParcelableExtra("signature");
            imgVtest.setImageBitmap(bitmap);
        }


        if (requestCode == 0) {
            message = data.getStringExtra("MESSAGE");
            if (message.startsWith("READING")) {
                Intent i2 = new Intent();
                i2.setClassName("com.centerm.nfcforb4a", "com.centerm.nfcforb4a.MainActivity");
                i2.putExtra("CMD", "00A4040C07A0000002471001|0084000008");
                i2.putExtra("REQUEST_CODE", 2);
                i2.putExtra("RESET", true);
                startActivityForResult(i2, 2);
            }
        }
    }

    public void showPrinter(View v) {
        Intent intent = new Intent("android.intent.action.PrintOnline");
        startActivity(intent);
    }

    public void showUnionpay(View v) {
        Intent intent = new Intent("android.intent.action.UnionpayActivity");
        startActivity(intent);
    }
}
