package io.nethermind.pushnotifications

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

        scanQrCodeButton.setOnClickListener {
            val intent = Intent(this, BarcodeScanner::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkBundle()
    }

    private fun checkBundle(){
        val bundle = intent.extras
        if(bundle?.containsKey("result") == true){
            val message = "Login data is ${bundle.get("result")}"
            Log.d(TAG, message)
            Toast.makeText(this,message , Toast.LENGTH_SHORT).show()
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
            Log.d(TAG, "Token is $msg")
        })
    }
}