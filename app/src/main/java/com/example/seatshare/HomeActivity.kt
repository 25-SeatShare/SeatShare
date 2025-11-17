package com.example.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        // 좌석 등록 버튼
        val seatRegistrationBtn = findViewById<Button>(R.id.btn_seat_registration)

        seatRegistrationBtn.setOnClickListener {
            val intent = Intent(this, SeatRegistration1Activity::class.java)
            startActivity(intent)
        }

        // 마이페이지 버튼
        val myPageBtn = findViewById<Button>(R.id.btn_mypage)

        myPageBtn.setOnClickListener {
            val intent = Intent(this, MyInfo1Activity::class.java)
            startActivity(intent)
        }

    }
}
