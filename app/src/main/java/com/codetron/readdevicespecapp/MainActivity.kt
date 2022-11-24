package com.codetron.readdevicespecapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textModel: TextView
    private lateinit var textManufacture: TextView
    private lateinit var textBoard: TextView
    private lateinit var textRadio: TextView
    private lateinit var textDeviceID: TextView
    private lateinit var textSerial: TextView

    private val requestId =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            }
        }

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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //textDeviceID.text = getString(R.string.device_id, tm.deviceId)
            textSerial.text = getString(
                R.string.serial,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial()
                else Build.SERIAL
            )
        } else {
            requestId.launch(android.Manifest.permission.READ_PHONE_STATE)
        }
    }
}