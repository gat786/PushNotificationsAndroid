package io.nethermind.pushnotifications

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.core.Amplify
import io.nethermind.pushnotifications.view.MainActivity
import kotlinx.android.synthetic.main.activity_feed_screen.*

class FeedScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_screen)

        logoutFromApp.setOnClickListener {
            Amplify.Auth.signOut({
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }, {
                Toast.makeText(this, "Signout Failed", Toast.LENGTH_SHORT).show()
            })
        }
    }
}