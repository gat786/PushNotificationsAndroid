package io.nethermind.pushnotifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMInit: FirebaseMessagingService() {
    val TAG = "FCMInit";
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed Token: $token");

        sendRegistrationToServer(token);
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d(TAG, "Message is received from the server")
        Log.d(TAG, p0.toString())
    }

    private fun sendRegistrationToServer(token: String) {
        Log.d(TAG, "If you want to send notification to this instance you need this token");
    }
}
