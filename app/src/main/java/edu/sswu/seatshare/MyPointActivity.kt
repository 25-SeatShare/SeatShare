package edu.sswu.seatshare

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyPointActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var totalPointText: TextView
    private lateinit var backButton: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PointAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔹 네가 올려준 XML (my_point) 사용
        setContentView(R.layout.my_point)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // View 연결
        totalPointText = findViewById(R.id.my_point_total)
        backButton = findViewById(R.id.my_point_back_button)
        recyclerView = findViewById(R.id.pointRecyclerView)

        // RecyclerView + Adapter 설정
        adapter = PointAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 뒤로가기
        backButton.setOnClickListener {
            finish()
        }

        // 데이터 불러오기
        loadMyPoint()
        loadPointLogs()
    }

    // 상단 "내 누적 포인트" 텍스트 채우기
    private fun loadMyPoint() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            toast("로그인이 필요합니다.")
            return
        }

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val points = doc.getLong("points") ?: 0L
                totalPointText.text = "${points}P"
            }
            .addOnFailureListener { e ->
                toast("포인트 불러오기 실패: ${e.localizedMessage}")
            }
    }

    // 아래 RecyclerView에 포인트 로그 리스트 채우기
    private fun loadPointLogs() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            toast("로그인이 필요합니다.")
            return
        }

        db.collection("users").document(uid)
            .collection("pointLogs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                val list = query.documents.map { doc ->
                    val delta = doc.getLong("delta") ?: 0L
                    val ts = doc.getTimestamp("createdAt") ?: Timestamp.now()

                    PointItem(
                        delta = delta,
                        createdAt = ts.toDate()
                    )
                }
                adapter.submitList(list)
            }
            .addOnFailureListener { e ->
                toast("포인트 내역 불러오기 실패: ${e.localizedMessage}")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
