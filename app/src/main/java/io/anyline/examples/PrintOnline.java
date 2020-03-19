package io.anyline.examples;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.smartpos.aidl.iccard.AidlICCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLayer;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.anyline.examples.mrz.ScanMrzActivity;

//import static io.anyline.examples.PdfActivity.PERMISSIONS_STORAGE;

public class PrintOnline extends devBase {
    private AidlICCard iccard = null;
    private String _cmd = "00A4040008";
    private String _thai_id_card = "A000000054480001";
    private String _req_cid = "80b0000402000d";
    //private String _req_thai_name = "80b00011020064";
    //private String _req_eng_name = "80b00075020064";
    //private String _req_gender = "80b000E1020001";
    //private String _req_dob = "80b000D9020008";
    private String _cardno = "A9EF7B30159C2CFCE9E9AC218945213B";
    private String _req_address = "80b01579020064";
    private String _req_issue_expire = "80b00167020012";
    private String _req_full_name = "80b000110200d1";
    private final Charset _UTF8_CHARSET = Charset.forName("TIS-620");
    private List<String> months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private List<String> months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.");
    private List<String> religions = Arrays.asList("ไม่นับถือศาสนา", "พุทธ", "อิสลาม", "คริสต์", "พราหมณ์-ฮินดู", "ซิกข์", "ยิว", "เชน", "โซโรอัสเตอร์", "บาไฮ", "ไม่ระบุ");

    public static String _id_card;
    public static String _thai_name;
    private String _eng_first_name;
    private String _eng_last_name;
    private String _birth_eng;
    private String _birth_th;
    //private String _gender_eng;
    //private String _gender_th;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static String _address;
    private String _issue_eng;
    private String _issue_th;
    private String _expire_eng;
    private String _expire_th;
    private String _religion;
    private byte[] _photo;
    public static String name_2;
    public static String address_2;
    private TextView name2;
    private TextView address2;

