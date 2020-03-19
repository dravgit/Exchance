package io.anyline.examples;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import at.nineyards.anyline.models.AnylineImage;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.scanviewresult.ScanViewResultActivity;
import io.anyline.examples.util.Constant;


abstract public class ScanActivity extends ScanningConfigurationActivity{


    /**
     * @return the cutout rect of the corresponding {@link AnylineBaseModuleView}
     */
    public abstract Rect getCutoutRect();

    /**
     * @return the actual used {@link AnylineBaseModuleView}
     */
    protected abstract AnylineBaseModuleView getScanView();

    /**
     * @return the module type view {@link io.anyline.examples.ScanModuleEnum.ScanModule}
     */
    protected abstract ScanModuleEnum.ScanModule getScanModule();

    protected long timeStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.tsslogo72);
        //setSupportActionBar(toolbar);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }
    @Override
    public boolean onNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetTime();

    }

    /**
     * resets the time used the calculate how many seconds the scan required from startScanning() until a result has been reported
     */
    protected void resetTime() {
        timeStarted = System.currentTimeMillis();
    }

    protected long milliSecondsPassedSinceStartedScanning() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - timeStarted);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();

    }




    protected String setupImagePath(AnylineImage image){
        String imagePath = "";
        try {
            if(this.getExternalFilesDir(null) != null) {

                imagePath = this
                        .getExternalFilesDir(null)
                        .toString() + "/results/" + "mrz_image";

            }else if(this.getFilesDir() != null){

                imagePath = this
                        .getFilesDir()
                        .toString() + "/results/" + "mrz_image";

            }
            File fullFile = new File(imagePath);
            //create the directory
            fullFile.mkdirs();
            image.save(fullFile, 100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return imagePath;
    }

    protected void startScanResultIntent(String scanMode, HashMap<String, String> scanResult, String path){
       // String path = setupImagePath(anylineOcrResult.getCutoutImage());

        //Intent i = new Intent(getBaseContext(), ScanViewResultActivity.class);
        Intent i = new Intent(getBaseContext(), PassportReader.class);
        i.putExtra(Constant.SCAN_MODULE, scanMode);
        i.putExtra(Constant.SCAN_RESULT_DATA, scanResult);
        i.putExtra(Constant.SCAN_FULL_PICTURE_PATH, path);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
        startActivity(i);
        finish();
    }

}
