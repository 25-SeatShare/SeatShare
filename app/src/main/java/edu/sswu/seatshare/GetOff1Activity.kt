package edu.sswu.seatshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlin.math.*

class GetOff1Activity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val LOCATION_PERMISSION_CODE = 5000

    private lateinit var currentStationText: TextView

    // ✅ 버튼으로 하차 인증을 눌렀는지 구분하기 위한 플래그
    private var triggeredByButton: Boolean = false

    data class Station(val name: String, val lat: Double, val lon: Double)

    private val line7StationsGPS = listOf(
        Station("군자", 37.557121, 127.079542),
        Station("어린이대공원", 37.548033, 127.074860),
        Station("건대입구", 37.540693, 127.070230),
        Station("자양", 37.531540, 127.067200),
        Station("청담", 37.519365, 127.053220),
        Station("강남구청", 37.517186, 127.041280),
        Station("학동", 37.514229, 127.029130),
        Station("논현", 37.511093, 127.021415),
        Station("반포", 37.508178, 127.011727),
        Station("고속터미널", 37.504465, 127.004943),
        Station("내방", 37.487618, 126.993513),
        Station("이수(총신대입구)", 37.486263, 126.981989),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_off_1)

        val intent = MyIntentHolder.getOffIntent
        val departure = intent?.getStringExtra("departure") ?: ""
        val arrive = intent?.getStringExtra("arrive") ?: ""

        findViewById<TextView>(R.id.arrive_station_text1_2).text = arrive



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentStationText = findViewById(R.id.current_station_text)

        // 뒤로가기
        findViewById<TextView>(R.id.get_off_1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // 하차 인증하기 버튼
        findViewById<Button>(R.id.get_off_1_confirm_button).setOnClickListener {
        triggeredByButton = true   // ✅ 버튼에서 실행되었음을 표시
            checkLocationPermission()
        }

        // 첫 로딩 시 현재 위치 자동 감지 (포인트 적립 X, 그냥 현재역만 표시용)
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
            if (triggeredByButton) {     // 버튼을 눌렀을 때만 인증 실행
                verifyGetOff()
            }
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

                        // ✅ 하차 인증 성공 & 버튼 눌러서 온 경우에만 포인트 적립
                        if (triggeredByButton) {
                            givePointForGetOff(uid)
                            triggeredByButton = false
                        }
                        MyIntentHolder.getOffIntent = null

                        // ★ 하차 성공 → 좌석 정보 삭제
                        db.collection("users")
                            .document(uid)
                            .collection("seats")
                            .document("current")
                            .delete()

                        // 성공 → 2페이지로 이동
                        val intent = Intent(this, GetOff2Activity::class.java)
                        intent.putExtra("departure", fromStation)
                        intent.putExtra("arrive", toStation)
                        intent.putExtra("platform", platform)
                        intent.putExtra("seatNumber", seatNum)
                        intent.putExtra("actualGetOffStation", nearest.name)
                        startActivity(intent)
                        finish()
                    } else {
                        // 실패 → 현재 페이지에서 메시지
                        toast("하차 인증 실패: 하차역과 일치하지 않습니다.")
                    }
                }
            }
    }

    // ✅ 하차 인증 성공 시 포인트 적립 + 로그 기록
    private fun givePointForGetOff(uid: String) {
        val userRef = db.collection("users").document(uid)

        // points 증가
        userRef.update("points", FieldValue.increment(1))

        // 로그 남기기
        userRef.collection("pointLogs").document().set(
            mapOf(
                "delta" to 1,
                "message" to "+1 적립 (하차 인증)",
                "createdAt" to Timestamp.now()
            )
        )

        // 사용자에게 안내
        toast("하차 인증 완료! 포인트 +1 적립되었습니다.")
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
