package edu.sswu.seatshare

import android.app.Dialog
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

        // 기본 선택 색 적용(0번 페이지)
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

        // ====== 좌석 버튼들에 클릭 리스너 연결 (팝업 띄우기) ======
        initSeatClickListeners()
    }

    // 페이지 인디케이터 색
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

    // 좌석 버튼들에 공통 클릭 리스너 세팅
    private fun initSeatClickListeners() {

        fun setSeatClick(buttonId: Int) {
            val btn = findViewById<Button>(buttonId)
            btn.setOnClickListener {
                val seatNumber = btn.text.toString()  // 버튼에 적힌 숫자 사용
                showSeatPopup(seatNumber)
            }
        }

        // ─── 1-1 페이지 좌석 ───
        setSeatClick(R.id.seat_left_1_1_2)
        setSeatClick(R.id.seat_left_2_1_2)
        setSeatClick(R.id.seat_left_3_1_2)
        setSeatClick(R.id.seat_right_4_1_2)
        setSeatClick(R.id.seat_right_5_1_2)
        setSeatClick(R.id.seat_right_6_1_2)

        // ─── 1-2 페이지 좌석 ───
        setSeatClick(R.id.seat_left_1_2_2)
        setSeatClick(R.id.seat_left_2_2_2)
        setSeatClick(R.id.seat_left_3_2_2)
        setSeatClick(R.id.seat_left_4_2_2)
        setSeatClick(R.id.seat_left_5_2_2)
        setSeatClick(R.id.seat_left_6_2_2)
        setSeatClick(R.id.seat_left_7_2_2)

        setSeatClick(R.id.seat_right_8_2_2)
        setSeatClick(R.id.seat_right_9_2_2)
        setSeatClick(R.id.seat_right_10_2_2)
        setSeatClick(R.id.seat_right_11_2_2)
        setSeatClick(R.id.seat_right_12_2_2)
        setSeatClick(R.id.seat_right_13_2_2)
        setSeatClick(R.id.seat_right_14_2_2)

        // ─── 1-3 페이지 좌석 ───
        setSeatClick(R.id.seat_left_1_3_2)
        setSeatClick(R.id.seat_left_2_3_2)
        setSeatClick(R.id.seat_left_3_3_2)
        setSeatClick(R.id.seat_left_4_3_2)
        setSeatClick(R.id.seat_left_5_3_2)
        setSeatClick(R.id.seat_left_6_3_2)
        setSeatClick(R.id.seat_left_7_3_2)

        setSeatClick(R.id.seat_right_8_3_2)
        setSeatClick(R.id.seat_right_9_3_2)
        setSeatClick(R.id.seat_right_10_3_2)
        setSeatClick(R.id.seat_right_11_3_2)
        setSeatClick(R.id.seat_right_12_3_2)
        setSeatClick(R.id.seat_right_13_3_2)
        setSeatClick(R.id.seat_right_14_3_2)

        // ─── 1-4 페이지 좌석 ───
        setSeatClick(R.id.seat_left_1_4_2)
        setSeatClick(R.id.seat_left_2_4_2)
        setSeatClick(R.id.seat_left_3_4_2)

        setSeatClick(R.id.seat_right_4_4_2)
        setSeatClick(R.id.seat_right_5_4_2)
        setSeatClick(R.id.seat_right_6_4_2)
    }

    // 좌석 선택 팝업 띄우기
    private fun showSeatPopup(seatNumber: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.seat_check_pop)

        // 좌석 번호 세팅 (n 부분)
        val seatNumText = dialog.findViewById<TextView>(R.id.seat_check_5_seat_num)
        seatNumText.text = seatNumber

        // 필요하면 하차역 / 정거장 정보도 여기서 세팅 가능
        // val arriveText = dialog.findViewById<TextView>(R.id.seat_check_5_arrive_station)
        // val cntText = dialog.findViewById<TextView>(R.id.seat_check_5_arrive_station_cnt)
        // arriveText.text = "홍대입구"
        // cntText.text = "3"

        // 닫기 버튼
        val closeBtn = dialog.findViewById<Button>(R.id.seat_check_5_close_btn)
        closeBtn.setOnClickListener {
            dialog.dismiss()  // 팝업 닫고 다시 SeatCheck3Activity 화면 유지
        }

        dialog.show()
    }
}
