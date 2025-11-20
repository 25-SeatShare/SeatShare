package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SeatRegistration2Activity : AppCompatActivity() {

    private lateinit var item1: LinearLayout
    private lateinit var item2: LinearLayout
    private lateinit var item3: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration2)

        //1에서 승차역, 하차역 받기
        val departure = intent.getStringExtra("departure")
        val arrive = intent.getStringExtra("arrive")

        findViewById<TextView>(R.id.departure_station_text1).text = departure
        findViewById<TextView>(R.id.departure_station_text2).text = departure
        findViewById<TextView>(R.id.arrive_station_text1).text = arrive
        findViewById<TextView>(R.id.arrive_station_text2).text = arrive

        //View 연결
        item1 = findViewById(R.id.time_item_1)
        item2 = findViewById(R.id.time_item_2)
        item3 = findViewById(R.id.time_item_3)

        //클릭 이벤트 연결
        item1.setOnClickListener { selectItem(item1) }
        item2.setOnClickListener { selectItem(item2) }
        item3.setOnClickListener { selectItem(item3) }

        //다음 버튼
        val nextBtn = findViewById<TextView>(R.id.seat_registration2_select_button)
        nextBtn.setOnClickListener {
            //3으로 넘기기
            val intent = Intent(this, edu.sswu.seatshare.SeatRegistration3Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            startActivity(intent)
        }

        //뒤로가기 버튼
        val backBtn = findViewById<TextView>(R.id.seat_registration2_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SeatRegistration1Activity::class.java))
        }
    }

    //선택된 아이템의 상태 바꿈
    private fun selectItem(selected: LinearLayout) {
        item1.isSelected = false
        item2.isSelected = false
        item3.isSelected = false
        selected.isSelected = true
    }
}
