package io.anyline.examples;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.centerm.smartpos.aidl.printer.AidlPrinter;
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener;
import com.centerm.smartpos.aidl.printer.PrintDataObject;
import com.centerm.smartpos.aidl.printer.PrintDataObject.ALIGN;
import com.centerm.smartpos.aidl.printer.PrintDataObject.SPACING;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.constant.DeviceErrorCode;
import com.centerm.smartpos.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KisadaM on 8/2/2017.
 */

public class PrintActivity extends devBase {
    private AidlPrinter printDev = null;
    private AidlPrinterStateChangeListener callback = new PrinterCallback();
    private EditText qrCode, barCode;
    private String qrStr;
    private String barStr;
    private Spinner spinner;
    private int typeIndex;
    private String codeStr;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.activity_printer);
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.tsslogo72);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setGrayOne(View v) {
        LogUtil.print(getString(R.string.printer_gray1));
        try {
            printDev.setPrinterGray(0x01);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGrayTwo(View v) {
        LogUtil.print(getString(R.string.printer_gray2));
        try {
            printDev.setPrinterGray(0x02);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGrayThree(View v) {
        LogUtil.print(getString(R.string.printer_gray3));
        try {
            printDev.setPrinterGray(0x03);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGrayFour(View v) {
        LogUtil.print(getString(R.string.printer_gray4));
        try {
            printDev.setPrinterGray(0x04);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printText(View v) throws Exception {
        List<PrintDataObject> list = new ArrayList<PrintDataObject>();
        list.add(new PrintDataObject("สระอู กู ดู รู หู ครู ตู", 24));
        list.add(new PrintDataObject(
                getString(R.string.printer_textsize_bigger), 24));
        /*list.add(new PrintDataObject(getString(R.string.printer_textsize_blod),
                8, true));
        list.add(new PrintDataObject(getString(R.string.printer_left), 8,
                false, ALIGN.LEFT));
        list.add(new PrintDataObject(getString(R.string.printer_center), 8,
                false, ALIGN.CENTER));
        list.add(new PrintDataObject(getString(R.string.printer_right), 8,
                false, ALIGN.RIGHT));
        list.add(new PrintDataObject(getString(R.string.printer_underline), 8,
                false, ALIGN.LEFT, true));
        list.add(new PrintDataObject(getString(R.string.printer_amount), 8,
                true, ALIGN.LEFT, false, false));
        list.add(new PrintDataObject("888.66", 24, true, ALIGN.LEFT, false,
                true));
        list.add(new PrintDataObject(getString(R.string.printer_newline), 8,
                false, ALIGN.LEFT, false, true));
        list.add(new PrintDataObject(
                getString(R.string.printer_acceptorid_name)));
        list.add(new PrintDataObject(getString(R.string.printer_line_distance),
                8, false, ALIGN.LEFT, false, true, 40, 28));
        for (int i = 0; i < 38; i += 3) {
            list.add(new PrintDataObject(
                    getString(R.string.printer_left_distance) + (i * 10), 8,
                    false, ALIGN.LEFT, false, true, 24, 0, (i * 10)));
        }*/
        try {
            this.printDev.printText(list, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String _xx){}

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

    public void spitPaper(View v) {
        try {
            printDev.spitPaper(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPrintState(View v) {
        try {
            int retCode = printDev.getPrinterState();
            getMessStr(retCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printBarCode(View v) {
        String str = "123456789";
        try {
            this.printDev.printBarCode(str, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initPrinter(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    printDev.initPrinter();
                    showMessage(getString(R.string.printer_init_success));
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(getString(R.string.printer_init_exception));
                }
            }
        }).start();
    }

    public void printLittleSize(View v) throws Exception {
        PrintDataObject printDataObject = null;
        try {
            printDataObject = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PrintDataObject> list = new ArrayList<PrintDataObject>();
        list.add(printDataObject);
        list.add(printDataObject);
        PrintDataObject printDataObject2 = new PrintDataObject(
                getString(R.string.printer_textsize_normal));
        printDataObject2.setText(getString(R.string.printer_textsize_small));
        printDataObject2.setIsLittleSize(true);
        list.add(printDataObject2);
        list.add(printDataObject2);
        try {
            printDev.printText(list, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printDoubleHigh(View v) {
        PrintDataObject printDataObject = null;
        try {
            printDataObject = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PrintDataObject> list = new ArrayList<PrintDataObject>();
        list.add(printDataObject);
        list.add(printDataObject);
        list.add(printDataObject);
        PrintDataObject printDataObject2 = null;
        try {
            printDataObject2 = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            printDataObject2.setText(getString(R.string.printer_texisize_higher));
        } catch (Exception e) {
            e.printStackTrace();
        }
        printDataObject2.setSpacing(SPACING.DOUBLE_HIGH);
        list.add(printDataObject2);
        list.add(printDataObject2);
        list.add(printDataObject2);
        try {
            printDev.printText(list, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printDoubleWidth(View v) {
        PrintDataObject printDataObject = null;
        try {
            printDataObject = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PrintDataObject> list = new ArrayList<PrintDataObject>();
        list.add(printDataObject);
        list.add(printDataObject);
        list.add(printDataObject);
        PrintDataObject printDataObject2 = null;
        try {
            printDataObject2 = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            printDataObject2.setText(getString(R.string.printer_textsize_wider));
        } catch (Exception e) {
            e.printStackTrace();
        }
        printDataObject2.setSpacing(SPACING.DOUBLE_WIDTH);
        list.add(printDataObject2);
        list.add(printDataObject2);
        list.add(printDataObject2);
        try {
            printDev.printText(list, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printDoubleHighWidth(View v) {
        PrintDataObject printDataObject = null;
        try {
            printDataObject = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PrintDataObject> list = new ArrayList<PrintDataObject>();
        list.add(printDataObject);
        list.add(printDataObject);
        list.add(printDataObject);
        PrintDataObject printDataObject2 = null;
        try {
            printDataObject2 = new PrintDataObject(
                    getString(R.string.printer_textsize_normal));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            printDataObject2.setText(getString(R.string.printer_higher_wider));
        } catch (Exception e) {
            e.printStackTrace();
        }
        printDataObject2.setSpacing(SPACING.DOUBLE_HIGH_WIDTH);
        list.add(printDataObject2);
        list.add(printDataObject2);
        list.add(printDataObject2);
        try {
            printDev.printText(list, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openQueue(View v) {
        try {
            if (printDev.setPrintQueue(true)) {
                showMessage(getString(R.string.open_queue_success));
            } else {
                showMessage(getString(R.string.open_queue_failed));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(getString(R.string.open_queue_exception)
                    + e.getMessage());
        }
    }

    public void closeQueue(View v) {
        try {
            if (printDev.setPrintQueue(false)) {
                showMessage(getString(R.string.close_queue_success));
            } else {
                showMessage(getString(R.string.close_queue_failed));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(getString(R.string.close_queue_exception)
                    + e.getMessage());
        }
    }

    public void printCodeType(View v) {
        try {
            printDev.printBarCodeExtend(codeStr, typeIndex, 600, 600,
                    Constant.ALIGN.CENTER, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printQRCodeFast(View v) {
        qrStr = "thin Solution System";
        if (TextUtils.isEmpty(qrStr)) {
            qrStr = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefgh";
        }
        try {
            printDev.printQRCodeFast(qrStr, 600, Constant.ALIGN.CENTER,
                    callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            printDev = AidlPrinter.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
