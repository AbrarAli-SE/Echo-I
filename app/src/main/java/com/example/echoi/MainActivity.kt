package com.example.echoi

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize the ImageView
        val imageView: ImageView = findViewById(R.id.character_icon)

        // Apply rotation animation to the AI logo
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        imageView.startAnimation(rotateAnimation)

        // Apply scale and fade animations to the AI logo
        val scaleAnimation = AnimatorInflater.loadAnimator(this, R.animator.scale) as AnimatorSet
        scaleAnimation.setTarget(imageView)

        val fadeAnimation = AnimatorInflater.loadAnimator(this, R.animator.fade) as AnimatorSet
        fadeAnimation.setTarget(imageView)

        // Combine scale and fade animations
        val combinedAnimation = AnimatorSet()
        combinedAnimation.playTogether(scaleAnimation, fadeAnimation)
        combinedAnimation.start()

        // Apply gradient color transition animation to the background
        val rootView: View = findViewById(R.id.root_layout)
        val layerDrawable = ContextCompat.getDrawable(this, R.drawable.backgroundgradient) as LayerDrawable
        val gradientDrawable = layerDrawable.getDrawable(0) as GradientDrawable

        val colorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, R.color.startColor),
            ContextCompat.getColor(this, R.color.centerColor),
            ContextCompat.getColor(this, R.color.endColor),
            ContextCompat.getColor(this, R.color.startColor)
        ).apply {
            duration = 5000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener { animator ->
                val animatedValue = animator.animatedValue as Int
                gradientDrawable.colors = intArrayOf(
                    animatedValue,
                    ContextCompat.getColor(this@MainActivity, R.color.centerColor),
                    ContextCompat.getColor(this@MainActivity, R.color.endColor)
                )
            }
        }

        colorAnimator.start()
        rootView.background = layerDrawable

        // Set OnClickListener to the voice button to navigate to ChattingActivity
        val voiceButton: ImageButton = findViewById(R.id.voice_button)
        voiceButton.setOnClickListener {
            val intent = Intent(this, ChattingActivity::class.java)
            startActivity(intent)
        }
    }
}
