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

class SeatRegistration4Activity : AppCompatActivity() {

    private var selectedSeatButton: Button? = null
    private var selectedSeatNumber: String? = null
    private var selectedPage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration4)

        val departure = intent.getStringExtra("departure")
        val arrive = intent.getStringExtra("arrive")
        val carNumber = intent.getStringExtra("car_number")
        val trainKey = intent.getStringExtra("trainKey")   // ★ 추가됨

        val page1Text = findViewById<TextView>(R.id.page_1_text)
        val page2Text = findViewById<TextView>(R.id.page_2_text)
        val page3Text = findViewById<TextView>(R.id.page_3_text)
        val page4Text = findViewById<TextView>(R.id.page_4_text)

        page1Text.text = "${carNumber}-1"
        page2Text.text = "${carNumber}-2"
        page3Text.text = "${carNumber}-3"
        page4Text.text = "${carNumber}-4"

        val page1 = findViewById<ImageView>(R.id.page_1_img)
        val page2 = findViewById<ImageView>(R.id.page_2_img)
        val page3 = findViewById<ImageView>(R.id.page_3_img)
        val page4 = findViewById<ImageView>(R.id.page_4_img)

        fun updatePageColor(index: Int) {
            page1.setColorFilter(null)
            page2.setColorFilter(null)
            page3.setColorFilter(null)
            page4.setColorFilter(null)

            val highlight = android.graphics.Color.parseColor("#9CC3E3")
            when (index) {
                0 -> page1.setColorFilter(highlight)
                1 -> page2.setColorFilter(highlight)
                2 -> page3.setColorFilter(highlight)
                3 -> page4.setColorFilter(highlight)
            }
        }

        updatePageColor(0)

        val flipper = findViewById<ViewFlipper>(R.id.seat_flipper)
        findViewById<ImageButton>(R.id.btn_next).setOnClickListener {
            flipper.showNext()
            updatePageColor(flipper.displayedChild)
        }
        findViewById<ImageButton>(R.id.btn_prev).setOnClickListener {
            flipper.showPrevious()
            updatePageColor(flipper.displayedChild)
        }

        val seatButtons = ArrayList<Button>()
        fun collectSeats(view: android.view.View) {
            if (view is Button) seatButtons.add(view)
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) collectSeats(view.getChildAt(i))
            }
        }
        collectSeats(flipper)

        seatButtons.forEach { btn ->
            btn.setOnClickListener {

                if (selectedSeatButton != null && selectedSeatButton != btn) {
                    Toast.makeText(this, "한 좌석만 선택해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (btn.isSelected) {
                    btn.isSelected = false
                    selectedSeatButton = null
                    selectedSeatNumber = null
                    selectedPage = null
                } else {
                    btn.isSelected = true
                    selectedSeatButton = btn
                    selectedSeatNumber = btn.text.toString()
                    val index = flipper.displayedChild
                    selectedPage = carNumber + "-" + (index + 1)
                }
            }
        }

        findViewById<Button>(R.id.seat_registration4_select_button).setOnClickListener {

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
            intent.putExtra("trainKey", trainKey)   // ★ 5로 전달
            startActivity(intent)
        }

        findViewById<TextView>(R.id.seat_registration4_back_button).setOnClickListener {
            startActivity(Intent(this, SeatRegistration3Activity::class.java))
        }
    }
}
