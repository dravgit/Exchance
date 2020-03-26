package io.anyline.examples

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.RemoteException
import android.util.Base64
import android.util.Log
import android.view.WindowManager.BadTokenException
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.centerm.centermposoversealib.thailand.AidlIdCardTha
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.smartpos.aidl.iccard.AidlICCard
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import io.anyline.examples.IcCardActivity
import kotlinx.android.synthetic.main.activity_idcard.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class IcCardActivity : BaseActivity1() {
    private val TAG = javaClass.simpleName
    private var aidlIdCardTha: AidlIdCardTha? = null
    private var aidlIcCard: AidlICCard? = null
    private val photoImg: ImageView? = null
    private val resultText: TextView? = null
    private val testBtn: Button? = null
    var time1: Long = 0
    private var aidlReady = false
    var scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mLoading: ProgressDialog? = null
    private val mediaPlayer: MediaPlayer? = null
    private val tVLaserNo: TextView? = null
    private val tVbp1no: TextView? = null
    private val tVChipNo: TextView? = null
    private var tVnameTH: TextView? = null
    private var tVlastnameENG: TextView? = null
    private var tVbirthTH: TextView? = null
    private var tVbirthENG: TextView? = null
    private var tVfirstnameENG: TextView? = null
    private var tVreligion: TextView? = null
    private var tVaddress: TextView? = null
    private var tVissueTH: TextView? = null
    private var tVissueENG: TextView? = null
    private var tVexpireTH: TextView? = null
    private var tVexpireENG: TextView? = null
    private var tVidcard: TextView? = null
    private val months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    private var iVphoto: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idcard)
        btn_iccard.setOnClickListener(){
            if(tVnameTH != null && tVidcard != null){
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt("idcard", tVidcard!!.text)
                    jsonObject.putOpt("nameth", tVnameTH!!.text)
                    jsonObject.putOpt("firstnameeng", tVfirstnameENG!!.text)
                    jsonObject.putOpt("lastnameeng", tVlastnameENG!!.text)
                    jsonObject.putOpt("birthth", tVbirthTH!!.text)
                    jsonObject.putOpt("birtheng", tVbirthENG!!.text)
                    jsonObject.putOpt("address", tVaddress!!.text)
                    jsonObject.putOpt("issueth", tVissueTH!!.text)
                    jsonObject.putOpt("issueeng", tVissueENG!!.text)
                    jsonObject.putOpt("exprieth", tVexpireTH!!.text)
                    jsonObject.putOpt("exprieeng", tVexpireENG!!.text)
                    jsonObject.putOpt("image", pic!!.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val data = Intent()
                Log.e("response", jsonObject.toString())
                data.putExtra("response", jsonObject.toString())
                setResult(Activity.RESULT_OK, data)
                finish()
            } else {

            }
        }
        //        findViewById(R.id.readBtn).setOnClickListener(this);
//        findViewById(R.id.stopBtn).setOnClickListener(this);
//        findViewById(R.id.readInfoBtn).setOnClickListener(this);
//        findViewById(R.id.readPhotoBtn).setOnClickListener(this);
//        findViewById(R.id.check_statue).setOnClickListener(this);
//        testBtn = findViewById(R.id.stability_test);
//        testBtn.setOnClickListener(this);
//        photoImg = (ImageView) findViewById(R.id.id_image);
//        resultText = (TextView) findViewById(R.id.info_text);
        mLoading = ProgressDialog(this)
        mLoading!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mLoading!!.setCanceledOnTouchOutside(false)
        mLoading!!.setMessage("Reading...")
        tVnameTH = findViewById(R.id.tVnameTH)
        tVlastnameENG = findViewById(R.id.tVlastnameENG)
        tVbirthTH = findViewById(R.id.tVbirthTH)
        tVbirthENG = findViewById(R.id.tVbirthENG)
        tVfirstnameENG = findViewById(R.id.tVfirstnameENG)
        tVreligion = findViewById(R.id.tVreligion)
        tVaddress = findViewById(R.id.tVaddress)
        tVissueTH = findViewById(R.id.tVissueTH)
        tVissueENG = findViewById(R.id.tVissueENG)
        tVexpireTH = findViewById(R.id.tVexpireTH)
        tVexpireENG = findViewById(R.id.tVexpireENG)
        tVidcard = findViewById(R.id.tVidcard)
        iVphoto = findViewById(R.id.iVphoto)
        Log.i("C", "Create2")
        bindService()
    }

    override fun bindService() {
        super.bindService()
        val intent = Intent()
        intent.setPackage("com.centerm.centermposoverseaservice")
        intent.action = "com.centerm.CentermPosOverseaService.MANAGER_SERVICE"
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager) {
        try {
            var device = deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD)
            if (device != null) {
                aidlIcCard = AidlICCard.Stub.asInterface(device)
                if (aidlIcCard != null) {
                    Log.e("MY", "IcCard bind success!")
                    //This is the IC card service object!!!!
//I am do nothing now and it is not null.
//you can do anything by yourselef later.
                    d()
                } else {
                    Log.e("MY", "IcCard bind fail!")
                }
            }
            device = deviceManager.getDevice(com.centerm.centermposoversealib.constant.Constant.OVERSEA_DEVICE_CODE.OVERSEA_DEVICE_TYPE_THAILAND_ID)
            if (device != null) {
                aidlIdCardTha = AidlIdCardTha.Stub.asInterface(device)
                aidlReady = aidlIdCardTha != null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var stTestContinue = false
    private var invokStart: Long = 0
    private var invokCount = 0
    private var totalTime: Long = 0
    @Throws(RemoteException::class)
    private fun startStabilityTest() {
        invokStart = System.currentTimeMillis()
        aidlIdCardTha!!.searchIDCard(60000, test)
    }

    var test: AidlIdCardThaListener = object : AidlIdCardThaListener.Stub() {
        @Throws(RemoteException::class)
        override fun onFindIDCard(idInfoThaBean: ThiaIdInfoBeen) {
            totalTime += System.currentTimeMillis() - invokStart
            invokCount++
            if (stTestContinue) {
                if (checkInfo(idInfoThaBean)) {
                    displayResult("Testing...")
                    runOnUiThread {
                        try {
                            try {
                                Thread.sleep(200)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            startStabilityTest()
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                            displayResult("Exception...")
                        }
                    }
                } else {
                    displayResult("Check Info ERROR:")
                }
            } else {
                displayResult("Cancel:")
            }
        }

        @Throws(RemoteException::class)
        override fun onTimeout() {
            displayResult("Timeout:")
            runOnUiThread {
                stTestContinue = false
                testBtn!!.text = "Start Stability Test"
            }
        }

        @Throws(RemoteException::class)
        override fun onError(i: Int, s: String) {
            displayResult("Error:$i $s")
            runOnUiThread {
                stTestContinue = false
                testBtn!!.text = "Start Stability Test"
            }
        }
    }
    private var save = false
    private var jsonStr: String? = null
    private fun checkInfo(info: ThiaIdInfoBeen): Boolean {
        if (save) {
            return if (jsonStr == info.toJSONString()) {
                true
            } else {
                false
            }
        } else {
            save = true
            jsonStr = info.toJSONString()
            showMsg(jsonFormat(jsonStr))
        }
        return true
    }

    private fun showInfo(msg: String, second: String) {
        runOnUiThread {
            //                resultText.setText(msg);
            try {
                val jObject = JSONObject(msg)
                Log.i(TAG, jObject.toString())
                val thName = jObject.getString("ThaiName")
                val regex = "(#)+"
                val output = thName.replace(regex.toRegex(), " ")
                tVnameTH!!.text = output
                var id_card = jObject.getString("CitizenId")
                id_card = id_card[0].toString() + "-" + id_card[1] + id_card[2] +
                        id_card[3] + id_card[4] + "-" + id_card[5] +
                        id_card[6] + id_card[7] + id_card[8] + id_card[9] +
                        "-" + id_card[10] + id_card[11] + "-" + id_card[12]
//                id_card = id_card.substring(0, 11) + "X-XX-X"
                tVidcard!!.text = id_card
//                val engname = jObject.getString("EnglishLastName")
                tVlastnameENG!!.text = jObject.getString("EnglishLastName")
                val _xx = jObject.getString("BirthDate")
                //                    String _day = "" + Integer.parseInt(_xx.substring(0, 2));
//                    String _month_eng = months_eng.get(Integer.parseInt(_xx.substring(2, 4))-1);
//                    String _month_th = months_th.get(Integer.parseInt(_xx.substring(2, 4))-1);
//                    String _year_th = _xx.substring(4, 8);
//                    String _year_eng = "" + (Integer.parseInt(_xx.substring(4, 8)) - 543);
//                    _year_th = _year_th.substring(0,2)+"XX";
//                    _year_eng = _year_eng.substring(0,2)+"XX";
                val _birth_eng = getDateFromJson(_xx, "en")
                val _birth_th = getDateFromJson(_xx, "th")
                tVbirthTH!!.text = _birth_th
                tVbirthENG!!.text = _birth_eng
                tVfirstnameENG!!.text = jObject.getString("EnglishFirstName")
                //                    tVreligion
                var addr = jObject.getString("Address")
                addr = addr.replace(regex.toRegex(), " ")
                tVaddress!!.text = addr
                val _issueTH = getDateFromJson(jObject.getString("CardIssueDate"), "th")
                tVissueTH!!.text = _issueTH
                val _issueEn = getDateFromJson(jObject.getString("CardIssueDate"), "en")
                tVissueENG!!.text = _issueEn
                val _expTh = getDateFromJson(jObject.getString("CardExpireDate"), "th")
                tVexpireTH!!.text = _expTh
                val _expEn = getDateFromJson(jObject.getString("CardExpireDate"), "en")
                tVexpireENG!!.text = _expEn
                tVreligion!!.text = jObject.optString("Religion", "")
                // String result = output + " " + id_card + " " + jObject.getString("EnglishFirstName") + " " + engname + " " + _birth_th + " " + _birth_eng + " " + addr + " " + _issueTH + " " + _issueEn + " " + _expTh + " " + _expEn + " " + jObject.getString("Religion");
                Toast.makeText(this@IcCardActivity,
                        "time running is " + second + "s", Toast.LENGTH_LONG).show()
                //                    JSONObject resultofjson = new JSONObject();
//                    resultofjson.put("resultjson",result);
//JSONObject obj1 = new JSONObject();
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun getDateFromJson(date: String, reg: String): String {
        val _day = "" + date.substring(0, 2).toInt()
        val _month_eng: String
        val _month_th: String
        var _year_th: String
        var _year_eng: String
        val _birth_eng: String
        val _birth_th: String
        if (reg == "en") {
            _month_eng = months_eng[date.substring(2, 4).toInt() - 1]
            _year_eng = "" + (date.substring(4, 8).toInt() - 543)
//            _year_eng = _year_eng.substring(0, 2) + "XX"
            _birth_eng = "$_day $_month_eng $_year_eng"
            return _birth_eng
        } else if (reg == "th") {
            _month_th = months_th[date.substring(2, 4).toInt() - 1]
            _year_th = date.substring(4, 8)
//            _year_th = _year_th.substring(0, 2) + "XX"
            _birth_th = "$_day $_month_th $_year_th"
            return _birth_th
        }
        return ""
    }

    private fun showMsg(msg: String) {
        runOnUiThread { resultText!!.text = msg }
    }

    private fun showPhoto(bmp: Bitmap) {
        runOnUiThread { iVphoto!!.setImageBitmap(bmp) }
    }

    private fun displayResult(msg: String) {
        runOnUiThread(Runnable {
            resultText!!.text = msg
            if (invokCount == 0) {
                return@Runnable
            }
            resultText.append("\nInvok $invokCount times\n")
            resultText.append("Total Consume " + totalTime / 1000f + " s\n")
            resultText.append("Average Consume " + totalTime / invokCount + " ms\n")
        })
    }

    private fun jsonFormat(s: String?): String {
        var level = 0
        val jsonForMatStr = StringBuffer()
        for (index in 0 until s!!.length) { //获取s中的每个字符
            val c = s[index]
            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr[jsonForMatStr.length - 1]) {
                jsonForMatStr.append(getLevelStr(level))
            }
            when (c) {
                '{', '[' -> {
                    jsonForMatStr.append(c.toString() + "\n")
                    level++
                }
                ',' -> jsonForMatStr.append(c.toString() + "\n")
                '}', ']' -> {
                    jsonForMatStr.append("\n")
                    level--
                    jsonForMatStr.append(getLevelStr(level))
                    jsonForMatStr.append(c)
                }
                else -> jsonForMatStr.append(c)
            }
        }
        return jsonForMatStr.toString()
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun d() { //        final MediaPlayer beep = MediaPlayer.create(this, beep_sound);
//        beep.start();
        val job: Runnable = object : Runnable {
            var _read = false
            override fun run() {
                try {
                    aidlIcCard!!.open()
                    if (aidlIcCard!!.status().toInt() == 1) {
                        if (!_read) {
                            _read = true
                            runOnUiThread {
                                try {
                                    if (!this@IcCardActivity.isFinishing) {
                                        try {
                                            mLoading!!.show()
                                        } catch (e: BadTokenException) {
                                            Log.e("WindowManagerBad ", e.toString())
                                        }
                                    }
                                    //                                        mLoading.show();
//                                        resultText.setText("");
                                    iVphoto!!.setImageBitmap(null)
                                    time1 = System.currentTimeMillis()
                                    timestart = time1
                                    //                                        showMsg("Reading...");
                                    aidlIdCardTha!!.searchIDCard(6000, object : AidlIdCardThaListener.Stub() {
                                        @Throws(RemoteException::class)
                                        override fun onFindIDCard(been: ThiaIdInfoBeen) {
                                            val s = been.toJSONString()
                                            Log.i(TAG, s)
                                            val end = System.currentTimeMillis()
                                            val rebmp = Bitmap.createScaledBitmap(been.photo, 85, 100, false)
                                            pic = convert(rebmp)
                                            Log.i(TAG, pic)
                                            showPhoto(been.photo)
                                            val b = ((end - time1) / 1000).toInt()
                                            val c = ((end - time1) / 100 % 10).toInt()
                                            //                                                showMsg(jsonFormat(s) + "\ncost: " + (end - time1) + " ms");
                                            showInfo(jsonFormat(s), "$b.$c")
                                            //                                                runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        tVChipNo.setText(been.getChipNo().substring(0, been.getChipNo().length()-4).concat("XXXX")+ "\n");
//                                                        tVbp1no.setText(been.getBp1no().substring(0, been.getBp1no().length()-4).concat("XXXX")+ "\n");
//                                                        tVLaserNo.setText(been.getLaserNumber().substring(0, been.getLaserNumber().length()-8).concat("XXXX"));
//                                                    }
//                                                });
                                            mLoading!!.dismiss()
                                            //                                                Intent intent = new Intent();
//                                                //Because the data of the picture is too large, so not added here.
//                                                intent.putExtra("result", s);
//                                                setResult(RESULT_OK, intent);
//                                                finish();
                                        }

                                        @Throws(RemoteException::class)
                                        override fun onTimeout() {
                                            log("TIME OUT")
                                            mLoading!!.dismiss()
                                        }

                                        @Throws(RemoteException::class)
                                        override fun onError(i: Int, s: String) {
                                            log("ERROR CODE:$i msg:$s")
                                            mLoading!!.dismiss()
                                        }
                                    })
                                } catch (e: RemoteException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        _read = false
                        runOnUiThread {
                            c()
                            mLoading!!.dismiss()
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS)
    }

    private fun c() {
        tVidcard!!.text = ""
        tVnameTH!!.text = ""
        tVlastnameENG!!.text = ""
        tVbirthTH!!.text = ""
        tVbirthENG!!.text = ""
        tVfirstnameENG!!.text = ""
        tVreligion!!.text = ""
        tVaddress!!.text = ""
        tVissueTH!!.text = ""
        tVissueENG!!.text = ""
        tVexpireTH!!.text = ""
        tVexpireENG!!.text = ""
        tVidcard!!.text = ""
        //        tVbp1no.setText("");
//        tVLaserNo.setText("");
//        tVChipNo.setText("");
        iVphoto!!.setImageBitmap(null)
        iVphoto!!.destroyDrawingCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduledExecutor.shutdownNow()
        if (aidlIcCard != null) {
            try {
                aidlIcCard!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        var timestart: Long = 0
        var pic: String? = null
        private fun getLevelStr(level: Int): String {
            val levelStr = StringBuffer()
            for (levelI in 0 until level) {
                levelStr.append("\t")
            }
            return levelStr.toString()
        }

        fun convert(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }
}