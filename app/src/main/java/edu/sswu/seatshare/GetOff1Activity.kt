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

class GetOff1Activity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_off_1)

        // 🔹 하차 인증하기 버튼
        findViewById<Button>(R.id.get_off_1_confirm_button).setOnClickListener {
            // 버튼을 눌렀을 때만 권한 체크
            if (hasLocationPermission()) {
                // 이미 허용되어 있으면 바로 다음 화면으로
                goToGetOff2()
            } else {
                // 아직 권한이 없으면 요청
                requestLocationPermission()
            }
        }

        // 🔹 뒤로가기 버튼
        findViewById<TextView>(R.id.get_off_1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // 현재 위치 권한이 있는지 확인
    private fun hasLocationPermission(): Boolean {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val fineGranted = ContextCompat.checkSelfPermission(this, fine)
        val coarseGranted = ContextCompat.checkSelfPermission(this, coarse)

        return fineGranted == PackageManager.PERMISSION_GRANTED &&
                coarseGranted == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청
    private fun requestLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        ActivityCompat.requestPermissions(
            this,
            arrayOf(fine, coarse),
            LOCATION_PERMISSION_CODE
        )
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (granted) {
                // i) 위치 권한 허용됨 → 다음 화면으로 이동
                goToGetOff2()
            } else {
                // ii) 권한 거절됨 → 안내만 띄우고 이 화면에 그대로 둠
                Toast.makeText(
                    this,
                    "위치 권한을 허용해야 하차 인증을 진행할 수 있습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 다음 페이지로 이동
    private fun goToGetOff2() {
        val intent = Intent(this, GetOff2Activity::class.java)
        startActivity(intent)
    }
}
