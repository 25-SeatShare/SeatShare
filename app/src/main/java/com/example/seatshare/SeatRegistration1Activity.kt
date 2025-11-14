package com.example.seatshare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SeatRegistration1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration1)

        // 버튼 12개 상태
        var isuState = 0
        var naebangState = 0
        var expressState = 0
        var banpoState = 0
        var nonhyeonState = 0
        var hakdongState = 0
        var gunjaState = 0
        var jayangeState = 0
        var childparkState = 0
        var konkukState = 0
        var cheongdamState = 0
        var gangnamState = 0

        // 버튼 12개 등록
        val isu = findViewById<ImageView>(R.id.isu)
        val naebang = findViewById<ImageView>(R.id.naebang)
        val express = findViewById<ImageView>(R.id.expressbusterminal)
        val banpo = findViewById<ImageView>(R.id.banpo)
        val nonhyeon = findViewById<ImageView>(R.id.nonhyeon)
        val hakdong = findViewById<ImageView>(R.id.hakdong)
        val gunja = findViewById<ImageView>(R.id.gunja)
        val jayange = findViewById<ImageView>(R.id.jayang)
        val childpark = findViewById<ImageView>(R.id.childrensgrandpark)
        val konkuk = findViewById<ImageView>(R.id.konkukuniv)
        val cheongdam = findViewById<ImageView>(R.id.cheongdam)
        val gangnam = findViewById<ImageView>(R.id.gangnam)

        // 클릭 시 상태 변경 함수
        fun applyState(btn: ImageView, state: Int): Int {
            val next = (state + 1) % 3
            when (next) {
                0 -> btn.setColorFilter(null)
                1 -> btn.setColorFilter(Color.parseColor("#803CB043")) // 초록
                2 -> btn.setColorFilter(Color.parseColor("#80D64545")) // 빨강
            }
            return next
        }

        //클릭했을 때
        isu.setOnClickListener { isuState = applyState(isu, isuState) }
        naebang.setOnClickListener { naebangState = applyState(naebang, naebangState) }
        express.setOnClickListener { expressState = applyState(express, expressState) }
        banpo.setOnClickListener { banpoState = applyState(banpo, banpoState) }
        nonhyeon.setOnClickListener { nonhyeonState = applyState(nonhyeon, nonhyeonState) }
        hakdong.setOnClickListener { hakdongState = applyState(hakdong, hakdongState) }
        gunja.setOnClickListener { gunjaState = applyState(gunja, gunjaState) }
        jayange.setOnClickListener { jayangeState = applyState(jayange, jayangeState) }
        childpark.setOnClickListener { childparkState = applyState(childpark, childparkState) }
        konkuk.setOnClickListener { konkukState = applyState(konkuk, konkukState) }
        cheongdam.setOnClickListener { cheongdamState = applyState(cheongdam, cheongdamState) }
        gangnam.setOnClickListener { gangnamState = applyState(gangnam, gangnamState) }

        // 다음 버튼: 승차역/하차역 1개씩 찾기
        findViewById<Button>(R.id.seat_registration1_select_button).setOnClickListener {

            var departure = "" //출발하는 역
            var arrive = "" //도착하는 역

            // 초록(1) → 승차역
            if (isuState == 1) departure = "이수"
            if (naebangState == 1) departure = "내방"
            if (expressState == 1) departure = "고속터미널"
            if (banpoState == 1) departure = "반포"
            if (nonhyeonState == 1) departure = "논현"
            if (hakdongState == 1) departure = "학동"

            if (gunjaState == 1) departure = "군자"
            if (jayangeState == 1) departure = "자양"
            if (childparkState == 1) departure = "어린이대공원"
            if (konkukState == 1) departure = "건대입구"
            if (cheongdamState == 1) departure = "청담"
            if (gangnamState == 1) departure = "강남구청"

            // 빨강(2) → 하차역
            if (isuState == 2) arrive = "이수"
            if (naebangState == 2) arrive = "내방"
            if (expressState == 2) arrive = "고속터미널"
            if (banpoState == 2) arrive = "반포"
            if (nonhyeonState == 2) arrive = "논현"
            if (hakdongState == 2) arrive = "학동"
            if (gunjaState == 2) arrive = "군자"
            if (jayangeState == 2) arrive = "자양"
            if (childparkState == 2) arrive = "어린이대공원"
            if (konkukState == 2) arrive = "건대입구"
            if (cheongdamState == 2) arrive = "청담"
            if (gangnamState == 2) arrive = "강남구청"

            // 둘 다 선택했는지 확인
            if (departure == "" || arrive == "") {
                Toast.makeText(this, "승차역(초록)과 하차역(빨강)을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2페이지로 전달
            val intent = Intent(this, SeatRegistration2Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            startActivity(intent)
        }

        // 뒤로가기
        findViewById<TextView>(R.id.seat_registration1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}
