package com.mohamed.touchlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    private val CHANNEL_ID = "touch_lock_channel"
    private val NOTIF_ID = 1001

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIF_ID, buildNotification())
        addOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun addOverlay() {
        if (overlayView != null) return

        // View شفافة بتاخد كل لمسة وتوقفها (return true من onTouchEvent)
        overlayView = View(this).apply {
            setOnTouchListener { _, _ -> true } // يبتلع أي لمسة
        }

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            // FLAG_NOT_FOCUSABLE مهم جدًا: يخلي الأزرار الفيزيائية (D-Pad / Keys)
            // تكمل شغلها عادي مع أي تطبيق فاتح تحت الطبقة دي، لأن الأوفرلاي
            // مش بياخد الـ Focus بتاع الكيبورد/الأزرار، بس بياخد اللمس بس.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "قفل اللمس",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("قفل اللمس مفعل")
            .setContentText("الأزرار شغالة - اللمس متوقف. افتح التطبيق بالأزرار عشان توقفه")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
