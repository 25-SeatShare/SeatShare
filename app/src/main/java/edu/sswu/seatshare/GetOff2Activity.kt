package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GetOff2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_off_2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val departureView = findViewById<TextView>(R.id.departure_station_)
        val arriveView = findViewById<TextView>(R.id.arrive_station_)
        val platformView = findViewById<TextView>(R.id.platform_number_)
        val seatView = findViewById<TextView>(R.id.seat_number_)

        // 1) 혹시 이전 화면에서 넘어온 값이 있으면 일단 먼저 보여줌 (빠르게 표시용)
        val intentDeparture = intent.getStringExtra("departure") ?: ""
        val intentArrive = intent.getStringExtra("arrive") ?: ""
        val intentSeatNum = intent.getStringExtra("seat_number") ?: ""
        val intentSeatPage = intent.getStringExtra("seat_page") ?: ""

        departureView.text = intentDeparture
        arriveView.text = intentArrive
        platformView.text = intentSeatPage
        seatView.text = intentSeatNum

        // 2) 실제 진짜 데이터는 Firestore 기준으로 동기화
        loadSeatFromFirestore(
            departureView,
            arriveView,
            platformView,
            seatView
        )

        // 뒤로가기 버튼
        val backBtn = findViewById<TextView>(R.id.get_off_2_back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, GetOff1Activity::class.java))
            finish()
        }

        // 처음으로 버튼
        val editBtn = findViewById<TextView>(R.id.get_off_2_go_home_button)
        editBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    /**
     * Firestore에서 users/{uid}/seats/current 불러와서
     * MySeatCheck 화면과 항상 같은 상태로 맞춰줌.
     * 좌석 정보가 없으면 공백으로 둠.
     */
    private fun loadSeatFromFirestore(
        departureView: TextView,
        arriveView: TextView,
        platformView: TextView,
        seatView: TextView
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            toast("로그인 상태가 아닙니다.")
            // 로그인 안 되어 있으면 그냥 intent 값(또는 공백) 유지
            return
        }

        val seatDocRef = db.collection("users")
            .document(uid)
            .collection("seats")
            .document("current")

        seatDocRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    // Firestore에 저장된 최신 좌석 정보로 동기화
                    val fromStation = snapshot.getString("fromStation") ?: ""
                    val toStation = snapshot.getString("toStation") ?: ""
                    val platform = snapshot.getString("platform") ?: ""
                    val seatNumber = snapshot.getString("seatNumber") ?: ""

                    departureView.text = fromStation
                    arriveView.text = toStation
                    platformView.text = platform
                    seatView.text = seatNumber
                } else {
                    // 좌석 등록이 한 번도 안 된 경우 → 전부 공백
                    departureView.text = ""
                    arriveView.text = ""
                    platformView.text = ""
                    seatView.text = ""
                }
            }
            .addOnFailureListener { e ->
                toast("좌석 정보를 불러오지 못했어요: ${e.localizedMessage}")
                // 실패 시에는 그냥 기존 표시( intent 값 ) 유지
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
