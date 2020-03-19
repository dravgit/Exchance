package io.anyline.examples;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;

/**
 * Created by KisadaM on 7/13/2017.
 */

public abstract class devBase extends AppCompatActivity {
    public AidlDeviceManager manager = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
            super.onPause();
    }

    @Override
    protected void onResume() {
            super.onResume();
        if(!this.getClass().getName().equals(MainActivity.class)){
            bindService();
        }
    }

    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("base", "action:" +intent.getAction());
        }

    }
    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.centerm.smartposservice");
        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            manager = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            manager = AidlDeviceManager.Stub.asInterface(service);
            if (null != manager) {
                onDeviceConnected(manager);
            }
        }
    };

    public abstract void onDeviceConnected(AidlDeviceManager deviceManager);
}
