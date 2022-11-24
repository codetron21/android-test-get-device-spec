package com.codetron.readdevicespecapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

//https://gist.github.com/AlaaEddinAlbarghoth/d2b61f3fef22dbd00507eba531a8a352

class MainActivity : AppCompatActivity() {

    private lateinit var textModel: TextView
    private lateinit var textManufacture: TextView
    private lateinit var textBoard: TextView
    private lateinit var textRadio: TextView
    private lateinit var textDeviceID: TextView
    private lateinit var textSerial: TextView

    private val phoneStatePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                textDeviceID.text = getString(R.string.device_id, getDeviceId())
                textSerial.text = getString(R.string.serial, getDeviceSerial(this))
            }
        }

    private val phoneMultipleStatePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.READ_PHONE_STATE] == true && it[Manifest.permission.READ_PRECISE_PHONE_STATE] == true) {
                textSerial.text = getString(R.string.serial, getDeviceSerial(this))
            }
        }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textModel = findViewById(R.id.text_model)
        textManufacture = findViewById(R.id.text_manufacture)
        textBoard = findViewById(R.id.text_board)
        textRadio = findViewById(R.id.text_radio)
        textDeviceID = findViewById(R.id.text_device_id)
        textSerial = findViewById(R.id.text_serial)

        textModel.text = getString(R.string.model, Build.MODEL)
        textManufacture.text = getString(R.string.manufacture, Build.MANUFACTURER)
        textBoard.text = getString(R.string.board, Build.BOARD)
        textRadio.text = getString(R.string.radio, Build.getRadioVersion())

        textDeviceID.text = getString(R.string.device_id, getDeviceId())
        textSerial.text = getString(R.string.serial, getDeviceSerial(this))
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String? {
        return if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            } else {
                val mTelephony = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                mTelephony.deviceId
            }
        } else {
            phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            null
        }
    }

    @SuppressLint("HardwareIds", "PrivateApi")
    fun getDeviceSerial(applicationContext: Context): String? {
        var serialNumber: String?

        try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)

            serialNumber = get.invoke(c, "gsm.sn1") as String

            when (serialNumber) {
                "" -> serialNumber = get.invoke(c, "ril.serialnumber") as String
            }

            when (serialNumber) {
                "" -> serialNumber = get.invoke(c, "ro.serialno") as String
            }

            when (serialNumber) {
                "" -> serialNumber = get.invoke(c, "sys.serialnumber") as String
            }

            @Suppress("DEPRECATION")
            when (serialNumber) {
                "" -> serialNumber = Build.SERIAL
            }

            when (serialNumber) {
                "" -> serialNumber = null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            serialNumber = null
        }

        if (serialNumber == "unknown") {
            try {
                val c = Class.forName("android.os.SystemProperties")
                val get = c.getMethod(
                    "get",
                    String::class.java,
                    String::class.java
                )
                serialNumber = get.invoke(c, "ril.serialnumber", "unknown") as String
            } catch (ignored: Exception) {
                Toast.makeText(applicationContext, "ignored ${ignored.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && serialNumber == "unknown") {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                serialNumber = Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            } else {
                phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && serialNumber == "unknown") {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_PRECISE_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                serialNumber = Build.getSerial()
            } else {
                phoneMultipleStatePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_PHONE_STATE
                    )
                )
            }
        }

        return serialNumber
    }
}

