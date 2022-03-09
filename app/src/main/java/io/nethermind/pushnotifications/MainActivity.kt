package io.nethermind.pushnotifications

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity";
        var firebaseToken = "";

        private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
        private var showOneTapUI = true
    }

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseToken()
        setupAmplify()

        scanQrCodeButton.setOnClickListener {
//            val intent = Intent(this, BarcodeScanner::class.java)
//            startActivity(intent)

//            Amplify.Auth.signInWithSocialWebUI(AuthProvider.google(),
//
//                this,
//                {
//                    Log.d(TAG, "Signing in successful ${it.nextStep}")
//                    val currentUser = Amplify.Auth.currentUser;
//                    Log.d(TAG, "Current user is ${currentUser}")
//                },
//                {
//                    Log.d(TAG, "Signing in failed")
//                    it.printStackTrace()
////                    Log.e(TAG, it.stackTrace.toString())
//                }
//            )


            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener {
                    try{
                        startIntentSenderForResult(
                            it.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null)
                    }
                    catch(e: IntentSender.SendIntentException){
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }

                }
                .addOnFailureListener{
                    Log.d(TAG, it.localizedMessage)
                }
        }

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("653454831064-4e876fhhvk5ifbrr0nklmddgi4om715j.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).setAutoSelectEnabled(true)
            .build()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d(TAG, "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d(TAG, "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...

                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
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