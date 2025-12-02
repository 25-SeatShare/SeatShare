package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SeatRegistration3Activity : AppCompatActivity() {

    private var selectedPlatform: ImageView? = null
    private var selectedCarNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration3)

        val departure = intent.getStringExtra("departure")
        val arrive = intent.getStringExtra("arrive")
        val trainKey = intent.getStringExtra("trainKey")  // ★ 추가됨

        val platforms = listOf<ImageView>(
            findViewById(R.id.platform1),
            findViewById(R.id.platform2),
            findViewById(R.id.platform3),
            findViewById(R.id.platform4),
            findViewById(R.id.platform5),
            findViewById(R.id.platform6),
            findViewById(R.id.platform7),
            findViewById(R.id.platform8),
        )

        platforms.forEach { it.alpha = 0.3f }

        val platformNumbers = listOf<TextView>(
            findViewById(R.id.platform1_text),
            findViewById(R.id.platform2_text),
            findViewById(R.id.platform3_text),
            findViewById(R.id.platform4_text),
            findViewById(R.id.platform5_text),
            findViewById(R.id.platform6_text),
            findViewById(R.id.platform7_text),
            findViewById(R.id.platform8_text),
        )

        platforms.forEachIndexed { index, platform ->
            platform.setOnClickListener {
                selectedPlatform?.alpha = 0.3f
                platform.alpha = 1.0f
                selectedPlatform = platform
                selectedCarNumber = platformNumbers[index].text.toString()
            }
        }

        findViewById<Button>(R.id.seat_registration3_select_button).setOnClickListener {

            if (selectedPlatform == null) {
                android.widget.Toast.makeText(this, "플랫폼을 선택해주세요", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeatRegistration4Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            intent.putExtra("car_number", selectedCarNumber)
            intent.putExtra("trainKey", trainKey)   // ★ 4로 전달
            startActivity(intent)
        }

        findViewById<TextView>(R.id.seat_registration3_back_button).setOnClickListener {
            startActivity(Intent(this, SeatRegistration2Activity::class.java))
        }
    }
}
