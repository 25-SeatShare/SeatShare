package edu.sswu.seatshare

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyRankingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // 상단 텍스트
    private lateinit var myRankingTitleRank: TextView
    private lateinit var myRankingTitleLevel: TextView
    private lateinit var myRankingCircleText: TextView

    // 포인트
    private lateinit var myPointText: TextView
    private lateinit var myRankingUpgradePoint: TextView

    // Lv1~Lv5 동그라미
    private lateinit var lvCircles: List<ImageView>

    // 상단 배경들
    private lateinit var myRankingBg1: View
    private lateinit var myRankingBg2: View

    // 등급 블럭(계단 모양 View)
    private lateinit var iconFlatinum: View
    private lateinit var iconGold: View
    private lateinit var iconSilver: View
    private lateinit var iconBronze: View

    // 등급 텍스트
    private lateinit var textFlatinum: TextView
    private lateinit var textGold: TextView
    private lateinit var textSilver: TextView
    private lateinit var textBronze: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_ranking)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // XML 연결
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

        myRankingBg1 = findViewById(R.id.my_ranking_bg1)
        myRankingBg2 = findViewById(R.id.my_ranking_bg2)

        // 등급 블럭(계단)
        iconFlatinum = findViewById(R.id.my_ranking_flatinum)
        iconGold = findViewById(R.id.my_ranking_Gold)
        iconSilver = findViewById(R.id.my_ranking_Silver)
        iconBronze = findViewById(R.id.my_ranking_Bronze)

        // 등급 텍스트
        textFlatinum = findViewById(R.id.my_ranking_flatinum_text)
        textGold = findViewById(R.id.my_ranking_Gold_text)
        textSilver = findViewById(R.id.my_ranking_Silver_text)
        textBronze = findViewById(R.id.my_ranking_Bronze_text)

        // 뒤로가기
        findViewById<TextView>(R.id.my_info1_back_button).setOnClickListener {
            finish()
        }

        // 포인트 불러오기 + UI 반영
        loadMyPointFromFirestore()
    }

    // Firestore에서 포인트 불러오기
    private fun loadMyPointFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val point = snapshot.getLong("points")?.toInt() ?: 0
                applyRankUI(point)
            }
            .addOnFailureListener {
                applyRankUI(0)
            }
    }

    // 포인트 기준으로 등급/레벨/포인트 안내 적용
    private fun applyRankUI(point: Int) {
        val info = getRankInfo(point)

        // 상단 등급 / 레벨 텍스트
        myRankingTitleRank.text = info.rank
        myRankingTitleLevel.text = "Lv.${info.level}"

        // 원 안에 들어가는 글자 (B / S / G / F)
        myRankingCircleText.text = when (info.rank) {
            "Bronze"   -> "B"
            "Silver"   -> "S"
            "Gold"     -> "G"
            "Flatinum" -> "F"
            else       -> "B"
        }

        // 현재 포인트
        myPointText.text = "${point}P"

        // 다음 단계까지 남은 포인트
        if (info.needMorePoint != null && info.needMorePoint > 0) {
            myRankingUpgradePoint.text = "${info.needMorePoint}P "
        } else {
            myRankingUpgradePoint.text = "최고 등급입니다!"
        }

        // 테마(색상) 적용
        applyRankTheme(info.rank)

        // 레벨 동그라미 표시
        updateLevelCircles(info.level, info.rank)
    }

    // ---------- 계단 블럭 초기화 / 테마 적용 ----------

    // 계단 형태 등급 블럭 초기화 (기본 회색 + 검정 글씨)
    private fun resetRankBlocks() {
        val defaultBlockColor = Color.parseColor("#E9E9E9")
        val defaultTextColor = Color.parseColor("#000000")

        iconFlatinum.setBackgroundColor(defaultBlockColor)
        iconGold.setBackgroundColor(defaultBlockColor)
        iconSilver.setBackgroundColor(defaultBlockColor)
        iconBronze.setBackgroundColor(defaultBlockColor)

        textFlatinum.setTextColor(defaultTextColor)
        textGold.setTextColor(defaultTextColor)
        textSilver.setTextColor(defaultTextColor)
        textBronze.setTextColor(defaultTextColor)
    }

    /** 등급별 상단 배경 + 아래 계단 블럭 색 적용 */
    private fun applyRankTheme(rank: String) {

        // 0) 먼저 계단 블럭 전부 기본으로 리셋
        resetRankBlocks()

        // 1) 상단 배경 색
        val (mainColorHex, bg2ColorHex) = when (rank) {
            "Flatinum" -> "#B8AB5F" to "#EFEACF"
            "Gold"     -> "#E4D159" to "#FBF5CF"
            "Silver"   -> "#96A2A4" to "#E5EBEC"
            "Bronze"   -> "#B8AB5F" to "#EFEACF"
            else       -> "#3A83BF" to "#EFEACF"
        }

        val mainColor = Color.parseColor(mainColorHex)
        val bg2Color = Color.parseColor(bg2ColorHex)

        myRankingBg1.setBackgroundColor(mainColor)
        myRankingBg2.setBackgroundColor(bg2Color)
        myRankingCircleText.setTextColor(mainColor)

        // 2) 계단 블럭 중 내 등급만 강조 색 입히기
        val colorFlatinum = Color.parseColor("#B8AB5F")  // 플래티넘 계열
        val colorGold     = Color.parseColor("#E4D159")  // 골드
        val colorSilver   = Color.parseColor("#96A2A4")  // 실버
        val colorBronze   = Color.parseColor("#B67A3A")  // 브론즈 톤 예시

        when (rank) {
            "Flatinum" -> {
                iconFlatinum.setBackgroundColor(colorFlatinum)
                textFlatinum.setTextColor(Color.WHITE)
            }
            "Gold" -> {
                iconGold.setBackgroundColor(colorGold)
                textGold.setTextColor(Color.WHITE)
            }
            "Silver" -> {
                iconSilver.setBackgroundColor(colorSilver)
                textSilver.setTextColor(Color.WHITE)
            }
            "Bronze" -> {
                iconBronze.setBackgroundColor(colorBronze)
                textBronze.setTextColor(Color.WHITE)
            }
        }
    }

    // ---------- 레벨 동그라미 표시 ----------

    /**
     * 현재 레벨에 해당하는 동그라미 하나만 색칠
     * 나머지는 비활성 회색
     */
    private fun updateLevelCircles(currentLevel: Int, rank: String) {

        // 활성 레벨 색
        val activeHex = when (rank) {
            "Flatinum" -> "#B8AB5F"
            "Gold"     -> "#E4D159"
            "Silver"   -> "#96A2A4"
            "Bronze"   -> "#B8AB5F"
            else       -> "#3A83BF"
        }
        val activeColor = Color.parseColor(activeHex)

        // 비활성(기본) 색
        val inactiveColor = Color.parseColor("#E0E0E0")

        lvCircles.forEachIndexed { index, imageView ->
            if (index == currentLevel - 1) {
                // 내 레벨 동그라미만 컬러
                imageView.imageTintList = ColorStateList.valueOf(activeColor)
            } else {
                // 나머지는 회색
                imageView.imageTintList = ColorStateList.valueOf(inactiveColor)
            }
        }
    }

    // ---------- 등급 표 / 계산 ----------

    data class RankCell(val minPoint: Int, val rank: String, val level: Int)
    data class RankInfo(
        val rank: String,
        val level: Int,
        val needMorePoint: Int?,   // 다음 칸까지 남은 포인트
        val nextCell: RankCell?    // 다음 단계 셀 (없으면 최고 등급)
    )

    // 포인트 → 등급/레벨 기준표
    private val rankTable = listOf(
        RankCell(5,  "Bronze", 1),
        RankCell(8,  "Bronze", 2),
        RankCell(11, "Bronze", 3),
        RankCell(14, "Bronze", 4),
        RankCell(17, "Bronze", 5),

        RankCell(20, "Silver", 1),
        RankCell(23, "Silver", 2),
        RankCell(26, "Silver", 3),
        RankCell(29, "Silver", 4),
        RankCell(32, "Silver", 5),

        RankCell(35, "Gold", 1),
        RankCell(38, "Gold", 2),
        RankCell(41, "Gold", 3),
        RankCell(44, "Gold", 4),
        RankCell(47, "Gold", 5),

        RankCell(50, "Flatinum", 1),
        RankCell(53, "Flatinum", 2),
        RankCell(56, "Flatinum", 3),
        RankCell(59, "Flatinum", 4),
        RankCell(62, "Flatinum", 5)
    )

    // 포인트로부터 현재 등급/레벨 + 다음 단계까지 남은 포인트 계산
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

        return RankInfo(current.rank, current.level, needMore, next)
    }
}
