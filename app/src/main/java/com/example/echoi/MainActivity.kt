import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputBar = findViewById<EditText>(R.id.input_bar) // Replace with your input bar's ID
        inputBar.setOnClickListener { v: View? ->
            val intent = Intent(
                this@MainActivity,
                ChatActivity::class.java
            )
            startActivity(intent)
        }
    }
}