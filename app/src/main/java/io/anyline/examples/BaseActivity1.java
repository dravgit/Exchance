package io.anyline.examples;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;

public abstract class BaseActivity1 extends Activity {

    protected AidlDeviceManager manager = null;
    protected Context mContext = this;
    private ScrollView scrollView;
    private TextView textView1;
    private TextView textView2;
    private int showLineNum = 0;
    public LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String msg1 = bundle.getString("msg1");
            String msg2 = bundle.getString("msg2");
            int color = bundle.getInt("color");
            updateView(msg1, msg2, color);
        }
    };

    public void showMessage(final String msg1, final String msg2,
                            final int color) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg1", msg1);
        bundle.putString("msg2", msg2);
        bundle.putInt("color", color);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    // 显示单条信息
    public void showMessage(final String msg1, final int color) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg1", msg1);
        bundle.putString("msg2", "");
        bundle.putInt("color", color);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void showMessage(String str) {
        this.showMessage(str, Color.LTGRAY);
    }

    public void clear() {
        linearLayout.removeAllViews();
    }

    public void updateView(final String msg1, final String msg2, final int color) {
        if (showLineNum % 50 == 0) { // 显示够20行的时候重新开始
            linearLayout.removeAllViews();
            showLineNum = 0;
        }
        showLineNum++;
        LayoutInflater inflater = getLayoutInflater();
        textView1.setText(msg1);
        textView2.setText(msg2);
        textView1.setTextColor(Color.DKGRAY);
        textView2.setTextColor(color);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!this.getClass().getName().equals(MainActivity.class)) {
            bindService();
        }
        if (!this.getClass().getName().equals(SwipeCardActivity.class)) {
            bindService();
        }
    }

    protected void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.centerm.smartposservice");
        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    protected void unbindService() {
        if (conn != null) {
            unbindService(conn);
        }
    }

    protected void log(String log) {
        Log.i("Centerm", log);
    }

    /**
     * 服务连接桥
     */
    protected ServiceConnection conn = new ServiceConnection() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }

    public abstract void onDeviceConnected(AidlDeviceManager deviceManager);
}
