package edu.sswu.seatshare

import android.os.Bundle
import android.widget.TextView
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
    private lateinit var adapter: PointLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔹 여기 레이아웃 이름을 실제 파일 이름으로 맞춰줘 (예: my_point.xml 이면 R.layout.my_point)
        setContentView(R.layout.my_point)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        totalPointText = findViewById(R.id.my_point_total)
        backButton = findViewById(R.id.my_point_back_button)
        recyclerView = findViewById(R.id.pointRecyclerView)

        adapter = PointLogAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton.setOnClickListener { finish() }

        loadMyPoint()
        loadPointLogs()
    }

    // 상단 "내 누적 포인트" 텍스트 채우기
    private fun loadMyPoint() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val points = doc.getLong("points") ?: 0L
                totalPointText.text = "${points}P"
            }
    }

    // 아래 RecyclerView에 포인트 로그들 넣기
    private fun loadPointLogs() {
        val uid = auth.currentUser?.uid ?: return

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
    }
}
