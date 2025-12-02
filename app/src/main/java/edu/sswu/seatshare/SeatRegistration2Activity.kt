package edu.sswu.seatshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class SeatRegistration2Activity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var item1: LinearLayout
    private lateinit var item2: LinearLayout
    private lateinit var item3: LinearLayout

    private lateinit var time1: TextView
    private lateinit var time2: TextView
    private lateinit var time3: TextView

    private lateinit var dest1: TextView
    private lateinit var dest2: TextView
    private lateinit var dest3: TextView

    // 7호선 전체 노선 리스트
    private val line7Stations = listOf(
        "장암","도봉산","수락산","마들","노원","중계","하계","공릉(서울산업대입구)",
        "태릉입구","먹골","중화","상봉","면목","사가정","용마산","중곡","군자",
        "어린이대공원","건대입구","뚝섬유원지","청담","강남구청","학동","논현","반포",
        "고속터미널","내방","이수(총신대입구)","남성","숭실대입구","상도","장승배기",
        "신대방삼거리","보라매","신풍","대림","남구로","가산디지털단지","철산","광명사거리",
        "천왕","온수","까치울","부천종합운동장","춘의","신중동","부천시청","상동","삼산체육관",
        "굴포천","부평구청"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_registration2)

        val departure = intent.getStringExtra("departure") ?: ""
        val arrive = intent.getStringExtra("arrive") ?: ""

        findViewById<TextView>(R.id.departure_station_text1).text = departure
        findViewById<TextView>(R.id.departure_station_text2).text = departure
        findViewById<TextView>(R.id.arrive_station_text1).text = arrive
        findViewById<TextView>(R.id.arrive_station_text2).text = arrive

        item1 = findViewById(R.id.time_item_1)
        item2 = findViewById(R.id.time_item_2)
        item3 = findViewById(R.id.time_item_3)

        time1 = findViewById(R.id.time_text_1)
        time2 = findViewById(R.id.time_text_2)
        time3 = findViewById(R.id.time_text_3)

        dest1 = findViewById(R.id.destination_text_1)
        dest2 = findViewById(R.id.destination_text_2)
        dest3 = findViewById(R.id.destination_text_3)

        item1.setOnClickListener { selectItem(item1) }
        item2.setOnClickListener { selectItem(item2) }
        item3.setOnClickListener { selectItem(item3) }

        loadRealtimeArrivals(departure, arrive)

        findViewById<TextView>(R.id.seat_registration2_select_button).setOnClickListener {
            val intent = Intent(this, SeatRegistration3Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.seat_registration2_back_button).setOnClickListener {
            startActivity(Intent(this, SeatRegistration1Activity::class.java))
        }
    }

    // 방향 자동 판별 + API 호출
    private fun loadRealtimeArrivals(departure: String, arrive: String) {
        val apiKey = "50594f444b6b6179313037566a56764c"

        val depName = mapToApiStationName(departure)
        val arrName = mapToApiStationName(arrive)

        val encoded = URLEncoder.encode(depName, "UTF-8")

        val urlString =
            "http://swopenapi.seoul.go.kr/api/subway/$apiKey/json/realtimeStationArrival/0/20/$encoded"

        println("🚀 REQUEST URL = $urlString")

        // 출발역/도착역 인덱스 추출
        val depIndex = line7Stations.indexOf(depName)
        val arrIndex = line7Stations.indexOf(arrName)

        // 방향 판별
        val isUpDirection = arrIndex < depIndex
        val targetDirection = if (isUpDirection) "상행" else "하행"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(urlString)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                val responseStr = client.newCall(request).execute().body?.string() ?: ""

                println("🔥 API RESPONSE = $responseStr")

                val root = JSONObject(responseStr)
                val list = root.optJSONArray("realtimeArrivalList")

                val trains = mutableListOf<Triple<Int, String, String>>()

                if (list != null) {
                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)

                        if (item.optString("subwayId") != "1007") continue
                        if (item.optString("updnLine") != targetDirection) continue

                        // 도착 예정 (초)
                        val seconds = item.optString("barvlDt").toIntOrNull() ?: continue
                        val minutes = seconds / 60
                        val minText = if (minutes <= 0) "곧 도착" else "${minutes}분 후"

                        val destText = item.optString("bstatnNm") + "행"

                        trains.add(Triple(seconds, minText, destText))
                    }
                }

                // 도착 임박 순으로 정렬 후 상위 3개
                val sorted = trains.sortedBy { it.first }.take(3)

                val result = sorted.map { it.second to it.third }

                withContext(Dispatchers.Main) {
                    applyResultToUI(result)
                }

            } catch (e: Exception) {
                println("❌ ERROR = ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeatRegistration2Activity, "요청 실패", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mapToApiStationName(name: String): String {
        return when (name) {
            "이수" -> "이수(총신대입구)"
            else -> name
        }
    }

    private fun applyResultToUI(result: List<Pair<String, String>>) {
        item1.visibility = View.GONE
        item2.visibility = View.GONE
        item3.visibility = View.GONE

        if (result.isEmpty()) {
            item1.visibility = View.VISIBLE
            time1.text = "도착 정보 없음"
            dest1.text = ""
            return
        }

        if (result.size >= 1) {
            item1.visibility = View.VISIBLE
            time1.text = result[0].first
            dest1.text = result[0].second
        }

        if (result.size >= 2) {
            item2.visibility = View.VISIBLE
            time2.text = result[1].first
            dest2.text = result[1].second
        }

        if (result.size >= 3) {
            item3.visibility = View.VISIBLE
            time3.text = result[2].first
            dest3.text = result[2].second
        }
    }

    private fun selectItem(selected: LinearLayout) {
        item1.isSelected = false
        item2.isSelected = false
        item3.isSelected = false
        selected.isSelected = true
    }
}
