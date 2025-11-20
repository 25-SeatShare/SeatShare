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

        //2에서 값 받기
        val departure = intent.getStringExtra("departure")
        val arrive = intent.getStringExtra("arrive")

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

        // 플랫폼 번호 텍스트들
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

        // 클릭된 것만 100%
        platforms.forEachIndexed { index, platform ->
            platform.setOnClickListener {
                // 이전 선택 → 30%
                selectedPlatform?.alpha = 0.3f

                // 새 선택 → 100%
                platform.alpha = 1.0f
                selectedPlatform = platform

                // ★ 선택된 칸 번호 저장 (필수)
                selectedCarNumber = platformNumbers[index].text.toString()
            }
        }


        // 다음으로 버튼
        val seatRegistrationBtn = findViewById<Button>(R.id.seat_registration3_select_button)
        seatRegistrationBtn.setOnClickListener {

            // 선택 안 했을 때 토스트 띄우고 return
            if (selectedPlatform == null) {
                android.widget.Toast.makeText(this, "플랫폼을 선택해주세요", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, edu.sswu.seatshare.SeatRegistration4Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            intent.putExtra("car_number", selectedCarNumber)
            startActivity(intent)
        }

        // 뒤로가기
        val backBtn = findViewById<TextView>(R.id.seat_registration3_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, edu.sswu.seatshare.SeatRegistration2Activity::class.java))
        }
    }
}
