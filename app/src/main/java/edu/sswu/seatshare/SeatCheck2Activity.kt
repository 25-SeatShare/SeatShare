package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SeatCheck2Activity : AppCompatActivity() {

    private var selectedPlatform: ImageView? = null
    private var selectedCarNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_check_2)

        val platforms = listOf<ImageView>(
            findViewById(R.id.platform1_2),
            findViewById(R.id.platform2_2),
            findViewById(R.id.platform3_2),
            findViewById(R.id.platform4_2),
            findViewById(R.id.platform5_2),
            findViewById(R.id.platform6_2),
            findViewById(R.id.platform7_2),
            findViewById(R.id.platform8_2)
        )

        // 기본 투명도 30%
        platforms.forEach { it.alpha = 0.3f }

        val platformNumbers = listOf<TextView>(
            findViewById(R.id.platform1_text_2),
            findViewById(R.id.platform2_text_2),
            findViewById(R.id.platform3_text_2),
            findViewById(R.id.platform4_text_2),
            findViewById(R.id.platform5_text_2),
            findViewById(R.id.platform6_text_2),
            findViewById(R.id.platform7_text_2),
            findViewById(R.id.platform8_text_2)
        )

        // 클릭하면 색 변경 + 번호 저장
        platforms.forEachIndexed { index, img ->
            img.setOnClickListener {

                selectedPlatform?.alpha = 0.3f
                img.alpha = 1.0f
                selectedPlatform = img

                selectedCarNumber = platformNumbers[index].text.toString()
            }
        }

        // 다음 버튼 → SeatCheck3으로
        findViewById<Button>(R.id.seat_check_2_next_button).setOnClickListener {

            if (selectedCarNumber == null) {
                Toast.makeText(this, "칸을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeatCheck3Activity::class.java)
            intent.putExtra("selected_platform", selectedCarNumber!!.toInt())
            startActivity(intent)
        }

        // 뒤로가기
        findViewById<TextView>(R.id.seat_check_2_back_button).setOnClickListener {
            startActivity(Intent(this, SeatCheck1Activity::class.java))
        }
    }
}
