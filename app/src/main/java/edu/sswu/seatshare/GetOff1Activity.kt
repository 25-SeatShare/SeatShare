package edu.sswu.seatshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.*

class GetOff1Activity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val LOCATION_PERMISSION_CODE = 5000

    private lateinit var currentStationText: TextView

    data class Station(val name: String, val lat: Double, val lon: Double)

    private val line7StationsGPS = listOf(
        Station("장암", 37.700109, 127.053196),
        Station("도봉산", 37.689313, 127.046222),
        Station("수락산", 37.677774, 127.055933),
        Station("마들", 37.664940, 127.057675),
        Station("노원", 37.655128, 127.061368),
        Station("중계", 37.644583, 127.064303),
        Station("하계", 37.635940, 127.067500),
        Station("공릉(서울산업대입구)", 37.625742, 127.072896),
        Station("태릉입구", 37.617983, 127.074673),
        Station("먹골", 37.610469, 127.077276),
        Station("중화", 37.602545, 127.079264),
        Station("상봉", 37.596362, 127.085031),
        Station("면목", 37.588579, 127.087503),
        Station("사가정", 37.580894, 127.088932),
        Station("용마산", 37.573646, 127.086727),
        Station("중곡", 37.565923, 127.086849),
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
        Station("남성", 37.484596, 126.971251),
        Station("숭실대입구", 37.496029, 126.953822),
        Station("상도", 37.502834, 126.947910),
        Station("장승배기", 37.504898, 126.939150),
        Station("신대방삼거리", 37.499720, 126.928280),
        Station("보라매", 37.499701, 126.920783),
        Station("신풍", 37.500080, 126.909930),
        Station("대림", 37.493105, 126.894913),
        Station("남구로", 37.486056, 126.887249),
        Station("가산디지털단지", 37.481426, 126.882675),
        Station("철산", 37.476050, 126.867911),
        Station("광명사거리", 37.479252, 126.854876),
        Station("천왕", 37.486637, 126.839577),
        Station("온수", 37.492970, 126.823388),
        Station("까치울", 37.506207, 126.810939),
        Station("부천종합운동장", 37.505431, 126.797376),
        Station("춘의", 37.503663, 126.787036),
        Station("신중동", 37.503048, 126.775960),
        Station("부천시청", 37.504631, 126.764326),
        Station("상동", 37.505781, 126.753083),
        Station("삼산체육관", 37.506411, 126.742153),
        Station("굴포천", 37.507018, 126.731274),
        Station("부평구청", 37.508336, 126.720548)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_off_1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentStationText = findViewById(R.id.arrive_station_text)

        // 뒤로가기
        findViewById<TextView>(R.id.get_off_1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // 하차 인증하기
        findViewById<TextView>(R.id.get_off_1_confirm_button).setOnClickListener {
            checkLocationPermission()
        }

        // 첫 로딩 시 현재 위치 자동 감지
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        if (
            ContextCompat.checkSelfPermission(this, fine) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, coarse) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(fine, coarse),
                LOCATION_PERMISSION_CODE
            )
        } else {
            autodetectCurrentStation()   // 현재역 표시
            verifyGetOff()              // 하차 인증 시도
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            autodetectCurrentStation()
        } else {
            toast("위치 권한이 필요합니다.")
        }
    }

    @Suppress("MissingPermission")
    private fun autodetectCurrentStation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc == null) {
                currentStationText.text = "감지 실패"
                return@addOnSuccessListener
            }
            val nearest = findNearestStation(loc.latitude, loc.longitude)
            currentStationText.text = nearest.name
        }
    }

    private fun findNearestStation(lat: Double, lon: Double): Station {
        var nearest = line7StationsGPS[0]
        var minDist = Double.MAX_VALUE

        for (s in line7StationsGPS) {
            val d = distance(lat, lon, s.lat, s.lon)
            if (d < minDist) {
                minDist = d
                nearest = s
            }
        }
        return nearest
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat/2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon/2).pow(2.0)
        return 2 * R * atan2(sqrt(a), sqrt(1-a))
    }

    /** 핵심 : 하차 인증 로직 */
    @Suppress("MissingPermission")
    private fun verifyGetOff() {

        val uid = auth.currentUser?.uid ?: return toast("로그인이 필요합니다.")

        db.collection("users").document(uid)
            .collection("seats").document("current")
            .get()
            .addOnSuccessListener { snapshot ->

                val toStation = snapshot.getString("toStation") ?: ""
                val fromStation = snapshot.getString("fromStation") ?: ""
                val platform = snapshot.getString("platform") ?: ""
                val seatNum = snapshot.getString("seatNumber") ?: ""

                if (toStation.isBlank()) {
                    toast("등록된 하차역이 없습니다.")
                    return@addOnSuccessListener
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc == null) {
                        toast("현재 위치 오류")
                        return@addOnSuccessListener
                    }

                    val nearest = findNearestStation(loc.latitude, loc.longitude)

                    if (nearest.name == toStation) {
                        // 성공 → 2페이지로 이동
                        val intent = Intent(this, GetOff2Activity::class.java)
                        intent.putExtra("departure", fromStation)
                        intent.putExtra("arrive", toStation)
                        intent.putExtra("platform", platform)
                        intent.putExtra("seatNumber", seatNum)
                        startActivity(intent)
                        finish()
                    } else {
                        // 실패 → 현재 페이지에서 메시지
                        toast("하차 인증 실패: 하차역과 일치하지 않습니다.")
                    }
                }
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
