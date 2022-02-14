package io.nethermind.pushnotifications

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG = "LaunchPage";
    var firebaseToken = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseToken()

        copyTokenButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(firebaseToken, firebaseToken);
            clipboard.setPrimaryClip(clip)
        }

        scanQrCodeButton.setOnClickListener {
            val intent = Intent(this, BarcodeScanner::class.java)
            startActivity(intent)
        }
    }

    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            firebaseToken = task.result

            Firebase.messaging.subscribeToTopic("notifications");

            // Log and toast
            val msg = firebaseToken
            deviceTokenTextView.text = firebaseToken
            Log.d(TAG, "Token is $msg")
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }
}