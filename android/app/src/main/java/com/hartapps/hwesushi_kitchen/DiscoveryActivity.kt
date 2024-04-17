package com.hartapps.hwesushi_kitchen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarPrinter
import android.content.SharedPreferences

class DiscoveryActivity: AppCompatActivity(), PrinterAdapter.OnPrinterSelectedListener {
    private var lanIsEnabled = true
    private var bluetoothIsEnabled = true
    private var usbIsEnabled = true
    private var _manager: StarDeviceDiscoveryManager? = null
    private val requestCode = 1000
    private lateinit var printerAdapter: PrinterAdapter
    private lateinit var saveButton: Button
    private lateinit var printerInfoTextView: TextView
    private val PREFS_NAME = "PrinterPrefs"
    private var selectedPrinter: PrinterModel = PrinterModel("", "")
    private val printerList: MutableList<PrinterModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        printerAdapter = PrinterAdapter(printerList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = printerAdapter

        saveButton = findViewById(R.id.savePrinterButton)
        printerInfoTextView = findViewById(R.id.savePrinterInfo)
        saveButton.setOnClickListener { savePrinterIdentifier(selectedPrinter) }
        val checkBoxLan = findViewById<CheckBox>(R.id.checkBoxLan)
        checkBoxLan.setOnClickListener { lanIsEnabled = checkBoxLan.isChecked }

        val checkBoxBluetooth = findViewById<CheckBox>(R.id.checkBoxBluetooth)
        checkBoxBluetooth.setOnClickListener { bluetoothIsEnabled = checkBoxBluetooth.isChecked }

        val checkBoxUsb = findViewById<CheckBox>(R.id.checkBoxUsb)
        checkBoxUsb.setOnClickListener { usbIsEnabled = checkBoxUsb.isChecked }

        val buttonDiscovery = findViewById<Button>(R.id.buttonDiscovery)
        buttonDiscovery.setOnClickListener { onPressDiscoveryButton() }

        // If you are using Android 12 and targetSdkVersion is 31 or later,
        // you have to request Bluetooth permission (Nearby devices permission) to use the Bluetooth printer.
        // https://developer.android.com/about/versions/12/features/bluetooth-permissions
        requestBluetoothPermission()
        displaySavedPrinterInfo()
    }

    private fun onPressDiscoveryButton() {
        printerList.clear()
        saveButton.visibility = View.GONE
        val interfaceTypes = mutableListOf<InterfaceType>()
        if (this.lanIsEnabled) {
            interfaceTypes += InterfaceType.Lan
        }
        if (this.bluetoothIsEnabled) {
            interfaceTypes += InterfaceType.Bluetooth
        }
        if (this.usbIsEnabled) {
            interfaceTypes += InterfaceType.Usb
        }

        // If you are using Android 12 and targetSdkVersion is 31 or later,
        // you have to request Bluetooth permission (Nearby devices permission) to use the Bluetooth printer.
        // https://developer.android.com/about/versions/12/features/bluetooth-permissions
        if (interfaceTypes.contains(InterfaceType.Bluetooth)) {
            if (!hasBluetoothPermission()) {
                showMessage("PERMISSION ERROR")
                Log.d("Discovery", "PERMISSION ERROR: You have to allow Nearby devices to use the Bluetooth printer.")
                return
            }
        }

        if (!isBluetoothEnabled(this)) {
            showMessage("Please enable bluetooth")
        }
        showMessage("Searching...")
        try {
            this._manager?.stopDiscovery()

            _manager = StarDeviceDiscoveryManagerFactory.create(
                interfaceTypes,
                applicationContext
            )
            _manager?.discoveryTime = 10000
            _manager?.callback = object : StarDeviceDiscoveryManager.Callback {
                override fun onPrinterFound(printer: StarPrinter) {
                    printerList.add(PrinterModel(printer.connectionSettings.interfaceType.name.uppercase(), printer.connectionSettings.identifier))
                    saveButton.visibility = View.VISIBLE
                    Log.d("Discovery", "Found printer: ${printer.connectionSettings.identifier}.")
                }

                override fun onDiscoveryFinished() {
                    showMessage("Discovery finished")
                    Log.d("Discovery", "Discovery finished.")
                }
            }

            _manager?.startDiscovery()
        } catch (e: Exception) {
            showMessage("Error: ${e}")
            Log.d("Discovery", "Error: ${e}")
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                ), requestCode
            )
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrinterSelected(printer: PrinterModel) {
        selectedPrinter = printer
    }

    private fun savePrinterIdentifier(printer: PrinterModel) {
        val sharedPrefs: SharedPreferences =
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putString("printerInterfaceType", printer.interfaceType)
        editor.putString("printerIdentifier", printer.identifier)
        editor.apply()
        showMessage("Printer saved")
        displaySavedPrinterInfo()
    }
    private fun displaySavedPrinterInfo() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPrinterInterface = prefs.getString("printerInterfaceType", "") ?: ""
        val savedPrinterIdentifier = prefs.getString("printerIdentifier", "") ?: ""

        val printerInfo = "Connected: $savedPrinterInterface - $savedPrinterIdentifier"
        if (savedPrinterInterface.isBlank() || savedPrinterIdentifier.isBlank()) {
            printerInfoTextView.text = printerInfo
            printerInfoTextView.visibility = View.GONE
        } else {
            printerInfoTextView.visibility = View.VISIBLE
            printerInfoTextView.text = printerInfo
        }
    }
}


fun isBluetoothEnabled(context: Context): Boolean {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Check if Bluetooth is supported on this device
    if (bluetoothAdapter == null) {
        // Bluetooth is not supported
        return false
    }

    // Check if Bluetooth is enabled
    return bluetoothAdapter.isEnabled
}
