package com.example.echoi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_SELECT = 2
    private val SHARED_PREFS = "sharedPrefs"
    private val USER_NAME = "userName"
    private val PROFILE_PIC_PATH = "profilePicPath"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.edit_name)
        val saveButton: Button = findViewById(R.id.save_button)
        val changePhotoButton: Button = findViewById(R.id.change_photo_button)

        // Load saved data
        loadProfile()

        changePhotoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun dispatchTakePictureIntent() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Change Photo")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pickPhoto, REQUEST_IMAGE_SELECT)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        profileImageView.setImageBitmap(imageBitmap)
                        saveImageToInternalStorage(imageBitmap)
                    }
                }
                REQUEST_IMAGE_SELECT -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        profileImageView.setImageURI(selectedImageUri)
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                        saveImageToInternalStorage(bitmap)
                    }
                }
            }
        }
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString()
        if (name.isNotEmpty()) {
            // Save the name in SharedPreferences
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(USER_NAME, name)
            editor.apply()
            Log.d("ProfileActivity", "Name saved: $name")
        } else {
            Log.w("ProfileActivity", "Name is empty, not saving")
        }
        finish() // Close the ProfileActivity
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val fileName = "profile_picture.png"
        val file = File(filesDir, fileName)
        try {
            val stream: FileOutputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            // Save the file path in SharedPreferences
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(PROFILE_PIC_PATH, file.absolutePath)
            editor.apply()
            Log.d("ProfileActivity", "Profile image saved to internal storage: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("ProfileActivity", "Failed to save image to internal storage", e)
        }
    }

    private fun loadProfile() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val name = sharedPreferences.getString(USER_NAME, "")
        val profilePicPath = sharedPreferences.getString(PROFILE_PIC_PATH, "")

        nameEditText.setText(name)
        if (!profilePicPath.isNullOrEmpty()) {
            val file = File(profilePicPath)
            if (file.exists()) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                profileImageView.setImageBitmap(bitmap)
                Log.d("ProfileActivity", "Profile loaded: Name - $name, Profile Pic Path - $profilePicPath")
            } else {
                Log.w("ProfileActivity", "Profile Pic Path is invalid or file does not exist")
            }
        } else {
            Log.w("ProfileActivity", "Profile Pic Path is empty")
        }
    }
}
