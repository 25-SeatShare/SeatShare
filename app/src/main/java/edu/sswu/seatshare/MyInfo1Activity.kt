package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyInfo1Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameTv: TextView
    private lateinit var emailTv: TextView
    private lateinit var badgeIv: ImageView
    private lateinit var backTv: TextView
    private lateinit var memberInfoTv: TextView
    private lateinit var pointCheckTv: TextView
    private lateinit var rankingCheckTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_info1)

        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // View 연결
        nameTv = findViewById(R.id.my_info1_name)
        emailTv = findViewById(R.id.my_info1_email)
        badgeIv = findViewById(R.id.my_info1_mobility_handicapped)
        backTv = findViewById(R.id.my_info1_back_button)
        memberInfoTv = findViewById(R.id.my_info1_member_info)
        pointCheckTv = findViewById(R.id.my_info1_point_check)
        rankingCheckTv = findViewById(R.id.my_info1_ranking_check)

        // 뒤로가기
        backTv.setOnClickListener {
            finish()
        }

        // "회원 정보 >" → MyInfo2Activity로 이동
        memberInfoTv.setOnClickListener {
            val intent = Intent(this, MyInfo2Activity::class.java)
            startActivity(intent)
        }

        // (필요하면 나중에 포인트/등급 페이지도 연결)
        // pointCheckTv.setOnClickListener { ... }
        // rankingCheckTv.setOnClickListener { ... }

        // 유저 정보 불러오기
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "회원 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val name = doc.getString("name") ?: "(이름 없음)"
                val email = doc.getString("email") ?: user.email.orEmpty()
                val isTransportVulnerable = doc.getBoolean("isTransportVulnerable") ?: false

                // UI 반영
                nameTv.text = name      // 옆에 "님" TextView는 XML에 따로 있음
                emailTv.text = email

                // 교통약자라면 하단 뱃지 보이기
                badgeIv.visibility = if (isTransportVulnerable) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "회원 정보 불러오기 실패: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
