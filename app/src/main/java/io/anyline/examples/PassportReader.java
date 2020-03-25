package io.anyline.examples;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.libepassport.readermanager.DataEPassport;
import com.google.libepassport.readermanager.EPassportReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ScanningConfigurationActivity;
import io.anyline.examples.mrz.ScanMrzActivity;
import io.anyline.examples.scanviewresult.ScanViewResultAdapter;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.util.Constant;

public class PassportReader extends ScanningConfigurationActivity {

    private String scanModule;
    private HashMap<String, String> result;
    private ScanViewResultAdapter scanResultAdapter;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView confirmationButton;
    private Button btn_response;
    private EPassportReader mEPassportReader;
    public EditText editNO;
    public DatePicker dpDOB,dpDOE;
    public TextView editEMRZ,editEDocNO,editEDocID,editEGender,
            editETitle,editEFirstname,editELastname,
            editEDOI,editEDOB,editEDOE,editEDOBOri,editEDOEOri,
            editENation,editEIssuerAuthor,editEIssuer,editEPOB,datafromMrz;
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
    String data;

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
        setContentView(R.layout.activity_passport_reader);
        //getLayoutInflater().inflate(R.layout.activity_result_scan_view, (ViewGroup) findViewById(R.id.placeholder));
        //recyclerView = (RecyclerView) findViewById(R.id.recyclerViewer);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        imageView = (ImageView) findViewById(R.id.imageView5);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            result= (HashMap<String, String>) intent.getSerializableExtra(Constant.SCAN_RESULT_DATA);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                scanModule = extras.getString(Constant.SCAN_MODULE, "").trim();

                Bitmap bmp =  BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                imageView.setImageBitmap(bmp);
            }
        } else {
            scanModule = savedInstanceState.getString(Constant.SCAN_MODULE);
        }


        if(result != null ) {
            //setupScanResultView();

            if(result.get(getResources().getString(R.string.mrz_date_of_birthday)).length()==10){

                data = "NAME : "+result.get(getResources().getString(R.string.mrz_given_names))+"\nSURNAME : "+result.get(getResources().getString(R.string.mrz_sur_names)) + "\nSEX : " +result.get(getResources().getString(R.string.mrz_sex))+"\nDATE OF BIRTH : "+result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0,8)+"XX"+
                        "\nDocument type : "+result.get(getResources().getString(R.string.mrz_document_type))+"\nDocument Number : "+result.get(getResources().getString(R.string.mrz_document_number)).substring(0,3)+"XXXX"+"\nExpiration Date : "+result.get(getResources().getString(R.string.mrz_expiration_date))+"\nNationality : "+result.get(getResources().getString(R.string.mrz_country_code));

            }else if(result.get(getResources().getString(R.string.mrz_date_of_birthday)).length()==9){
                data = "NAME : "+result.get(getResources().getString(R.string.mrz_given_names))+"\nSURNAME : "+result.get(getResources().getString(R.string.mrz_sur_names)) + "\nSEX : " +result.get(getResources().getString(R.string.mrz_sex))+"\nDATE OF BIRTH : "+result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0,7)+"XX"+
                        "\nDocument type : "+result.get(getResources().getString(R.string.mrz_document_type))+"\nDocument Number : "+result.get(getResources().getString(R.string.mrz_document_number)).substring(0,3)+"XXXX"+"\nExpiration Date : "+result.get(getResources().getString(R.string.mrz_expiration_date))+"\nNationality : "+result.get(getResources().getString(R.string.mrz_country_code));

            }else if(result.get(getResources().getString(R.string.mrz_date_of_birthday)).length()==8){
                data = "NAME : "+result.get(getResources().getString(R.string.mrz_given_names))+"\nSURNAME : "+result.get(getResources().getString(R.string.mrz_sur_names)) + "\nSEX : " +result.get(getResources().getString(R.string.mrz_sex))+"\nDATE OF BIRTH : "+result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0,6)+"XX"+
                        "\nDocument type : "+result.get(getResources().getString(R.string.mrz_document_type))+"\nDocument Number : "+result.get(getResources().getString(R.string.mrz_document_number)).substring(0,3)+"XXXX"+"\nExpiration Date : "+result.get(getResources().getString(R.string.mrz_expiration_date))+"\nNationality : "+result.get(getResources().getString(R.string.mrz_country_code));

            }else if(result.get(getResources().getString(R.string.mrz_date_of_birthday)).length()==7){
                data = "NAME : "+result.get(getResources().getString(R.string.mrz_given_names))+"\nSURNAME : "+result.get(getResources().getString(R.string.mrz_sur_names)) + "\nSEX : " +result.get(getResources().getString(R.string.mrz_sex))+"\nDATE OF BIRTH : "+result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0,5)+"XXXX"+
                        "\nDocument type : "+result.get(getResources().getString(R.string.mrz_document_type))+"\nDocument Number : "+result.get(getResources().getString(R.string.mrz_document_number)).substring(0,3)+"XXXX"+"\nExpiration Date : "+result.get(getResources().getString(R.string.mrz_expiration_date))+"\nNationality : "+result.get(getResources().getString(R.string.mrz_country_code));
            }
            else if(result.get(getResources().getString(R.string.mrz_date_of_birthday)).length()==6) {
                data = "NAME : " + result.get(getResources().getString(R.string.mrz_given_names)) + "\nSURNAME : " + result.get(getResources().getString(R.string.mrz_sur_names)) + "\nSEX : " + result.get(getResources().getString(R.string.mrz_sex)) + "\nDATE OF BIRTH : " + result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0, 4) + "XXXX" +
                        "\nDocument type : " + result.get(getResources().getString(R.string.mrz_document_type)) + "\nDocument Number : " + result.get(getResources().getString(R.string.mrz_document_number)).substring(0, 3) + "XXXX" + "\nExpiration Date : " + result.get(getResources().getString(R.string.mrz_expiration_date)) + "\nNationality : " + result.get(getResources().getString(R.string.mrz_country_code));
            }
            datafromMrz = (TextView) findViewById(R.id.textView4);
            datafromMrz.setText(data);
        }
