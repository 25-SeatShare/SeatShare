package edu.sswu.seatshare   // ← 네 패키지명으로 맞춰줘!

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyRankingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // 상단 등급/레벨 텍스트
    private lateinit var myRankingTitleRank: TextView      // "Silver"
    private lateinit var myRankingTitleLevel: TextView     // "Lv.3"
    private lateinit var myRankingCircleText: TextView     // 동그라미 안의 "B/S/G/F"

    // 현재 포인트 + 남은 포인트 문구
    private lateinit var myPointText: TextView             // "nP"
    private lateinit var myRankingUpgradePoint: TextView   // "3P "

    // 레벨 동그라미(Lv1~Lv5)
    private lateinit var lvCircles: List<ImageView>

    // ── 색상 (active는 파란색, inactive는 회색) ─────────────────────
    private val activeColor = Color.parseColor("#3A83BF")
    private val inactiveColor = Color.parseColor("#CCCCCC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_ranking)   // xml: my_ranking.xml

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ====== XML 연결 ======
        myRankingTitleRank = findViewById(R.id.my_ranking_title1_rank)
        myRankingTitleLevel = findViewById(R.id.my_ranking_title1_level)
        myRankingCircleText = findViewById(R.id.my_ranking_title2)

        myPointText = findViewById(R.id.my_ranking_mypoint)
        myRankingUpgradePoint = findViewById(R.id.my_ranking_upgrade1)

        lvCircles = listOf(
            findViewById(R.id.my_ranking_Lv1),
            findViewById(R.id.my_ranking_Lv2),
            findViewById(R.id.my_ranking_Lv3),
            findViewById(R.id.my_ranking_Lv4),
            findViewById(R.id.my_ranking_Lv5)
        )

        // 뒤로가기
        findViewById<TextView>(R.id.my_info1_back_button).setOnClickListener {
            finish()
        }

        // Firestore에서 내 포인트 가져와서 UI 적용
        loadMyPointFromFirestore()
    }

    // ───────────────── Firestore에서 포인트 읽기 ─────────────────
    private fun loadMyPointFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val point = snapshot.getLong("points")?.toInt() ?: 0
                applyRankUI(point)
            }
            .addOnFailureListener {
                // 실패 시 0P 기준
                applyRankUI(0)
            }
    }

    // ───────────────── 포인트 → 등급/레벨/남은 포인트, UI 적용 ─────────────────
    private fun applyRankUI(point: Int) {
        val info = getRankInfo(point)

        // 상단: "나의 등급은 Silver  Lv.3"
        myRankingTitleRank.text = info.rank           // 예) "Silver"
        myRankingTitleLevel.text = "Lv.${info.level}" // 예) "Lv.3"

        // 동그라미 안의 한 글자 (B, S, G, F)
        myRankingCircleText.text = when (info.rank) {
            "Bronze" -> "B"
            "Silver" -> "S"
            "Gold" -> "G"
            "Flatinum" -> "F"
            else -> "B"
        }

        // 현재 포인트 nP
        myPointText.text = "${point}P"

        // "3P 더 모으면..." / 최고 등급일 때
        if (info.needMorePoint != null && info.needMorePoint > 0) {
            myRankingUpgradePoint.text = "${info.needMorePoint}P "
        } else {
            myRankingUpgradePoint.text = "최고 등급입니다!"
        }

        // Lv1~Lv5 동그라미 색칠
        updateLevelCircles(info.level)
    }

    // 동그라미 색 바꾸기 (outline 하나만 써도 됨, tint로 색만 변경)
    private fun updateLevelCircles(currentLevel: Int) {
        lvCircles.forEachIndexed { index, imageView ->
            if (index < currentLevel) {
                // 현재 레벨 이하 = 파란색
                imageView.imageTintList = ColorStateList.valueOf(activeColor)
            } else {
                // 나머지 = 회색
                imageView.imageTintList = ColorStateList.valueOf(inactiveColor)
            }
        }
    }

    // ───────────────── 등급/레벨 표 & 계산 로직 ─────────────────

    data class RankCell(
        val minPoint: Int,
        val rank: String,   // "Bronze", "Silver", "Gold", "Flatinum"
        val level: Int      // 1~5
    )

    data class RankInfo(
        val rank: String,
        val level: Int,
        val needMorePoint: Int?,   // 다음 단계까지 남은 P (없으면 null)
        val nextCell: RankCell?
    )

    // 이미지에 그려놓은 표 그대로 옮겨적기
    private val rankTable = listOf(
        // Bronze
        RankCell(5,  "Bronze", 1),
        RankCell(8,  "Bronze", 2),
        RankCell(11, "Bronze", 3),
        RankCell(14, "Bronze", 4),
        RankCell(17, "Bronze", 5),

        // Silver
        RankCell(20, "Silver", 1),
        RankCell(23, "Silver", 2),
        RankCell(26, "Silver", 3),
        RankCell(29, "Silver", 4),
        RankCell(32, "Silver", 5),

        // Gold
        RankCell(35, "Gold", 1),
        RankCell(38, "Gold", 2),
        RankCell(41, "Gold", 3),
        RankCell(44, "Gold", 4),
        RankCell(47, "Gold", 5),

        // Flatinum
        RankCell(50, "Flatinum", 1),
        RankCell(53, "Flatinum", 2),
        RankCell(56, "Flatinum", 3),
        RankCell(59, "Flatinum", 4),
        RankCell(62, "Flatinum", 5)
    )

    private fun getRankInfo(point: Int): RankInfo {
        var current = rankTable.first()
        var next: RankCell? = null

        for (cell in rankTable) {
            if (point >= cell.minPoint) {
                current = cell
            } else {
                next = cell
                break
            }
        }

        val needMore = next?.let { it.minPoint - point }

        return RankInfo(
            rank = current.rank,
            level = current.level,
            needMorePoint = needMore,
            nextCell = next
        )
    }
}
