package edu.sswu.seatshare

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max
import kotlin.math.min

class MyRankingActivity : AppCompatActivity() {

    // 상단 등급/레벨 텍스트
    private lateinit var tvRank: TextView           // Bronze / Silver / Gold / Flatinum
    private lateinit var tvLevel: TextView          // Lv.1 ~ Lv.5
    private lateinit var tvRankShort: TextView      // B / S / G / F

    // “nP 더 모으면 다음 단계로 승급”
    private lateinit var tvUpgradePoint: TextView   // nP 부분만 바뀌는 TextView(id: my_ranking_upgrade1)

    // 레벨 동그라미
    private lateinit var lvCircles: Array<ImageView>

    // 등급 막대
    private lateinit var vFlat: View
    private lateinit var vGold: View
    private lateinit var vSilver: View
    private lateinit var vBronze: View

    // 등급 텍스트
    private lateinit var tvFlat: TextView
    private lateinit var tvGold: TextView
    private lateinit var tvSilver: TextView
    private lateinit var tvBronze: TextView

    // 색상 상수
    private val activeColor = Color.parseColor("#3A83BF")
    private val barDefaultColor = Color.parseColor("#E9E9E9")
    private val textDefaultColor = Color.BLACK
    private val textWhite = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_ranking)

        // 뒤로가기
        findViewById<TextView>(R.id.my_info1_back_button).setOnClickListener {
            finish()
        }

        // 상단 텍스트
        tvRank = findViewById(R.id.my_ranking_title1_rank)
        tvLevel = findViewById(R.id.my_ranking_title1_level)
        tvRankShort = findViewById(R.id.my_ranking_title2)

        // 업그레이드 안내 텍스트
        tvUpgradePoint = findViewById(R.id.my_ranking_upgrade1)

        // 레벨 동그라미
        lvCircles = arrayOf(
            findViewById(R.id.my_ranking_Lv1),
            findViewById(R.id.my_ranking_Lv2),
            findViewById(R.id.my_ranking_Lv3),
            findViewById(R.id.my_ranking_Lv4),
            findViewById(R.id.my_ranking_Lv5)
        )

        // 등급 막대 뷰
        vFlat = findViewById(R.id.my_ranking_flatinum)
        vGold = findViewById(R.id.my_ranking_Gold)
        vSilver = findViewById(R.id.my_ranking_Silver)
        vBronze = findViewById(R.id.my_ranking_Bronze)

        // 등급 라벨 텍스트
        tvFlat = findViewById(R.id.my_ranking_flatinum_text)
        tvGold = findViewById(R.id.my_ranking_Gold_text)
        tvSilver = findViewById(R.id.my_ranking_Silver_text)
        tvBronze = findViewById(R.id.my_ranking_Bronze_text)

        // Todo 나중에 DB에서 포인트 값을 가져와서 대입
        // 예) val points = doc.getLong("points")?.toInt() ?: 0
        val points = 26   // 테스트용 포인트

        applyRankingUI(points)
    }

    //포인트에 따라 등급,레벨,색상,텍스트를 적용
    private fun applyRankingUI(points: Int) {
        val startPoint = 5          // Bronze Lv1 시작 포인트
        val stepPoint = 3           // 레벨 한 단계당 3P
        val maxStep = 4 * 5 - 1     // Bronze~Flatinum Lv1~Lv5 총 20단계 → 0~19

        // 5P 미만이면 최소 Bronze Lv1
        val step = if (points < startPoint) 0
        else min((points - startPoint) / stepPoint, maxStep)

        val tierIndex = step / 5           // 0:Bronze, 1:Silver, 2:Gold, 3:Flatinum
        val level = step % 5 + 1           // 1~5

        val (rankName, rankShort) = when (tierIndex) {
            0 -> "Bronze" to "B"
            1 -> "Silver" to "S"
            2 -> "Gold" to "G"
            else -> "Flatinum" to "F"
        }

        // 1) 상단 등급/레벨 텍스트 적용
        tvRank.text = rankName
        tvLevel.text = "Lv.$level"
        tvRankShort.text = rankShort

        // 2) 레벨 동그라미 색 적용
        for (i in lvCircles.indices) {
            val iv = lvCircles[i]
            if (i < level) {
                iv.setColorFilter(activeColor)
            } else {
                iv.clearColorFilter()
            }
        }

        // 3) 다음 레벨까지 필요한 포인트 계산
        val nextStep = min(step + 1, maxStep)
        val currentStepPoint = startPoint + step * stepPoint
        val nextStepPoint = startPoint + (step + 1) * stepPoint

        val needPoint = if (step >= maxStep) {
            0   // 최고 등급이면 0P (문구는 그대로 두되 숫자만 0)
        } else {
            max(0, nextStepPoint - points)
        }
        tvUpgradePoint.text = "${needPoint}P"

        // 4) 등급 막대/텍스트 색 초기화 후 현재 등급만 강조
        resetGradeBars()

        when (rankName) {
            "Bronze" -> {
                vBronze.setBackgroundColor(activeColor)
                tvBronze.setTextColor(textWhite)
            }
            "Silver" -> {
                vSilver.setBackgroundColor(activeColor)
                tvSilver.setTextColor(textWhite)
            }
            "Gold" -> {
                vGold.setBackgroundColor(activeColor)
                tvGold.setTextColor(textWhite)
            }
            "Flatinum" -> {
                vFlat.setBackgroundColor(activeColor)
                tvFlat.setTextColor(textWhite)
            }
        }
    }

    //등급,막대,텍스트를 기본 상태로 리셋
    private fun resetGradeBars() {
        vFlat.setBackgroundColor(barDefaultColor)
        vGold.setBackgroundColor(barDefaultColor)
        vSilver.setBackgroundColor(barDefaultColor)
        vBronze.setBackgroundColor(barDefaultColor)

        tvFlat.setTextColor(textDefaultColor)
        tvGold.setTextColor(textDefaultColor)
        tvSilver.setTextColor(textDefaultColor)
        tvBronze.setTextColor(textDefaultColor)
    }
}
