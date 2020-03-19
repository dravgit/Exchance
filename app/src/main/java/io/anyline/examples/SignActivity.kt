package io.anyline.examples

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_sign.*
import org.bouncycastle.asn1.x500.style.RFC4519Style.c
import java.io.*


class SignActivity : AppCompatActivity() {
    val REQUEST_EXTERNAL_STORAGE = 1
    val PERMISSIONS_STORAGE = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var mSignaturePad: SignaturePad? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        clearsign_btn.setOnClickListener{
            signature_pad.clear()
        }
        mSignaturePad = findViewById(R.id.signature_pad) as SignaturePad
        mSignaturePad!!.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                Toast.makeText(this@SignActivity, "OnStartSigning", Toast.LENGTH_SHORT).show()
            }

            override fun onSigned() {
                save_btn.setEnabled(true)
                clearsign_btn.setEnabled(true)
            }

            override fun onClear() {
                save_btn.setEnabled(false)
                clearsign_btn.setEnabled(false)
            }
        })
        save_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val signatureBitmap: Bitmap = mSignaturePad!!.getSignatureBitmap()
                val output = Intent()
                output.putExtra("signature", signatureBitmap)
                setResult(Activity.RESULT_OK, output)
                finish()
//                if (addJpgSignatureToGallery(signatureBitmap)) {
//                    Toast.makeText(this@SignActivity, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@SignActivity, "Unable to store the signature", Toast.LENGTH_SHORT).show()
//                }
//                if (addSvgSignatureToGallery(mSignaturePad!!.signatureSvg)) {
//                    Toast.makeText(this@SignActivity, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@SignActivity, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show()
//                }

            }
        })
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@SignActivity, "Cannot write images to external storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun getAlbumStorageDir(albumName: String?): File? { // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created")
        }
        return file
    }

    @Throws(IOException::class)
    fun saveBitmapToJPG(bitmap: Bitmap, photo: File?) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
//        canvas.drawBitmap(bitmap, 0, 0, null)
        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.close()
    }

    fun addJpgSignatureToGallery(signature: Bitmap): Boolean {
        var result = false
        try {
            val photo = File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()))
//            saveBitmapToJPG(signature, photo)
            scanMediaFile(photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }



    private fun scanMediaFile(photo: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri: Uri = Uri.fromFile(photo)
        mediaScanIntent.data = contentUri
        this@SignActivity.sendBroadcast(mediaScanIntent)
        try {
            if (contentUri != null) {

            }
        } catch (e: Exception) { //handle exception
        }

    }

    fun addSvgSignatureToGallery(signatureSvg: String?): Boolean {
        var result = false
        try {
            val svgFile = File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()))
            val stream: OutputStream = FileOutputStream(svgFile)
            val writer = OutputStreamWriter(stream)
            writer.write(signatureSvg)
            writer.close()
            stream.flush()
            stream.close()
            scanMediaFile(svgFile)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity the activity from which permissions are checked
     */
    fun verifyStoragePermissions(activity: Activity?) { // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) { // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

}