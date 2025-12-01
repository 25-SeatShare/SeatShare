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

        checkLocationPermission()

        // 하차 인증하기 버튼
        findViewById<Button>(R.id.get_off_1_confirm_button).setOnClickListener {
            startActivity(Intent(this, GetOff2Activity::class.java))
        }

        // 뒤로가기 버튼
        findViewById<TextView>(R.id.get_off_1_back_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }


    }

    //위치권한 허용 관련
    private fun checkLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val fineGranted = ContextCompat.checkSelfPermission(this, fine)
        val coarseGranted = ContextCompat.checkSelfPermission(this, coarse)

        if (fineGranted != PackageManager.PERMISSION_GRANTED ||
            coarseGranted != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(fine, coarse),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (!granted) {
                Toast.makeText(
                    this,
                    "위치 권한을 허용해야 서비스를 이용할 수 있습니다.",
                    Toast.LENGTH_LONG
                ).show()

                // 홈 화면(MainActivity)로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                finish()  // 이 화면 종료
            }
        }
    }
}

