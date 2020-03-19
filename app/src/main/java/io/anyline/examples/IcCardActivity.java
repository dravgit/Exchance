package io.anyline.examples;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.centermposoversealib.thailand.AidlIdCardTha;
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener;
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen;
import com.centerm.smartpos.aidl.iccard.AidlICCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class IcCardActivity extends BaseActivity1 {
    private String TAG = getClass().getSimpleName();
    private AidlIdCardTha aidlIdCardTha;
    private AidlICCard aidlIcCard;
    private ImageView photoImg;
    private TextView resultText;
    private Button testBtn;
    public long time1;
    public static long timestart;
    private boolean aidlReady = false;
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private ProgressDialog mLoading;
    private MediaPlayer mediaPlayer;
    private TextView tVLaserNo, tVbp1no,tVChipNo, tVnameTH, tVlastnameENG, tVbirthTH, tVbirthENG, tVfirstnameENG, tVreligion, tVaddress, tVissueTH, tVissueENG, tVexpireTH, tVexpireENG, tVidcard;
    private List<String> months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private List<String> months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.");
    private ImageView iVphoto;
    public static String pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);

//        findViewById(R.id.readBtn).setOnClickListener(this);
//        findViewById(R.id.stopBtn).setOnClickListener(this);
//        findViewById(R.id.readInfoBtn).setOnClickListener(this);
//        findViewById(R.id.readPhotoBtn).setOnClickListener(this);
//        findViewById(R.id.check_statue).setOnClickListener(this);
//        testBtn = findViewById(R.id.stability_test);
//        testBtn.setOnClickListener(this);
//        photoImg = (ImageView) findViewById(R.id.id_image);
//        resultText = (TextView) findViewById(R.id.info_text);
        mLoading = new ProgressDialog(this);
        mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.setMessage("Reading...");
        tVnameTH = findViewById(R.id.tVnameTH);
        tVlastnameENG = findViewById(R.id.tVlastnameENG);
        tVbirthTH = findViewById(R.id.tVbirthTH);
        tVbirthENG = findViewById(R.id.tVbirthENG);
        tVfirstnameENG = findViewById(R.id.tVfirstnameENG);
        tVreligion = findViewById(R.id.tVreligion);
        tVaddress = findViewById(R.id.tVaddress);
        tVissueTH = findViewById(R.id.tVissueTH);
        tVissueENG = findViewById(R.id.tVissueENG);
        tVexpireTH = findViewById(R.id.tVexpireTH);
        tVexpireENG = findViewById(R.id.tVexpireENG);
        tVidcard = findViewById(R.id.tVidcard);
        iVphoto = findViewById(R.id.iVphoto);

        Log.i("C", "Create2");
        bindService();
    }

    @Override
    protected void bindService() {
        super.bindService();
        Intent intent = new Intent();
        intent.setPackage("com.centerm.centermposoverseaservice");
        intent.setAction("com.centerm.CentermPosOverseaService.MANAGER_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }




    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            IBinder device = deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD);
            if (device != null) {
                aidlIcCard = AidlICCard.Stub.asInterface(device);
                if (aidlIcCard != null) {
                    Log.e("MY", "IcCard bind success!");
                    //This is the IC card service object!!!!
                    //I am do nothing now and it is not null.
                    //you can do anything by yourselef later.
                    d();
                } else {
                    Log.e("MY", "IcCard bind fail!");
                }
            }
            device = deviceManager.getDevice(com.centerm.centermposoversealib.constant.Constant.OVERSEA_DEVICE_CODE.OVERSEA_DEVICE_TYPE_THAILAND_ID);
            if (device != null) {
                aidlIdCardTha = AidlIdCardTha.Stub.asInterface(device);
                aidlReady = aidlIdCardTha != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean stTestContinue = false;
    private long invokStart = 0;
    private int invokCount = 0;
    private long totalTime = 0;

    private void startStabilityTest() throws RemoteException {
        invokStart = System.currentTimeMillis();
        aidlIdCardTha.searchIDCard(60000, test);
    }

    AidlIdCardThaListener test = new AidlIdCardThaListener.Stub() {
        @Override
        public void onFindIDCard(ThiaIdInfoBeen idInfoThaBean) throws RemoteException {
            totalTime += (System.currentTimeMillis() - invokStart);
            invokCount++;
            if (stTestContinue) {
                if (checkInfo(idInfoThaBean)) {
                    displayResult("Testing...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                startStabilityTest();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                displayResult("Exception...");
                            }
                        }
                    });
                } else {
                    displayResult("Check Info ERROR:");
                }
            } else {
                displayResult("Cancel:");
            }
        }

        @Override
        public void onTimeout() throws RemoteException {
            displayResult("Timeout:");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stTestContinue = false;
                    testBtn.setText("Start Stability Test");
                }
            });
        }

        @Override
        public void onError(int i, String s) throws RemoteException {
            displayResult("Error:" + i + " " + s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stTestContinue = false;
                    testBtn.setText("Start Stability Test");
                }
            });
        }
    };

    private boolean save = false;
    private String jsonStr;

    private boolean checkInfo(ThiaIdInfoBeen info) {
        if (save) {
            if (jsonStr.equals(info.toJSONString())) {
                return true;
            } else {
                return false;
            }
        } else {
            save = true;
            jsonStr = info.toJSONString();
            showMsg(jsonFormat(jsonStr));
        }
        return true;
    }

    private void showInfo(final String msg, final String second) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                resultText.setText(msg);
                try {
                    JSONObject jObject = new JSONObject(msg);
                    Log.i(TAG, jObject.toString());
                    String thName = jObject.getString("ThaiName");
                    String regex = "(#)+";
                    String output = thName.replaceAll(regex, " ");
                    tVnameTH.setText(output);
                    String id_card = jObject.getString("CitizenId");
                    id_card = id_card.charAt(0) + "-" + id_card.charAt(1) + id_card.charAt(2) +
                            id_card.charAt(3) + id_card.charAt(4) + "-" + id_card.charAt(5) +
                            id_card.charAt(6) + id_card.charAt(7) + id_card.charAt(8) + id_card.charAt(9) +
                            "-" + id_card.charAt(10) + id_card.charAt(11) + "-" + id_card.charAt(12);
                    id_card = id_card.substring(0, 11) + "X-XX-X";
                    tVidcard.setText(id_card);
                    String engname = jObject.getString("EnglishLastName");
                    tVlastnameENG.setText(jObject.getString("EnglishLastName"));
                    String _xx = jObject.getString("BirthDate");
//                    String _day = "" + Integer.parseInt(_xx.substring(0, 2));
//                    String _month_eng = months_eng.get(Integer.parseInt(_xx.substring(2, 4))-1);
//                    String _month_th = months_th.get(Integer.parseInt(_xx.substring(2, 4))-1);
//                    String _year_th = _xx.substring(4, 8);
//                    String _year_eng = "" + (Integer.parseInt(_xx.substring(4, 8)) - 543);
//                    _year_th = _year_th.substring(0,2)+"XX";
//                    _year_eng = _year_eng.substring(0,2)+"XX";
                    String _birth_eng = getDateFromJson(_xx, "en");
                    String _birth_th = getDateFromJson(_xx, "th");
                    tVbirthTH.setText(_birth_th);
                    tVbirthENG.setText(_birth_eng);
                    tVfirstnameENG.setText(jObject.getString("EnglishFirstName"));
//                    tVreligion
                    String addr = jObject.getString("Address");
                    addr = addr.replaceAll(regex, " ");
                    tVaddress.setText(addr);
                    String _issueTH = getDateFromJson(jObject.getString("CardIssueDate"), "th");
                    tVissueTH.setText(_issueTH);
                    String _issueEn = getDateFromJson(jObject.getString("CardIssueDate"), "en");
                    tVissueENG.setText(_issueEn);
                    String _expTh = getDateFromJson(jObject.getString("CardExpireDate"), "th");
                    tVexpireTH.setText(_expTh);
                    String _expEn = getDateFromJson(jObject.getString("CardExpireDate"), "en");
                    tVexpireENG.setText(_expEn);
                    tVreligion.setText(jObject.optString("Religion", ""));
                    // String result = output + " " + id_card + " " + jObject.getString("EnglishFirstName") + " " + engname + " " + _birth_th + " " + _birth_eng + " " + addr + " " + _issueTH + " " + _issueEn + " " + _expTh + " " + _expEn + " " + jObject.getString("Religion");
                    Toast.makeText(IcCardActivity.this,
                            "time running is " + second + "s", Toast.LENGTH_LONG).show();
//                    JSONObject resultofjson = new JSONObject();
//                    resultofjson.put("resultjson",result);
                    //JSONObject obj1 = new JSONObject();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getDateFromJson(String date, String reg) {

        String _day = "" + Integer.parseInt(date.substring(0, 2));
        String _month_eng;
        String _month_th;
        String _year_th;
        String _year_eng;
        String _birth_eng;
        String _birth_th;
        if (reg.equals("en")) {
            _month_eng = months_eng.get(Integer.parseInt(date.substring(2, 4)) - 1);
            _year_eng = "" + (Integer.parseInt(date.substring(4, 8)) - 543);
            _year_eng = _year_eng.substring(0, 2) + "XX";
            _birth_eng = _day + " " + _month_eng + " " + _year_eng;
            return _birth_eng;
        } else if (reg.equals("th")) {
            _month_th = months_th.get(Integer.parseInt(date.substring(2, 4)) - 1);
            _year_th = date.substring(4, 8);
            _year_th = _year_th.substring(0, 2) + "XX";
            _birth_th = _day + " " + _month_th + " " + _year_th;
            return _birth_th;
        }

        return "";
    }

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.setText(msg);
            }
        });
    }

    private void showPhoto(final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iVphoto.setImageBitmap(bmp);
            }
        });
    }

    private void displayResult(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.setText(msg);
                if (invokCount == 0) {
                    return;
                }
                resultText.append("\nInvok " + invokCount + " times\n");
                resultText.append("Total Consume " + (totalTime / 1000f) + " s\n");
                resultText.append("Average Consume " + (totalTime / invokCount) + " ms\n");
            }
        });
    }

    private String jsonFormat(String s) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < s.length(); index++) {
            //获取s中的每个字符
            char c = s.charAt(index);
            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    public void d() throws InterruptedException, ExecutionException {
//        final MediaPlayer beep = MediaPlayer.create(this, beep_sound);
//        beep.start();
        Runnable job = new Runnable() {

            boolean _read = false;

            @Override
            public void run() {
                try {

                    aidlIcCard.open();
                    if (aidlIcCard.status() == 1) {
                        if (!_read) {
                            _read = true;
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        if (!(IcCardActivity.this).isFinishing()) {
                                            try {
                                                mLoading.show();
                                            } catch (WindowManager.BadTokenException e) {
                                                Log.e("WindowManagerBad ", e.toString());
                                            }
                                        }

//                                        mLoading.show();
//                                        resultText.setText("");

                                        iVphoto.setImageBitmap(null);
                                        time1 = System.currentTimeMillis();
                                        timestart = time1;
//                                        showMsg("Reading...");
                                        aidlIdCardTha.searchIDCard(6000, new AidlIdCardThaListener.Stub() {
                                            @Override
                                            public void onFindIDCard(final ThiaIdInfoBeen been) throws RemoteException {

                                                String s = been.toJSONString();
                                                Log.i(TAG, s);
                                                long end = System.currentTimeMillis();
                                                Bitmap rebmp = Bitmap.createScaledBitmap(been.getPhoto(), 85, 100, false);
                                                pic = convert(rebmp);
                                                Log.i(TAG, pic);
                                                showPhoto(been.getPhoto());
                                                int b = (int) ((end - time1)/1000);
                                                int c = (int) (((end - time1)/100)%10);
//                                                showMsg(jsonFormat(s) + "\ncost: " + (end - time1) + " ms");
                                                showInfo(jsonFormat(s), (b + "." + c));
//                                                runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        tVChipNo.setText(been.getChipNo().substring(0, been.getChipNo().length()-4).concat("XXXX")+ "\n");
//                                                        tVbp1no.setText(been.getBp1no().substring(0, been.getBp1no().length()-4).concat("XXXX")+ "\n");
//                                                        tVLaserNo.setText(been.getLaserNumber().substring(0, been.getLaserNumber().length()-8).concat("XXXX"));
//                                                    }
//                                                });

                                                mLoading.dismiss();

//                                                Intent intent = new Intent();
//                                                //Because the data of the picture is too large, so not added here.
//                                                intent.putExtra("result", s);
//                                                setResult(RESULT_OK, intent);
//                                                finish();
                                            }

                                            @Override
                                            public void onTimeout() throws RemoteException {
                                                log("TIME OUT");
                                                mLoading.dismiss();
                                            }

                                            @Override
                                            public void onError(int i, String s) throws RemoteException {
                                                log("ERROR CODE:" + i + " msg:" + s);
                                                mLoading.dismiss();
                                            }
                                        });
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }

                    } else {
                        _read = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                c();
                                mLoading.dismiss();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void c() {
        tVidcard.setText("");
        tVnameTH.setText("");
        tVlastnameENG.setText("");
        tVbirthTH.setText("");
        tVbirthENG.setText("");
        tVfirstnameENG.setText("");
        tVreligion.setText("");
        tVaddress.setText("");
        tVissueTH.setText("");
        tVissueENG.setText("");
        tVexpireTH.setText("");
        tVexpireENG.setText("");
        tVidcard.setText("");
//        tVbp1no.setText("");
//        tVLaserNo.setText("");
//        tVChipNo.setText("");
        iVphoto.setImageBitmap(null);
        iVphoto.destroyDrawingCache();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutor.shutdownNow();
        if (aidlIcCard != null) {
            try {
                aidlIcCard.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }
}
