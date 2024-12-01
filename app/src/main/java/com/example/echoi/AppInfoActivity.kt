package com.example.echoi

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppInfoActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val infoTextView: TextView = findViewById(R.id.info_text_view)
        infoTextView.text = """
Welcome to EchoI!

EchoI is designed to enhance your communication experience with intuitive voice recognition and seamless messaging capabilities. Stay connected and enjoy smooth interactions with advanced technology at your fingertips.

For any assistance or queries, please contact us at: abrarali.se@gmail.com

Thank you for using EchoI!

        """.trimIndent()
    }
}
