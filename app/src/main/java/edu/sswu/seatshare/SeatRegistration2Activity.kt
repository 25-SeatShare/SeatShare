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
        "군자",
        "어린이대공원","건대입구","뚝섬유원지","청담","강남구청","학동","논현","반포",
        "고속터미널","내방","이수(총신대입구)"
    )

    // 선택된 열차 정보 기억
    private var selectedTrainKey: String? = null

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

            if (selectedTrainKey == null) {
                Toast.makeText(this, "열차를 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeatRegistration3Activity::class.java)
            intent.putExtra("departure", departure)
            intent.putExtra("arrive", arrive)
            intent.putExtra("trainKey", selectedTrainKey)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.seat_registration2_back_button).setOnClickListener {
            startActivity(Intent(this, SeatRegistration1Activity::class.java))
        }
    }

    // 방향 판별 + API 호출 + 열차 정보 구하기
    private fun loadRealtimeArrivals(departure: String, arrive: String) {
        val apiKey = "50594f444b6b6179313037566a56764c"

        val depName = mapToApiStationName(departure)
        val arrName = mapToApiStationName(arrive)

        val encoded = URLEncoder.encode(depName, "UTF-8")

        val urlString =
            "http://swopenapi.seoul.go.kr/api/subway/$apiKey/json/realtimeStationArrival/0/20/$encoded"

        // 출발역/도착역 인덱스
        val depIndex = line7Stations.indexOf(depName)
        val arrIndex = line7Stations.indexOf(arrName)

        val isUpDirection = arrIndex < depIndex
        val targetDirection = if (isUpDirection) "상행" else "하행"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(urlString)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                val responseStr = client.newCall(request).execute().body?.string() ?: ""
                val root = JSONObject(responseStr)
                val list = root.optJSONArray("realtimeArrivalList")

                val trains = mutableListOf<Triple<Int, String, JSONObject>>()

                if (list != null) {
                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)

                        if (item.optString("subwayId") != "1007") continue
                        if (item.optString("updnLine") != targetDirection) continue

                        val seconds = item.optString("barvlDt").toIntOrNull() ?: continue
                        val minutes = seconds / 60

                        val minText = if (minutes <= 0) "곧 도착" else "${minutes}분 후"

                        trains.add(Triple(seconds, minText, item))
                    }
                }

                val sorted = trains.sortedBy { it.first }.take(3)

                // UI 표시 데이터 변환
                val uiData = sorted.map {
                    val json = it.third
                    val dest = json.optString("bstatnNm") + "행"
                    Pair(it.second, dest)
                }

                // 각 item 에 trainKey 저장
                if (sorted.isNotEmpty()) {
                    item1.setOnClickListener {
                        val json = sorted[0].third
                        selectedTrainKey = buildTrainKey(json)
                        selectItem(item1)
                    }
                }
                if (sorted.size >= 2) {
                    item2.setOnClickListener {
                        val json = sorted[1].third
                        selectedTrainKey = buildTrainKey(json)
                        selectItem(item2)
                    }
                }
                if (sorted.size >= 3) {
                    item3.setOnClickListener {
                        val json = sorted[2].third
                        selectedTrainKey = buildTrainKey(json)
                        selectItem(item3)
                    }
                }

                withContext(Dispatchers.Main) {
                    applyResultToUI(uiData)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeatRegistration2Activity, "요청 실패", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 열차를 Firestore에서 구분할 수 있는 키 생성
    private fun buildTrainKey(item: JSONObject): String {
        val trainNo = item.optString("btrainNo")
        val subwayId = item.optString("subwayId")
        val upDown = item.optString("updnLine")
        val start = item.optString("bstatnNm")
        val time = item.optString("recptnDt")

        return "${subwayId}_${trainNo}"
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

        if (result.isNotEmpty()) {
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
