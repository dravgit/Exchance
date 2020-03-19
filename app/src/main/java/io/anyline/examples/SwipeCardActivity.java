package io.anyline.examples;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.smartpos.aidl.magcard.AidlMagCard;
import com.centerm.smartpos.aidl.magcard.AidlMagCardListener;
import com.centerm.smartpos.aidl.magcard.TrackData;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SwipeCardActivity extends devBase {

    private AidlMagCard magCard = null;
    ArrayList<String> trackInfo = new ArrayList();
    int speed = 0;
    private List<String> months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private List<String> months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.");
    private ProgressDialog mLoading;
    private TextView expdate;
    private TextView name;
    private TextView bdate;
    private TextView xid;
    private Handler mHandler = new Handler();
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.setContentView(R.layout.activity_magcard);
            super.onCreate(savedInstanceState);

            expdate = (TextView) findViewById(R.id.expdate);
            expdate.setText("");
            name = (TextView) findViewById(R.id.name);
            name.setText("");
            bdate = (TextView) findViewById(R.id.bdate);
            bdate.setText("");
            xid = (TextView) findViewById(R.id.xid);
            xid.setText("");

            mLoading = new ProgressDialog(this);
            mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoading.setCanceledOnTouchOutside(false);
            mLoading.setMessage("Reading...");

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.tsslogo72);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (magCard != null) {
            scheduledExecutor.shutdown();
            magCard = null;
        }
    }

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            magCard = AidlMagCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_MAGCARD));
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                if (magCard != null)
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
            @Override
            public void run() {
                try {
                    magCard.open();
                    x();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private void c() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        expdate.setText("");
                        name.setText("");
                        bdate.setText("");
                        xid.setText("");
                    }
                });
            }

            private String hextostring(String _hex) {
                try {
                    byte[] bytes = HexUtil.hexStringToByte(_hex);
                    return new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void x() {
                try {
                    magCard.swipeCard(30000, new AidlMagCardListener.Stub() {
                        @Override
                        public void onSwipeCardTimeout() throws RemoteException {
                        }

                        @Override
                        public void onSwipeCardSuccess(TrackData arg0)
                                throws RemoteException {
                            //hextostring(arg0.getFirstTrackData());
                            //hextostring(argc();0.getSecondTrackData());
                            String _name = hextostring(arg0.getFirstTrackData());
                            _name = _name.replace(" ", "");
                            _name = _name.replace("^", "");
                            final String[] _xname = _name.split("\\$");
                            final String[] _second = hextostring(arg0.getSecondTrackData()).substring(6, hextostring(arg0.getSecondTrackData()).length()).split("=");
                            //final String _encry = hextostring(arg0.getEncryptTrackData());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String _expdate = _second[1].substring(0, 4);
                                    if (_expdate.startsWith("9999")) {
                                        expdate.setText("ตลอดชีพ");
                                    } else {
                                        String _y = "" + (Integer.parseInt("20" + _expdate.substring(0, 2)) + 543);
                                        int _m = Integer.parseInt(_expdate.substring(2, 4));
                                        expdate.setText(months_th.get(_m - 1) + " " + _y);
                                    }
                                    name.setText(_xname[2] + " " + _xname[1] + " " + _xname[0]);
                                    String _bdate = _second[1].substring(4, _second[1].length());
                                    String _year = "" + (Integer.parseInt(_bdate.substring(0, 4)) + 543);
                                    int _month = Integer.parseInt(_bdate.substring(4, 6));
                                    int _day = Integer.parseInt(_bdate.substring(6, 8));
                                    bdate.setText(_day + " " + months_th.get(_month - 1) + " " + _year);
                                    char[] _x = _second[0].toUpperCase().toCharArray();
                                    xid.setText(_x[0] + " " + _x[1] + _x[2] + _x[3] + _x[4] + " " + _x[5] + _x[6] + _x[7] + _x[8] + _x[9] + " " + _x[10] + _x[11] + " " + _x[12]);
                                }
                            });
                        }

                        @Override
                        public void onSwipeCardFail() throws RemoteException {
                        }

                        @Override
                        public void onSwipeCardException(int arg0)
                                throws RemoteException {
                        }

                        @Override
                        public void onCancelSwipeCard() throws RemoteException {
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

}
