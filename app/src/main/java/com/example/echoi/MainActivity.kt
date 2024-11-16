package com.example.echoi

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Apply rotation animation to the AI logo
        val imageView: ImageView = findViewById(R.id.character_icon)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        imageView.startAnimation(rotateAnimation)

        // Apply gradient color transition animation to the background
        val rootView: View = findViewById(R.id.root_layout)
//        val gradientDrawable = ContextCompat.getDrawable(this, R.drawable.animated_gradient) as GradientDrawable
//
//        val colorAnimator = ObjectAnimator.ofInt(
//            gradientDrawable,
//            "colors",
//            ContextCompat.getColor(this, R.color.startColor),
//            ContextCompat.getColor(this, R.color.centerColor),
//            ContextCompat.getColor(this, R.color.endColor),
//            ContextCompat.getColor(this, R.color.startColor)
//        ).apply {
//            setEvaluator(ArgbEvaluator())
//            duration = 5000
//            repeatCount = ObjectAnimator.INFINITE
//            repeatMode = ObjectAnimator.REVERSE
//        }
//
//        colorAnimator.start()
//        rootView.background = gradientDrawable
//
//        // Set OnClickListener to the voice button to navigate to ChattingActivity
//        val voiceButton: ImageButton = findViewById(R.id.voice_button)
//        voiceButton.setOnClickListener {
//            val intent = Intent(this, ChattingActivity::class.java)
//            startActivity(intent)
//        }
    }
}
