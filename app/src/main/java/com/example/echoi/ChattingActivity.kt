package com.example.echoi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ChattingActivity : AppCompatActivity() {
    private lateinit var inputMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiClient: ApiClient
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening = false
    private var listeningHandler: Handler? = null
    private var listeningRunnable: Runnable? = null
    private var dotCount = 0

    private val SHARED_PREFS = "sharedPrefs"
    private val USER_NAME = "userName"
    private val PROFILE_PIC_PATH = "profilePicPath"
    private val MESSAGES_KEY = "messages"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_interface)

        // Initialize views
        inputMessage = findViewById(R.id.input_message)
        sendButton = findViewById(R.id.send_button)
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        val userPicture: ImageView = findViewById(R.id.userPicture)
        val userNameTextView: TextView = findViewById(R.id.user_name)

        // Initialize ChatAdapter
        chatAdapter = ChatAdapter(this)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load saved data
        loadProfileData(userPicture, userNameTextView)
        loadMessages()  // Load messages after initializing chatAdapter

        // Initialize ApiClient
        apiClient = ApiClient()

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {
                startListeningAnimation()
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                stopListeningAnimation()
            }

            override fun onError(error: Int) {
                isListening = false
                Toast.makeText(this@ChattingActivity, "Error recognizing speech. Please try again.", Toast.LENGTH_SHORT).show()
                stopListeningAnimation()
            }

            override fun onResults(results: Bundle?) {
                stopListeningAnimation() // Stop the animation before setting text
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputMessage.setText(matches[0])
                }
                isListening = false
                updateSendButtonState()
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Handle mic/send button click
        sendButton.setOnClickListener {
            if (isListening) {
                speechRecognizer.stopListening()
                isListening = false
                updateSendButtonState()
                stopListeningAnimation()
            } else {
                if (inputMessage.text.isNullOrEmpty()) {
                    startListening()
                } else {
                    sendMessage()
                }
            }
        }

        inputMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Check and request audio recording permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun loadProfileData(userPicture: ImageView, userNameTextView: TextView) {
        try {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            val name = sharedPreferences.getString(USER_NAME, "User Name")
            val profilePicPath = sharedPreferences.getString(PROFILE_PIC_PATH, "")

            userNameTextView.text = name
            if (!profilePicPath.isNullOrEmpty()) {
                val file = File(profilePicPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    userPicture.setImageBitmap(bitmap)
                    Log.d("ChattingActivity", "Profile loaded: Name - $name, Profile Pic Path - $profilePicPath")
                } else {
                    Log.w("ChattingActivity", "Profile Pic Path is invalid or file does not exist")
                }
            } else {
                userPicture.setImageResource(R.drawable.abraralipic) // Default profile picture
                Log.w("ChattingActivity", "Profile Pic Path is empty")
            }
        } catch (e: Exception) {
            Log.e("ChattingActivity", "Error loading profile data", e)
        }
    }

    private fun loadMessages() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val json = sharedPreferences.getString(MESSAGES_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<Message>>() {}.type
            val savedMessages: List<Message> = Gson().fromJson(json, type)
            chatAdapter.addMessages(savedMessages)
        }
    }

    private fun saveMessages(messages: List<Message>) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(messages)
        editor.putString(MESSAGES_KEY, json)
        editor.apply()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        speechRecognizer.startListening(intent)
        isListening = true
        updateSendButtonState()
        startListeningAnimation()
    }

    private fun startListeningAnimation() {
        listeningHandler = Handler(Looper.getMainLooper())
        listeningRunnable = object : Runnable {
            override fun run() {
                val dots = ".".repeat((dotCount % 4) + 1)
                inputMessage.setText("Listening $dots")
                dotCount++
                listeningHandler?.postDelayed(this, 500)
            }
        }
        listeningHandler?.post(listeningRunnable!!)
    }

    private fun stopListeningAnimation() {
        listeningHandler?.removeCallbacks(listeningRunnable!!)
        listeningHandler = null
        listeningRunnable = null
    }

    private fun updateSendButtonState() {
        if (inputMessage.text.isNullOrEmpty()) {
            sendButton.setImageResource(R.drawable.mic)
        } else {
            sendButton.setImageResource(R.drawable.send)
        }
    }

    private fun sendMessage() {
        val messageText = inputMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Display user's message
            val userMessage = Message("User", messageText)
            chatAdapter.addMessage(userMessage)
            chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

            // Save messages
            saveMessages(chatAdapter.getMessages())

            // Send user's message to the API
            apiClient.sendTextToApi(messageText) { response ->
                runOnUiThread {
                    if (response != null) {
                        // Display API response
                        val assistantMessage = Message("Assistant", response)
                        chatAdapter.addMessage(assistantMessage)

                        // Save messages
                        saveMessages(chatAdapter.getMessages())

                        chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                    } else {
                        Toast.makeText(this, "Failed to get response from API", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Clear the input bar and revert the button to mic icon
            inputMessage.text.clear()
            updateSendButtonState()
        }
    }
}
