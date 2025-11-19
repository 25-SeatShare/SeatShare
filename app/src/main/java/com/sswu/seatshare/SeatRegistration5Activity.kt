package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.sswu.seatshare.R

class SeatRegistration5Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration5)

        //출발역, 도착역 받기
        val departure = intent.getStringExtra("departure") ?: ""
        val arrive = intent.getStringExtra("arrive") ?: ""

        findViewById<TextView>(R.id.departure_station_).text = departure
        findViewById<TextView>(R.id.arrive_station_).text = arrive

        //플랫폼 번호, 좌석 번호
        val seatNum = intent.getStringExtra("seat_number") ?: ""
        val seatPage = intent.getStringExtra("seat_page") ?: ""

        findViewById<TextView>(R.id.platform_number_).text = seatPage
        findViewById<TextView>(R.id.seat_number_).text = seatNum

        //처음으로 버튼
        val nextBtn = findViewById<TextView>(R.id.seat_registration5_go_home_button)
        nextBtn.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
        }

        //뒤로가기 버튼
        val backBtn = findViewById<TextView>(R.id.seat_registration5_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SeatRegistration4Activity::class.java))
        }
    }
}
