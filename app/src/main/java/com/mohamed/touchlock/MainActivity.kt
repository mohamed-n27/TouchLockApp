package com.mohamed.touchlock

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.permissionButton).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.enableButton).setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
                statusText.text = "الحالة: اللمس مقفول - الأزرار شغالة"
            } else {
                statusText.text = "لازم توافق على إذن العرض الأول (زرار 1)"
                requestOverlayPermission()
            }
        }

        findViewById<Button>(R.id.disableButton).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            statusText.text = "الحالة: اللمس شغال"
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            statusText.text = "الإذن متاح - اضغط زرار 2 للتفعيل"
        }
    }
}