//        recyclerView.setAdapter(scanResultAdapter);
        /*confirmationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();

            }
        });*/
        setupScanResult();

        Button butt = (Button) findViewById(R.id.button);
        Button butt1 = (Button) findViewById(R.id.btn_passport);
        butt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA},1000);
                    } else{
                        Intent intent = new Intent(PassportReader.this,ScanMrzActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    Intent intent = new Intent(PassportReader.this,ScanMrzActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        butt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("test","test");
                    jsonObject.put("test1","test1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent data = new Intent();
                data.putExtra("response", jsonObject.toString());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });


        ///////////////////////////////////////
        editNO = findViewById(R.id.editNo);
        dpDOB = findViewById(R.id.dpDOB);
//        dpDOB.init(1980, 1, 1,null);
        dpDOE = findViewById(R.id.dpDOE);
//        dpDOE.init(2018, 1, 1,null);

        imageEFace = findViewById(R.id.imgEFace);
        //editEMRZ = findViewById(R.id.editEMRZ);
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
                    Toast.makeText(PassportReader.this,getString(R.string.msg_nfc_off),Toast.LENGTH_SHORT).show();
                }
            });
            alertbox.show();
        }

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }

    private void setupScanResultView(){
        if(scanModule.equals(getResources().getString(R.string.title_mrz))){

            //for the specific insertion order it is needed a linkedHashMap reconstruct here
            //android transform via bundle transfer the linkedHashMap into a Hashmap
            LinkedHashMap<String, String> orderedHashMap = new LinkedHashMap();

            orderedHashMap.put(getResources().getString(R.string.mrz_given_names), result.get(getResources().getString(R.string.mrz_given_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sur_names), result.get(getResources().getString(R.string.mrz_sur_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sex), result.get(getResources().getString(R.string.mrz_sex)));
            orderedHashMap.put(getResources().getString(R.string.mrz_date_of_birthday), result.get(getResources().getString(R.string.mrz_date_of_birthday)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_type), result.get(getResources().getString(R.string.mrz_document_type)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_number), result.get(getResources().getString(R.string.mrz_document_number)));
            orderedHashMap.put(getResources().getString(R.string.mrz_expiration_date), result.get(getResources().getString(R.string.mrz_expiration_date)));
            orderedHashMap.put(getResources().getString(R.string.mrz_country_code), result.get(getResources().getString(R.string.mrz_country_code)));


            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMap);

        }else if(scanModule.equals(getResources().getString(R.string.title_driving_license))){
            LinkedHashMap<String, String> orderedHashMapDrivingLicense = new LinkedHashMap();

            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_sur_names), result.get(getResources().getString(R.string.driving_license_sur_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_given_names), result.get(getResources().getString(R.string.driving_license_given_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_DOB), result.get(getResources().getString(R.string.driving_license_DOB)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_document_code), result.get(getResources().getString(R.string.driving_license_document_code)));

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMapDrivingLicense);

        }else if(scanModule.equals(getResources().getString(R.string.category_energy))){
            LinkedHashMap<String, String> orderedHashMapEnergy = new LinkedHashMap();

            orderedHashMapEnergy.put(getResources().getString(R.string.reading_result), result.get(getResources().getString(R.string.reading_result)));
            orderedHashMapEnergy.put(getResources().getString(R.string.barcode), result.get(getResources().getString(R.string.barcode)));

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMapEnergy);
        }
        else{

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), result);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    private void askForPermission(String permission, Integer requestCode) {
        String[] permiss = permission.split(",");
        if (ActivityCompat.checkSelfPermission(PassportReader.this, permiss[0]) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PassportReader.this, permiss[0])) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(PassportReader.this, permiss, requestCode);

            } else {

                ActivityCompat.requestPermissions(PassportReader.this, permiss, requestCode);
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
    protected void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        adapter.disableForegroundDispatch(this);
    }


    public String subPattern(String org){
        String subYear,subDay,subMonth,subAll;
        String delimiter = "/";
        String[] subArray = org.split(delimiter);
        if(subArray[2].length() == 4){
            subYear = subArray[2].substring(Math.max(subArray[2].length() - 2, 0));
            if(subArray[1].length()==1){
                subMonth = "0"+subArray[1];
            }else{
                subMonth = subArray[1];
            }
            if(subArray[0].length()==1){
                subDay = "0"+subArray[0];
            }else{
                subDay = subArray[0];
            }
        }else{
            subYear = subArray[2];
            if(subArray[0].length()==1){
                subMonth = "0"+subArray[0];
            }else{
                subMonth = subArray[0];
            }
            if(subArray[1].length()==1){
                subDay = "0"+subArray[1];
            }else{
                subDay = subArray[1];
            }
        }
        subAll = subYear+subMonth+subDay;
        return subAll;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String org_expiration = result.get(getResources().getString(R.string.mrz_expiration_date));
        String org_birthday = result.get(getResources().getString(R.string.mrz_date_of_birthday));
        String docNO = result.get(getResources().getString(R.string.mrz_document_number));
        String docDOB =  subPattern(org_birthday);
        String docDOE = subPattern(org_expiration);
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

            PassportReader.NfcEPassportTask nfcEPassportTask = new PassportReader.NfcEPassportTask(mEPassportReader, IsoDep.get(tag));
            nfcEPassportTask.execute();

        }catch (Exception ex){
            Toast.makeText(PassportReader.this,ex.toString(),Toast.LENGTH_SHORT).show();
//            Toast.makeText(PassportReader.this,pattern+"+++++"+pattern2,Toast.LENGTH_SHORT).show();
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

            //editEMRZ.setText(dataEPassport.getMRZData());
            editEDocNO.setText(dataEPassport.getPassportNo().substring(0,3)+"XXXX");
            editEDocID.setText(dataEPassport.getIdentificationNo().substring(0,8)+"XXXXX");
            editEGender.setText(dataEPassport.getGender());
            editETitle.setText(dataEPassport.getTitleNameEN());
            editEFirstname.setText(dataEPassport.getFirstNameEN());
            editELastname.setText(dataEPassport.getLastNameEN());
            if(dataEPassport.getDateOfIssue() != null) {
                editEDOI.setText(dataEPassport.getDateOfIssue().substring(4, 6) + "/" + dataEPassport.getDateOfIssue().substring(6, 8) + "/" + dataEPassport.getDateOfIssue().substring(0, 4));
            }
            //editEDOBOri.setText(dataEPassport.getDateOfBirthOri());
            editEDOB.setText(dataEPassport.getDateOfBirth().substring(4,6)+"/"+dataEPassport.getDateOfBirth().substring(6,8)+"/"+dataEPassport.getDateOfBirth().substring(0,2)+"XX");
            //editEDOB.setText(result.get(getResources().getString(R.string.mrz_date_of_birthday)).substring(0,8)+"XX");

            //editEDOEOri.setText(dataEPassport.getDateOfExpireOri());
            editEDOE.setText(dataEPassport.getDateOfExpire().substring(4,6)+"/"+dataEPassport.getDateOfExpire().substring(6,8)+"/"+dataEPassport.getDateOfExpire().substring(0,4));
            //editEDOE.setText(result.get(getResources().getString(R.string.mrz_expiration_date)));
            //editEDOE.setText(dataEPassport.getDateOfExpire());
            editENation.setText(dataEPassport.getNationality());
            editEIssuerAuthor.setText(dataEPassport.getMinistryOfForeignAffairs());
            editEIssuer.setText(dataEPassport.getIssuer());
            editEPOB.setText(dataEPassport.getPlaceOfBirth());
           // setUINew(bmp,dataEPassport);
        } else{
            Toast.makeText(PassportReader.this,"Read Error",Toast.LENGTH_SHORT).show();
//            Toast.makeText(PassportReader.this,result.get(getResources().getString(R.string.mrz_document_number))+"  birth date  "+pattern+"  expire  "+pattern2,Toast.LENGTH_SHORT).show();
        }
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
                        Toast.makeText(PassportReader.this,e.toString(),Toast.LENGTH_SHORT).show();
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