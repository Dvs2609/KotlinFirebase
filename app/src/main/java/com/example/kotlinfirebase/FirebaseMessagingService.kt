package com.example.kotlinfirebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Looper.prepare()

        Handler().post{
            Toast.makeText(baseContext, (message.notification?.title
                    + "\n" + message.notification?.body) , Toast.LENGTH_LONG).show()
        }
        Looper.loop()
    }



}