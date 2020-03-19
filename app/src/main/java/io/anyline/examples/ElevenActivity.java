package io.anyline.examples;

import android.os.Bundle;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;

/**
 * Created by KisadaM on 7/19/2017.
 */

public class ElevenActivity extends devBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.activity_eleven);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {

    }
}
