package edu.sswu.seatshare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class SeatCheck3Activity : AppCompatActivity() {

    private lateinit var flipper: ViewFlipper

    private lateinit var page1Img: ImageView
    private lateinit var page2Img: ImageView
    private lateinit var page3Img: ImageView
    private lateinit var page4Img: ImageView

    private lateinit var page1Text: TextView
    private lateinit var page2Text: TextView
    private lateinit var page3Text: TextView
    private lateinit var page4Text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_check_3)

        // seatcheck2에서 받은 칸 번호(1~8)
        val platformNumber = intent.getIntExtra("selected_platform", -1)

        // 텍스트 연결
        page1Text = findViewById(R.id.page_1_text_2)
        page2Text = findViewById(R.id.page_2_text_2)
        page3Text = findViewById(R.id.page_3_text_2)
        page4Text = findViewById(R.id.page_4_text_2)

        if (platformNumber != -1) {
            page1Text.text = "$platformNumber-1"
            page2Text.text = "$platformNumber-2"
            page3Text.text = "$platformNumber-3"
            page4Text.text = "$platformNumber-4"
        }

        // 플리퍼
        flipper = findViewById(R.id.seat_flipper_2)

        // 페이지 아이콘들
        page1Img = findViewById(R.id.page_1_img)
        page2Img = findViewById(R.id.page_2_img)
        page3Img = findViewById(R.id.page_3_img)
        page4Img = findViewById(R.id.page_4_img)

        // 기본 선택 색 적용 (0번 페이지)
        updatePageColor(0)

        // 화살표 이동
        val btnPrev = findViewById<ImageButton>(R.id.btn_prev)
        val btnNext = findViewById<ImageButton>(R.id.btn_next)

        btnNext.setOnClickListener {
            flipper.showNext()
            updatePageColor(flipper.displayedChild)
        }

        btnPrev.setOnClickListener {
            flipper.showPrevious()
            updatePageColor(flipper.displayedChild)
        }

        // 처음으로
        findViewById<Button>(R.id.seat_check_3_goHome_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // 뒤로가기
        findViewById<TextView>(R.id.seat_check_3_back_button).setOnClickListener {
            startActivity(Intent(this, SeatCheck2Activity::class.java))
        }
    }

    private fun updatePageColor(index: Int) {

        // 색 초기화
        page1Img.setColorFilter(null)
        page2Img.setColorFilter(null)
        page3Img.setColorFilter(null)
        page4Img.setColorFilter(null)

        // 선택된 탭만 파란색
        val highlight = Color.parseColor("#9CC3E3")

        when (index) {
            0 -> page1Img.setColorFilter(highlight)
            1 -> page2Img.setColorFilter(highlight)
            2 -> page3Img.setColorFilter(highlight)
            3 -> page4Img.setColorFilter(highlight)
        }
    }
}
