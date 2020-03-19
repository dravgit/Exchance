package io.anyline.examples

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import app.TssServerApplication
import io.anyline.examples.R
import io.anyline.examples.configulations.TinyWebServer
import io.anyline.examples.entity.TransectionModel
import kotlinx.android.synthetic.main.activity_ip_sever.*


class IpSeverActivity : AppCompatActivity(), TinyWebServer.CallListener {
    val KEY_ICCARD = "iccard"
    val KEY_SWIPECARD = "swipecard"
    val KEY_PASSPORT = "passport"
    val KEY_SIGN = "sign"
    val KEY_CAMERA = "camera"
    val KEY_PRINT = "print"
    val KEY_SETUP = "setup"
    var canResponse: Boolean = false
    var response = ""
    override fun onMethodCall(methodName: String?, param: TransectionModel?): String {
        Log.e("panya", "onMethodCall : " + methodName)
        if (methodName.equals(KEY_SETUP)) {
            canResponse = true
        } else if (methodName.equals(KEY_SIGN)) {
            val intent = Intent(this@IpSeverActivity, SignActivity::class.java)
            startActivityForResult(intent,101)
        } else if (methodName.equals(KEY_ICCARD)) {
            val intent = Intent(this@IpSeverActivity, IcCardActivity::class.java)
            startActivityForResult(intent,102)
        }
        else if (methodName.equals(KEY_SWIPECARD)) {
            val intent = Intent(this@IpSeverActivity, SwipeCardActivity::class.java)
            startActivityForResult(intent,103)
        }
        else if (methodName.equals(KEY_PASSPORT)) {
            val intent = Intent(this@IpSeverActivity, PassportReader::class.java)
            startActivityForResult(intent,104)
        }
        else if (methodName.equals(KEY_CAMERA)) {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent,105)
        }
        else if (methodName.equals(KEY_PRINT)) {
            val intent = Intent(this@IpSeverActivity, PrintActivity::class.java)
            startActivityForResult(intent,106)
        }
        while (!canResponse) {
            Thread.sleep(1000)
        }
        canResponse = false
        return response
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        response = data?.getStringExtra("response").toString()
        canResponse = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip_sever)
        txt_ip.text = TssServerApplication.ipAddress
    }

    override fun onResume() {
        super.onResume()
        TinyWebServer.startServer(TssServerApplication.ipAddress, 9000, "/web/public_html", this)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onPause() {
        super.onPause()
        TinyWebServer.stopServer()
    }
}