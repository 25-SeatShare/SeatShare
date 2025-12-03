package edu.sswu.seatshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.math.*

class SeatCheck1Activity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 100
    private val client = OkHttpClient()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var timeItem1: LinearLayout
    private lateinit var timeItem2: LinearLayout
    private lateinit var timeItem3: LinearLayout
    private lateinit var timeItem4: LinearLayout

    private lateinit var timeText1: TextView
    private lateinit var timeText2: TextView
    private lateinit var timeText3: TextView
    private lateinit var timeText4: TextView

    private lateinit var destText1: TextView
    private lateinit var destText2: TextView
    private lateinit var destText3: TextView
    private lateinit var destText4: TextView

    private lateinit var departureText: TextView

    private var selectedTrainKey: String? = null
    private var currentDeparture: String? = null

    // 역 + GPS
    data class Station(val name: String, val lat: Double, val lon: Double)

    private val line7StationsGPS = listOf(
        Station("군자", 37.557121, 127.079542),
        Station("어린이대공원", 37.548033, 127.074860),
        Station("건대입구", 37.540693, 127.070230),
        Station("뚝섬유원지", 37.531540, 127.067200),
        Station("청담", 37.519365, 127.053220),
        Station("강남구청", 37.517186, 127.041280),
        Station("학동", 37.514229, 127.029130),
        Station("논현", 37.511093, 127.021415),
        Station("반포", 37.508178, 127.011727),
        Station("고속터미널", 37.504465, 127.004943),
        Station("내방", 37.487618, 126.993513),
        Station("이수(총신대입구)", 37.486263, 126.981989),
    )

    // ★ 역 이름 배열 따로 생성 → index 찾기 쉽게 함
    private val line7Names = line7StationsGPS.map { it.name }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat_check_1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        departureText = findViewById(R.id.departure_station_text)

        timeItem1 = findViewById(R.id.time_item_1)
        timeItem2 = findViewById(R.id.time_item_2)
        timeItem3 = findViewById(R.id.time_item_3)
        timeItem4 = findViewById(R.id.time_item_4)

        timeText1 = findViewById(R.id.time_text_1)
        timeText2 = findViewById(R.id.time_text_2)
        timeText3 = findViewById(R.id.time_text_3)
        timeText4 = findViewById(R.id.time_text_4)

        destText1 = findViewById(R.id.destination_text_1)
        destText2 = findViewById(R.id.destination_text_2)
        destText3 = findViewById(R.id.destination_text_3)
        destText4 = findViewById(R.id.destination_text_4)

        val passedDeparture = intent.getStringExtra("departure")

        if (!passedDeparture.isNullOrBlank()) {
            currentDeparture = mapToApiStationName(passedDeparture)
            departureText.text = currentDeparture
            loadRealtimeArrivals(currentDeparture!!)
        } else {
            checkLocationPermission()
        }

        timeItem1.setOnClickListener { selectTrain(it) }
        timeItem2.setOnClickListener { selectTrain(it) }
        timeItem3.setOnClickListener { selectTrain(it) }
        timeItem4.setOnClickListener { selectTrain(it) }

        findViewById<TextView>(R.id.seat_check_1_next_button).setOnClickListener {
            if (selectedTrainKey == null) {
                Toast.makeText(this, "열차를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeatCheck2Activity::class.java)
            intent.putExtra("trainKey", selectedTrainKey)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.seat_check_1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun mapToApiStationName(name: String): String {
        return when (name) {
            "이수" -> "이수(총신대입구)"
            else -> name
        }
    }

    private fun checkLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ContextCompat.checkSelfPermission(this, fine) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, coarse) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(fine, coarse), LOCATION_PERMISSION_CODE)
        } else {
            getCurrentLocationAndDetectStation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            getCurrentLocationAndDetectStation()
        } else {
            Toast.makeText(this, "위치 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Suppress("MissingPermission")
    private fun getCurrentLocationAndDetectStation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(this, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nearest = findNearestStation(location.latitude, location.longitude)
                currentDeparture = nearest.name
                departureText.text = nearest.name

                loadRealtimeArrivals(nearest.name)
            }
    }

    private fun findNearestStation(lat: Double, lon: Double): Station {
        var nearest = line7StationsGPS[0]
        var minDist = Double.MAX_VALUE

        for (st in line7StationsGPS) {
            val d = distance(lat, lon, st.lat, st.lon)
            if (d < minDist) {
                minDist = d
                nearest = st
            }
        }
        return nearest
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6372.8 * 1000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        return 2 * R * atan2(sqrt(a), sqrt(1 - a))
    }

    // ★ 다음역을 계산하는 함수 추가
    private fun getNextStation(current: String, updnLine: String): String {
        val idx = line7Names.indexOf(current)
        if (idx == -1) return "다음역 정보 없음"

        return if (updnLine.contains("상행")) {
            if (idx + 1 < line7Names.size) line7Names[idx + 1] else current
        } else {
            if (idx - 1 >= 0) line7Names[idx - 1] else current
        }
    }

    // 🔥 실시간 API
    private fun loadRealtimeArrivals(departure: String) {
        val apiKey = "50594f444b6b6179313037566a56764c"
        val station = URLEncoder.encode(mapToApiStationName(departure), "UTF-8")
        val url =
            "http://swopenapi.seoul.go.kr/api/subway/$apiKey/json/realtimeStationArrival/0/20/$station"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val responseStr = client.newCall(request).execute().body?.string() ?: ""

                val list = JSONObject(responseStr).optJSONArray("realtimeArrivalList")
                val trains = mutableListOf<Triple<String, String, String>>()

                if (list != null) {
                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)

                        if (item.optString("subwayId") != "1007") continue

                        val barvlDt = item.optString("barvlDt").toIntOrNull() ?: continue
                        val minutes = barvlDt / 60

                        val timeText = if (minutes <= 0) "곧 도착" else "${minutes}분 후"

                        val updn = item.optString("updnLine") // ★ 상행/하행

                        val nextStation = getNextStation(departure, updn) // ★ 다음역 계산

                        val dest = "$nextStation 방면" // ★ 변경된 UI

                        val trainKey =
                            item.optString("subwayId") + "_" + item.optString("btrainNo")

                        trains.add(Triple(trainKey, timeText, dest))
                        if (trains.size == 4) break
                    }
                }

                withContext(Dispatchers.Main) {
                    applyTrainList(trains)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeatCheck1Activity, "열차 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyTrainList(trains: List<Triple<String, String, String>>) {
        timeItem1.visibility = View.GONE
        timeItem2.visibility = View.GONE
        timeItem3.visibility = View.GONE
        timeItem4.visibility = View.GONE

        if (trains.isEmpty()) {
            timeItem1.visibility = View.VISIBLE
            timeText1.text = "도착 정보 없음"
            destText1.text = ""
            return
        }

        val items = listOf(timeItem1, timeItem2, timeItem3, timeItem4)
        val times = listOf(timeText1, timeText2, timeText3, timeText4)
        val dests = listOf(destText1, destText2, destText3, destText4)


        for (i in trains.indices) {
            items[i].visibility = View.VISIBLE
            times[i].text = trains[i].second
            dests[i].text = trains[i].third
            items[i].tag = trains[i].first
        }
    }

    private fun selectTrain(view: View) {
        timeItem1.isSelected = false
        timeItem2.isSelected = false
        timeItem3.isSelected = false
        timeItem4.isSelected = false
        view.isSelected = true

        selectedTrainKey = view.tag as? String
    }
}
