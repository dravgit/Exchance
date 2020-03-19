package io.anyline.examples;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;

import com.centerm.smartpos.aidl.rfcard.AidlRFCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by KisadaM on 8/2/2017.
 */

public class UnionpayActivity extends devBase {
    private AidlRFCard rfcard = null;

    private ProgressDialog mLoading;
    private Handler mHandler = new Handler();
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            rfcard = AidlRFCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_RFCARD));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void d() throws InterruptedException, ExecutionException {
        Runnable job = new Runnable() {
            boolean _read = false;

            @Override
            public void run() {
                try {
                    rfcard.open();
                    if(rfcard.status() == 1){
                        if(rfcard.reset() != null && !_read){
                            /*if(rfcard.sendAsync(HexUtil.hexStringToByte(_cmd + _thai_id_card)) != null){
                                _read = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.show();
                                    }
                                });
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading.dismiss();
                                    }
                                });
                            }*/
                        }
                    }
                    else{
                        _read = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(rfcard != null) {
                scheduledExecutor.shutdown();
                rfcard.close();
                rfcard = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.activity_unionpay);
        super.onCreate(savedInstanceState);

        mLoading = new ProgressDialog(this);
        mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.setMessage("Reading...");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.tsslogo72);
    }
}
