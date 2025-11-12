package com.athleticai.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutNotificationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var restTimerJob: Job? = null
    
    companion object {
        const val ACTION_START_REST_TIMER = "START_REST_TIMER"
        const val ACTION_STOP_REST_TIMER = "STOP_REST_TIMER"
        const val EXTRA_REST_SECONDS = "rest_seconds"
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 1
        
        fun startRestTimer(context: Context, restSeconds: Int) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                action = ACTION_START_REST_TIMER
                putExtra(EXTRA_REST_SECONDS, restSeconds)
            }
            context.startForegroundService(intent)
        }
        
        fun stopRestTimer(context: Context) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                action = ACTION_STOP_REST_TIMER
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_REST_TIMER -> {
                val restSeconds = intent.getIntExtra(EXTRA_REST_SECONDS, 90)
                startRestTimer(restSeconds)
            }
            ACTION_STOP_REST_TIMER -> {
                stopRestTimer()
            }
        }
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Rest Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Rest timer notifications during workouts"
                setSound(null, null)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startRestTimer(seconds: Int) {
        // Cancel any existing timer
        restTimerJob?.cancel()
        
        // Start foreground service with initial notification
        val notification = createRestNotification(seconds, seconds)
        startForeground(NOTIFICATION_ID, notification)
        
        // Start countdown
        restTimerJob = serviceScope.launch {
            for (remaining in seconds downTo 0) {
                val notification = if (remaining == 0) {
                    createRestCompleteNotification()
                } else {
                    createRestNotification(remaining, seconds)
                }
                
                NotificationManagerCompat.from(this@WorkoutNotificationService)
                    .notify(NOTIFICATION_ID, notification)
                
                if (remaining > 0) {
                    delay(1000)
                } else {
                    // Rest complete - stop service after short delay
                    delay(3000)
                    stopRestTimer()
                }
            }
        }
    }
    
    private fun stopRestTimer() {
        restTimerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createRestNotification(remainingSeconds: Int, totalSeconds: Int): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%d:%02d", minutes, seconds)
        
        val progress = ((totalSeconds - remainingSeconds).toFloat() / totalSeconds * 100).toInt()
        
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Timer")
            .setContentText("$timeText remaining")
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setSilent(true)
            .build()
    }
    
    private fun createRestCompleteNotification(): Notification {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Complete!")
            .setContentText("Ready for your next set")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}