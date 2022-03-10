package io.nethermind.pushnotifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.IdentityProvider
import com.amazonaws.mobile.client.UserStateDetails
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseToken()
        setupAmplify()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso)

        scanQrCodeButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signinIntent = gsc.signInIntent;
        startActivityForResult(signinIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1000){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_SHORT).show()
                federateWithGoogle(account = account)
            }catch ( exception: ApiException){
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }

        }
    }

    /**
     * Federate with Google authentication
     */
    fun federateWithGoogle(account: GoogleSignInAccount) {
        if (account.idToken != null) {
            Log.d(TAG, "Federating with Google")
//            val userDetails = AWSMobileClient.getInstance().federatedSignIn(IdentityProvider.GOOGLE.name, account.idToken)
//            Log.d(TAG, userDetails.details.toString())

            AWSMobileClient.getInstance().federatedSignIn(IdentityProvider.GOOGLE.toString(), account.idToken, object :
                Callback<UserStateDetails> {
                override fun onResult(details: UserStateDetails) {
                    // onResult is called, but the user information not saved to the cloud
                    Log.d(TAG, "federated")
                }

                override fun onError(e: Exception) {
                    Log.e(TAG, "sign-in error", e)
                }
            })

        } else {
            Log.d(TAG, "Federating with Google (ID token is null, so nothing happening)")
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