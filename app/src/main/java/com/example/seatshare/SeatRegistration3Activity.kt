package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SeatRegistration3Activity : AppCompatActivity() {

    private var selectedPlatform: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration3)

        // 선택 전: 불투명도 30% -> 선택 후: 불투명도 100%
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
        // 기본 불투명도 30%
        platforms.forEach { it.alpha = 0.3f }
        // 클릭된 것만 100%
        platforms.forEach { platform ->
            platform.setOnClickListener {
                // 이전 선택된 애 → 다시 50%
                selectedPlatform?.alpha = 0.3f
                // 이번에 선택된 애 → 100%
                platform.alpha = 1.0f
                selectedPlatform = platform
            }
        }

        // 다음으로 버튼
        val seatRegistrationBtn = findViewById<Button>(R.id.seat_registration3_select_button)
        seatRegistrationBtn.setOnClickListener {
            val intent = Intent(this, SeatRegistration4Activity::class.java)
            startActivity(intent)
        }

        // 뒤로가기
        val backBtn = findViewById<TextView>(R.id.seat_registration3_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SeatRegistration2Activity::class.java))
        }
    }
}
