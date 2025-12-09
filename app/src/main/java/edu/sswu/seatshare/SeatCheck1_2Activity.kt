package edu.sswu.seatshare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SeatCheck1_2Activity : AppCompatActivity() {

    private var selectedStation: String? = null
    private lateinit var stationViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_check_1_2)

        // XML ImageView 연결
        val isu = findViewById<ImageView>(R.id.isu_2)
        val naebang = findViewById<ImageView>(R.id.naebang_2)
        val express = findViewById<ImageView>(R.id.expressbusterminal_2)
        val banpo = findViewById<ImageView>(R.id.banpo_2)
        val nonhyeon = findViewById<ImageView>(R.id.nonhyeon_2)
        val hakdong = findViewById<ImageView>(R.id.hakdong_2)

        val gunja = findViewById<ImageView>(R.id.gunja_2)
        val jayng = findViewById<ImageView>(R.id.jayang_2)
        val childrens = findViewById<ImageView>(R.id.childrensgrandpark_2)
        val konkuk = findViewById<ImageView>(R.id.konkukuniv_2)
        val cheongdam = findViewById<ImageView>(R.id.cheongdam_2)
        val gangnam = findViewById<ImageView>(R.id.gangnam_2)

        // 리스트로 묶기
        stationViews = listOf(
            isu, naebang, express, banpo, nonhyeon, hakdong,
            gunja, jayng, childrens, konkuk, cheongdam, gangnam
        )

        // 클릭 시 선택처리
        stationViews.forEach { img ->
            img.setOnClickListener { selectStation(img) }
        }

        // 완료 버튼
        findViewById<TextView>(R.id.seat_check_1_2_select_button).setOnClickListener {
            if (selectedStation == null) return@setOnClickListener

            val intent = Intent(this, SeatCheck1Activity::class.java)
            intent.putExtra("departure", selectedStation)
            startActivity(intent)
            finish()
        }

        // 뒤로가기
        findViewById<TextView>(R.id.seat_check_1_2_back_button).setOnClickListener {
            finish()
        }
    }

    private fun selectStation(view: ImageView) {
        // 1) 모든 역 아이콘의 필터 초기화
        stationViews.forEach { it.clearColorFilter() }

        // 2) 선택된 아이콘만 초록색 필터 적용
        view.setColorFilter(Color.parseColor("#804CAF50"))


        // 3) 선택된 역 이름 저장
        selectedStation = when (view.id) {
            R.id.isu_2 -> "이수"
            R.id.naebang_2 -> "내방"
            R.id.expressbusterminal_2 -> "고속터미널"
            R.id.banpo_2 -> "반포"
            R.id.nonhyeon_2 -> "논현"
            R.id.hakdong_2 -> "학동"

            R.id.gunja_2 -> "군자"
            R.id.jayang_2 -> "자양"
            R.id.childrensgrandpark_2 -> "어린이대공원"
            R.id.konkukuniv_2 -> "건대입구"
            R.id.cheongdam_2 -> "청담"
            R.id.gangnam_2 -> "강남구청"

            else -> null
        }
    }
}
