package com.example.echoi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashImage: ImageView = findViewById(R.id.splash_image)
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        splashImage.startAnimation(animation)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_out, R.anim.fade_out)
            finish()
        }, 3000) // Total duration should match the animation duration (1500ms fade-in + 1500ms fade-out)
    }
}
