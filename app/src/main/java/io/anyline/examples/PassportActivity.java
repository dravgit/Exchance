package io.anyline.examples;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.assist.AssistStructure;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.libepassport.readermanager.DataEPassport;
import com.google.libepassport.readermanager.EPassportReader;

import java.util.Arrays;

import io.anyline.examples.mrz.ScanMrzActivity;

public class PassportActivity extends AppCompatActivity {
    private EPassportReader mEPassportReader;
    public EditText editNO;
    public DatePicker dpDOB,dpDOE;
    public EditText editEMRZ,editEDocNO,editEDocID,editEGender,
            editETitle,editEFirstname,editELastname,
            editEDOI,editEDOB,editEDOE,editEDOBOri,editEDOEOri,
            editENation,editEIssuerAuthor,editEIssuer,editEPOB;
    public TextView editEMRZs,editEDocNOs,editEDocIDs,editEGenders,
            editETitles,editEFirstnames,editELastnames,
            editEDOIs,editEDOBs,editEDOEs,editEDOBOris,editEDOEOris,
            editENations,editEIssuerAuthors,editEIssuers,editEPOBs;
    public ImageView imageEFace;
    public ImageView imgEFaces;
    public RadioButton radioButtonYes;
    public RadioButton radioButtonMRZ;
    private static final int REQUEST_NFC = 1;
    private static final int REQUEST_PERMISSION = 99;
    private ProgressDialog loadingDialog;
    ;
    private RelativeLayout viewCard;
    Boolean check = false;

