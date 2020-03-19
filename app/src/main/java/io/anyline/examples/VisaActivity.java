package io.anyline.examples;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import com.centerm.smartpos.aidl.iccard.AidlICCard;
import com.centerm.smartpos.aidl.magcard.AidlMagCard;
import com.centerm.smartpos.aidl.magcard.AidlMagCardListener;
import com.centerm.smartpos.aidl.magcard.TrackData;
import com.centerm.smartpos.aidl.rfcard.AidlRFCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by KisadaM on 7/16/2017.
 */

public class VisaActivity extends devBase {

    private AidlMagCard magCard = null;
    private AidlICCard iccard = null;
    private AidlRFCard rfcard = null;
    ArrayList<String> trackInfo = new ArrayList();
    private final Charset _UTF8_CHARSET = Charset.forName("TIS-620");
    int speed = 0;
    private ProgressDialog mLoading;
    private TextView xid;
    private TextView x4char;
    private TextView xvali;
    private Handler mHandler = new Handler();
    boolean _MagCardread = false;
    boolean _ICread = false;
    boolean _RFread = false;
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.setContentView(R.layout.activity_visa);
            super.onCreate(savedInstanceState);
            xid = (TextView) findViewById(R.id.tNumber);
            xid.setText("");
            x4char = (TextView) findViewById(R.id.char4);
            x4char.setText("");
            xvali = (TextView) findViewById(R.id.vali);
            xvali.setText("");

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
        scheduledExecutor.shutdown();
        magCard = null;
        iccard = null;
        rfcard = null;
    }

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            magCard = AidlMagCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_MAGCARD));
            iccard = AidlICCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD));
            rfcard = AidlRFCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_RFCARD));
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                if (magCard != null)
                    dMAG();

                if (iccard != null)
                    dIC();

                if (rfcard != null)
                    dRF();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void dMAG() throws InterruptedException, ExecutionException {
        Runnable job = new Runnable() {
            @Override
            public void run() {
                try {
                    magCard.open();
                    if (!_ICread)
                        x();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private void c() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        xid.setText("");
                        x4char.setText("");
                        xvali.setText("");
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
                    magCard.swipeCard(2000, new AidlMagCardListener.Stub() {
                        @Override
                        public void onSwipeCardTimeout() throws RemoteException {
                        }

                        @Override
                        public void onSwipeCardSuccess(TrackData arg0)
                                throws RemoteException {
                            final String cardno = arg0.getCardno();
                            final String expdate = arg0.getExpiryDate();
                            //String _xx = hextostring(arg0.getFirstTrackData());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!cardno.startsWith("60")) {
                                        char[] _x = cardno.toUpperCase().toCharArray();
                                        //xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + " " + _x[4] + _x[5] + _x[6] + _x[7] + " " + _x[8] + _x[9] + _x[10] + _x[11] + " " + _x[12] + _x[13] + _x[14] + _x[15]);
                                        xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + "    " + _x[4] + _x[5] + _x[6] + _x[7] + "    XXXX    " + _x[12] + _x[13] + _x[14] + _x[15]);
                                        x4char.setText(" " + _x[0] + _x[1] + _x[2] + _x[3]);
                                        _x = expdate.toUpperCase().toCharArray();
                                        xvali.setText(" " + _x[2] + _x[3] + " / " + _x[0] + _x[1]);
                                    } else {
                                        c();
                                    }
                                    _MagCardread = true;
                                    _RFread = false;
                                    _ICread = false;
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

    public void dIC() throws InterruptedException, ExecutionException {
        Runnable job = new Runnable() {

            @Override
            public void run() {
                try {
                    iccard.open();
                    if (iccard.status() == 1) {
                        if (iccard.reset() != null && !_ICread) {
                            String _j = HexUtil.bcd2str(iccard.sendAsync(HexUtil.hexStringToByte("00A404000E63616C63756C61746F722E617070")));
                            if (iccard.sendAsync(HexUtil.hexStringToByte("00A404000E315041592E5359532E444446303100")) != null) {
                                _RFread = false;
                                _ICread = true;
                                _MagCardread = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.show();
                                    }
                                });
                                String _xxx = HexUtil.bcd2str(iccard.sendAsync(HexUtil.hexStringToByte("00B2010C00")));
                                int _xx = _xxx.toUpperCase().indexOf("4F07") + 4;
                                if(_xx == 3){
                                    _xx = _xxx.toUpperCase().indexOf("4F08") + 4;
                                }
                                final String _cmd = _xxx.substring(_xx, _xx + 14);
                                _xxx = HexUtil.bcd2str(iccard.sendAsync(HexUtil.hexStringToByte("00A4040007" + _cmd + "00")));
                                _xxx = HexUtil.bcd2str(iccard.sendAsync(HexUtil.hexStringToByte("00B2010C00")));

                                _xx = _xxx.toUpperCase().indexOf("5713") + 4;
                                _xx = (_xx == 3 ? _xx + 5 : _xx);
                                final String _cmdx = _xxx.substring(_xx, _xx + 16);
                                final int _l = _xxx.length();
                                final String expdate = _xxx.toUpperCase().substring(_xxx.indexOf(_cmdx) + _cmdx.length() + 1, _xxx.indexOf(_cmdx) + _cmdx.length() + 5);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (_cmd.startsWith("A0000000031010") || _cmd.startsWith("A0000006770101")) {
                                            char[] _x = _cmdx.toUpperCase().toCharArray();
                                            //xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + " " + _x[4] + _x[5] + _x[6] + _x[7] + " " + _x[8] + _x[9] + _x[10] + _x[11] + " " + _x[12] + _x[13] + _x[14] + _x[15]);
                                            xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + "    " + _x[4] + _x[5] + _x[6] + _x[7] + "    XXXX    " + _x[12] + _x[13] + _x[14] + _x[15]);
                                            x4char.setText(" " + _x[0] + _x[1] + _x[2] + _x[3]);
                                            if (n(expdate)) {
                                                _x = expdate.toUpperCase().toCharArray();
                                                xvali.setText(" " + _x[2] + _x[3] + " / " + _x[0] + _x[1]);
                                            } else {
                                                xvali.setText("Canceled Card" );
                                            }
                                        }
                                        mLoading.dismiss();
                                    }
                                });
                            }
                        }
                    } else {
                        _ICread = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!_MagCardread && !_ICread && !_RFread) {
                                    c();
                                    _MagCardread = true;
                                }

                                mLoading.dismiss();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c();
                            mLoading.dismiss();
                        }
                    });
                }
            }

            private boolean n(String _val) {
                boolean _return = false;
                try {
                    Integer.parseInt(_val);
                    _return = true;
                } catch (Exception e) {
                    _return = false;
                }
                return _return;
            }

            private byte[] r(byte[] data) {
                int index = data.length - 1;
                while ((index > 0) && (data[(index - 1)] == 32)) {
                    index--;
                    if (index == 0) {
                        return null;
                    }
                }
                return Arrays.copyOfRange(data, 0, index);
            }


            private void c() {
                xid.setText("");
                x4char.setText("");
                xvali.setText("");
            }
        };

        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void dRF() throws InterruptedException, ExecutionException {
        Runnable job = new Runnable() {

            @Override
            public void run() {
                try {
                    rfcard.open();
                    if (rfcard.status() == 1) {
                        if (rfcard.reset() != null && !_RFread) {
                            String _xxx = HexUtil.bcd2str(rfcard.sendAsync(HexUtil.hexStringToByte("00A404000E325041592E5359532E444446303100")));
                            if (_xxx != null) {
                                _RFread = true;
                                _ICread = false;
                                _MagCardread = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.show();
                                    }
                                });

                                int _xx = _xxx.toUpperCase().indexOf("4F07") + 4;
                                final String _cmd = _xxx.substring(_xx, _xx + 14); //00A4040008 A0000000041010 0200
                                _xxx = HexUtil.bcd2str(rfcard.sendAsync(HexUtil.hexStringToByte("00A4040007" + _cmd + "00")));
                                _xxx = HexUtil.bcd2str(rfcard.sendAsync(HexUtil.hexStringToByte("80A800002383213220400000000000003000000000000008260000000000082614091500338F507800")));//80A80000048302800000

                                _xx = _xxx.toUpperCase().indexOf("5713") + 4;
                                final String _cmdx = _xxx.substring(_xx, _xx + 16);
                                final int _l = _xxx.length();
                                final String expdate = _xxx.toUpperCase().substring(_xxx.indexOf(_cmdx) + _cmdx.length() + 1, _xxx.indexOf(_cmdx) + _cmdx.length() + 5);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (_cmd.startsWith("A0000000031010")) {
                                            char[] _x = _cmdx.toUpperCase().toCharArray();
                                            //xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + " " + _x[4] + _x[5] + _x[6] + _x[7] + " " + _x[8] + _x[9] + _x[10] + _x[11] + " " + _x[12] + _x[13] + _x[14] + _x[15]);
                                            xid.setText(" " + _x[0] + _x[1] + _x[2] + _x[3] + "    " + _x[4] + _x[5] + _x[6] + _x[7] + "    XXXX    " + _x[12] + _x[13] + _x[14] + _x[15]);
                                            x4char.setText(" " + _x[0] + _x[1] + _x[2] + _x[3]);
                                            _x = expdate.toUpperCase().toCharArray();
                                            xvali.setText(" " + _x[2] + _x[3] + " / " + _x[0] + _x[1]);
                                        }
                                        mLoading.dismiss();
                                    }
                                });
                            }
                        }
                    } else {
                        _RFread = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!_MagCardread && !_ICread && !_RFread) {
                                    c();
                                    _MagCardread = true;
                                }

                                mLoading.dismiss();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c();
                            mLoading.dismiss();
                        }
                    });
                }
            }

            private byte[] r(byte[] data) {
                int index = data.length - 1;
                while ((index > 0) && (data[(index - 1)] == 32)) {
                    index--;
                    if (index == 0) {
                        return null;
                    }
                }
                return Arrays.copyOfRange(data, 0, index);
            }


            private void c() {
                xid.setText("");
                x4char.setText("");
                xvali.setText("");
            }
        };

        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private static String e(int value) {
        String hex = Integer.toHexString(value);
        hex = hex.length() % 2 == 1 ? "0" + hex : hex;
        return hex.toUpperCase();
    }
}
