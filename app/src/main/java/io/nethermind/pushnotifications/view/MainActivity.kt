package io.nethermind.pushnotifications.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import io.nethermind.pushnotifications.FeedScreen
import io.nethermind.pushnotifications.R
import io.nethermind.pushnotifications.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity";
        var firebaseToken = "";

    }

    val model: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFirebaseToken()
        setupAmplify()

        loginWithGoogleButton.setOnClickListener {
//            val intent = Intent(this, BarcodeScanner::class.java)
//            startActivity(intent)

            Amplify.Auth.signInWithSocialWebUI(AuthProvider.google(),
                this,
                {
                    Log.d(TAG, "Signing in successful $it")
                    runOnUiThread {
                        Toast.makeText(this, "Log in Successful", Toast.LENGTH_SHORT).show()
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

        logoutButton.setOnClickListener {
            Amplify.Auth.signOut({
                Log.d(TAG, "Signed out successfully");
                runOnUiThread {
                    model.isLoggedIn.value = false
                }
            }, {
                Log.d(TAG, "Sign out failed");
            })
        }


    }

    fun getCurrentUserData() {
        Amplify.Auth.fetchAuthSession(
            {
                Log.i(TAG, "Auth session = ${it.isSignedIn}")
                runOnUiThread {
                    val intent = Intent(this, FeedScreen::class.java)
                    startActivity(intent)
                }
            },
            { error -> Log.e(TAG, "Failed to fetch auth session", error) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE) {
            Amplify.Auth.handleWebUISignInResponse(data)
        }
    }

    private fun setupAmplify() {
        try {
            Log.i(TAG, "Initialised Amplify")
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            getCurrentUserData()
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialise Amplify", error)
        }
    }

    private fun checkBundle() {
        val bundle = intent.extras
        if (bundle?.containsKey("result") == true) {
            val message = "Login data is ${bundle.get("result")}"
            Log.d(TAG, message)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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