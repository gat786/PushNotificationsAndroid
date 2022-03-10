package io.nethermind.pushnotifications

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity";
        var firebaseToken = "";
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseToken()
        setupAmplify()

        scanQrCodeButton.setOnClickListener {
//            val intent = Intent(this, BarcodeScanner::class.java)
//            startActivity(intent)

            Amplify.Auth.signInWithSocialWebUI(AuthProvider.google(),
                this,
                {
                    Log.d(TAG, "Signing in successful $it")
                    val currentUser = Amplify.Auth.currentUser;
                    Log.d(TAG, "Current user is ${currentUser}")
                    runOnUiThread {
                        Toast.makeText(this, "Current logged in user is $currentUser", Toast.LENGTH_SHORT).show()
                    }

                },
                {
                    Log.d(TAG, "Signing in failed")

                    it.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Log in failed", Toast.LENGTH_SHORT).show()
                    }
//                    Log.e(TAG, it.stackTrace.toString())
                }
            )
        }
    }

    private fun setupAmplify(){
        try{
            Log.i(TAG, "Initialised Amplify")
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            fetchCurrentUser()
        }catch (error: AmplifyException){
            Log.e(TAG, "Could not initialise Amplify", error)
        }
    }

    private fun fetchCurrentUser(){
        Amplify.Auth.fetchAuthSession(
            { Log.i("AmplifyQuickstart", "Auth session = $it") },
            { error -> Log.e("AmplifyQuickstart", "Failed to fetch auth session", error) }
        )
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