package com.example.testgpio

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.util.Log
import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var rfcommSocket: BluetoothSocket
    lateinit var device: BluetoothDevice
    val TAG = "BluetoothExample"
    val LOCATION_PERMISSION_REQUEST_CODE = 123 // You can choose any value you want
    val BLUETOOTH_PERMISSION_REQUEST_CODE = 456
    private val UUID_STR = "00001101-0000-1000-8000-00805F9B34FB"
    private val DEVICE_NAME = "linaro-alip"
    val textBuilder = StringBuilder()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn_connect = findViewById(R.id.btn_connect) as Button
        val btn_start = findViewById(R.id.btn_start) as Button
        val btn_stop = findViewById(R.id.btn_stop) as Button
        val btn_ledon = findViewById(R.id.btn_ledon) as Button
        val btn_ledoff = findViewById(R.id.btn_ledoff) as Button
        val btn_lcdred = findViewById(R.id.btn_lcdred) as Button
        val btn_lcdgreen = findViewById(R.id.btn_lcdgreen) as Button
        val btn_lcdblue = findViewById(R.id.btn_lcdblue) as Button
        val btn_lcdyellow = findViewById(R.id.btn_lcdyellow) as Button
        val btn_lcdcyan = findViewById(R.id.btn_lcdcyan) as Button
        val btn_lcdpurple = findViewById(R.id.btn_lcdpurple) as Button
        val btn_lcdwhite = findViewById(R.id.btn_lcdwhite) as Button
        val btn_lcdblack = findViewById(R.id.btn_lcdblack) as Button
        val btn_servoleft = findViewById(R.id.btn_servoleft) as Button
        val btn_servomiddle = findViewById(R.id.btn_servomiddle) as Button
        val btn_servoright = findViewById(R.id.btn_servoright) as Button
        val textarea = findViewById(R.id.tv_log) as TextView
        textarea.movementMethod = ScrollingMovementMethod()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }

        //This specific action is required since my personal mobile needs GPS enabled to discover devices
        //(not written in any official documentation but needed nonetheless)
        if (!isLocationEnabled(this) && Build.VERSION.SDK_INT <= 30) {
            Toast.makeText(
                this,
                "Location should be enabled since Location services are needed on some devices for correctly locating other Bluetooth devices",
                Toast.LENGTH_SHORT
            ).show()
            enableLocation(this)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            checkAndRequestLocationPermission()
        }

        checkAndRequestBluetoothPermission()

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.btn_start -> sendCommand(rfcommSocket, "start", textarea)
                R.id.btn_stop -> {
                    sendCommand(rfcommSocket, "bye", textarea)
                    rfcommSocket.close()
                }
                R.id.btn_ledon -> sendCommand(rfcommSocket, "led_on", textarea)
                R.id.btn_ledoff -> sendCommand(rfcommSocket, "led_off", textarea)
                R.id.btn_lcdred -> sendCommand(rfcommSocket, "lcd_red", textarea)
                R.id.btn_lcdgreen -> sendCommand(rfcommSocket, "lcd_green", textarea)
                R.id.btn_lcdblue -> sendCommand(rfcommSocket, "lcd_blue", textarea)
                R.id.btn_lcdyellow -> sendCommand(rfcommSocket, "lcd_yellow", textarea)
                R.id.btn_lcdcyan -> sendCommand(rfcommSocket, "lcd_cyan", textarea)
                R.id.btn_lcdpurple -> sendCommand(rfcommSocket, "lcd_purple", textarea)
                R.id.btn_lcdwhite -> sendCommand(rfcommSocket, "lcd_white", textarea)
                R.id.btn_lcdblack -> sendCommand(rfcommSocket, "lcd_black", textarea)
                R.id.btn_servoleft -> sendCommand(rfcommSocket, "servo_left", textarea)
                R.id.btn_servomiddle -> sendCommand(rfcommSocket, "servo_middle", textarea)
                R.id.btn_servoright -> sendCommand(rfcommSocket, "servo_right", textarea)
                R.id.btn_connect -> {
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                    if (pairedDevices != null) {
                        for (pairedDevice in pairedDevices) {
                            if (pairedDevice.name == DEVICE_NAME) {
                                device = pairedDevice
                                break
                            }
                        }
                    }

                    if (device == null) {
                        textBuilder.append("\nDevice not found")
                    } else {
                        textBuilder.append("\nFound device: ${device.name} (${device.address})")
                    }

                    try {
                        rfcommSocket =
                            device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STR))
                        rfcommSocket.connect()
                        textBuilder.append("\nConnected to device: ${device.name} (${device.address})")
                    } catch (e: IOException) {
                        textBuilder.append("\nIOException during connection: ${e.message}")
                    } finally {
                        handler.post { textarea.text = textBuilder.toString() }
                    }
                }
            }
        }

        btn_connect.setOnClickListener(clickListener)
        btn_start.setOnClickListener(clickListener)
        btn_stop.setOnClickListener(clickListener)
        btn_ledon.setOnClickListener(clickListener)
        btn_ledoff.setOnClickListener(clickListener)
        btn_lcdred.setOnClickListener(clickListener)
        btn_lcdgreen.setOnClickListener(clickListener)
        btn_lcdblue.setOnClickListener(clickListener)
        btn_lcdyellow.setOnClickListener(clickListener)
        btn_lcdcyan.setOnClickListener(clickListener)
        btn_lcdpurple.setOnClickListener(clickListener)
        btn_lcdwhite.setOnClickListener(clickListener)
        btn_lcdblack.setOnClickListener(clickListener)
        btn_servoleft.setOnClickListener(clickListener)
        btn_servomiddle.setOnClickListener(clickListener)
        btn_servoright.setOnClickListener(clickListener)
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBtIntent)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(ComponentActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isLocationEnabled
    }

    fun enableLocation(activity: Activity) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivity(intent)
    }

    private fun checkAndRequestBluetoothPermission() {
        if (hasBluetoothPermission()) {
            // Bluetooth permission is already granted
        } else {
            // Request Bluetooth permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestLocationPermission() {
        if (hasLocationPermission()) {
            // Location permission is already granted
            //viewModel.scanForDevices()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            // Check if all permissions were granted
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions were granted
                //viewModel.scanForDevices()
            } else {
                // At least one permission was not granted
                Log.i(TAG, "At least one of the permissions was not granted.")
                Toast.makeText(
                    this,
                    "At least one of the permissions was not granted. Go to app settings and give permissions manually",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location Permission Accepted: Do something
                //viewModel.scanForDevices()
            } else {
                // Location Permission Denied: Do something
                Log.i(TAG, "Permission Denied")
                Toast.makeText(
                    this,
                    "You must manually select the option 'Allow all the time' for location in order for this app to work!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sendCommand(socket: BluetoothSocket, command: String, textarea: TextView) {
        try {
            val charset = Charsets.UTF_8
            val outputStream = socket.outputStream
            val inputStream = socket.inputStream

            outputStream.write(command.toByteArray(charset))
            outputStream.flush()
            
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead != -1) {
                val response = String(buffer, 0, bytesRead)
                textBuilder.append("\n" + response)
                handler.post { textarea.text = textBuilder.toString() }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error sending/receiving command: ${e.message}")
        } catch (e: InterruptedException) {
            Log.e(TAG, "Thread sleep interrupted: ${e.message}")
        }
    }
}
