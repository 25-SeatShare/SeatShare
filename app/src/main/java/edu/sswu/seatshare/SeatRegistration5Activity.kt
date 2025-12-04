package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SeatRegistration5Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration5)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val departure = intent.getStringExtra("departure") ?: ""
        val arrive = intent.getStringExtra("arrive") ?: ""
        val trainKey = intent.getStringExtra("trainKey") ?: ""

        if (trainKey.isBlank()) {
            toast("열차 정보 오류")
            finish()
            return
        }

        val seatNum = intent.getStringExtra("seat_number") ?: ""
        val seatPage = intent.getStringExtra("seat_page") ?: ""

        findViewById<TextView>(R.id.departure_station_).text = departure
        findViewById<TextView>(R.id.arrive_station_).text = arrive
        findViewById<TextView>(R.id.platform_number_).text = seatPage
        findViewById<TextView>(R.id.seat_number_).text = seatNum

        // 좌석 정보만 저장 (포인트 적립 X)
        saveSeat(trainKey, departure, arrive, seatPage, seatNum)

        findViewById<TextView>(R.id.seat_registration5_go_home_button).setOnClickListener {
            // 처음으로 눌렀을 때 안내 문구
            toast("하차 인증 시 포인트 +1 적립됩니다")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.seat_registration5_back_button).setOnClickListener {
            startActivity(Intent(this, SeatRegistration4Activity::class.java))
            finish()
        }
    }

    private fun saveSeat(
        trainKey: String,
        departure: String,
        arrive: String,
        seatPage: String,
        seatNum: String
    ) {
        val uid = auth.currentUser?.uid ?: return toast("로그인 상태가 아닙니다.")

        val userRef = db.collection("users").document(uid)
        val trainRef = db.collection("trainSeats").document(trainKey)
        val seatRef = trainRef.collection("seats")
            .document("${seatPage}_${seatNum}")

        val seatData = hashMapOf(
            "uid" to uid,
            "fromStation" to departure,
            "toStation" to arrive,
            "seatPage" to seatPage,
            "seatNumber" to seatNum,
            "updatedAt" to Timestamp.now()
        )

        db.runTransaction { tx ->

            val existing = tx.get(seatRef)
            if (existing.exists()) {
                throw IllegalStateException("이미 선택된 좌석입니다.")
            }

            // 좌석 정보 저장
            tx.set(seatRef, seatData)

            // 유저의 현재 좌석 정보만 기록 (포인트 X)
            tx.set(
                userRef.collection("seats").document("current"),
                mapOf(
                    "trainKey" to trainKey,
                    "fromStation" to departure,
                    "toStation" to arrive,
                    "platform" to seatPage,
                    "seatNumber" to seatNum,
                    "updatedAt" to Timestamp.now()
                )
            )
        }.addOnSuccessListener {
            toast("좌석 등록 완료!")
        }.addOnFailureListener {
            toast(it.message ?: "좌석 등록 실패")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
