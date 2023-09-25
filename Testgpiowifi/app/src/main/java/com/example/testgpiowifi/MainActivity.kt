package com.example.testgpiowifi

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    //private val host = "192.168.86.164"
    private val port = 50007
    private lateinit var socket: Socket
    private lateinit var outputStream: OutputStreamWriter
    private lateinit var inputStream: BufferedReader
    private val TAG = "WifiExample"
    private val textBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hostipText: EditText = findViewById(R.id.ipAddressEditText)
        val textarea = findViewById(R.id.tv_log) as TextView
        textarea.movementMethod = ScrollingMovementMethod()
        initializeViews(textarea, hostipText)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        requestWifiPermission()
    }

    private fun initializeViews(textarea: TextView, hostipText: EditText) {
        val buttons = arrayOf(
            R.id.btn_connect, R.id.btn_start, R.id.btn_stop, R.id.btn_ledon,
            R.id.btn_ledoff, R.id.btn_lcdred, R.id.btn_lcdgreen, R.id.btn_lcdblue,
            R.id.btn_lcdyellow, R.id.btn_lcdcyan, R.id.btn_lcdpurple, R.id.btn_lcdwhite,
            R.id.btn_lcdblack, R.id.btn_servoleft, R.id.btn_servomiddle, R.id.btn_servoright
        )

        for (buttonId in buttons) {
            findViewById<Button>(buttonId).setOnClickListener { onButtonClick(buttonId, textarea, hostipText) }
        }
    }

    private fun onButtonClick(viewId: Int, textarea: TextView, hostipText: EditText) {
        val command = when (viewId) {
            R.id.btn_start -> "start"
            R.id.btn_stop -> "bye"
            R.id.btn_ledon -> "led_on"
            R.id.btn_ledoff -> "led_off"
            R.id.btn_lcdred -> "lcd_red"
            R.id.btn_lcdgreen -> "lcd_green"
            R.id.btn_lcdblue -> "lcd_blue"
            R.id.btn_lcdyellow -> "lcd_yellow"
            R.id.btn_lcdcyan -> "lcd_cyan"
            R.id.btn_lcdpurple -> "lcd_purple"
            R.id.btn_lcdwhite -> "lcd_white"
            R.id.btn_lcdblack -> "lcd_black"
            R.id.btn_servoleft -> "servo_left"
            R.id.btn_servomiddle -> "servo_middle"
            R.id.btn_servoright -> "servo_right"
            else -> ""
        }

        if (viewId == R.id.btn_connect) {
            Thread { establishConnection(textarea, hostipText) }.start()
        } else if (viewId == R.id.btn_stop) {
            Thread {
                sendCommand(command, textarea)
                closeConnection()
            }.start()
        } else {
            Thread { sendCommand(command, textarea) }.start()
        }
    }

    private fun requestWifiPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 1)
        } else {
            showToast("WiFi permission granted already")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showToast("WiFi permission granted")
        } else {
            showError("WiFi permission not granted")
        }
    }

    private fun establishConnection(textarea: TextView, hostipText: EditText) {
        try {
            val host = hostipText.text.toString()
            socket = Socket(host, port)
            outputStream = OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            inputStream = BufferedReader(InputStreamReader(socket.getInputStream()))
            appendText("Connected to server" + host + " : " + port.toString(), textarea)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error sending/receiving command: ${e.message}")
            appendText("Connected to server failed: ${e.message}", textarea)
        }
    }

    private fun sendCommand(command: String, textarea: TextView) {
        try {
            outputStream.write(command)
            outputStream.flush()
            var line: String?

            while (inputStream.readLine().also { line = it } != null) {
                textBuilder.append(line).append("\n")
                runOnUiThread {
                    textarea.text = textBuilder.toString()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error sending/receiving command: ${e.message}")
        } catch (e: InterruptedException) {
            Log.e(TAG, "Thread sleep interrupted: ${e.message}")
        }
    }

    private fun closeConnection() {
        try {
            outputStream.close()
            inputStream.close()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private fun appendText(message: String, textarea: TextView) {
        textBuilder.append("$message\n")
        runOnUiThread { textarea.text = textBuilder.toString() }
    }

    private fun showError(message: String) {
        Log.e("PermissionError", message)
        showToast("PermissionError: $message")
    }
}