    private void setPassNew(){
        imgEFaces = (ImageView) findViewById(R.id.imgEFaces);
        editEDocNOs = (TextView) findViewById(R.id.editEDocNOs);
        editETitles = (TextView) findViewById(R.id.editETitles);
        editEFirstnames = (TextView) findViewById(R.id.editEFirstnames);
        editELastnames = (TextView) findViewById(R.id.editELastnames);
        editEDOBOris = (TextView) findViewById(R.id.editEDOBOris);


        editEDocIDs = (TextView) findViewById(R.id.editEDocIDs);
        editEPOBs = (TextView) findViewById(R.id.editEPOBs);
        editEDOBs = (TextView) findViewById(R.id.editEDOBs);
        editEIssuerAuthors = (TextView) findViewById(R.id.editEIssuerAuthors);
        editEGenders = (TextView) findViewById(R.id.editEGenders);
        editEDOIs = (TextView) findViewById(R.id.editEDOIs);
        editEDOEOris = (TextView) findViewById(R.id.editEDOEOris);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editNO = findViewById(R.id.editNo);
        dpDOB = findViewById(R.id.dpDOB);
        dpDOB.init(1980, 1, 1,null);
        dpDOE = findViewById(R.id.dpDOE);
        dpDOE.init(2018, 1, 1,null);

        imageEFace = findViewById(R.id.imgEFace);
        editEMRZ = findViewById(R.id.editEMRZ);
        editEDocNO = findViewById(R.id.editEDocNO);
        editEDocID = findViewById(R.id.editEDocID);
        editEGender = findViewById(R.id.editEGender);
        editETitle = findViewById(R.id.editETitle);
        editEFirstname = findViewById(R.id.editEFirstname);
        editELastname = findViewById(R.id.editELastname);
        editEDOI = findViewById(R.id.editEDOI);
        editEDOBOri = findViewById(R.id.editEDOBOri);
        editEDOB = findViewById(R.id.editEDOB);
        editEDOEOri = findViewById(R.id.editEDOEOri);
        editEDOE = findViewById(R.id.editEDOE);
        editENation = findViewById(R.id.editENation);
        editEIssuerAuthor = findViewById(R.id.editEIssuerAuthor);
        editEIssuer = findViewById(R.id.editEIssuer);
        editEPOB = findViewById(R.id.editEPOB);
        setPassNew();
        radioButtonYes = findViewById(R.id.RdbYes);
        radioButtonMRZ = findViewById(R.id.mrzmode);

        mEPassportReader = new EPassportReader();
        loadingDialog = ProgressDialog.show(this, "Fetch Data", "Loading...", true, false);
        loadingDialog.dismiss();

        String permis = Manifest.permission.NFC;
        askForPermission(permis,REQUEST_PERMISSION);
        RadioGroup radioGroup=(RadioGroup)findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.mrzmode)
                {
                    if (radioButtonMRZ.isChecked()) {
                        Intent intent = new Intent(PassportActivity.this,ScanMrzActivity.class);
                        startActivity(intent);
                    }


                }
            }
        });

        boolean checkDeviceNFC = Utility.checkDeviceNFC(this);

        if (!checkDeviceNFC){
            Toast.makeText(this,getString(R.string.nfc_not_support),Toast.LENGTH_SHORT).show();
            finish();
        }
        boolean checkEnableNFC = Utility.checkEnableNFC(this);
        if (!checkEnableNFC){
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setTitle("Info");
            alertbox.setMessage(getString(R.string.msg_nfcon));
            alertbox.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivityForResult(intent,REQUEST_NFC);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivityForResult(intent,REQUEST_NFC);
                    }
                }
            });
            alertbox.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(PassportActivity.this,getString(R.string.msg_nfc_off),Toast.LENGTH_SHORT).show();
                }
            });
            alertbox.show();
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        String[] permiss = permission.split(",");
        if (ActivityCompat.checkSelfPermission(PassportActivity.this, permiss[0]) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PassportActivity.this, permiss[0])) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(PassportActivity.this, permiss, requestCode);

            } else {

                ActivityCompat.requestPermissions(PassportActivity.this, permiss, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(getApplicationContext(), this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String[][] filter = new String[][] { new String[] { "android.nfc.tech.IsoDep" } };
        adapter.enableForegroundDispatch(this, pendingIntent, null, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String docNO = editNO.getText().toString();

        String docDOB = Utility.getDateFormatDatePicker(dpDOB);
        String docDOE = Utility.getDateFormatDatePicker(dpDOE);
//        if (docNO.equalsIgnoreCase("") ||
//                docDOB.equalsIgnoreCase("") ||
//                docDOE.equalsIgnoreCase("")){
//            Toast.makeText(MainActivity.this,"กรูณากรอกข้อมูล",Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
                startReadDocument(tag, docNO,docDOB,docDOE);
            }
        }
    }

    private void startReadDocument(Tag tag, String documentNo, String birthDate, String expiryDate) {
        try{
            loadingDialog.dismiss();
            loadingDialog.show();
            if (radioButtonYes.isChecked()){
                mEPassportReader.setKeyAuthen(documentNo,birthDate,expiryDate);
            }

            PassportActivity.NfcEPassportTask nfcEPassportTask = new PassportActivity.NfcEPassportTask(mEPassportReader,IsoDep.get(tag));
            nfcEPassportTask.execute();

        }catch (Exception ex){
            Toast.makeText(PassportActivity.this,ex.toString(),Toast.LENGTH_SHORT).show();
            if (loadingDialog!=null)
                loadingDialog.dismiss();
        }

    }

    private void setDataUI(DataEPassport dataEPassport) {
        if (loadingDialog!=null)
            loadingDialog.dismiss();

        if (dataEPassport != null){
            Bitmap bmp = Utility.byteArraytoBitmap(dataEPassport.getFaceImageByte());
            imageEFace.setImageBitmap(bmp);

            editEMRZ.setText(dataEPassport.getMRZData());
            editEDocNO.setText(dataEPassport.getPassportNo());
            editEDocID.setText(dataEPassport.getIdentificationNo());
            editEGender.setText(dataEPassport.getGender());
            editETitle.setText(dataEPassport.getTitleNameEN());
            editEFirstname.setText(dataEPassport.getFirstNameEN());
            editELastname.setText(dataEPassport.getLastNameEN());
            editEDOI.setText(dataEPassport.getDateOfIssue());
            editEDOBOri.setText(dataEPassport.getDateOfBirthOri());
            editEDOB.setText(dataEPassport.getDateOfBirth());
            editEDOEOri.setText(dataEPassport.getDateOfExpireOri());
            editEDOE.setText(dataEPassport.getDateOfExpire());
            editENation.setText(dataEPassport.getNationality());
            editEIssuerAuthor.setText(dataEPassport.getMinistryOfForeignAffairs());
            editEIssuer.setText(dataEPassport.getIssuer());
            editEPOB.setText(dataEPassport.getPlaceOfBirth());
            setUINew(bmp,dataEPassport);
        } else{
            Toast.makeText(PassportActivity.this,"Read Error",Toast.LENGTH_SHORT).show();
        }
    }
    private void setUINew(Bitmap bmp,DataEPassport dataEPassport){
        imgEFaces.setImageBitmap(bmp);
        editEDocNOs.setText(dataEPassport.getPassportNo());
        editETitles.setText(dataEPassport.getTitleNameEN());
        editEFirstnames.setText(dataEPassport.getFirstNameEN());
        editELastnames.setText(dataEPassport.getLastNameEN());
        editEDOBOris.setText(dataEPassport.getDateOfBirthOri());
        editEDOBs.setText(dataEPassport.getDateOfBirth());
        editEDocIDs.setText(dataEPassport.getIdentificationNo());
        editEPOBs.setText(dataEPassport.getPlaceOfBirth());
        editEIssuerAuthors.setText(dataEPassport.getMinistryOfForeignAffairs());
        editEGenders.setText(dataEPassport.getGender());
        editEDOIs.setText(dataEPassport.getDateOfIssue());
        editEDOEOris.setText(dataEPassport.getDateOfExpireOri());
    }
    public class NfcEPassportTask extends AsyncTask<Void, Void, DataEPassport> {
        private EPassportReader mEPassportReader;
        private IsoDep mIsoDep;
        public NfcEPassportTask(EPassportReader ePassportReader,IsoDep isoDep) {
            mEPassportReader = ePassportReader;
            mIsoDep = isoDep;
        }

        @Override
        protected DataEPassport doInBackground(Void... params) {
            DataEPassport dataEPassport = null;
            try{

                if (radioButtonYes.isChecked()){
                    dataEPassport = mEPassportReader.readEPassportBAC(mIsoDep);
                } else{
                    dataEPassport = mEPassportReader.readEPassport(mIsoDep);
                }

            }catch (final Exception e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(PassportActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                    }

                });

            }
            return dataEPassport;
        }

        @Override
        protected void onPostExecute(DataEPassport result) {
            setDataUI(result);
        }

    }

}

