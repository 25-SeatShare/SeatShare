package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyPointActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_point)

        //누적 포인트
        val totalPoint = findViewById<TextView>(R.id.my_point_total)

        // RecyclerView 연결
        val recycler = findViewById<RecyclerView>(R.id.pointRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)

        // 테스트 데이터 — 나중에 서버연결하면 여기에 서버값 넣기
        val pointList = listOf(
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "-1 차감"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "-1 차감"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "-1 차감"),
            PointItem("25.10.07", "15:36", "+1 적립"),
            PointItem("25.10.07", "15:36", "+1 적립"),
        )

        val adapter = PointAdapter(pointList)
        recycler.adapter = adapter

        //뒤로가기 버튼
        val backBtn = findViewById<TextView>(R.id.my_point_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, MyInfo1Activity::class.java))
        }
    }
}