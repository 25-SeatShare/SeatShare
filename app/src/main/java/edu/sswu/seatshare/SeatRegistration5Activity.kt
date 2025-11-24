package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SeatRegistration5Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration5)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 출발역, 도착역 받기
        val departure = intent.getStringExtra("departure") ?: ""
        val arrive = intent.getStringExtra("arrive") ?: ""

        findViewById<TextView>(R.id.departure_station_).text = departure
        findViewById<TextView>(R.id.arrive_station_).text = arrive

        // 플랫폼 번호, 좌석 번호
        val seatNum = intent.getStringExtra("seat_number") ?: ""
        val seatPage = intent.getStringExtra("seat_page") ?: ""

        findViewById<TextView>(R.id.platform_number_).text = seatPage
        findViewById<TextView>(R.id.seat_number_).text = seatNum

        // 🔹 여기에서: 좌석 저장 + 포인트 +1 적립 + 로그 남기기
        saveSeatAndAddPoint(departure, arrive, seatPage, seatNum)

        // 처음으로 버튼
        val nextBtn = findViewById<TextView>(R.id.seat_registration5_go_home_button)
        nextBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // 뒤로가기 버튼
        val backBtn = findViewById<TextView>(R.id.seat_registration5_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SeatRegistration4Activity::class.java))
            finish()
        }
    }

    /**
     * 좌석 정보를 Firestore에 저장하고
     * users/{uid}.points 는 +1
     * users/{uid}/pointLogs 에 로그 한 줄 추가
     */
    private fun saveSeatAndAddPoint(
        departure: String,
        arrive: String,
        seatPage: String,
        seatNum: String
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            toast("로그인 상태가 아닙니다.")
            return
        }

        val userDocRef = db.collection("users").document(uid)

        // 좌석 정보 (원하면 컬렉션/문서 구조는 바꿔도 됨)
        val seatData = hashMapOf(
            "fromStation" to departure,
            "toStation" to arrive,
            "platform" to seatPage,
            "seatNumber" to seatNum,
            "updatedAt" to Timestamp.now()
        )

        // 예시: users/{uid}/seats/current 에 저장
        userDocRef.collection("seats").document("current")
            .set(seatData)
            .addOnSuccessListener {
                // 좌석 저장 성공 → 포인트 +1
                userDocRef.update("points", FieldValue.increment(1))

                // 포인트 로그 남기기
                val logData = hashMapOf(
                    "delta" to 1L,
                    "type" to "seat_register",
                    "message" to "+1 적립 (좌석 등록)",
                    "createdAt" to Timestamp.now()
                )
                userDocRef.collection("pointLogs").add(logData)
            }
            .addOnFailureListener { e ->
                toast("좌석 저장 실패: ${e.localizedMessage}")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
