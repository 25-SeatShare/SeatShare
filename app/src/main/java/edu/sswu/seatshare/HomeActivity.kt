package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var seatRegistrationBtn: Button

    // 좌석 등록 가능 여부 상태 저장
    private var seatRegistrationLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)

        seatRegistrationBtn = findViewById(R.id.btn_seat_registration)

        // 좌석 등록 버튼 클릭 이벤트
        seatRegistrationBtn.setOnClickListener {

            if (seatRegistrationLocked) {
                Toast.makeText(
                    this,
                    "이미 좌석이 등록되어 있습니다.\n하차 인증 후 이용해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, SeatRegistration1Activity::class.java))
        }

        findViewById<Button>(R.id.btn_mypage).setOnClickListener {
            startActivity(Intent(this, MyInfo1Activity::class.java))
        }

        findViewById<Button>(R.id.btn_point).setOnClickListener {
            startActivity(Intent(this, MyPointActivity::class.java))
        }

        findViewById<Button>(R.id.btn_seat_check).setOnClickListener {
            startActivity(Intent(this, SeatCheck1Activity::class.java))
        }

        findViewById<Button>(R.id.btn_seat_info).setOnClickListener {
            startActivity(Intent(this, MySeatCheckActivity::class.java))
        }

        findViewById<Button>(R.id.btn_get_off).setOnClickListener {
            startActivity(Intent(this, GetOff1Activity::class.java))
        }

        checkSeatStatusAndUpdateUI()
    }

    override fun onResume() {
        super.onResume()
        checkSeatStatusAndUpdateUI()
    }

    /** Firestore에서 좌석 상태 확인 */
    private fun checkSeatStatusAndUpdateUI() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid)
            .collection("seats").document("current")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {
                    // 좌석 이미 등록됨 → 비활성화처럼 보이게 처리
                    seatRegistrationLocked = true
                    seatRegistrationBtn.alpha = 0.5f  // UI 흐리게
                } else {
                    // 좌석 없음 → 활성화
                    seatRegistrationLocked = false
                    seatRegistrationBtn.alpha = 1.0f
                }
            }
            .addOnFailureListener {
                seatRegistrationLocked = false
                seatRegistrationBtn.alpha = 1.0f
            }
    }
}
