package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import edu.sswu.seatshare.R

class SeatRegistration4Activity : AppCompatActivity() {

    private var selectedSeatButton: Button? = null
    private var selectedSeatNumber: String? = null
    private var selectedPage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration4)

        //3에서 승차역, 하차역, 플랫폼 번호 받아오기
        val departure = intent.getStringExtra("departure")
        val arrive = intent.getStringExtra("arrive")
        val carNumber = intent.getStringExtra("car_number")

        val page1Text = findViewById<TextView>(R.id.page_1_text)
        val page2Text = findViewById<TextView>(R.id.page_2_text)
        val page3Text = findViewById<TextView>(R.id.page_3_text)
        val page4Text = findViewById<TextView>(R.id.page_4_text)

        // 3에서 받은 carNumber로 텍스트 설정
        page1Text.text = "${carNumber}-1"
        page2Text.text = "${carNumber}-2"
        page3Text.text = "${carNumber}-3"
        page4Text.text = "${carNumber}-4"


        //n-1, n-2, n-3, n-4 아이콘 색 바꾸기
        val page1 = findViewById<ImageView>(R.id.page_1_img)
        val page2 = findViewById<ImageView>(R.id.page_2_img)
        val page3 = findViewById<ImageView>(R.id.page_3_img)
        val page4 = findViewById<ImageView>(R.id.page_4_img)

        fun updatePageColor(index: Int) {
            // 초기화 (모두 원래 색으로)
            page1.setColorFilter(null)
            page2.setColorFilter(null)
            page3.setColorFilter(null)
            page4.setColorFilter(null)

            // 현재 페이지만 색 적용
            val highlight = android.graphics.Color.parseColor("#9CC3E3")
            when (index) {
                0 -> page1.setColorFilter(highlight)
                1 -> page2.setColorFilter(highlight)
                2 -> page3.setColorFilter(highlight)
                3 -> page4.setColorFilter(highlight)
            }
        }
        //앱 처음 들어왔을때 초기 색
        updatePageColor(0)

        //뷰플리퍼 동작
        val flipper = findViewById<ViewFlipper>(R.id.seat_flipper)
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

        //좌석 버튼 가져오기
        val seatButtons = ArrayList<Button>()

        val rootLayout = findViewById<ViewFlipper>(R.id.seat_flipper)
        fun collectSeats(view: android.view.View) {
            if (view is Button && view.background.constantState != null) {
                seatButtons.add(view)
            }
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    collectSeats(view.getChildAt(i))
                }
            }
        }
        collectSeats(rootLayout)

        //좌석 클릭 이벤트 설정
        seatButtons.forEach { btn ->
            btn.setOnClickListener {

                // 이미 다른 좌석 선택되어 있는데 또 선택하려고 하면 막기
                if (selectedSeatButton != null && selectedSeatButton != btn) {
                    Toast.makeText(this, "한 좌석만 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 선택 상태 toggle
                if (btn.isSelected) {
                    // 선택 취소
                    btn.isSelected = false
                    selectedSeatButton = null
                    selectedSeatNumber = null
                    selectedPage = null
                } else {
                    // 새로운 좌석 선택
                    btn.isSelected = true
                    selectedSeatButton = btn
                    selectedSeatNumber = btn.text.toString()

                    // 현재 페이지 계산
                    val index = flipper.displayedChild
                    selectedPage = carNumber + "-" + (index + 1)
                }
            }
        }

        // 다음으로 버튼: 5로 전달
        val seatRegistrationBtn = findViewById<Button>(R.id.seat_registration4_select_button)
        seatRegistrationBtn.setOnClickListener {

            if (selectedSeatNumber == null || selectedPage == null) {
                Toast.makeText(this, "좌석을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeatRegistration5Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            intent.putExtra("car_number", carNumber)
            intent.putExtra("seat_page", selectedPage)
            intent.putExtra("seat_number", selectedSeatNumber)
            startActivity(intent)

        }

        // 뒤로가기
        val backBtn = findViewById<TextView>(R.id.seat_registration4_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SeatRegistration3Activity::class.java))
        }
    }
}
