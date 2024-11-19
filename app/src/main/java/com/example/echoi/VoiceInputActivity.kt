package com.example.echoi

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class VoiceInputActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var blurBackground: View
    private lateinit var listeningEffectContainer: LinearLayout
    private lateinit var liveText: TextView
    private lateinit var voiceInputButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var apiClient: ApiClient
    private lateinit var textToSpeech: TextToSpeech
    private var isListening = false
    private var heightAnimators = mutableListOf<ValueAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_input)

        // Initialize views
        blurBackground = findViewById(R.id.blur_background)
        listeningEffectContainer = findViewById(R.id.listening_effect_container)
        liveText = findViewById(R.id.live_text)
        voiceInputButton = findViewById(R.id.voice_input_button)

        apiClient = ApiClient()
        textToSpeech = TextToSpeech(this, this)

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                liveText.text = "Listening..."
                startAnimation()
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                liveText.text = "Processing..."
                stopAnimation()
                resetButtonState()
                isListening = false
            }
            override fun onError(error: Int) {
                liveText.text = "Error recognizing speech. Please try again."
                stopAnimation()
                resetButtonState()
                isListening = false
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val sttText = matches[0]
                    liveText.text = sttText
                    sendTextToApi(sttText)
                }
                stopAnimation()
                resetButtonState()
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    liveText.text = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Handle voice input button click
        voiceInputButton.setOnClickListener {
            if (isListening) {
                // Stop listening if already listening (Cancel)
                stopListening()
            } else {
                startListening()
            }
        }

        // Check and request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        // Set initial state
        resetToInitialState()
    }

    private fun resetToInitialState() {
        // Initial state setup
        liveText.text = ""
        liveText.visibility = View.VISIBLE
        blurBackground.visibility = View.VISIBLE
        listeningEffectContainer.visibility = View.VISIBLE
        stopAnimation()
        voiceInputButton.text = "Tap to Speak"
        voiceInputButton.setBackgroundColor(ContextCompat.getColor(this, R.color.original_button_color))
        isListening = false
    }

    private fun startListening() {
        // Show listening animation and blur background
        blurBackground.visibility = View.VISIBLE
        listeningEffectContainer.visibility = View.VISIBLE

        // Update button to cancel
        voiceInputButton.text = "Cancel"
        voiceInputButton.setBackgroundResource(R.drawable.voice_button_background) // Set button color to red with rounded edges

        // Start speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        speechRecognizer.startListening(intent)

        // Show live text conversion
        liveText.visibility = View.VISIBLE
        liveText.text = "Listening..."

        // Start animation
        startAnimation()
        isListening = true
    }

    private fun stopListening() {
        // Stop speech recognition
        speechRecognizer.stopListening()

        // Reset to initial state
        resetToInitialState()
    }

    private fun startAnimation() {
        // Clear previous animators
        heightAnimators.clear()

        // Play animation
        val durations = listOf(900L, 1100L, 1000L, 1500L, 1700L) // Slower durations for smoother effect

        for (i in 0 until listeningEffectContainer.childCount) {
            val view = listeningEffectContainer.getChildAt(i)
            val heightAnimator = ValueAnimator.ofFloat(0.9f, 1.05f, 1.0f, 0.7f).apply {
                duration = durations[i % durations.size] // Use different durations
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                addUpdateListener {
                    val animatedValue = it.animatedValue as Float
                    view.pivotY = view.height / 2.0f // Set pivot to center
                    view.scaleY = animatedValue // Animate height with both positive and negative y-axis
                }
            }
            heightAnimators.add(heightAnimator)
            heightAnimator.start()
        }
    }

    private fun stopAnimation() {
        // Stop animation and clear animators
        heightAnimators.forEach { it.cancel() }
        heightAnimators.clear()

        // Reset view scales
        for (i in 0 until listeningEffectContainer.childCount) {
            val view = listeningEffectContainer.getChildAt(i)
            view.scaleY = 1.0f
        }
    }

    private fun resetButtonState() {
        // Update button to tap to speak
        voiceInputButton.text = "Tap to Speak"
        voiceInputButton.setBackgroundColor(ContextCompat.getColor(this, R.color.original_button_color)) // Reset button color
    }

    private fun sendTextToApi(text: String) {
        apiClient.sendTextToApi(text) { responseText ->
            runOnUiThread {
                liveText.text = responseText ?: "Error processing request."
                textToSpeech.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        }
    }
}