    private TextView idcard;
    private TextView thname;
    private TextView engfname;
    private TextView englname;
    private TextView engbirth;
    private TextView thbirth;
    private TextView address;
    private TextView engissue;
    private TextView thissue;
    private TextView engexpire;
    private TextView thexpire;
    private TextView religion;
    private ImageView xphoto;
    private ProgressDialog mLoading;
    myAsyncTask MyAsyncTask;
    int i = 0;
    public static String age;
    private Handler mHandler = new Handler();
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.activity_print_online);
        super.onCreate(savedInstanceState);

        idcard = (TextView)findViewById(R.id.tVidcard2);idcard.setText("");
        thname = (TextView)findViewById(R.id.tVnameTH2);thname.setText("");
        thbirth = (TextView)findViewById(R.id.tVbirthTH2);thbirth.setText("");
        address = (TextView)findViewById(R.id.tVaddress2);address.setText("");
        name2 = (TextView)findViewById(R.id.name2);name2.setText("");
        address2 =(TextView)findViewById(R.id.address2);address2.setText("");

        Button butt = (Button) findViewById(R.id.button3);

        butt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyAsyncTask=new myAsyncTask();
                MyAsyncTask.execute();


            }
        });

        //engexpire = (TextView)findViewById(R.id.tVexpireENG);engexpire.setText("");
        //thexpire = (TextView)findViewById(R.id.tVexpireTH);thexpire.setText("");
        //religion = (TextView)findViewById(R.id.tVreligion);religion.setText("");
        xphoto = (ImageView)findViewById(R.id.iVphoto);xphoto.setImageBitmap(null);
        //xphoto.destroyDrawingCache();

        mLoading = new ProgressDialog(this);
        mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.setMessage("Reading...");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.tsslogo72);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(iccard != null) {
                scheduledExecutor.shutdown();
                iccard.close();
                iccard = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            this.iccard = AidlICCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                if(iccard != null)
                    d();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void d() throws InterruptedException, ExecutionException {
        Runnable job = new Runnable() {
            boolean _read = false;

            @Override
            public void run() {
                try {
                    iccard.open();
                    if(iccard.status() == 1){
                        if(iccard.reset() != null && !_read){
                            if(iccard.sendAsync(HexUtil.hexStringToByte(_cmd + _thai_id_card)) != null){
                                _read = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.show();
                                    }
                                });

                                i = i+1;
                                a(new String(iccard.sendAsync(HexUtil.hexStringToByte(_req_cid)), _UTF8_CHARSET), 0);
                                a(new String(iccard.sendAsync(HexUtil.hexStringToByte(_req_full_name)), _UTF8_CHARSET), 1);
                                a(new String(iccard.sendAsync(HexUtil.hexStringToByte(_req_address)), _UTF8_CHARSET), 2);
                                a(new String(iccard.sendAsync(HexUtil.hexStringToByte(_req_issue_expire)), _UTF8_CHARSET), 3);
                                //m();
                                //iccard.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.dismiss();
                                    }
                                });
                            }
                        }
                    }
                    else{
                        _read = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //c();
                                mLoading.dismiss();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private byte[] r(byte[] data)
            {
                int index = data.length - 1;
                while ((index > 0) && (data[(index - 1)] == 32))
                {
                    index--;
                    if (index == 0) {
                        return null;
                    }
                }
                return Arrays.copyOfRange(data, 0, index);
            }

            private void m(){
                try {
                    ByteArrayOutputStream _a = new ByteArrayOutputStream();
                    for (int i = 0; i < 20; i++)
                    {
                        int xwd;
                        int xof = i * 254 + 379; //379-381
                        xwd = i == 20 ? 38 : 254;

                        String sp2 = e(xof >> 8 & 0xff);
                        String sp3 = e(xof & 0xff);
                        String sp6 = e(xwd & 0xff);

                        byte[] _xx = r(r(iccard.sendAsync(HexUtil.hexStringToByte("80B0" + sp2 + sp3 + "0200" + sp6)))); //0200 - 0201
                        if(_xx != null)
                            _a.write(_xx, 0, _xx.length);
                    }
                    _a.flush();
                    _photo = _a.toByteArray();
                    String _b = Base64.encodeToString(_photo, Base64.DEFAULT);
                    _a.close();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    _photo = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    _photo = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap _bm = BitmapFactory.decodeByteArray(_photo, 0, _photo.length);
                        xphoto.setImageBitmap(_bm);
                    }
                });
            }

            private void c(){
                idcard.setText("");
                thname.setText("");

                thbirth.setText("");
                address.setText("");
                //engexpire.setText("");
                //thexpire.setText("");
                //religion.setText("");
                xphoto.setImageBitmap(null);xphoto.destroyDrawingCache();
            }

            private String a(String _val, int _index){
                String _xx = _val;
                switch (_index){
                    case 0:
                        if(_xx != null | _xx.length() != 0){
                            _xx = _val.replaceAll("#", " ");
                            _xx = _xx.substring(0, _xx.length() - 2);
                            char[] achars = _xx.toUpperCase().toCharArray();
                            _id_card = achars[0] + " " + achars[1] + achars[2] + achars[3] + achars[4] + " " + achars[5] + achars[6]+ achars[7] + achars[8] + achars[9] + " " + achars[10] + achars[11] + " " + achars[12];
                            _id_card = _id_card.substring(0,11)+"X XX X";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(i==1) {
                                        idcard.setText(_id_card);
                                    }
                                }
                            });
                        }
                        break;
                    case 1:
                        if(_xx != null | _xx.length() != 0){
                            int _first_space = _val.indexOf(" ");
                            _thai_name = _xx.substring(0, _first_space).replaceAll("#", " ");
                            _xx = _xx.substring(_first_space, _xx.length()-2);
                            _xx = _xx.trim();
                            _first_space = _xx.indexOf(" ");
                            String _eng_name = _xx.substring(0, _first_space).replaceAll("#", " ");
                            String[] _eng_name_list = _eng_name.split(" ");
                            _eng_first_name = _eng_name_list[0] + " " + _eng_name_list[1];
                            _eng_last_name = _eng_name_list[3];
                            _xx = _xx.substring(_first_space, _xx.length());
                            _xx = _xx.trim();
                            String _year_th = _xx.substring(0, 4);
                            String _year_eng = "" + (Integer.parseInt(_xx.substring(0, 4)) - 543);
                            _year_th = _year_th.substring(0,2)+"XX";
                            _year_eng = _year_eng.substring(0,2)+"XX";

                            String _month_eng = months_eng.get(Integer.parseInt(_xx.substring(4, 6))-1);
                            String _month_th = months_th.get(Integer.parseInt(_xx.substring(4, 6))-1);
                            String _day = "" + Integer.parseInt(_xx.substring(6, 8));
                            age = getAge(Integer.parseInt(_xx.substring(0, 4)) - 543,Integer.parseInt(_xx.substring(4, 6))-1,Integer.parseInt(_xx.substring(6, 8)));
                            _birth_eng = _day + " " + _month_eng + " " + _year_eng;
                            _birth_th = _day + " " + _month_th + " " + _year_th;
							/*if(Integer.parseInt(_xx.substring(8, 9)) == 1) {
								_gender_eng = "Male";
								_gender_th = "ชาย";
							}else{
								_gender_eng = "Female";
								_gender_th = "หญิง";
							}*/
                            //_xx = _thai_name + "\n" + _eng_first_name + "\r\n" + _eng_last_name + "\r\n" + _birth_th + "\n" + _birth_eng + "\n" + _gender_eng + "\r\n" + _gender_th;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (i==1){
                                        thname.setText(_thai_name);
                                        thbirth.setText(age);
                                        name_2 = _thai_name;
                                    }
                                    if(i==2){
                                        name2.setText(_thai_name);
                                    }
                                }
                            });
                        }
                        break;
                    case 2:
                        if(_xx != null | _xx.length() != 0){
                            _xx = _val.replaceAll("#", " ");
                            _xx = _xx.substring(0, _xx.length() - 2);
                            _xx = _xx.replace("ตำบล", "ต.");
                            _xx = _xx.replace("อำเภอ", "อ.");
                            _xx = _xx.replace("จังหวัด", "จ.");
                            _address = "       " + _xx;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(i==1){
                                        address.setText(_address);
                                        address_2 =_address;
                                    }
                                    if(i==2){
                                        i=0;
                                        address2.setText(_address);
                                    }
                                }
                            });
                        }
                        break;
                    case 3:
                        if(_xx != null | _xx.length() != 0){
                            _xx = _val.replaceAll("#", " ");
                            _xx = _xx.substring(0, _xx.length() - 2);
                            String _year_th = _xx.substring(0, 4);
                            String _year_eng = "" + (Integer.parseInt(_xx.substring(0, 4)) - 543);
                            String _month_eng = months_eng.get(Integer.parseInt(_xx.substring(4, 6))-1);
                            String _month_th = months_th.get(Integer.parseInt(_xx.substring(4, 6))-1);
                            String _day = "" + Integer.parseInt(_xx.substring(6, 8));
                            _issue_eng = _day + " " + _month_eng + " " + _year_eng;
                            _issue_th = _day + " " + _month_th + " " + _year_th;

                            _year_th = _xx.substring(8, 12);
                            _year_eng = "" + (Integer.parseInt(_xx.substring(8, 12)) - 543);
                            _month_eng = months_eng.get(Integer.parseInt(_xx.substring(12, 14))-1);
                            _month_th = months_th.get(Integer.parseInt(_xx.substring(12, 14))-1);
                            _day = "" + Integer.parseInt(_xx.substring(14, 16));
                            _expire_eng = _day + " " + _month_eng + " " + _year_eng;
                            _expire_th = _day + " " + _month_th + " " + _year_th;
                            int _in = Integer.parseInt(_xx.substring(16, 18));
                            _religion = religions.get(_in);
                            if(_in == 99){ _religion = religions.get(10); }
                            _xx = _issue_eng + "\r\n" + _issue_th + "\r\n" + _expire_eng + "\r\n" + _expire_th + "\r\n" + _religion;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //engexpire.setText(_expire_eng);
                                    //thexpire.setText(_expire_th);
                                    //religion.setText(_religion);
                                }
                            });
                        }
                        break;
                    default:
                }
                return _xx;
            }
        };

        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private static String e(int value)
    {
        String hex = Integer.toHexString(value);
        hex = hex.length() % 2 == 1 ? "0" + hex : hex;
        return hex.toUpperCase();
    }

    public void verifyStoragePermissions(Activity activity) {

        // Check if we have write permission
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);

            }


        } else {
            createPDF();
        }
    }


    public void createPDF(){
        //create document object
        Document document=new Document();

        //output file path
        String outpath= Environment.getExternalStorageDirectory()+"/insurancePDF.pdf";

        try {
            //BaseColor mColorAccent = new BaseColor(255, 255, 255, 255);
            float mHeadingFontSize = 16.0f;
            float mValueFontSize = 26.0f;

            PdfWriter Writer = PdfWriter.getInstance(document, new FileOutputStream(outpath));
            document.open();
            Drawable d = getResources().getDrawable(R.drawable.tsslogo);
            BitmapDrawable bitDw = ((BitmapDrawable) d);
            Bitmap bmp = bitDw.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());
            image.scaleAbsolute(90,30);
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);

            BaseFont font = BaseFont.createFont("assets/fonts/THSarabun.ttf", "tis-620", BaseFont.EMBEDDED);
            Font mOrderIdFont = new Font(font, mHeadingFontSize, Font.NORMAL, BaseColor.BLACK);
            Font mOrderIdFont1 = new Font(font, 14.0f, Font.NORMAL, BaseColor.BLACK);
            Font mOrderIdFont2 = new Font(font, 10.0f, Font.NORMAL, BaseColor.BLACK);
            Font mOrderIdFont3 = new Font(font, 12.0f, Font.NORMAL, BaseColor.BLACK);
            PdfPTable table;
            PdfPCell cell;
            LineDash solid = new SolidLine();
            LineDash dotted = new DottedLine();
            LineDash dashed = new DashedLine();

            table = new PdfPTable(1);
            cell = new PdfPCell(new Phrase("\n"));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, null, null));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.setHeaderRows(50);
            cell = new PdfPCell(new Phrase("",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell = new PdfPCell(new Phrase("         ตารางกรมธรรม์ใจ",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell = new PdfPCell(new Phrase("",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            cell = new PdfPCell(new Phrase("รหัสบริษัท วปส. 8\nCompany Code: OIC AII 08",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("การประกันอุบัติเหตุทางใจ\n(กรณีชาระเบ้ียประกันรายปี)\nคุ้มครอง 24 ชั่วโมง ทั่วโลก\n",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("กรมธรรม์ประกันภัยเลขที่\nPolicy No. 33-วปส.8-2561",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            cell = new PdfPCell(new Phrase("1. ผู้เอาประกันภัย: ชื่อและที่อยู่\n"+name_2,mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("The Insured: Name and Address\n"+address_2,mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("เลขประจำตัวประชาชน\n"+_id_card+"\nอาชีพ\n" +
                    "ชั้นอาชีพ\n" +
                    "อายุ"+age+"\n\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            cell = new PdfPCell(new Phrase("2. ผู้รับประโยชน์: ชื่อและที่อยู่\n"+_thai_name,mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("The Insured: Name and Address\n"+_address,mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ความสัมพันธ์กับผู้เอาประกันภัย\n\n\n\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            cell = new PdfPCell(new Phrase("3. ระยะเวลาประกันภัย: เริ่มต้นวันที่ 29-06-2561 เวลา 11.15 น. สิ้นสุดวันที่ ไม่มีสิ้นสุด เวลา 16.30 น.\n Period of Insurance: From 29-06-2018 at 11.15 hours to at 16.30 hours\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            cell = new PdfPCell(new Phrase("4. จำนวนจำกัดความรับผิด:กรมธรรม์ประกันภัยนี้ให้การคุ้มครองเฉพาะผลของการบาดเจ็บทางร่างกายในข้อที่มี\nจำนวนเงิน เอาประกันภัยระบุไว้เท่านั้น\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("ข้อตกลงคุ้มครอง / เอกสารแนบท้าย\n Insuring Agreement / Endorsement\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("จำนวนเงินเอาประกันภัย (บาท)\n Sum Insured (Baht)",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ความรับผิดส่วนแรก (บาท หรือ วัน)\n Deductible (Baht or days)",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("เบี้ยประกันภัย (บาท)\nPremiums (Baht)",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("คุ้มครองหัวใจ, ความผูกพันธ์ \nและความรู้สึกในทุกกรณี\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ความรับผิดชอบมากมาย",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ร่วมกิจกรรมทุกกรณี",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("หัวใจ",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("เอกสารแนบท้าย: วปส. เป็นแล้ว รักษา \nไม่หาย รักในวปส. เป็นโรคเรื้อรัง\n",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("เบี้ยประกันภัยสำหรับภัยเพิ่ม Additional Premium",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ส่วนลดเบี้ยประกันภัย Premium Discount",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("เบี้ยประกันภัยสุทธิ Net Premium",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ภาษี Tax",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("อากรแสตมป์ Stamps",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("เบี้ยประกันภัยรวม",mOrderIdFont3));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(4);
            cell = new PdfPCell(new Phrase("ตัวแทนประกันวินาศภัย\n [] Agent",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(solid, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("นายหน้าประกันวินาศภัย\n [] Broker",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("ใบอนุญาตเลขที่\n [] License No.",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, solid, solid));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("",mOrderIdFont1));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, solid, solid, solid));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(2);
            cell = new PdfPCell(new Phrase("วันทำสัญญาประกันภัย 29-06-2561\nAgreement made on",mOrderIdFont2));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, null, null));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("วันออกกรมธรรม์ประกันภัย 29-06-2561\n Policy issued on",mOrderIdFont2));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, null, null));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            cell = new PdfPCell(new Phrase("เพื่อเป็นหลักฐาน บริษัท โดยผู้มีอานาจกระทำการแทนบริษัท ได้ลงลายมือชื่อและประทับตราของบริษัทไว้เป็นสำคัญ ณ สำนักงานของบริษัท\nAs evidence, the Company has caused this policy to be signed by duly authorized persons and the Company’s stamp to be affixed at its office",mOrderIdFont2));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setCellEvent(new CustomBorder(null, null, null, null));
            table.addCell(cell);
            document.add(table);


            document.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    interface LineDash {
        public void applyLineDash(PdfContentByte canvas);
    }

    static class SolidLine implements LineDash {
        public void applyLineDash(PdfContentByte canvas) { }
    }

    static class DottedLine implements LineDash {
        public void applyLineDash(PdfContentByte canvas) {
            canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
            canvas.setLineDash(0, 4, 2);
        }
    }

    static class DashedLine implements LineDash {
        public void applyLineDash(PdfContentByte canvas) {
            canvas.setLineDash(3, 3);
        }
    }

    static class CustomBorder implements PdfPCellEvent {
        protected LineDash left;
        protected LineDash right;
        protected LineDash top;
        protected LineDash bottom;
        public CustomBorder(LineDash left, LineDash right,
                            LineDash top, LineDash bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
        public void cellLayout(PdfPCell cell, Rectangle position,
                               PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            if (top != null) {
                canvas.saveState();
                top.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getTop());
                canvas.lineTo(position.getLeft(), position.getTop());
                canvas.stroke();
                canvas.restoreState();
            }
            if (bottom != null) {
                canvas.saveState();
                bottom.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getBottom());
                canvas.lineTo(position.getLeft(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
            if (right != null) {
                canvas.saveState();
                right.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getTop());
                canvas.lineTo(position.getRight(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
            if (left != null) {
                canvas.saveState();
                left.applyLineDash(canvas);
                canvas.moveTo(position.getLeft(), position.getTop());
                canvas.lineTo(position.getLeft(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
        }
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    private class myAsyncTask extends AsyncTask<Void, Void, Void> {



        @Override
        protected void onPreExecute() {
            mLoading = new ProgressDialog(PrintOnline.this);
            mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoading.setCanceledOnTouchOutside(false);
            mLoading.setMessage("please wait...");
            mLoading.show();
        }

        protected Void doInBackground(Void... args) {
            verifyStoragePermissions(PrintOnline.this);

            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            File file=new File(Environment.getExternalStorageDirectory()+"/insurancePDF.pdf");
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            if ( mLoading.isShowing()) {
               // mLoading.dismiss();
            }
        }
    }



}
