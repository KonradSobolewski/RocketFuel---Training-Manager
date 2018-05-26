package com.example.konrad.rocketfuel

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.support.v4.app.ActivityCompat
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog

class QRcode : AppCompatActivity(),ZXingScannerView.ResultHandler{

    private val REQUEST_CAMERA = 1
    private lateinit var  scannerView : ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!checkPermission()) {
                requestPermission()
            }
        }
    }

    private fun checkPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA), REQUEST_CAMERA)
    }

    public override fun onResume() {
        super.onResume()
        val currentApiVersion = android.os.Build.VERSION.SDK_INT
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            } else {
                requestPermission()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        scannerView.stopCamera()
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (!cameraAccepted) {
                    Toast.makeText(applicationContext,  getString(R.string.perNotGranted), Toast.LENGTH_LONG).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            showMessageOKCancel(getString(R.string.allowPer),
                                    DialogInterface.OnClickListener { _, _ ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(CAMERA),
                                                    REQUEST_CAMERA)
                                        }
                                    })
                            return
                        }
                    }
                }
            }
        }
    }

    override fun handleResult(result: Result) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.scanResult))
        builder.setPositiveButton(getString(R.string.ok), { _, _ ->
            scannerView.resumeCameraPreview(this) })
        builder.setNeutralButton(getString(R.string.visit), { _, _ ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.text)))
        })
        builder.setMessage(result.text)
        builder.create().show()
    }
}
