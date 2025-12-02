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
import com.google.firebase.firestore.FirebaseFirestore

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

    private lateinit var trainKey: String
    private var platformNumber: Int = -1

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_check_3)

        trainKey = intent.getStringExtra("trainKey") ?: ""
        platformNumber = intent.getIntExtra("selected_platform", -1)

        // 페이지 텍스트
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

        // 번호 아이콘
        page1Img = findViewById(R.id.page_1_img)
        page2Img = findViewById(R.id.page_2_img)
        page3Img = findViewById(R.id.page_3_img)
        page4Img = findViewById(R.id.page_4_img)

        updatePageColor(0)

        findViewById<ImageButton>(R.id.btn_prev).setOnClickListener {
            flipper.showPrevious()
            updatePageColor(flipper.displayedChild)
        }
        findViewById<ImageButton>(R.id.btn_next).setOnClickListener {
            flipper.showNext()
            updatePageColor(flipper.displayedChild)
        }

        // 좌석 클릭 리스너 + Firestore 점유 확인
        initSeatButtons()

        // 처음으로
        findViewById<Button>(R.id.seat_check_3_goHome_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // 뒤로가기
        findViewById<TextView>(R.id.seat_check_3_back_button).setOnClickListener {
            startActivity(Intent(this, SeatCheck2Activity::class.java))
        }
    }

    // 색 적용
    private fun updatePageColor(i: Int) {
        page1Img.setColorFilter(null)
        page2Img.setColorFilter(null)
        page3Img.setColorFilter(null)
        page4Img.setColorFilter(null)

        val color = Color.parseColor("#9CC3E3")

        when(i){
            0 -> page1Img.setColorFilter(color)
            1 -> page2Img.setColorFilter(color)
            2 -> page3Img.setColorFilter(color)
            3 -> page4Img.setColorFilter(color)
        }
    }

    // 좌석 클릭 이벤트 + Firestore 조회
    private fun initSeatButtons() {

        fun attach(buttonId: Int) {
            val btn = findViewById<Button>(buttonId)
            btn.setOnClickListener {

                val pageIndex = flipper.displayedChild + 1
                val seatNumber = btn.text.toString()
                val seatPage = "${platformNumber}-$pageIndex"
                val docId = "${seatPage}_${seatNumber}"

                db.collection("trainSeats")
                    .document(trainKey)
                    .collection("seats")
                    .document(docId)
                    .get()
                    .addOnSuccessListener {

                        if (it.exists()) {
                            // 이미 점유됨 → 빨간색 + 팝업 표시
                            btn.setBackgroundColor(Color.parseColor("#ff8787"))
                            showSeatPopup(
                                seatNumber,
                                occupied = true,
                                from = it.getString("fromStation") ?: "",
                                to = it.getString("toStation") ?: ""
                            )
                        } else {
                            // 빈 좌석
                            btn.setBackgroundColor(Color.parseColor("#d9d9d9"))
                            showSeatPopup(seatNumber, occupied = false)
                        }
                    }
            }
        }

        // 페이지별 좌석 모두 등록 (기존 코드 그대로)
        val ids = listOf(
            // 1-1
            R.id.seat_left_1_1_2, R.id.seat_left_2_1_2, R.id.seat_left_3_1_2,
            R.id.seat_right_4_1_2, R.id.seat_right_5_1_2, R.id.seat_right_6_1_2,

            // 1-2
            R.id.seat_left_1_2_2, R.id.seat_left_2_2_2, R.id.seat_left_3_2_2,
            R.id.seat_left_4_2_2, R.id.seat_left_5_2_2, R.id.seat_left_6_2_2, R.id.seat_left_7_2_2,
            R.id.seat_right_8_2_2, R.id.seat_right_9_2_2, R.id.seat_right_10_2_2,
            R.id.seat_right_11_2_2, R.id.seat_right_12_2_2, R.id.seat_right_13_2_2, R.id.seat_right_14_2_2,

            // 1-3
            R.id.seat_left_1_3_2, R.id.seat_left_2_3_2, R.id.seat_left_3_3_2, R.id.seat_left_4_3_2,
            R.id.seat_left_5_3_2, R.id.seat_left_6_3_2, R.id.seat_left_7_3_2,
            R.id.seat_right_8_3_2, R.id.seat_right_9_3_2, R.id.seat_right_10_3_2,
            R.id.seat_right_11_3_2, R.id.seat_right_12_3_2, R.id.seat_right_13_3_2, R.id.seat_right_14_3_2,

            // 1-4
            R.id.seat_left_1_4_2, R.id.seat_left_2_4_2, R.id.seat_left_3_4_2,
            R.id.seat_right_4_4_2, R.id.seat_right_5_4_2, R.id.seat_right_6_4_2
        )

        ids.forEach { attach(it) }
    }


    // 좌석 팝업 표시
    private fun showSeatPopup(
        seatNumber: String,
        occupied: Boolean,
        from: String = "",
        to: String = ""
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.seat_check_pop)

        dialog.findViewById<TextView>(R.id.seat_check_5_seat_num).text = seatNumber

        val statusText = dialog.findViewById<TextView>(R.id.seat_check_5_status)

        if (occupied) {
            statusText.text = "착석 중"
            statusText.setTextColor(Color.RED)

            dialog.findViewById<TextView>(R.id.seat_check_5_arrive_station).text =
                "출발: $from / 도착: $to"
        } else {
            statusText.text = "빈 좌석"
            statusText.setTextColor(Color.GRAY)
        }

        dialog.findViewById<Button>(R.id.seat_check_5_close_btn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
